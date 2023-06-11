package com.air.nc5dev.ui.searchdatabasefull;

import com.air.nc5dev.ui.MyTableCellEditor;
import com.air.nc5dev.ui.MyTableRenderer;
import com.air.nc5dev.util.*;
import com.air.nc5dev.util.jdbc.ConnectionUtil;
import com.air.nc5dev.util.searchfulldatabase.DbConnectionFactory;
import com.air.nc5dev.util.searchfulldatabase.SearchFullDatabaseConfigVO;
import com.air.nc5dev.util.searchfulldatabase.SearchResultVO;
import com.air.nc5dev.util.searchfulldatabase.TaskFather;
import com.air.nc5dev.vo.NCDataSourceVO;
import com.alibaba.fastjson.JSON;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.*;
import com.intellij.ui.table.JBTable;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

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
public class SearchFullDataBaseDialog extends DialogWrapper {
    JComponent contentPane;
    Project project;
    JTextField textField_selectAllTableSql;
    JTextField textField_key;
    JTextArea textArea_msg;
    JTextField textField_skipColumns;
    JTextField textField_threadNum;
    JTextField textField_hotwebsProject;
    JTextField textField_savePath;
    JBCheckBox checkBox_fastQuery;
    JBCheckBox checkBox_async;
    JProgressBar threadProgressBar;
    JBTable selectTable;
    DefaultTableModel defaultTableModel;
    JComboBox comboBox_dataSourceIndex;
    JComboBox comboBox_LikeType;
    JButton button_Search;
    AtomicReference<TaskFather> taskFatherAtomicReference = new AtomicReference<>();

    public static volatile SearchFullDataBaseDialog me = null;

