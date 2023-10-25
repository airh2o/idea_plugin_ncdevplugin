package com.air.nc5dev.ui.adddeffiled;

import com.air.nc5dev.enums.NcVersionEnum;
import com.air.nc5dev.ui.exportbmf.ExportbmfDialog;
import com.air.nc5dev.util.*;
import com.air.nc5dev.util.idea.LogUtil;
import com.air.nc5dev.vo.meta.PropertyDTO;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.*;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.util.*;

/***
 *     弹框UI        </br>
 *           </br>
 *           </br>
 *           </br>
 * @author air Email: 209308343@qq.com
 * @date 2019/12/25 0025 15:29
 * @Param
 * @return
 */
@Data
public class AddDefField2BmfDialog extends DialogWrapper {
    public static String[] tableNames = new String[]{
            "序号", "组件名", "组件显示名", "模块", "名称空间"
            , "ID", "文件版本号", "文件名", "组件类型", "文件路径"
    };
    public static String[] tableAttrs = new String[]{
            "order1", "name", "displayName", "ownModule", "namespace"
            , "id", "fileVersion", "fileName", "metaType", "filePath"
    };


    public static String[] tablePropNames = new String[]{
            "选择", "序号", "名称", "显示名称", "类型样式"
            , "类型", "字段名称", "字段类型", "参照名称"
    };
    public static String[] tablePropAttrs = new String[]{
            "select", "attrsequence", "name", "displayName", "dataTypeStyleName"
            , "typeName", "fieldName", "fieldType", "refModelName"
    };


    VirtualFile bmf;
    JBTabbedPane contentPane;
    JBPanel panel_main;

    DefaultComboBoxModel<ClassComboxDTO> comboBoxModelClass;
    ComboBox comboBoxClass;

    JBLabel labelInfo;

    DefaultTableModel tableModelPropertis;
    ProperteListTable tablePropertis;
    JScrollPane panel_tablePropertis;

    DefaultTableModel tableModelClass;
    ClassEntityListTable tableClass;
    JScrollPane panel_tableClass;

    int height = 800;
    int width = 1200;
    Project project;

    /**
     * 列表
     */
    volatile List<PropertyDTO> result;
    /**
     * key=calssid 实体id, value =属性列表
     */
    Map<String, List<PropertyDTO>> propertys = new HashMap<>();

    public AddDefField2BmfDialog(Project project, VirtualFile bmf) {
        super(project);
        this.project = project;
        this.bmf = bmf;
        createCenterPanel();
        init();
        setOKButtonText("保存到文件");
        setCancelButtonText("关闭窗口");
        setTitle("快速新增自定义项字段或复制字段到元数据文件(表格点击右键操作)");
    }

    private void createCenterPanel0() throws Exception {
        JBTabbedPane jtab = new JBTabbedPane();
        contentPane = jtab;
        contentPane.setBounds(0, 0, getWidth(), getHeight());
        {
            int x = 1;
            int y = 1;
            int w = 60;
            int h = 40;

            panel_main = new JBPanel();
            panel_main.setLayout(null);
            panel_main.setBounds(0, 0, 1200, 800);
            jtab.addTab(bmf.getPath(), panel_main);

            comboBoxModelClass = new DefaultComboBoxModel<ClassComboxDTO>(readClassList());
            JBLabel label = new JBLabel("选择实体:");
            label.setBounds(1, y, w, 60);
            panel_main.add(label);
            comboBoxClass = new ComboBox(comboBoxModelClass);
            comboBoxClass.setBounds(x + w, y, 150, h);
            panel_main.add(comboBoxClass);

            labelInfo = new JBLabel();
            labelInfo.setBounds(1, y = 45 + 150 + 40, getWidth() - 50, 30);
            panel_main.add(labelInfo);

            tableModelClass = new DefaultTableModel(null, tableNames);
            tableClass = new ClassEntityListTable(tableModelClass, this);
            panel_tableClass = new JBScrollPane(tableClass);
            panel_tableClass.setAutoscrolls(true);
            panel_tableClass.setBounds(x = 1, y = 45 + 150 + 40 + 25, getWidth() - 50, 330);
            panel_main.add(panel_tableClass);

            tableModelPropertis = new DefaultTableModel(null, tablePropNames);
            tablePropertis = new ProperteListTable(tableModelPropertis, this);
            panel_tablePropertis = new JBScrollPane(tablePropertis);
            panel_tablePropertis.setAutoscrolls(true);
            panel_tablePropertis.setBounds(x = 1, y = 45 + 150 + 40 + 25, getWidth() - 50, 330);
            panel_main.add(panel_tablePropertis);

            initListeners();
        }

        //设置点默认值
        initDefualtValues();
    }

