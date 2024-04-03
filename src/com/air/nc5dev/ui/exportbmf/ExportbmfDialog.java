package com.air.nc5dev.ui.exportbmf;

import cn.hutool.core.io.FileUtil;
import com.air.nc5dev.enums.NcVersionEnum;
import com.air.nc5dev.util.CollUtil;
import com.air.nc5dev.util.IoUtil;
import com.air.nc5dev.util.NCPropXmlUtil;
import com.air.nc5dev.util.ProjectNCConfigUtil;
import com.air.nc5dev.util.ReflectUtil;
import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.util.V;
import com.air.nc5dev.util.idea.LogUtil;
import com.air.nc5dev.util.jdbc.ConnectionUtil;
import com.air.nc5dev.util.jdbc.resulthandel.VOArrayListResultSetExtractor;
import com.air.nc5dev.util.meta.QueryCompomentVOUtil;
import com.air.nc5dev.util.meta.xml.MeatBaseInfoReadUtil;
import com.air.nc5dev.util.meta.xml.MetaXmlBuilder4NC6;
import com.air.nc5dev.vo.NCDataSourceVO;
import com.air.nc5dev.vo.meta.ComponentAggVO;
import com.air.nc5dev.vo.meta.ComponentDTO;
import com.air.nc5dev.vo.meta.SearchComponentVO;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.components.JBTextArea;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/***
 *     弹框UI        <br>
 *           <br>
 *           <br>
 *           <br>
 * @author air Email: 209308343@qq.com
 * @date 2019/12/25 0025 15:29
 * @Param
 * @return
 */
@Data
public class ExportbmfDialog extends DialogWrapper {
    public static String[] tableNames = new String[]{
            "序号", "组件名", "组件显示名", "模块", "名称空间"
            , "版本号", "ID", "文件版本号", "文件名", "组件类型"
            , "文件路径"
    };
    public static String[] tableAttrs = new String[]{
            "order1", "name", "displayName", "ownModule", "namespace"
            , "version", "id", "fileVersion", "fileName", "metaType"
            , "filePath"
    };

    public static Map<Project, Map<String, SearchComponentVO>> CACHE = new ConcurrentHashMap<>();

    JBTabbedPane contentPane;
    JBPanel panel_main;
    BmfListTable table;
    DefaultComboBoxModel<NcVersionEnum> comboBoxModelNCVersion;
    DefaultComboBoxModel<NCDataSourceVO> comboBoxModelDb;
    ComboBox comboBoxNCVersion;
    ComboBox comboBoxDb;
    JBTextArea textFieldSerach;
    JBCheckBox checkBoxOnlyNoMatch;
    JBCheckBox checkBoxOnlyHasFile;
    JBCheckBox checkBoxOnlyBmf;
    JButton buttonSearch;
    JBLabel labelInfo;
    JScrollPane panel_table;
    DefaultTableModel tableModel;
    AtomicBoolean update = new AtomicBoolean(false);
    int height = 800;
    int width = 1200;
    Project project;
    AtomicBoolean runing = new AtomicBoolean(false);
    volatile List<SearchComponentVO> result;