    public SearchFullDataBaseDialog(Project project) {
        super(project);
        this.project = project;
        init();
        setTitle("数据库表内容全局模糊搜索");
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
            label.setBounds(x, y, 82, height);
            jbxxp.add(label);
            threadProgressBar = new JProgressBar();
            threadProgressBar.setBounds(x + 85, y, 480, height);
            // threadProgressBar.setForeground(Color.BLACK);
            threadProgressBar.setStringPainted(true);
            jbxxp.add(threadProgressBar);

            label = new JBLabel("日志:");
            label.setBounds(x, y += height + 5, 82, height);
            jbxxp.add(label);
            textArea_msg = new JBTextArea();
            JBScrollPane jbScrollPane = new JBScrollPane(textArea_msg);
            jbScrollPane.setAutoscrolls(true);
            jbScrollPane.setBounds(x, y += height + 5, 650, 120);
            textArea_msg.setEnabled(true);
            textArea_msg.setEditable(true);
            textArea_msg.setLineWrap(true);
            jbxxp.add(jbScrollPane);

            label = new JBLabel("搜索内容:");
            label.setBounds(x, y += 120 + 5, 180, height);
            jbxxp.add(label);
            textField_key = new JBTextField();
            textField_key.setBounds(x + 180, y, 600, height);
            jbxxp.add(textField_key);

            label = new JBLabel("查询数据库所有表名称的sql:");
            label.setBounds(x, y += height + 5, 180, height);
            jbxxp.add(label);
            textField_selectAllTableSql = new JBTextField();
            textField_selectAllTableSql.setBounds(x + 180, y, 462, height);
            jbxxp.add(textField_selectAllTableSql);

            label = new JBLabel("跳过列名(列名的某部分contains判断)多个用英文,隔开:");
            label.setBounds(x, y += height + 5, 340, height);
            jbxxp.add(label);
            textField_skipColumns = new JBTextField();
            textField_skipColumns.setBounds(x + 340, y, 400, height);
            jbxxp.add(textField_skipColumns);

            label = new JBLabel("数据源:");
            label.setBounds(x, y += height + 5, 82, height);
            jbxxp.add(label);
            final List<NCDataSourceVO> dataSourceVOS = NCPropXmlUtil.getDataSourceVOS();
            comboBox_dataSourceIndex = new JComboBox(new Vector(
                    dataSourceVOS.stream()
                            .map(v -> v.getDataSourceName() + '/' + v.getUser())
                            .collect(Collectors.toList())
            ));
            if (comboBox_dataSourceIndex.getModel().getSize() > 0) {
                comboBox_dataSourceIndex.setSelectedIndex(ConvertUtil.toInt(ProjectNCConfigUtil.getConfigValue(
                        "data_source_index"), 0));
            }
            comboBox_dataSourceIndex.addActionListener(e -> {
                NCDataSourceVO dataSourceVO = dataSourceVOS.get(comboBox_dataSourceIndex.getSelectedIndex());
                if (dataSourceVO.getDatabaseType().toLowerCase().contains("oracle")) {
                    textField_selectAllTableSql.setText(" select table_name from user_tables ");
                } else if (dataSourceVO.getDatabaseType().toLowerCase().contains("server")) {
                    textField_selectAllTableSql.setText(" select name from sys.objects where type='U' ");
                } else if (dataSourceVO.getDatabaseType().toLowerCase().contains("mysql")) {
                    textField_selectAllTableSql.setText(" show tables ");
                }
            });
            comboBox_dataSourceIndex.setBounds(x + 85, y, 300, height);
            jbxxp.add(comboBox_dataSourceIndex);
            JButton button_TestDb = new JButton("测试连接");
            button_TestDb.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Integer i = comboBox_dataSourceIndex.getSelectedIndex();
                    NCDataSourceVO ds = NCPropXmlUtil.get(i);
                    if (ds == null) {
                        Messages.showErrorDialog(project, "数据源索引不存在: " + i + " ,数据源数量: "
                                        + (NCPropXmlUtil.getDataSourceVOS() == null ? 0 :
                                        NCPropXmlUtil.getDataSourceVOS().size())
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
            button_TestDb.setBounds(x + 400, y, 85, height);
            jbxxp.add(button_TestDb);

            label = new JBLabel("是否多线程查询(不建议):");
            label.setBounds(x, y += height + 5, 150, height);
            jbxxp.add(label);
            checkBox_async = new JBCheckBox();
            checkBox_async.setSelected(false);
            checkBox_async.addActionListener(e -> textField_threadNum.setEnabled(checkBox_async.isSelected()));
            checkBox_async.setBounds(x + 160, y, 200, height);
            jbxxp.add(checkBox_async);

            label = new JBLabel("线程数量:");
            label.setBounds(x, y += height + 5, 82, height);
            jbxxp.add(label);
            textField_threadNum = new JBTextField();
            textField_threadNum.setBounds(x + 85, y, 300, height);
            textField_threadNum.setEnabled(false);
            jbxxp.add(textField_threadNum);
            textField_threadNum.setColumns(10);

            label = new JBLabel("Like语句匹配类型:");
            label.setBounds(x, y += height + 5, 100, height);
            jbxxp.add(label);
            comboBox_LikeType = new JComboBox(new Vector(
                    CollUtil.newArrayList("like '%内容%'", "like '内容%'", "like '%内容'", "='内容'")
            ));
            comboBox_LikeType.setSelectedIndex(ConvertUtil.toInt(ProjectNCConfigUtil.getConfigValue(
                    "data_source_index"), 0));
            comboBox_LikeType.setBounds(x + 100, y, 300, height);
            jbxxp.add(comboBox_LikeType);

            label = new JBLabel("是否快速查询(不分开查询每个列):");
            label.setBounds(x, y += height + 5, 82, height);
            jbxxp.add(label);
            checkBox_fastQuery = new JBCheckBox();
            checkBox_fastQuery.setBounds(x + 85, y, 200, height);
            jbxxp.add(checkBox_fastQuery);

            label = new JBLabel("查询结果:");
            label.setBounds(x, y = y + height + 5, 280, height);
            jbxxp.add(label);
            Vector heads = new Vector();
            heads.add("选择");
            heads.add("序号");
            heads.add("表名");
            heads.add("列名");
            heads.add("SQL语句");
            defaultTableModel = new DefaultTableModel(heads, 0);
            selectTable = new JBTable(defaultTableModel);
            jbScrollPane = new JBScrollPane(selectTable);
            jbScrollPane.setAutoscrolls(true);
            selectTable.setBorder(LineBorder.createBlackLineBorder());
            selectTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            selectTable.addMouseListener(new MySelectFileTableMouseAdpaterImpl(this));
            jbScrollPane.setBounds(x, y = y + height + 5, 770, 200);
            // 对每一列设置单元格渲染器
            for (int i = 0; i < selectTable.getColumnCount(); i++) {
                selectTable.getColumnModel().getColumn(i).setCellRenderer(new MyTableRenderer());
            }
            selectTable.getColumnModel().getColumn(0).setCellEditor(
                    new MyTableCellEditor(this, new JBCheckBox()));
            jbxxp.add(jbScrollPane);

            button_Search = new JButton("开始搜索");
            button_Search.addActionListener(e -> {
                if (taskFatherAtomicReference.get() != null) {
                    taskFatherAtomicReference.get().stopAll();
                    taskFatherAtomicReference.compareAndSet(taskFatherAtomicReference.get(), null);
                } else {
                    onOK();
                }
            });
            button_Search.setBounds(x + 400, y - height - 30, 150, 40);
            jbxxp.add(button_Search);

            JButton button_CloseDB = new JButton("关闭数据库");
            button_CloseDB.addActionListener(e -> {
                new DbConnectionFactory().closeDBAll();
            });
            button_CloseDB.setBounds(x + 560, y - height - 30, 150, 40);
            jbxxp.add(button_CloseDB);
        }

        //设置点默认值
        initDefualtValues();
    }

    public void initDefualtValues() {
        // select name from sys.objects where type='U'
        // select table_name from user_tables
        textField_selectAllTableSql.setText(" select table_name from user_tables ");
        textField_threadNum.setText("100");
        checkBox_fastQuery.setSelected(true);
        textField_skipColumns.setText("size,ts,dr,version,");
    }

    public void set2UI(SearchFullDatabaseConfigVO c) {
        textField_selectAllTableSql.setText(c.getTableListSql());
        textField_threadNum.setText(c.getThreadNum() + "");
        checkBox_fastQuery.setSelected(c.isFastQuery());

        if (c.getSkipColumns() != null) {
            textField_skipColumns.setText(Joiner.on(',').skipNulls().join(c.getSkipColumns()));
        }
    }

    public SearchFullDatabaseConfigVO getFromUI() {
        SearchFullDatabaseConfigVO re = SearchFullDatabaseConfigVO.builder()
                .fastQuery(checkBox_fastQuery.isSelected())
                .tableListSql(textField_selectAllTableSql.getText())
                .threadNum(Integer.parseInt(textField_threadNum.getText().trim()))
                .key(StringUtil.trim(textField_key.getText()))
                .build();
        if (textField_skipColumns.getText() != null && !textField_skipColumns.getText().trim().isEmpty()) {
            re.getSkipColumns().addAll(
                    Splitter.on(',')
                            .omitEmptyStrings()
                            .trimResults()
                            .splitToList(textField_skipColumns.getText().toLowerCase())
            );
        }
        re.setDataSource(NCPropXmlUtil.get(comboBox_dataSourceIndex.getSelectedIndex()));

        return re;
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
            while (defaultTableModel.getRowCount() > 0) {
                defaultTableModel.removeRow(0);
            }

            for (SearchResultVO row : rows) {
                Vector v = new Vector();
                v.add(row.isSelect());
                v.add(row.getRow());
                v.add(row.getTable());
                v.add(row.getField());
                v.add(row.getSql());
                defaultTableModel.addRow(v);
            }

            fitTableColumns(selectTable);
        });
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
        final SearchFullDatabaseConfigVO config = getFromUI();
        if (config == null
                || StringUtil.isBlank(config.getKey())
                || config.getDataSource() == null) {
            return;
        }