    /**
     * 搜索
     *
     * @param e
     */
    public void onSearch(ActionEvent e) {

    }

    /**
     * 导出
     */
    @Override
    protected void doOKAction() {
        save2Files();
        // super.doOKAction();
    }

    public void save2Files() {
        Task.Backgroundable backgroundable = new Task.Backgroundable(project, "正在写入文件...请耐心等待") {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {
                    save2Files0(indicator);
                } finally {
                }
            }
        };
        backgroundable.setCancelText("停止任务");
        backgroundable.setCancelTooltipText("停止这个任务");
        ProgressManager.getInstance().run(backgroundable);
    }

    public void save2Files0(ProgressIndicator indicator) {
        try {
            for (PropertyDTO v : result) {
                if (indicator != null) {
                    if (indicator.isCanceled()) {
                        LogUtil.infoAndHide("手工取消任务完成，导出元数据文件: ");
                        return;
                    }
                    indicator.setText("导出： " + v.getName() + "  " + v.getDisplayName() + " 中...");
                }
            }

            LogUtil.infoAndHide("写入元数据文件完成!");
        } catch (Throwable e) {
            e.printStackTrace();
            LogUtil.error(e.getMessage(), e);
        } finally {
        }
    }

    public void initFiles(int type) {
        new ExportbmfDialog(getProject()).initFiles(type);
    }

    @Override
    public void doCancelAction() {
        super.doCancelAction();
    }

    @Override
    protected JComponent createCenterPanel() {
        if (contentPane == null) {
            try {
                createCenterPanel0();
            } catch (Exception e) {
                e.printStackTrace();
                LogUtil.error(e.getMessage(), e);
            }
        }

        return this.contentPane;
    }

    public void initDefualtValues() {
        try {
            NcVersionEnum ncVersion = ProjectNCConfigUtil.getNCVersion(getProject());
            comboBoxClass.setSelectedItem(ncVersion);
            labelInfo.setText("注意:粉红色背景意思是找不到bmf文件或版本和数据库不一致！(导出的元数据文件一定要自己测试是否正常哈！)" +
                    " 表格行点击鼠标右键可以导出指定行! 暂不支持导出bpf业务操作组件!");
        } catch (Throwable e) {
            e.printStackTrace();
            LogUtil.error(e.getMessage(), e);
        }
    }

    @Nullable
    @Override
    protected String getDimensionServiceKey() {
        return this.getClass().getName();
    }

    private void onCancel() {
        dispose();
    }

    private void initListeners() {
        tablePropertis.addMouseListener(new ProperteListTableMouseListenerImpl(this));
    }

    public void removeAll(DefaultTableModel tm) {
        while (tm.getRowCount() > 0) {
            tm.removeRow(0);
        }
    }

    public void loadDatas(List<PropertyDTO> vs) {
        result = vs;
        DefaultTableModel tm = tableModelPropertis;
        removeAll(tm);

        if (CollUtil.isNotEmpty(vs)) {
            for (PropertyDTO e : vs) {
                Vector row = new Vector();
                for (int i = 0; i < tableAttrs.length; i++) {
                    row.add(ReflectUtil.getFieldValue(e, tableAttrs[i]));
                }
                tm.addRow(row);
            }
            tablePropertis.fitTableColumns();
        }
    }

    public List<PropertyDTO> getSelects() {
        for (PropertyDTO p : result) {

        }
        return null;
    }

    private Vector<ClassComboxDTO> readClassList() {

        return null;
    }

    private List<PropertyDTO> readPropsList() {

        return null;
    }
}
