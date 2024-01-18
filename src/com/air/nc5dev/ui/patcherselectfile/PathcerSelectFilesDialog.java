package com.air.nc5dev.ui.patcherselectfile;

import com.air.nc5dev.ui.PatcherDialog;
import com.air.nc5dev.util.CollUtil;
import com.air.nc5dev.util.idea.LogUtil;
import com.air.nc5dev.vo.PatcherSelectFileVO;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTabbedPane;
import lombok.Data;
import org.jetbrains.annotations.Nullable;
import org.tangsu.mstsc.ui.BaseSimpleListTable;
import org.tangsu.mstsc.vo.WindowSizeVO;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

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
public class PathcerSelectFilesDialog extends DialogWrapper {
    public static String[] tableNames = new String[]{
            "序号", "类名", "文件地址", "备注", "排序"
    };
    public static String[] tableAttrs = new String[]{
            "order1", "name", "path", "memo", "sort"
    };

    JBTabbedPane contentPane;
    JBPanel panel_mstsc;
    BaseSimpleListTable table;
    DefaultComboBoxModel<WindowSizeVO> comboBoxSizeModel;
    JScrollPane panel_table;
    DefaultTableModel tableModel;
    AtomicBoolean update = new AtomicBoolean(false);
    int height = 800;
    int width = 1200;
    Project project;

    public PathcerSelectFilesDialog(Project project) {
        super(project);
        this.project = project;
        createCenterPanel();
        init();
        setTitle("查看文件列表补丁导出选定的文件列表");
    }

    @Override
    public void doCancelAction() {
        super.doCancelAction();
    }

    public JComponent getContentPane() {
        return createCenterPanel();
    }


    @Override
    protected void doOKAction() {
        super.doOKAction();
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

    private void createCenterPanel0() throws Exception {
        JBTabbedPane jtab = new JBTabbedPane();
        contentPane = jtab;
        contentPane.setBounds(0, 0, getWidth(), getHeight());
        JBLabel label;

        {
            int x = 1;
            int y = 1;

            panel_mstsc = new JBPanel();
            panel_mstsc.setLayout(null);
            panel_mstsc.setBounds(0, 0, 1200, 800);
            jtab.addTab("文件列表", panel_mstsc);

            tableModel = new DefaultTableModel(null, tableNames);
            table = new BaseSimpleListTable(tableModel);
            panel_table = new JBScrollPane(table);
            panel_table.setAutoscrolls(true);
            panel_table.setBounds(1, 1, getWidth() - 50, 330);
            panel_mstsc.add(panel_table);

            initListeners();
        }

        //设置点默认值
        initDefualtValues();
    }

    public void initDefualtValues() {
        try {
            loadDatas();
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
        table.addMouseListener(new PatcherSelectFilesTableMouseListenerImpl(this));
    }

    public void loadDatas() throws SQLException, InstantiationException, IllegalAccessException {
        List<PatcherSelectFileVO> vs = PatcherDialog.getForceAddSelectFiles(getProject());
        loadDatas(vs);
    }

    public void removeAll(DefaultTableModel tm) {
        update.set(false);
        while (tm.getRowCount() > 0) {
            tm.removeRow(0);
        }
    }

    public void loadDatas(List<PatcherSelectFileVO> vs) {
        update.set(false);
        DefaultTableModel tm = tableModel;
        removeAll(tm);

        if (CollUtil.isNotEmpty(vs)) {
            int i = 1;
            for (PatcherSelectFileVO e : vs) {
                Vector row = new Vector();
                for (String arr : tableAttrs) {
                    if (arr.equals(tableAttrs[0])) {
                        row.add(i++);
                    } else if (arr.equals(tableAttrs[1])) {
                        row.add(e.getFile().getName().substring(0, e.getFile().getName().length() - 5));
                    } else if (arr.equals(tableAttrs[2])) {
                        row.add(e.getPath());
                    } else if (arr.equals(tableAttrs[3])) {
                        row.add(e.getMemo());
                    } else if (arr.equals(tableAttrs[4])) {
                        row.add(e.getSort());
                    }
                }
                tm.addRow(row);
            }
            table.fitTableColumns();
        }
    }
}