    public ExportbmfDialog(Project project) {
        super(project);
        this.project = project;
        createCenterPanel();
        init();
        setOKButtonText("导出全部结果");
        setCancelButtonText("关闭窗口");
        setTitle("从数据库抽取生成BMF最新文件");
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
            jtab.addTab("元数据列表", panel_main);

            comboBoxModelNCVersion = new DefaultComboBoxModel<>(NcVersionEnum.values());
            final List<NCDataSourceVO> dataSourceVOS = NCPropXmlUtil.getDataSourceVOS(getProject());
            comboBoxModelDb = new DefaultComboBoxModel<>(new Vector(
                    dataSourceVOS.stream()
                            .map(v -> v.getDataSourceName() + '/' + v.getUser())
                            .collect(Collectors.toList())
            ));

            JBLabel label = new JBLabel("NC版本:");
            label.setBounds(1, y, w, 60);
            panel_main.add(label);
            comboBoxNCVersion = new ComboBox(comboBoxModelNCVersion);
            comboBoxNCVersion.setBounds(x + w, y, 150, h);
            panel_main.add(comboBoxNCVersion);

            label = new JBLabel("数据源:");
            label.setBounds(x += w + 155, y, w, h);
            panel_main.add(label);
            comboBoxDb = new ComboBox(comboBoxModelDb);
            comboBoxDb.setBounds(x += w, y, 150, h);
            panel_main.add(comboBoxDb);

            checkBoxOnlyNoMatch = new JBCheckBox("只显示版本不一致的");
            checkBoxOnlyNoMatch.setBounds(x += 155, y, 140, h);
            panel_main.add(checkBoxOnlyNoMatch);

            checkBoxOnlyHasFile = new JBCheckBox("只显示能找到文件的");
            checkBoxOnlyHasFile.setBounds(x += 145, y, 140, h);
            panel_main.add(checkBoxOnlyHasFile);

            checkBoxOnlyBmf = new JBCheckBox("只显示BMF");
            checkBoxOnlyBmf.setBounds(x += 145, y, 140, h);
            panel_main.add(checkBoxOnlyBmf);

            textFieldSerach = new JBTextArea();
            textFieldSerach.setEditable(true);
            textFieldSerach.setLineWrap(true);
            textFieldSerach.setBounds(x = 1, y += h + 5, w = getWidth() - 200, h = 150);
            panel_main.add(textFieldSerach);

            buttonSearch = new JButton("搜索");
            buttonSearch.setBounds(x += w + 5, y, w = 100, h = 40);
            panel_main.add(buttonSearch);

            labelInfo = new JBLabel();
            labelInfo.setBounds(1, y = 45 + 150 + 40, getWidth() - 50, 30);
            panel_main.add(labelInfo);
            tableModel = new DefaultTableModel(null, tableNames);
            table = new BmfListTable(tableModel, this);
            panel_table = new JBScrollPane(table);
            panel_table.setAutoscrolls(true);
            panel_table.setBounds(x = 1, y = 45 + 150 + 40 + 25, getWidth() - 50, 330);
            panel_main.add(panel_table);

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
        ConnectionUtil.initDataSourceClass(
                NCPropXmlUtil.getDataSourceVOS(getProject()).get(comboBoxDb.getSelectedIndex())
                , project
                , contentPane);

        if (CACHE.get(getProject()) == null) {
            initFiles(1);
        } else {
            initFiles(0);
        }
    }

    private void doSearch() {
        Connection con = null;
        Statement st = null;
        ResultSet rs = null;
        try {
            String sql = textFieldSerach.getText().trim();
            NCDataSourceVO ds = NCPropXmlUtil.getDataSourceVOS(getProject()).get(comboBoxDb.getSelectedIndex());
            con = ConnectionUtil.getConn(ds);
            st = con.createStatement();
            rs = st.executeQuery(sql);
            result = new VOArrayListResultSetExtractor<SearchComponentVO>(SearchComponentVO.class).extractData(rs);
            if (CollUtil.isEmpty(result)) {
                loadDatas(null);
                return;
            }

            result.sort((r1, r2) -> V.get(r1.getOrder1(), 0).compareTo(V.get(r2.getOrder1(), 0)));

            //匹配 bmf文件这些
            Map<String, SearchComponentVO> bmfmap = CACHE.get(getProject());
            if (bmfmap != null) {
                boolean onlyNoMatch = checkBoxOnlyNoMatch.isSelected();
                boolean onlyHasFile = checkBoxOnlyHasFile.isSelected();
                boolean onlyOnlyBmf = checkBoxOnlyBmf.isSelected();
                Iterator<SearchComponentVO> it = result.iterator();
                while (it.hasNext()) {
                    SearchComponentVO vo = it.next();

                    if (onlyOnlyBmf) {
                        if (Boolean.TRUE.equals(vo.getIsbizmodel())
                                /* || (
                                        StringUtil.isNotBlank(vo.getMetaType())
                                                && !".bmf".equalsIgnoreCase(vo.getMetaType())
                                )*/
                        ) {
                            it.remove();
                        }
                    }

                    SearchComponentVO b = bmfmap.get(vo.getId());
                    if (b == null) {
                        vo.setFileName(null);
                        vo.setFilePath(null);
                        vo.setFileVersion(null);

                        if (onlyHasFile) {
                            it.remove();
                        }

                        continue;
                    }

                    vo.setFileName(b.getFileName());
                    vo.setFilePath(b.getFilePath());
                    vo.setFileVersion(b.getFileVersion());

                    if (onlyOnlyBmf && vo.getFileName() != null && !vo.getFileName().toLowerCase().endsWith(".bmf")) {
                        it.remove();
                    } else {
                        if (onlyNoMatch) {
                            if (StringUtil.equalsIgnoreCase(vo.getVersion(), vo.getFileVersion())) {
                                it.remove();
                            }
                        }
                    }
                }
            }

            loadDatas(result);
        } catch (Throwable e) {
            e.printStackTrace();
            LogUtil.error(e.getMessage(), e);
        } finally {
            IoUtil.close(rs);
            IoUtil.close(st);
            IoUtil.close(con);
        }
    }

