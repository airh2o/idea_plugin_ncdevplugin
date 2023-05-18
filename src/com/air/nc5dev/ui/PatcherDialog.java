package com.air.nc5dev.ui;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import com.air.nc5dev.enums.NcVersionEnum;
import com.air.nc5dev.util.*;
import com.air.nc5dev.util.idea.LogUtil;
import com.air.nc5dev.util.idea.ProjectUtil;
import com.air.nc5dev.util.jdbc.ConnectionUtil;
import com.air.nc5dev.vo.ExportContentVO;
import com.air.nc5dev.vo.NCDataSourceVO;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.*;
import com.intellij.ui.table.JBTable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/***
 *   导出NC 补丁包的 弹框UI        </br>
 *           </br>
 *           </br>
 *           </br>
 * @author air Email: 209308343@qq.com
 * @date 2019/12/25 0025 15:29
 * @Param
 * @return
 */
@Data
public class PatcherDialog
        extends DialogWrapper {
    //全局标识， 是否有导出任务未处理完
    private static boolean isRuning = false;
    private JComponent contentPane;
    private AnActionEvent event;
    private JTextField textField_saveName;
    private JTextField textField_savePath;
    JBCheckBox filtersql;
    JBCheckBox rebuild;
    JComboBox dataSourceIndex;
    JComboBox ncVersion;
    JBCheckBox reNpmBuild;
    JBCheckBox format4Ygj;
    JBCheckBox exportResources;
    JBCheckBox exportSql;
    JBCheckBox onleyFullSql;
    JBCheckBox deleteDir;
    JBCheckBox reWriteSourceFile;
    JBCheckBox zip;
    JBCheckBox selectExport;
    JBTable selectTable;
    DefaultTableModel defaultTableModel;

    public PatcherDialog(AnActionEvent event) {
        super(event.getProject());
        this.event = event;
        init();
        setTitle("导出NC补丁包..."
                       /* + (
                        ExportContentVO.EVENT_POPUP_CLICK.equals(event.getPlace())
                                ? "(项目树点击的导出:只导出选中的模块和文件。如果不想这样 可以点击 Tools菜单选中导出)"
                                : "(Tools菜单点击的导出:导出全部项目。如果不想这样 可以点击 左侧项目树 选中要导出的模块或具体文件)"
                )*/
        );
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        if (contentPane == null) {
            int x = 21;
            int y = 16;
            int height = 30;
            int width = 600;

            contentPane = //new JBPanel();
                    new JBScrollPane();
            contentPane.setLayout(null);
            contentPane.setAutoscrolls(true);
            JLabel label = new JBLabel("补丁名称:");
            label.setBounds(x, y, 82, height);
            contentPane.add(label);
            textField_saveName = new JBTextField();
            textField_saveName.setBounds(117, 14, 462, height);
            contentPane.add(textField_saveName);
            textField_saveName.setColumns(10);

            JLabel label_1 = new JBLabel("导出位置:");
            label_1.setBounds(x, 50, 82, height);
            contentPane.add(label_1);
            textField_savePath = new JBTextField();
            textField_savePath.setColumns(10);
            textField_savePath.setBounds(117, y = y + height + 5, 462, height);
            contentPane.add(textField_savePath);
            JButton button_selectSavePath = new JButton("选择路径");
            button_selectSavePath.setBounds(583, 50, 113, height);
            button_selectSavePath.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String userDir = System.getProperty("user.home");
                    JFileChooser fileChooser = new JFileChooser(userDir);
                    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    int flag = fileChooser.showOpenDialog(null);
                    if (flag == JFileChooser.APPROVE_OPTION) {
                        textField_savePath.setText(fileChooser.getSelectedFile().getAbsolutePath());
                    }
                }
            });
            contentPane.add(button_selectSavePath);

            JLabel label_3 = new JBLabel("汇总SQL文件时 过滤重复SQL:");
            contentPane.add(label_3);
            filtersql = new JBCheckBox();
            filtersql.setSelected("true".equalsIgnoreCase(ProjectNCConfigUtil.getConfigValue("filtersql", "true")));
            JBPanel panel1 = new JBPanel();
            //  panel1.setBorder(LineBorder.createGrayLineBorder());
            panel1.setLayout(new BoxLayout(panel1, BoxLayout.X_AXIS));
            panel1.setBounds(x, y = y + height + 5, width, height);
            panel1.add(label_3);
            panel1.add(filtersql);
            contentPane.add(panel1);

            JLabel label_2 = new JBLabel("是否导出SQL:");
            exportSql = new JBCheckBox();
            exportSql.setSelected("true".equalsIgnoreCase(ProjectNCConfigUtil.getConfigValue("exportSql", "true")));
            JBPanel panel2 = new JBPanel();
            //panel2.setBorder(LineBorder.createGrayLineBorder());
            panel2.setLayout(new BoxLayout(panel2, BoxLayout.X_AXIS));
            panel2.setBounds(x, y = y + height + 5, width, height);
            panel2.add(label_2);
            panel2.add(exportSql);
            contentPane.add(panel2);

            label_2 = new JBLabel("是否只保留全量sql单个文件:");
            onleyFullSql = new JBCheckBox();
            onleyFullSql.setSelected("true".equalsIgnoreCase(ProjectNCConfigUtil.getConfigValue("onleyFullSql", "true")));
            panel2 = new JBPanel();
            //panel2.setBorder(LineBorder.createGrayLineBorder());
            panel2.setLayout(new BoxLayout(panel2, BoxLayout.X_AXIS));
            panel2.setBounds(x, y = y + height + 5, width, height);
            panel2.add(label_2);
            panel2.add(onleyFullSql);
            contentPane.add(panel2);

            label_2 = new JBLabel("强制IDEA连接数据库导出SQL:");
            rebuild = new JBCheckBox();
            rebuild.setSelected("true".equalsIgnoreCase(ProjectNCConfigUtil.getConfigValue("rebuildsql", "false")));
            panel2 = new JBPanel();
            //panel2.setBorder(LineBorder.createGrayLineBorder());
            panel2.setLayout(new BoxLayout(panel2, BoxLayout.X_AXIS));
            panel2.setBounds(x, y = y + height + 5, width, height);
            panel2.add(label_2);
            panel2.add(rebuild);
            contentPane.add(panel2);

            JLabel label_4 = new JBLabel("强制IDEA连接数据库导出SQL使用NC配置的数据源第几个(0开始):");
            List<NCDataSourceVO> dataSourceVOS = NCPropXmlUtil.getDataSourceVOS();
            dataSourceIndex = new JComboBox(new Vector(
                    dataSourceVOS.stream()
                            .map(v -> v.getDataSourceName() + '/' + v.getUser())
                            .collect(Collectors.toList())
            ));
            dataSourceIndex.setSelectedIndex(ConvertUtil.toInt(ProjectNCConfigUtil.getConfigValue("data_source_index"), 0));
            contentPane.add(dataSourceIndex);
            JButton button_TestDb = new JButton("测试连接");
            button_TestDb.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Integer i = dataSourceIndex.getSelectedIndex();
                    NCDataSourceVO ds = NCPropXmlUtil.get(i);
                    if (ds == null) {
                        Messages.showErrorDialog(event.getProject(), "数据源索引不存在: " + i + " ,数据源数量: "
                                        + (NCPropXmlUtil.getDataSourceVOS() == null ? 0 :
                                        NCPropXmlUtil.getDataSourceVOS().size())
                                , "错误:");
                        return;
                    }
                    Connection conn = null;
                    try {
                        conn = ConnectionUtil.getConn(ds);
                        Messages.showInfoMessage(event.getProject(), JSON.toJSONString(ds, true), "恭喜!测试成功:");
                    } catch (Throwable ex) {
                        ex.printStackTrace();
                        Messages.showErrorDialog(event.getProject(), "连接失败: " + JSON.toJSONString(ds, true)
                                + " !错误原因:\n" + ExceptionUtil.toStringLines(ex, 5), "错误");
                    } finally {
                        IoUtil.close(conn);
                    }
                }
            });
            JBPanel panel3 = new JBPanel();
            //  panel3.setBorder(LineBorder.createGrayLineBorder());
            panel3.setLayout(new BoxLayout(panel3, BoxLayout.X_AXIS));
            panel3.setBounds(x, y = y + height + 5, width, height);
            panel3.add(label_4);
            panel3.add(dataSourceIndex);
            panel3.add(button_TestDb);
            contentPane.add(panel3);

            JLabel label_5 = new JBLabel("强制指定导出使用的NC版本:");
            ncVersion = new JComboBox<NcVersionEnum>(NcVersionEnum.values());
            ncVersion.setSelectedItem(ProjectNCConfigUtil.getNCVerSIon());
            JBPanel panel4 = new JBPanel();
            //  panel4.setBorder(LineBorder.createGrayLineBorder());
            panel4.setLayout(new BoxLayout(panel4, BoxLayout.X_AXIS));
            panel4.setBounds(x, y = y + height + 5, width, height);
            panel4.add(label_5);
            panel4.add(ncVersion);
            contentPane.add(panel4);

            JLabel label_6 = new JBLabel("是否导出前端资源(resources):");
            exportResources = new JBCheckBox();
            exportResources.setSelected("true".equalsIgnoreCase(ProjectNCConfigUtil.getConfigValue("exportResources",
                    "true")));
            JBPanel panel5 = new JBPanel();
            //  panel5.setBorder(LineBorder.createGrayLineBorder());
            panel5.setLayout(new BoxLayout(panel5, BoxLayout.X_AXIS));
            panel5.setBounds(x, y = y + height + 5, width, height);
            panel5.add(label_6);
            panel5.add(exportResources);
            contentPane.add(panel5);

            label_6 = new JBLabel("自动删除hotwebs的dist后执行npm run build:");
            reNpmBuild = new JBCheckBox();
            reNpmBuild.setSelected("true".equalsIgnoreCase(ProjectNCConfigUtil.getConfigValue("reNpmBuild", "true")));
            panel5 = new JBPanel();
            //  panel5.setBorder(LineBorder.createGrayLineBorder());
            panel5.setLayout(new BoxLayout(panel5, BoxLayout.X_AXIS));
            panel5.setBounds(x, y = y + height + 5, width, height);
            panel5.add(label_6);
            panel5.add(reNpmBuild);
            contentPane.add(panel5);

            JLabel label_7 = new JBLabel("导出云管家格式:");
            format4Ygj = new JBCheckBox();
            format4Ygj.setSelected("true".equalsIgnoreCase(ProjectNCConfigUtil.getConfigValue("format4Ygj", "true")));
            JBPanel panel6 = new JBPanel();
            //  panel6.setBorder(LineBorder.createGrayLineBorder());
            panel6.setLayout(new BoxLayout(panel6, BoxLayout.X_AXIS));
            panel6.setBounds(x, y = y + height + 5, width, height);
            panel6.add(label_7);
            panel6.add(format4Ygj);
            contentPane.add(panel6);

            JLabel label_13 = new JBLabel("生成zip压缩文件:");
            zip = new JBCheckBox();
            zip.setSelected("true".equalsIgnoreCase(ProjectNCConfigUtil.getConfigValue("zip", "true")));
            JBPanel panel13 = new JBPanel();
            //  panel6.setBorder(LineBorder.createGrayLineBorder());
            panel13.setLayout(new BoxLayout(panel13, BoxLayout.X_AXIS));
            panel13.setBounds(x, y = y + height + 5, width, height);
            panel13.add(label_13);
            panel13.add(zip);
            contentPane.add(panel13);

            JLabel label_12 = new JBLabel("是否只保留zip文件(删除补丁文件夹):");
            deleteDir = new JBCheckBox();
            deleteDir.setSelected("true".equalsIgnoreCase(ProjectNCConfigUtil.getConfigValue("deleteDir", "true")));
            deleteDir.addActionListener(ev -> {
                if (deleteDir.isSelected()) {
                    zip.setSelected(true);
                }
            });
            JBPanel panel12 = new JBPanel();
            //  panel6.setBorder(LineBorder.createGrayLineBorder());
            panel12.setLayout(new BoxLayout(panel12, BoxLayout.X_AXIS));
            panel12.setBounds(x, y = y + height + 5, width, height);
            panel12.add(label_12);
            panel12.add(deleteDir);
            contentPane.add(panel12);

            JLabel label_10 = new JBLabel("混淆覆写导出的源码文件内容(java文件和js源码:__SOURCE__CODE__):");
            reWriteSourceFile = new JBCheckBox();
            reWriteSourceFile.setSelected("true".equalsIgnoreCase(ProjectNCConfigUtil.getConfigValue(
                    "reWriteSourceFile", "false")));
            JBPanel panel7 = new JBPanel();
            //  panel7.setBorder(LineBorder.createGrayLineBorder());
            panel7.setLayout(new BoxLayout(panel7, BoxLayout.X_AXIS));
            panel7.setBounds(x, y = y + height + 5, width, height);
            panel7.add(label_10);
            panel7.add(reWriteSourceFile);
            contentPane.add(panel7);

            JLabel label_8 = new JBLabel("只导出选中的模块或文件:");
            selectExport = new JBCheckBox();
            this.selectExport.setSelected(ExportContentVO.EVENT_POPUP_CLICK.equals(event.getPlace()));
            this.selectExport.addChangeListener((e) -> updateSelectFileTable());
            JBPanel panel10 = new JBPanel();
            panel10.setLayout(new BoxLayout(panel10, BoxLayout.X_AXIS));
            panel10.setBounds(x, y = y + height + 5, 500, height);
            panel10.add(label_8);
            panel10.add(selectExport);
            contentPane.add(panel10);

            JLabel label_9 = new JBLabel("当前选中的要导出的内容:");
            label_9.setBounds(x, y = y + height + 5, 280, height);
            contentPane.add(label_9);
            Vector heads = new Vector();
            heads.add("选择");
            heads.add("路径");
            defaultTableModel = new DefaultTableModel(heads, 0);
            updateSelectFileTable();
            selectTable = new JBTable(defaultTableModel);
            selectTable.setBorder(LineBorder.createBlackLineBorder());
            selectTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            selectTable.addMouseListener(new MySelectFileTableMouseAdpaterImpl(this));
            JScrollPane jScrollPane = new JBScrollPane(this.selectTable);
            jScrollPane.setAutoscrolls(true);
            jScrollPane.setBounds(x, y = y + height + 5, 770, 200);
            // 对每一列设置单元格渲染器
            for (int i = 0; i < selectTable.getColumnCount(); i++) {
                selectTable.getColumnModel().getColumn(i).setCellRenderer(new MyTableRenderer());
            }
            selectTable.getColumnModel().getColumn(0).setCellEditor(new MyTableCellEditor(this, new JBCheckBox()));
            contentPane.add(jScrollPane);

            //设置点默认值
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss");
            this.textField_saveName.setText("modules");
            this.textField_savePath.setText(ProjectUtil.getDefaultProject().getBasePath()
                    + File.separatorChar + "patchers"
                    + File.separatorChar + "patcher-" + event.getProject().getName() + "-" + LocalDateTime.now().format(formatter)
            );

            contentPane.setPreferredSize(new Dimension(width + 40, y + 10));
        }

        return this.contentPane;
    }

    @Data
    @AllArgsConstructor
    public static class MySelectFileTableMouseAdpaterImpl extends MouseAdapter {
        PatcherDialog patcherDialog;

        @Override
        public void mousePressed(MouseEvent me) {
            if (SwingUtilities.isRightMouseButton(me)) {
                final int row = patcherDialog.selectTable.rowAtPoint(me.getPoint());
                System.out.println("row:" + row);
                if (row != -1) {
                    final int column = patcherDialog.selectTable.columnAtPoint(me.getPoint());

                    final JPopupMenu popup = new JPopupMenu();
                    JMenuItem selectAllOrNot = new JMenuItem("全选/全消");

                    selectAllOrNot.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            //高亮选择指定的行
                            List<SelectDTO> ds = patcherDialog.getSelectDtos();
                            if (ds != null) {
                                for (SelectDTO d : ds) {
                                    d.setSelect(!d.isSelect());
                                }
                                patcherDialog.setSelectTableDatas(ds);
                            }
                            // patcherDialog.selectTable.setRowSelectionInterval(row, row);
                        }
                    });
                    popup.add(selectAllOrNot);
                    popup.add(new JSeparator());

                    JMenuItem edit = new JMenuItem("编辑");
                    popup.add(edit);
                    edit.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            System.out.println("编辑");
                            patcherDialog.selectTable.clearSelection(); //清除高亮选择状态
                            patcherDialog.selectTable.editCellAt(row, column); //设置某列为可编辑
                        }
                    });

                    JMenuItem calcel = new JMenuItem("删行");
                    calcel.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            List<SelectDTO> ds = patcherDialog.getSelectDtos();
                            if (ds != null && ds.size() > row) {
                                ds.remove(row);
                                patcherDialog.setSelectTableDatas(ds);
                            }
                        }
                    });
                    popup.add(new JSeparator());
                    popup.add(calcel);
                    popup.show(me.getComponent(), me.getX(), me.getY());
                }
            }
        }
    }

    public void fitTableColumns(JTable myTable) {
        if (myTable == null) {
            return;
        }
        JTableHeader header = myTable.getTableHeader();
        int rowCount = myTable.getRowCount();
        Enumeration columns = myTable.getColumnModel().getColumns();
        while (columns.hasMoreElements()) {
            TableColumn column = (TableColumn) columns.nextElement();
            int col = header.getColumnModel().getColumnIndex(column.getIdentifier());
            int width = (int) myTable.getTableHeader().getDefaultRenderer()
                    .getTableCellRendererComponent(myTable, column.getIdentifier()
                            , false, false, -1, col).getPreferredSize().getWidth();
            for (int row = 0; row < rowCount; row++) {
                int preferedWidth = (int) myTable.getCellRenderer(row, col).getTableCellRendererComponent(myTable,
                        myTable.getValueAt(row, col), false, false, row, col).getPreferredSize().getWidth();
                width = Math.max(width, preferedWidth);
            }
            header.setResizingColumn(column); // 此行很重要
            column.setWidth(width + myTable.getIntercellSpacing().width + 5);
        }
    }

    public static Map<Project, List<VirtualFile>> FORCEADDSELECTFILES = new ConcurrentHashMap<>();
    private void updateSelectFileTable() {
        ArrayList<SelectDTO> rows = Lists.newArrayList();
        if (selectExport.isSelected()) {
            VirtualFile[] selects = LangDataKeys.VIRTUAL_FILE_ARRAY.getData(event.getDataContext());
            List<VirtualFile> vs = FORCEADDSELECTFILES.getOrDefault(event.getProject(), new CopyOnWriteArrayList<>());
            for (VirtualFile select : selects) {
                rows.add(SelectDTO.builder()
                        .select(true)
                        .path(select.getPath())
                        .file(select)
                        .build());
            }
            for (VirtualFile select : vs) {
                rows.add(SelectDTO.builder()
                        .select(true)
                        .path(select.getPath())
                        .file(select)
                        .build());
            }
        } else {
            rows.add(SelectDTO.builder()
                    .select(true)
                    .path("全部")
                    .build());
        }

        setSelectTableDatas(rows);
    }

    @lombok.Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class SelectDTO {
        @Builder.Default
        boolean select = true;
        String path;
        VirtualFile file;
    }

    List<SelectDTO> selectDtos;

    public void setSelectTableDatas(List<SelectDTO> rows) {
        setSelectDtos(rows);

        while (defaultTableModel.getRowCount() > 0) {
            defaultTableModel.removeRow(0);
        }

        for (SelectDTO row : rows) {
            Vector v = new Vector();
            v.add(row.isSelect());
            v.add(row.getPath());
            defaultTableModel.addRow(v);
        }

        fitTableColumns(selectTable);
    }

    /***
     *   点击了确认，开始导出补丁包        </br>
     *           </br>
     *           </br>
     * @author air Email: 209308343@qq.com
     * @date 2019/12/25 0025 16:17
     * @Param []
     * @return void
     */
    public void onOK() {
        if (isRuning) {
            LogUtil.error("上一次导出任务还未处理完，请稍后再试!");
            isRuning = false;
            return;
        }

        if ((null == this.textField_saveName.getText()) || ("".equals(this.textField_saveName.getText()))) {
            LogUtil.error("请输入补丁包名字!");
            return;
        }
        if ((null == this.textField_savePath.getText()) || ("".equals(this.textField_savePath.getText()))) {
            LogUtil.error("请选择补丁要导出到的路径!");
            return;
        }

        String dirName = this.textField_saveName.getText();
        dirName = null == dirName || dirName.trim().isEmpty() ? "export" : dirName;
        String exportPath = this.textField_savePath.getText() + File.separatorChar + dirName;
        Task.Backgroundable backgroundable = new Task.Backgroundable(event.getProject(), "导出中...请等待...") {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                isRuning = true;
                indicator.setText("正在玩命导出NC补丁包中...");
                indicator.setText2("导出成功后会自动打开文件夹： " + exportPath);
                indicator.setIndeterminate(true);

                try {
                    long s = System.currentTimeMillis();
                    ExportContentVO contentVO = new ExportContentVO();
                    contentVO.outPath = exportPath;
                    contentVO.project = event.getProject();
                    contentVO.event = event;
                    contentVO.indicator = indicator;
                    contentVO.filtersql = filtersql.isSelected();
                    contentVO.rebuildsql = rebuild.isSelected();
                    contentVO.reWriteSourceFile = reWriteSourceFile.isSelected();
                    contentVO.reNpmBuild = reNpmBuild.isSelected();
                    contentVO.deleteDir = deleteDir.isSelected();
                    contentVO.format4Ygj = format4Ygj.isSelected();
                    contentVO.exportResources = exportResources.isSelected();
                    contentVO.exportSql = exportSql.isSelected();
                    contentVO.onleyFullSql = onleyFullSql.isSelected();
                    contentVO.zip = zip.isSelected();
                    contentVO.data_source_index = dataSourceIndex.getSelectedIndex();
                    contentVO.ncVersion = (NcVersionEnum) ncVersion.getSelectedItem();

                    contentVO.init();
                    contentVO.selectExport = selectExport.isSelected();
                    if (contentVO.selectExport) {
                        Vector<Vector> rows = defaultTableModel.getDataVector();
                        contentVO.setSelectFiles(Lists.newArrayList());
                        for (Vector row : rows) {
                            if ("true".equals(row.get(0)) || Boolean.TRUE.equals(row.get(0))) {
                                String path = StringUtil.replaceChars((String) row.get(1), "/", File.separator);
                                path = StringUtil.replaceChars(path, "\\", File.separator);
                                contentVO.getSelectFiles().add(path);
                            }
                        }
                        contentVO.initSelectModules();
                    }

                    if (contentVO.exportResources && contentVO.reNpmBuild) {
                        //你懂得！
                        reBuildNpmPatcther(indicator);
                    }

                    ExportNCPatcherUtil.export(contentVO);
                    long e = System.currentTimeMillis();
                    LogUtil.info("导出成功,耗时:" + ((e - s) / 1000.0d) + " (秒s)!硬盘路径： " + contentVO.getOutPath());

                    File dirToOpen = new File(contentVO.getOutPath());

                    try {
                        if (contentVO.zip) {
                            zipPathcers(contentVO, dirToOpen);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    File opendir = null;
                    if (contentVO.isFormat4Ygj()) {
                        if (contentVO.isDeleteDir()) {
                            try {
                                FileUtil.del(dirToOpen);
                            } catch (Throwable ex) {
                            }
                        }

                        opendir = dirToOpen.getParentFile();
                    } else {
                        if (contentVO.isDeleteDir()) {
                            try {
                                FileUtil.del(dirToOpen.getParentFile());
                            } catch (Throwable ex) {
                            }
                        }
                        opendir = dirToOpen.getParentFile().getParentFile();
                    }

                    if (opendir != null) {
                        try {
                            Desktop desktop = Desktop.getDesktop();
                            desktop.open(opendir);
                        } catch (Throwable ioException) {
                            ioException.printStackTrace();
                            LogUtil.info("导出成功！路径: " + opendir.getPath());
                        }
                    }
                } catch (Throwable iae) {
                    LogUtil.error("自动打开路径失败: " + ExceptionUtil.getExcptionDetall(iae));
                } finally {
                    isRuning = false;
                }
            }
        };
        backgroundable.setCancelText("放弃吧,没有卵用的按钮");
        backgroundable.setCancelTooltipText("这是一个没有卵用的按钮");
        ProgressManager.getInstance().run(backgroundable);

        dispose();
    }

    private void reBuildNpmPatcther(@NotNull ProgressIndicator indicator) throws IOException {
        if (new File(event.getProject().getBasePath(), "hotwebs").isDirectory()) {
            indicator.setText("强制删除前端hotwebs的dist后执行npm run build中...");
            IoUtil.cleanUpDirFiles(new File(event.getProject().getBasePath(), "hotwebs" + File.separatorChar + "dist"));
            String cm = ExecUtil.npmBuild(new File(event.getProject().getBasePath(), "hotwebs").getPath()
                    , (line) -> {
                        indicator.setText("npm building:" + StrUtil.removeAllLineBreaks(line));
                    }
            );
            LogUtil.info("前端 npm run build: " + cm);
        }
    }

    private void zipPathcers(ExportContentVO contentVO, File dirToOpen) {
        File zip = null;
        if (contentVO.isFormat4Ygj()) {
            zip = new File(dirToOpen.getParentFile(), dirToOpen.getName() + ".zip");
            contentVO.indicator.setText("自动打包成zip压缩包中..." + zip.getPath());
            zip = ZipUtil.zip(dirToOpen);
        } else {
            zip = new File(dirToOpen.getParentFile().getParentFile(), dirToOpen.getParentFile().getName() + ".zip");
            contentVO.indicator.setText("自动打包成zip压缩包中..." + zip.getPath());
            File src = ZipUtil.zip(dirToOpen.getParentFile());
            zip = new File(dirToOpen.getParentFile().getParentFile(), dirToOpen.getParentFile().getName() + ".zip");
        }
        LogUtil.info("自动打包成补丁zip压缩包的硬盘路径： " + zip.getPath());
    }

    @Nullable
    @Override
    protected String getDimensionServiceKey() {
        return "PatcherDialog";
    }

    private void onCancel() {
        dispose();
    }
}