        button_Search.setText("停止搜索");
        Task.Backgroundable backgroundable = new Task.Backgroundable(project, "数据库全局搜索中...请稍后...") {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setText("正在搜索数据库...");
                indicator.setIndeterminate(true);
                long s = System.currentTimeMillis();
                config.indicator = indicator;
                config.dialog = SearchFullDataBaseDialog.this;
                TaskFather taskFather = new TaskFather(config);
                try {
                    if (taskFatherAtomicReference.get() != null) {
                        taskFatherAtomicReference.get().setStop(true);
                        button_Search.setEnabled(false);
                        return;
                    }

                    taskFatherAtomicReference.compareAndSet(null, taskFather);

                    new Thread(() -> {
                        try {
                            while (taskFather.getThreadPool().getActiveCount() > 0) {
                                taskFather.checkState();
                                TimeUnit.SECONDS.sleep(5);
                            }
                        } catch (Exception e) {
                        }
                    }, "Search_Database_Full_watchStop").start();


                    if (checkBox_async.isSelected()) {
                        taskFather.start();
                    } else {
                        taskFather.syncRun();
                    }
                } catch (Exception e) {
                    taskFather.stopAll();
                    e.printStackTrace();
                } finally {
                }
            }
        };
        backgroundable.setCancelText("放弃吧,没有卵用的按钮");
        backgroundable.setCancelTooltipText("这是一个没有卵用的按钮");
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
        SearchFullDataBaseDialog dialog;

        @Override
        public void mousePressed(MouseEvent me) {
            if (SwingUtilities.isRightMouseButton(me)) {
                final int row = dialog.selectTable.rowAtPoint(me.getPoint());
                System.out.println("row:" + row);
                if (row != -1) {
                    final int column = dialog.selectTable.columnAtPoint(me.getPoint());

                    final JPopupMenu popup = new JPopupMenu();
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
                            dialog.selectTable.clearSelection(); //清除高亮选择状态
                            dialog.selectTable.editCellAt(row, column); //设置某列为可编辑
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

}