    /**
     * 导出
     */
    @Override
    protected void doOKAction() {
        export2Files(getResult());
        // super.doOKAction();
    }

    public void export2Files(List<SearchComponentVO> vs) {
        Task.Backgroundable backgroundable = new Task.Backgroundable(project, "正在导出...请耐心等待") {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {
                    export2Files0(vs, indicator);
                } finally {
                }
            }
        };
        backgroundable.setCancelText("停止任务");
        backgroundable.setCancelTooltipText("停止这个任务");
        ProgressManager.getInstance().run(backgroundable);
    }

    public void export2Files0(List<SearchComponentVO> vs, ProgressIndicator indicator) {
        //选择保存位置
        File outDir = new File(getProject().getBasePath(), "数据库导出的元数据文件");
        if (!outDir.exists()) {
            outDir.mkdirs();
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        fileChooser.setCurrentDirectory(outDir);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle("选择保存路径(选择文件夹):");
        int flag = fileChooser.showSaveDialog(null);
        if (flag == JFileChooser.APPROVE_OPTION
                && fileChooser.getSelectedFile() != null) {
            outDir = fileChooser.getSelectedFile();
        } else {
            outDir = null;
        }

        if (outDir == null) {
            return;
        }

        Connection con = null;
        try {
            NCDataSourceVO ds = NCPropXmlUtil.getDataSourceVOS(getProject()).get(comboBoxDb.getSelectedIndex());
            con = ConnectionUtil.getConn(ds);

            for (SearchComponentVO v : vs) {
                if (indicator != null) {
                    if (indicator.isCanceled()) {
                        LogUtil.infoAndHide("手工取消任务完成，导出元数据文件: " + outDir.getPath());
                        return;
                    }
                    indicator.setText("导出： " + v.getName() + "  " + v.getDisplayName() + " 中...");
                }
                ComponentAggVO agg = new QueryCompomentVOUtil(con).queryVOAgg(v.getId(), getProject());
                MetaXmlBuilder4NC6 metaXmlBuilder4NC6 = new MetaXmlBuilder4NC6(con, agg);
                String str = metaXmlBuilder4NC6.toBmfStr();
                FileUtil.writeUtf8String(str, new File(outDir, StringUtil.get(v.getFileName()
                        , v.getOwnModule() + '_' + v.getName() + ".bmf")));
            }

            LogUtil.infoAndHide("导出元数据文件完成: " + outDir.getPath());

            IoUtil.tryOpenFileExpolor(outDir);
        } catch (Throwable e) {
            e.printStackTrace();
            LogUtil.error(e.getMessage(), e);
        } finally {
            IoUtil.close(con);
        }
    }

    private void searchAllBmfFile(ProgressIndicator indicator) {
        File home = new File(ProjectNCConfigUtil.getNCHomePath(getProject()));
        if (!home.isDirectory()) {
            return;
        }

        File modules = new File(home, "modules");
        if (!modules.isDirectory()) {
            return;
        }

        List<File> bmfs = V.get(IoUtil.getAllFiles(modules
                , true
                , f -> f.isFile() || !(
                        f.getName().equalsIgnoreCase(".idea")
                                || f.getName().equalsIgnoreCase(".git")
                                || f.getName().equalsIgnoreCase(".settings")
                                || f.getName().equalsIgnoreCase(".svn")
                )
                , ".bmf", ".bpf"), new ArrayList<>());

        readBmfFileInfo2Cache(bmfs, indicator);

        searchProjectBmfFile(indicator);
    }

    private void readBmfFileInfo2Cache(List<File> bmfs, ProgressIndicator indicator) {
        CACHE.putIfAbsent(getProject(), new ConcurrentHashMap<>());
        Map<String, SearchComponentVO> m = CACHE.get(getProject());
        for (File bmf : bmfs) {
            try {
                if (indicator != null) {
                    if (indicator.isCanceled()) {
                        LogUtil.infoAndHide("取消缓存BMF任务完成");
                        return;
                    }
                    indicator.setText("正在读取BMF文件到缓存: " + bmf.getPath());
                }

                ComponentDTO c = MeatBaseInfoReadUtil.readComponentInfo(FileUtil.readUtf8String(bmf));
                SearchComponentVO s = ReflectUtil.copy2VO(c, SearchComponentVO.class);
                s.setFileName(bmf.getName());
                s.setFilePath(bmf.getPath());
                s.setFileVersion(c.getVersion());

                if (StringUtil.isBlank(c.getMetaType())) {
                    if (Boolean.FALSE.equals(c.getIsbizmodel()) || StringUtil.endWithIgnoreCase(s.getFileName(),
                            ".bmf")) {
                        c.setMetaType(".bmf");
                    } else {
                        c.setMetaType(".bpf");
                    }
                }

                m.put(c.getId(), s);
            } catch (Throwable e) {
                System.err.println(bmf.getPath() + " error " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void searchProjectBmfFile(ProgressIndicator indicator) {
        List<File> bmfs = V.get(IoUtil.getAllFiles(
                new File(getProject().getBasePath())
                , true
                , f -> f.isFile() || !(
                        f.getName().equalsIgnoreCase(".idea")
                                || f.getName().equalsIgnoreCase(".git")
                                || f.getName().equalsIgnoreCase(".settings")
                                || f.getName().equalsIgnoreCase(".svn")
                )
                , ".bmf", ".bpf"), new ArrayList<>());

        readBmfFileInfo2Cache(bmfs, indicator);
    }

    public void initFiles(int type) {
        if (runing.get()) {
            LogUtil.infoAndHide("请等待上一次搜索完成...再试");
            return;
        }

        long start = System.currentTimeMillis();
        runing.set(true);
        Task.Backgroundable backgroundable = new Task.Backgroundable(project, "正在搜索...请耐心等待(第一次搜索会初始化扫描HOME中BMF文件.." +
                ".初始化只会运行一次)") {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {
                    if (1 == type) {
                        searchAllBmfFile(indicator);
                    } else if (0 == type) {
                        searchProjectBmfFile(indicator);
                    }
                } finally {
                    runing.set(false);

                    doSearch();

                    long end = System.currentTimeMillis();
                    labelInfo.setText("搜索完成,匹配数: " + CollUtil.size(result) + "   耗时: " + ((end - start) / 1000) + " 秒");
                }
            }
        };
        backgroundable.setCancelText("停止任务");
        backgroundable.setCancelTooltipText("停止这个任务");
        ProgressManager.getInstance().run(backgroundable);
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
            textFieldSerach.setText("select " +
                    "\n        rownum as order1, name,displayname,ownmodule, namespace,version, id " +
                    "\nfrom md_component where 1=1 " +
                    "\n        and displayname like '%销售订单%' " +
                    "\norder by ts desc ");
            NcVersionEnum ncVersion = ProjectNCConfigUtil.getNCVersion(getProject());
            comboBoxNCVersion.setSelectedItem(ncVersion);
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
        table.addMouseListener(new ExportbmfTableMouseListenerImpl(this));
        buttonSearch.addActionListener(this::onSearch);
    }

    public void removeAll(DefaultTableModel tm) {
        update.set(false);
        while (tm.getRowCount() > 0) {
            tm.removeRow(0);
        }
    }

    public void loadDatas(List<SearchComponentVO> vs) {
        result = vs;
        update.set(false);
        DefaultTableModel tm = tableModel;
        removeAll(tm);

        if (CollUtil.isNotEmpty(vs)) {
            for (SearchComponentVO e : vs) {
                Vector row = new Vector();
                for (int i = 0; i < tableAttrs.length; i++) {
                    row.add(ReflectUtil.getFieldValue(e, tableAttrs[i]));
                }
                tm.addRow(row);
            }
            table.fitTableColumns();
        }
    }
}
