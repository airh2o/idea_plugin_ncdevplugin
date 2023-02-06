package com.air.nc5dev.ui;

import cn.hutool.core.util.StrUtil;
import com.air.nc5dev.enums.NcVersionEnum;
import com.air.nc5dev.util.*;
import com.air.nc5dev.util.idea.LogUtil;
import com.air.nc5dev.util.idea.ProjectUtil;
import com.air.nc5dev.util.jdbc.ConnectionUtil;
import com.air.nc5dev.vo.ExportContentVO;
import com.air.nc5dev.vo.NCDataSourceVO;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.table.JBTable;
import org.apache.commons.compress.utils.Lists;
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
import java.io.File;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
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
    JBCheckBox selectExport;
    JBTable selectTable;
    DefaultTableModel defaultTableModel;

    public PatcherDialog(AnActionEvent event) {
        super(event.getProject());
        this.event = event;
        init();
        setTitle("导出NC补丁包..." + (
                ExportContentVO.EVENT_POPUP_CLICK.equals(event.getPlace())
                        ? "(项目树点击的导出:只导出选中的模块和文件。如果不想这样 可以点击 Tools菜单选中导出)"
                        : "(Tools菜单点击的导出:导出全部项目。如果不想这样 可以点击 左侧项目树 选中要导出的模块或具体文件)"
        ));
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        if (contentPane == null) {
            contentPane = //new JBPanel();
                    new JBScrollPane();
            contentPane.setLayout(null);
            contentPane.setAutoscrolls(true);

            JLabel label = new JBLabel("补丁名称:");
            label.setBounds(21, 16, 82, 31);
            contentPane.add(label);
            textField_saveName = new JBTextField();
            textField_saveName.setBounds(117, 14, 462, 36);
            contentPane.add(textField_saveName);
            textField_saveName.setColumns(10);

            JLabel label_1 = new JBLabel("导出位置:");
            label_1.setBounds(21, 50, 82, 31);
            contentPane.add(label_1);
            textField_savePath = new JBTextField();
            textField_savePath.setColumns(10);
            textField_savePath.setBounds(117, 50, 462, 36);
            contentPane.add(textField_savePath);
            JButton button_selectSavePath = new JButton("选择路径");
            button_selectSavePath.setBounds(583, 50, 113, 36);
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
            label_3.setBounds(21, 90, 200, 31);
            contentPane.add(label_3);
            filtersql = new JBCheckBox();
            filtersql.setSelected("true".equalsIgnoreCase(ProjectNCConfigUtil.getConfigValue("filtersql", "true")));
            filtersql.setBounds(190, 90, 60, 36);
            contentPane.add(filtersql);

            JLabel label_2 = new JBLabel("强制IDEA连接数据库导出SQL:");
            label_2.setBounds(21, 116, 200, 31);
            contentPane.add(label_2);
            rebuild = new JBCheckBox();
            rebuild.setSelected("true".equalsIgnoreCase(ProjectNCConfigUtil.getConfigValue("rebuildsql", "false")));
            rebuild.setBounds(190, 116, 60, 36);
            contentPane.add(rebuild);

            JLabel label_4 = new JBLabel("强制IDEA连接数据库导出SQL使用NC配置的数据源第几个(0开始):");
            label_4.setBounds(21, 150, 400, 31);
            contentPane.add(label_4);
            List<NCDataSourceVO> dataSourceVOS = NCPropXmlUtil.getDataSourceVOS();
            dataSourceIndex = new JComboBox(new Vector(
                    dataSourceVOS.stream()
                            .map(v -> v.getDataSourceName() + '/' + v.getUser())
                            .collect(Collectors.toList())
            ));
            dataSourceIndex.setSelectedIndex(ConvertUtil.toInt(ProjectNCConfigUtil.getConfigValue("data_source_index"), 0));
            dataSourceIndex.setBounds(380, 150, 100, 35);
            contentPane.add(dataSourceIndex);
            JButton button_TestDb = new JButton("测试连接");
            button_TestDb.setBounds(500, 150, 113, 42);
            button_TestDb.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Integer i = dataSourceIndex.getSelectedIndex();
                    NCDataSourceVO ds = NCPropXmlUtil.get(i);
                    if (ds == null) {
                        Messages.showErrorDialog(event.getProject(), "数据源索引不存在: " + i + " ,数据源数量: "
                                        + (NCPropXmlUtil.getDataSourceVOS() == null ? 0 : NCPropXmlUtil.getDataSourceVOS().size())
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
            contentPane.add(button_TestDb);

            JLabel label_5 = new JBLabel("强制指定导出使用的NC版本:");
            label_5.setBounds(21, 200, 180, 30);
            contentPane.add(label_5);
            ncVersion = new JComboBox<NcVersionEnum>(NcVersionEnum.values());
            ncVersion.setSelectedItem(ProjectNCConfigUtil.getNCVerSIon());
            ncVersion.setBounds(180, 200, 100, 30);
            contentPane.add(ncVersion);

            JLabel label_6 = new JBLabel("自动删除hotwebs的dist后执行npm run build:");
            label_6.setBounds(21, 225, 280, 30);
            contentPane.add(label_6);
            reNpmBuild = new JBCheckBox();
            reNpmBuild.setSelected("true".equalsIgnoreCase(ProjectNCConfigUtil.getConfigValue("reNpmBuild", "true")));
            reNpmBuild.setBounds(280, 225, 60, 36);
            contentPane.add(reNpmBuild);

            JLabel label_7 = new JBLabel("导出云管家格式:");
            label_7.setBounds(21, 250, 280, 30);
            contentPane.add(label_7);
            format4Ygj = new JBCheckBox();
            format4Ygj.setSelected("true".equalsIgnoreCase(ProjectNCConfigUtil.getConfigValue("format4Ygj", "true")));
            format4Ygj.setBounds(280, 250, 60, 36);
            contentPane.add(format4Ygj);

            JLabel label_8 = new JBLabel("只导出选中的模块或文件:");
            label_8.setBounds(21, 280, 280, 30);
            contentPane.add(label_8);
            selectExport = new JBCheckBox();
            this.selectExport.setSelected(ExportContentVO.EVENT_POPUP_CLICK.equals(event.getPlace()));
            this.selectExport.setBounds(280, 280, 60, 36);
            this.selectExport.addChangeListener((e) -> updateSelectFileTable());
            contentPane.add(this.selectExport);

            JLabel label_9 = new JBLabel("当前选中的要导出的内容:");
            label_9.setBounds(21, 300, 280, 30);
            contentPane.add(label_9);
            Vector heads = new Vector();
            heads.add("选择");
            heads.add("路径");
            defaultTableModel = new DefaultTableModel(heads, 0);
            updateSelectFileTable();
            selectTable = new JBTable(defaultTableModel);
            selectTable.setBorder(LineBorder.createBlackLineBorder());
            JScrollPane jScrollPane = new JBScrollPane(this.selectTable);
            jScrollPane.setAutoscrolls(true);
            jScrollPane.setBounds(21, 330, 770, 200);
            // 对每一列设置单元格渲染器
            for (int i = 0; i < selectTable.getColumnCount(); i++) {
                selectTable.getColumnModel().getColumn(i).setCellRenderer(new MyTableRenderer());
            }
            selectTable.getColumnModel().getColumn(0).setCellEditor(new MyTableCellEditor(new JBCheckBox()));
            contentPane.add(jScrollPane);

            //设置点默认值
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss");
            this.textField_saveName.setText("modules");
            this.textField_savePath.setText(ProjectUtil.getDefaultProject().getBasePath()
                    + File.separatorChar + "patchers"
                    + File.separatorChar + "patcher-" + event.getProject().getName() + "-" + LocalDateTime.now().format(formatter)
            );
        }

        contentPane.setPreferredSize(new Dimension(780, 600));
        return this.contentPane;
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
            column.setWidth(width + myTable.getIntercellSpacing().width);
        }
    }

    private void updateSelectFileTable() {
        ArrayList<Vector> rows = Lists.newArrayList();
        if (selectExport.isSelected()) {
            VirtualFile[] selects = LangDataKeys.VIRTUAL_FILE_ARRAY.getData(event.getDataContext());
            for (VirtualFile select : selects) {
                Vector row = new Vector();
                rows.add(row);
                row.add(true);
                row.add(select.getPath());
            }
        } else {
            Vector row = new Vector();
            rows.add(row);
            row.add(true);
            row.add("全部");
        }

        while (defaultTableModel.getRowCount() > 0) {
            defaultTableModel.removeRow(0);
        }

        for (Vector row : rows) {
            defaultTableModel.addRow(row);
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
                    contentVO.reNpmBuild = reNpmBuild.isSelected();
                    contentVO.format4Ygj = format4Ygj.isSelected();
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

                    if (contentVO.reNpmBuild) {
                        //你懂得！
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

                    ExportNCPatcherUtil.export(contentVO);
                    long e = System.currentTimeMillis();
                    LogUtil.info("导出成功,耗时:" + ((e - s) / 1000.0d) + " (秒s)!硬盘路径： " + contentVO.getOutPath());

                    Desktop desktop = Desktop.getDesktop();
                    File dirToOpen = new File(contentVO.getOutPath());
                    desktop.open(dirToOpen);
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

    @Nullable
    @Override
    protected String getDimensionServiceKey() {
        return "PatcherDialog";
    }

    private void onCancel() {
        dispose();
    }
}
