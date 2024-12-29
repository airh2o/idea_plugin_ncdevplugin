package com.air.nc5dev.ui.searchnclogs;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.LineHandler;
import cn.hutool.core.util.StrUtil;
import com.air.nc5dev.ui.SearchTableFieldDialog;
import com.air.nc5dev.util.CollUtil;
import com.air.nc5dev.util.ConvertUtil;
import com.air.nc5dev.util.ExceptionUtil;
import com.air.nc5dev.util.IoUtil;
import com.air.nc5dev.util.NCPropXmlUtil;
import com.air.nc5dev.util.ProjectNCConfigUtil;
import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.util.idea.LogUtil;
import com.air.nc5dev.util.jdbc.ConnectionUtil;
import com.air.nc5dev.util.searchfulldatabase.SearchResultVO;
import com.air.nc5dev.vo.NCDataSourceVO;
import com.alibaba.fastjson.JSON;
import com.google.common.base.Joiner;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.table.JBTable;
import com.intellij.util.PatternUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileFilter;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Handler;
import java.util.regex.Pattern;
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
public class SearchNclogsDialog extends DialogWrapper {
    JComponent contentPane;
    Project project;
    JTextField textField_match;
    JTextField textField_sucessFile;
    JTextArea textArea_msg;
    JTextField textField_sqlFile;
    JTextField textField_savePath;
    JTextField textField_filefilter;
    JBCheckBox checkBox_autoCommit;
    JBCheckBox checkBox_skipErrorLine;
    JBCheckBox checkBox_matchRex;
    JProgressBar threadProgressBar;
    JBTable table_result;
    DefaultTableModel tableModel_result;
    JComboBox comboBox_dataSourceIndex;
    JComboBox comboBox_LikeType;
    JButton button_execute;
    JButton button_commit;
    JButton button_openlogfile;
    Connection connection = null;
    public static volatile SearchNclogsDialog me = null;
    Charset encode = Charset.forName("GBK");
    public static volatile String nclogs = "";
    public static volatile String match = "";
    public static volatile String fileFilter = "";
    public static volatile String outfile = "";
    AtomicReference<ProgressIndicator> indicatorAtomicReference = new AtomicReference<>(null);

    public SearchNclogsDialog(Project project) {
        super(project);
        this.project = project;
        init();
        setTitle("搜索nclogs内容");
    }

    public JComponent getContentPane() {
        return createCenterPanel();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        if (contentPane == null) {
            contentPaneInit();
        }

        return this.contentPane;
    }

    private void contentPaneInit() {
        JBTabbedPane jtab = new JBTabbedPane();
        contentPane = jtab;
        JBLabel label_2;
        JBPanel panel2;
        JBPanel panel5;
        JLabel label_6;
        {
            int x = 21;
            int y = 16;
            int height = 30;
            int width = 600;

            JBPanel jbxxp = new JBPanel();
            jbxxp.setLayout(null);
            JBScrollPane jbxx = new JBScrollPane(jbxxp);
            jbxx.setAutoscrolls(true);
            jtab.addTab("配置", jbxx);

            JLabel label = new JBLabel("进度:");
            label.setBounds(x, y, 60, height);
            jbxxp.add(label);
            threadProgressBar = new JProgressBar();
            threadProgressBar.setBounds(x + 85, y, 800, height);
            // threadProgressBar.setForeground(Color.BLACK);
            threadProgressBar.setStringPainted(true);
            jbxxp.add(threadProgressBar);

            label = new JBLabel("要搜索的文件夹路径(多个?隔开):");
            label.setBounds(x, y = threadProgressBar.getY() + threadProgressBar.getHeight() + 2, 180, height);
            jbxxp.add(label);
            textField_sqlFile = new JBTextField();
            textField_sqlFile.setBounds(label.getX() + label.getWidth() + 2, y, 750, height);
            jbxxp.add(textField_sqlFile);
            JButton button_selectSqlFile = new JButton("选择nclogs文件夹路径");
            button_selectSqlFile.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JFileChooser fileChooser = new JFileChooser(getProject().getBasePath());
                    fileChooser.setDialogTitle("选择nclogs文件夹(可多选):");
                    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    fileChooser.setMultiSelectionEnabled(true);
                    int flag = fileChooser.showOpenDialog(null);
                    if (flag != JFileChooser.APPROVE_OPTION) {
                        return;
                    }

                    File[] fs = fileChooser.getSelectedFiles();
                    if (CollUtil.isEmpty(fs)) {
                        return;
                    }

                    textField_sqlFile.setText(Arrays.stream(fs).map(f -> f.getPath()).collect(Collectors.joining("?")));
                }
            });
            button_selectSqlFile.setBounds(textField_sqlFile.getX() + textField_sqlFile.getWidth() + 2, y, 150, height);
            jbxxp.add(button_selectSqlFile);

            label = new JBLabel("输出的结果文件:");
            label.setBounds(x, y = textField_sqlFile.getY() + textField_sqlFile.getHeight() + 2, 180, height);
            jbxxp.add(label);
            textField_sucessFile = new JBTextField();
            textField_sucessFile.setBounds(label.getX() + label.getWidth() + 2, y, 800, height);
            textField_sucessFile.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() > 1) {
                        IoUtil.tryOpenFileExpolor(new File(textField_sucessFile.getText()));
                    }
                }
            });
            jbxxp.add(textField_sucessFile);
            button_openlogfile = new JButton("打开结果文件");
            button_openlogfile.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    IoUtil.tryOpenFileExpolor(new File(textField_sucessFile.getText()));
                }
            });
            button_openlogfile.setBounds(textField_sucessFile.getX() + textField_sucessFile.getWidth() + 2, y, 120, height);
            jbxxp.add(button_openlogfile);


            label = new JBLabel("文件名过滤规则(支持正则):");
            label.setBounds(x, y += height + 5, 180, height);
            jbxxp.add(label);
            textField_filefilter = new JBTextField();
            textField_filefilter.setBounds(label.getX() + label.getWidth() + 2, y, 800, height);
            jbxxp.add(textField_filefilter);

            label = new JBLabel("匹配文本:");
            label.setBounds(x, y += height + 5, 180, height);
            jbxxp.add(label);
            textField_match = new JBTextField();
            textField_match.setBounds(label.getX() + label.getWidth() + 2, y, 750, height);
            textField_match.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() > 1) {
                        IoUtil.tryOpenFileExpolor(new File(textField_match.getText()));
                    }
                }
            });
            jbxxp.add(textField_match);
            label = new JBLabel("是否支持正则:");
            label.setBounds(textField_match.getX() + textField_match.getWidth() + 2, y, 150, height);
            jbxxp.add(label);
            checkBox_matchRex = new JBCheckBox();
            checkBox_matchRex.setSelected(true);
            checkBox_matchRex.setBounds(label.getX() + label.getWidth() + 2, y, 30, height);
            jbxxp.add(checkBox_matchRex);

            label = new JBLabel("数据源:");
            label.setBounds(x, y += height + 5, 60, height);
            jbxxp.add(label);
            final List<NCDataSourceVO> dataSourceVOS = NCPropXmlUtil.getDataSourceVOS(getProject());
            comboBox_dataSourceIndex = new JComboBox(new Vector(
                    dataSourceVOS.stream()
                            .map(v -> v.getDataSourceName() + '/' + v.getUser())
                            .collect(Collectors.toList())
            ));
            if (comboBox_dataSourceIndex.getModel().getSize() > 0) {
                try {
                    comboBox_dataSourceIndex.setSelectedIndex(ConvertUtil.toInt(ProjectNCConfigUtil.getConfigValue(
                            "data_source_index"), 0));
                } catch (Throwable e) {
                }
            }
            comboBox_dataSourceIndex.setBounds(label.getX() + label.getWidth() + 2, y, 250, height);
            //  jbxxp.add(comboBox_dataSourceIndex);
            JButton button_TestDb = new JButton("测试连接");
            button_TestDb.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Integer i = comboBox_dataSourceIndex.getSelectedIndex();
                    NCDataSourceVO ds = NCPropXmlUtil.get(i);
                    if (ds == null) {
                        Messages.showErrorDialog(project, "数据源索引不存在: " + i + " ,数据源数量: "
                                        + (NCPropXmlUtil.getDataSourceVOS(getProject()) == null ? 0 :
                                        NCPropXmlUtil.getDataSourceVOS(getProject()).size())
                                , "错误:");
                        return;
                    }
                    Connection conn = null;
                    try {
                        conn = ConnectionUtil.getConn(ds);
                        Messages.showInfoMessage(project, JSON.toJSONString(ds, true), "恭喜!测试成功:");
                    } catch (Throwable ex) {
                        ex.printStackTrace();
                        Messages.showErrorDialog(project, "连接失败: " + JSON.toJSONString(ds, true)
                                + " !错误原因:\n" + ExceptionUtil.toStringLines(ex, 5), "错误");
                    } finally {
                        IoUtil.close(conn);
                    }
                }
            });
            button_TestDb.setBounds(comboBox_dataSourceIndex.getX() + comboBox_dataSourceIndex.getWidth() + 2, y, 85,
                    height);
            //   jbxxp.add(button_TestDb);

            label = new JBLabel("是否忽略错误行:");
            label.setBounds(button_TestDb.getX() + button_TestDb.getWidth() + 2, y, 120, height);
            //  jbxxp.add(label);
            checkBox_skipErrorLine = new JBCheckBox();
            checkBox_skipErrorLine.setSelected(true);
            checkBox_skipErrorLine.setBounds(label.getX() + label.getWidth() + 2, y, 30, height);
            //   jbxxp.add(checkBox_skipErrorLine);

            label = new JBLabel("是否自动提交:");
            label.setBounds(checkBox_skipErrorLine.getX() + checkBox_skipErrorLine.getWidth() + 2, y, 100, height);
            //   jbxxp.add(label);
            checkBox_autoCommit = new JBCheckBox();
            checkBox_autoCommit.setSelected(false);
            checkBox_autoCommit.setBounds(label.getX() + label.getWidth() + 2, y, 30, height);
            //  jbxxp.add(checkBox_autoCommit);

            label = new JBLabel("查询结果:");
            label.setBounds(x, y = y + height + 5, 80, height);
            jbxxp.add(label);
            tableModel_result = new DefaultTableModel(new Object[]{"序号", "是否成功", "文件路径", "行内容"}, 0);
            table_result = new JBTable(tableModel_result);
            JBScrollPane jbScrollPane = new JBScrollPane(table_result);
            jbScrollPane.setAutoscrolls(true);
            table_result.setBorder(LineBorder.createBlackLineBorder());
            table_result.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            table_result.addMouseListener(new MySelectFileTableMouseAdpaterImpl(this));
            jbScrollPane.setBounds(x, y = y + height + 5, 770, 200);
            jbxxp.add(jbScrollPane);

            button_execute = new JButton("执行");
            button_execute.addActionListener(e -> {
                onExecute();
            });
            button_execute.setBounds(label.getX() + label.getWidth() + 2, label.getY(), 150, 30);
            jbxxp.add(button_execute);

            button_commit = new JButton("结束事务(关闭连接)");
            button_commit.addActionListener(e -> {
                boolean commit = Messages.showYesNoDialog("确定 提交事务, 取消 回滚事务!"
                        , "请选择提交还是回滚:", Messages.getQuestionIcon()) != Messages.OK;

                try {
                    if (connection != null) {
                        if (commit) {
                            connection.commit();
                            textArea_msg.append("提交事务完成!!\n");
                        } else {
                            connection.rollback();
                            textArea_msg.append("回滚事务完成!!\n");
                        }
                    }
                } catch (Throwable ex) {
                    ex.printStackTrace();
                    LogUtil.error("数据库出错:" + ex.toString(), ex);
                    RuntimeException e2 = new RuntimeException(
                            "数据库出错:" + ex.toString()
                                    + "，错误栈:" + ExceptionUtil.toString(ExceptionUtil.getTopCase(ex))
                            , ex);
                    e2.setStackTrace(ex.getStackTrace());
                    throw e2;
                }
                IoUtil.close(connection);
                button_commit.setEnabled(false);
            });
            button_commit.setEnabled(false);
            button_commit.setBounds(button_execute.getX() + button_execute.getWidth() + 2, label.getY(),
                    button_execute.getWidth(), button_execute.getHeight());
            //  jbxxp.add(button_commit);

            label = new JBLabel("日志:");
            label.setBounds(x, jbScrollPane.getY() + jbScrollPane.getHeight() + 2, 60, height);
            jbxxp.add(label);
            textArea_msg = new JBTextArea();
            jbScrollPane = new JBScrollPane(textArea_msg);
            jbScrollPane.setAutoscrolls(true);
            jbScrollPane.setBounds(label.getX(), label.getY() + label.getHeight() + 2, 800, 120);
            textArea_msg.setEnabled(true);
            textArea_msg.setEditable(true);
            textArea_msg.setLineWrap(true);
            jbxxp.add(jbScrollPane);
        }

        //设置点默认值
        initDefualtValues();
    }

    public void initDefualtValues() {
        checkBox_autoCommit.setSelected(false);

        if (StrUtil.isBlank(outfile)) {
            outfile = new File(System.getProperty("user.dir") + File.separator + "匹配结果_"
                    + StringUtil.replaceAll(DateUtil.now(), ":", "-") + ".log").getPath();
        }
        if (StrUtil.isBlank(match)) {
            match = "callid=";
        }

        textField_sucessFile.setText(outfile);
        textField_match.setText(match);
        textField_filefilter.setText(fileFilter);
        textField_sqlFile.setText(nclogs);
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

    volatile List<SearchResultVO> SearchResultVOs;

    public void setSelectTableDatas(List<SearchResultVO> rows) {
        setSearchResultVOs(rows);

        SwingUtilities.invokeLater(() -> {
            while (tableModel_result.getRowCount() > 0) {
                tableModel_result.removeRow(0);
            }

            for (SearchResultVO row : rows) {
                Vector v = new Vector();
                v.add(row.isSelect());
                v.add(row.getRow());
                v.add(row.getTable());
                v.add(row.getField());
                v.add(row.getSql());
                tableModel_result.addRow(v);
            }

            fitTableColumns(table_result);
        });
    }

    /***
     *   点击了确认，开始导出补丁包        <br>
     *           <br>
     *           <br>
     * @author air Email: 209308343@qq.com
     * @date 2019/12/25 0025 16:17
     * @Param []
     * @return void
     */
    public void onExecute() {
        if (indicatorAtomicReference.get() != null && !indicatorAtomicReference.get().isCanceled()) {
            indicatorAtomicReference.get().cancel();
            button_execute.setText("执行");
            indicatorAtomicReference.set(null);
            return;
        }

        String ps = textField_sqlFile.getText();
        if (StringUtil.isBlank(ps)) {
            return;
        }

        nclogs = ps;
        match = textField_match.getText();
        fileFilter = textField_filefilter.getText();
        outfile = textField_sucessFile.getText();
        List<File> sqls = StringUtil.split(ps, '?').stream()
                .filter(s -> StringUtil.isNotBlank(s))
                .map(s -> new File(s))
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(sqls)) {
            return;
        }

        if (Messages.showYesNoDialog("你确定开始搜索?"
                , "警告", Messages.getQuestionIcon()) != Messages.OK) {
            return;
        }

        try {
            run0(sqls);
        } finally {
            indicatorAtomicReference.set(null);
        }
    }

    private void run0(List<File> sqls) {
        button_execute.setText("停止执行");
        MyFileFilter myFileFilter = new MyFileFilter();
        Task.Backgroundable backgroundable = new Task.Backgroundable(project, "执行中...请稍后...") {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                indicatorAtomicReference.set(indicator);
                indicator.setText("正在搜索...");
                indicator.setIndeterminate(true);
                long s = System.currentTimeMillis();
                Statement statement = null;
                Set<Integer> okIndexs = new HashSet<>();
                String matchText = textField_match.getText();
                boolean checkBox_matchRexSelected = checkBox_matchRex.isSelected();
                try {
                    File outF = null;
                    if (StringUtil.isNotBlank(textField_sucessFile.getText())) {
                        outF = new File(textField_sucessFile.getText());
                    }

                    final File ok = outF;
                    indicator.setText("读取文件内容中...");
                    for (File f : sqls) {
                        if (indicator.isCanceled()) {
                            return;
                        }

                        List<File> fs = FileUtil.loopFiles(f, myFileFilter);
                        if (CollUtil.isEmpty(fs)) {
                            continue;
                        }

                        SwingUtilities.invokeLater(() -> {
                            threadProgressBar.setMinimum(0);
                            threadProgressBar.setValue(0);
                            threadProgressBar.setMaximum(fs.size() - 1);
                        });

                        int i2 = 0;
                        for (final File file : fs) {
                            if (!file.isFile()) {
                                continue;
                            }
                            indicator.setText("读取文件内容中:" + file.getPath());
                            final int ii = i2++;
                            SwingUtilities.invokeLater(() -> {
                                threadProgressBar.setValue(ii);
                                textArea_msg.append(String.format(file.getPath() + " 一共 %s 文件, 当前 %s , 剩余 %s \n"
                                        , fs.size(), ii + 1, fs.size() - ii - 1));
                            });

                            FileUtil.readLines(file, encode, new LineHandler() {
                                @Override
                                public void handle(String line) {
                                    if (indicator.isCanceled()) {
                                        return;
                                    }

                                    indicator.setText(line);
                                    try {
                                        boolean has = false;
                                        if (line.contains(matchText)) {
                                            has = true;
                                        } else {
                                            if (checkBox_matchRexSelected) {
                                                try {
                                                    Pattern compile = Pattern.compile(matchText);
                                                    if (compile.matcher(line).find()) {
                                                        has = true;
                                                    }
                                                    has = false;
                                                } catch (Throwable e) {
                                                    int a = 1;
                                                }
                                            }
                                        }

                                        if (has) {
                                            SwingUtilities.invokeLater(() -> {
                                                Vector v = new Vector();
                                                v.add(tableModel_result.getRowCount() + 1);
                                                v.add("成功");
                                                v.add("" + file.getPath());
                                                v.add(line);
                                                tableModel_result.addRow(v);
                                                fitTableColumns(table_result);
                                            });

                                            if (ok != null) {
                                                FileUtil.appendString(line + '\n', ok, encode);
                                            }
                                        }
                                    } catch (Throwable e) {
                                        SwingUtilities.invokeLater(() -> {
                                            String msg = "执行出错:" + e.getMessage() + " ,line=" + line + "\n";
                                            textArea_msg.append(msg);
                                            indicator.setText(msg);

                                            Vector v = new Vector();
                                            v.add(tableModel_result.getRowCount() + 1);
                                            v.add("失败");
                                            v.add(e.toString());
                                            v.add(line);
                                            tableModel_result.addRow(v);
                                            fitTableColumns(table_result);

                                            if (!checkBox_skipErrorLine.isSelected()) {
                                                RuntimeException ex = new RuntimeException(
                                                        "执行出错:" + e.toString()
                                                                + "，错误栈:" + ExceptionUtil.toString(ExceptionUtil.getTopCase(e))
                                                        , e);
                                                ex.setStackTrace(e.getStackTrace());
                                                throw ex;
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    }
                } catch (Throwable e) {
                    SwingUtilities.invokeLater(() -> {
                        e.printStackTrace();
                        String msg =
                                "执行出错:" + e.toString() + "，错误栈:" + ExceptionUtil.toString(ExceptionUtil.getTopCase(e));
                        LogUtil.error(msg, e);
                        RuntimeException ex = new RuntimeException(msg, e);
                        indicator.setText(msg);

                        ex.setStackTrace(e.getStackTrace());
                        throw ex;
                    });
                } finally {
                    SwingUtilities.invokeLater(() -> {
                        IoUtil.close(statement);
                        button_execute.setText("执行");
                        textArea_msg.append("执行结束\n");
                    });
                }
            }
        };
        backgroundable.setCancelText("停止");
        backgroundable.setCancelTooltipText("停止");
        ProgressManager.getInstance().run(backgroundable);
    }


    @Nullable
    @Override
    protected String getDimensionServiceKey() {
        return this.getClass().getName();
    }

    private void onCancel() {
        dispose();
    }

    @Data
    @AllArgsConstructor
    public static class MySelectFileTableMouseAdpaterImpl extends MouseAdapter {
        SearchNclogsDialog dialog;

        @Override
        public void mousePressed(MouseEvent me) {
            if (SwingUtilities.isRightMouseButton(me)) {
                final int row = dialog.table_result.rowAtPoint(me.getPoint());
                System.out.println("row:" + row);
                if (row != -1) {
                    final int column = dialog.table_result.columnAtPoint(me.getPoint());

                    final JPopupMenu popup = new JPopupMenu();

                    JMenuItem item = new JMenuItem("查找");
                    popup.add(item);
                    item.addActionListener(event -> {
                        new SearchTableFieldDialog(dialog.getProject(), dialog.getTable_result()).show();
                    });

                    JMenuItem selectAllOrNot = new JMenuItem("全选/全消");

                    selectAllOrNot.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            //高亮选择指定的行
                            List<SearchResultVO> ds = dialog.getSearchResultVOs();
                            if (ds != null) {
                                for (SearchResultVO d : ds) {
                                    d.setSelect(!d.isSelect());
                                }
                                dialog.setSelectTableDatas(ds);
                            }
                            // patcherDialog.selectTable.setRowSelectionInterval(row, row);
                        }
                    });
                    popup.add(selectAllOrNot);
                    popup.add(new JSeparator());

                    JMenuItem copyAll = new JMenuItem("复制全部");
                    popup.add(copyAll);
                    copyAll.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            System.out.println("复制全部");
                            setSysClipboardText(Joiner.on('\n').skipNulls().join(
                                    dialog.getSearchResultVOs().stream()
                                            .map(r -> r.getSql())
                                            .collect(Collectors.toSet())
                            ));
                        }

                        /**
                         * 将字符串复制到剪切板。
                         */
                        public void setSysClipboardText(String writeMe) {
                            try {
                                Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
                                Transferable tText = new StringSelection(writeMe);
                                clip.setContents(tText, null);
                            } catch (Throwable e) {
                            }
                        }
                    });

                    JMenuItem copy = new JMenuItem("复制行");
                    popup.add(copy);
                    copy.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            System.out.println("复制行");
                            setSysClipboardText(dialog.getSearchResultVOs().get(row).getSql());
                        }

                        /**
                         * 将字符串复制到剪切板。
                         */
                        public void setSysClipboardText(String writeMe) {
                            try {
                                Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
                                Transferable tText = new StringSelection(writeMe);
                                clip.setContents(tText, null);
                            } catch (Throwable e) {
                            }
                        }
                    });

                    JMenuItem edit = new JMenuItem("编辑");
                    popup.add(edit);
                    edit.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            System.out.println("编辑");
                            dialog.table_result.clearSelection(); //清除高亮选择状态
                            dialog.table_result.editCellAt(row, column); //设置某列为可编辑
                        }
                    });

                    JMenuItem calcel = new JMenuItem("删行");
                    calcel.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            List<SearchResultVO> ds = dialog.getSearchResultVOs();
                            if (ds != null && ds.size() > row) {
                                ds.remove(row);
                                dialog.setSelectTableDatas(ds);
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

    public static class MyFileFilter implements FileFilter {
        @Override
        public boolean accept(File pathname) {
            if (!pathname.isFile()) {
                return false;
            }

            if (StrUtil.isBlank(SearchNclogsDialog.fileFilter)) {
                return true;
            }

            String name = pathname.getName();
            if (StrUtil.contains(name, SearchNclogsDialog.fileFilter)) {
                return true;
            }

            try {
                Pattern compile = Pattern.compile(SearchNclogsDialog.fileFilter);
                if (compile.matcher(name).find()) {
                    return true;
                }
                return false;
            } catch (Throwable e) {
                int a = 1;
            }

            return true;
        }
    }

}
