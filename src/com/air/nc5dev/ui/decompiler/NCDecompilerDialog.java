package com.air.nc5dev.ui.decompiler;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.air.nc5dev.decompiler.DecompilerUtil;
import com.air.nc5dev.ui.MyTableCellEditor;
import com.air.nc5dev.ui.MyTableRenderer;
import com.air.nc5dev.ui.SearchTableFieldDialog;
import com.air.nc5dev.util.ProjectNCConfigUtil;
import com.air.nc5dev.util.idea.LogUtil;
import com.air.nc5dev.vo.NCDataSourceVO;
import com.google.common.collect.Sets;
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
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.table.JBTable;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

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
public class NCDecompilerDialog extends DialogWrapper {
    JBTabbedPane contentPane;
    JBPanel panel_main;
    DefaultComboBoxModel<NCDataSourceVO> comboBoxModelDb;
    ComboBox comboBoxDb;
    JBTextArea textFieldSerach;
    JButton buttonStart;
    JButton buttonStop;
    JBLabel labelInfo;
    JBTextField home;
    JBTextField out;
    JBTextField encoding;
    JBCheckBox outlog;
    JBCheckBox jarname;
    JBTable paths;
    DefaultTableModel pathsTableModel;
    int height = 300;
    int width = 700;
    Project project;
    long start;
    AtomicInteger threadNum = new AtomicInteger(Runtime.getRuntime().availableProcessors() >> 2);
    boolean AutoLogScr;
    boolean doubleThread = false;//true;

    public NCDecompilerDialog(Project project) {
        super(project);
        this.project = project;
        createCenterPanel();
        init();
        setOKActionEnabled(false);
        setCancelButtonText("取消");
        setTitle("NC目录一键反编译代码(仅做学习测试用,请勿违法使用,本人不负任何责任!)");
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
            panel_main.setBounds(0, 0, height, width);
            jtab.addTab("选项", panel_main);

         /*   final List<NCDataSourceVO> dataSourceVOS = NCPropXmlUtil.getDataSourceVOS(getProject());
            comboBoxModelDb = new DefaultComboBoxModel<>(new Vector(
                    dataSourceVOS.stream()
                            .map(v -> v.getDataSourceName() + '/' + v.getUser())
                            .collect(Collectors.toList())
            ));

            JBLabel label = new JBLabel("数据源:");
            label.setBounds(1, y, w, 60);
            panel_main.add(label);
            comboBoxDb = new ComboBox(comboBoxModelDb);
            comboBoxDb.setBounds(x += w, y, 150, h);
            panel_main.add(comboBoxDb);*/

            labelInfo = new JBLabel();
            labelInfo.setBounds(1
                    , y //+= comboBoxDb.getHeight() + 3
                    , getWidth()
                    , h);
            panel_main.add(labelInfo);

            JBLabel label = new JBLabel();
            label.setBounds(1
                    , labelInfo.getY() + labelInfo.getHeight() + 5
                    , 80
                    , h);
            label.setText("NCHOME:");
            panel_main.add(label);
            home = new JBTextField();
            home.setEditable(true);
            home.setBounds(label.getX() + label.getWidth() + 5, label.getY(), contentPane.getWidth() - 10 - label.getWidth(), h);
            home.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    loadPaths();
                }
            });
            panel_main.add(home);

            label = new JBLabel();
            label.setBounds(1
                    , home.getY() + home.getHeight() + 5
                    , 80
                    , 30);
            label.setText("输出路径:");
            panel_main.add(label);
            out = new JBTextField();
            out.setEditable(true);
            out.setBounds(label.getX() + label.getWidth() + 5, label.getY(), contentPane.getWidth() - 10 - label.getWidth(), h);
            panel_main.add(out);

            outlog = new JBCheckBox("输出日志文件");
            outlog.setEnabled(true);
            outlog.setSelected(false);
            outlog.setBounds(1, label.getY() + label.getHeight() + 10, 130, h);
            panel_main.add(outlog);

            jarname = new JBCheckBox("非用友NC系列"); //Jar文件名作为文件夹
            jarname.setEnabled(true);
            jarname.setSelected(false);
            jarname.setBounds(outlog.getX() + outlog.getWidth() + 5, outlog.getY(), 150, h);
            panel_main.add(jarname);

            label = new JBLabel();
            label.setBounds(jarname.getX() + jarname.getWidth() + 5
                    , jarname.getY()
                    , 100
                    , h);
            label.setText("输出文件编码:");
            panel_main.add(label);
            encoding = new JBTextField();
            encoding.setEditable(true);
            encoding.setBounds(label.getX() + label.getWidth() + 5, label.getY(), 150, h);
            panel_main.add(encoding);

            buttonStart = new JButton("启动反编译");
            buttonStart.setBounds(encoding.getX() + encoding.getWidth() + 5
                    , encoding.getY()
                    , 100
                    , h);
            panel_main.add(buttonStart);
            buttonStart.addActionListener(e -> execute());

            buttonStop = new JButton("停止反编译");
            buttonStop.setBounds(buttonStart.getX() + buttonStart.getWidth() + 5
                    , buttonStart.getY()
                    , 100
                    , h);
            panel_main.add(buttonStop);
            buttonStop.addActionListener(e -> stopTask());

            Vector heads = new Vector();
            heads.add("选择");
            heads.add("路径");
            heads.add("状态");
            pathsTableModel = new DefaultTableModel(heads, 0);
            paths = new JBTable(pathsTableModel);
            paths.setBorder(LineBorder.createBlackLineBorder());
            paths.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            paths.addMouseListener(new MySelectFileTableMouseAdpaterImpl(this, paths));
            // 对每一列设置单元格渲染器
            MyTableRenderer cellRenderer = new MyTableRenderer();
            cellRenderer.getSelectColorIndex().put(0, new Color(170, 200, 140));
            cellRenderer.getUnSelectColorIndex().put(0, new Color(150, 50, 50));
            cellRenderer.getColumn2ToolTipTextMap().put(0, "勾选的才会被反编译");
            for (int i = 0; i < paths.getColumnCount(); i++) {
                paths.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
            }
            paths.getColumnModel().getColumn(0).setCellEditor(new MyTableCellEditor(this, new JBCheckBox()));
            //textFieldSerach.setLineWrap(true);
            JBScrollPane jbScrollPane = new JBScrollPane(paths);
            jbScrollPane.setBounds(x = 1, encoding.getY() + encoding.getHeight() + 2, getWidth(), 200);
            panel_main.add(jbScrollPane);

            textFieldSerach = new JBTextArea();
            textFieldSerach.setEditable(true);
            textFieldSerach.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() != MouseEvent.BUTTON1) {
                        final JPopupMenu popup = new JPopupMenu();
                        JMenuItem item = new JMenuItem((isAutoLogScr() ? "关闭" : "打开 ") + "自动滚动");
                        popup.add(item);
                        item.addActionListener(event -> {
                            setAutoLogScr(!isAutoLogScr());
                        });

                        item = new JMenuItem(isDoubleThread() ? "关闭双重线程" : "打开双重线程(文件夹和里面文件都采用多线程反编译)");
                        //   popup.add(item);
                        item.addActionListener(event -> {
                            setDoubleThread(!isDoubleThread());
                        });

                        item = new JMenuItem("设置反编译线程数量");
                        popup.add(item);
                        item.addActionListener(event -> {
                            getThreadNum().set(NumberUtil.parseInt(StrUtil.blankToDefault(
                                    JOptionPane.showInputDialog("请输入反编译并发线程数量： "
                                            , getThreadNum().get()), "" + getThreadNum().get())));
                        });

                        popup.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            });
            //textFieldSerach.setLineWrap(true);
            JBScrollPane jbScrollPane2 = new JBScrollPane(textFieldSerach);
            jbScrollPane2.setBounds(x = 1, jbScrollPane.getY() + jbScrollPane.getHeight() + 5, getWidth(), 200);
            panel_main.add(jbScrollPane2);
        }

        //设置点默认值
        initDefualtValues();
    }

    public void loadPaths() {
        out.setText(home.getText() + File.separatorChar + "decom");
        clearTable();
        Set<File> ps = null;
        if (jarname.isSelected()) {
            ps = loadAllChildDir(new File(home.getText()));
        } else {
            ps = DecompilerUtil.loadPaths(home.getText());
        }

        //   textFieldSerach.append("匹配到路径列表： \n");
        for (File p : ps) {
            //  textFieldSerach.append(p.getPath() + "\n");
            Vector v = new Vector();
            v.add(true);
            v.add(p.getPath());
            v.add("待处理");
            pathsTableModel.addRow(v);
        }

        fitTableColumns(paths);
    }

    private Set<File> loadAllChildDir(File dir) {
        LinkedHashSet<File> all = Sets.newLinkedHashSet();
        if (dir == null) {
            return all;
        }

        File[] fs = dir.listFiles();
        if (fs == null) {
            return all;
        }
        for (File f : fs) {
            if (f.isDirectory()) {
                all.addAll(loadAllChildDir(f));
                continue;
            }

            if (f.getName().toLowerCase().endsWith(".jar")
                    || f.getName().toLowerCase().endsWith(".class")) {
                all.add(f.getParentFile());
            }
        }

        return all;
    }

    public void clearTable() {
        while (pathsTableModel.getRowCount() > 0) {
            pathsTableModel.removeRow(0);
        }
    }

    /**
     *
     */
    @Override
    protected void doOKAction() {
        try {
            execute();
        } finally {
            close(1);
        }
    }

    private void execute() {
        start = System.currentTimeMillis();
        Task.Backgroundable backgroundable = new Task.Backgroundable(project, "正在生成...耗时会比较长...完成后会自动打开...请耐心等待") {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {
                    //  getButtonStart().setEnabled(false);
                    //  getOKAction().setEnabled(false);
                    // getCancelAction().setEnabled(false);
                    // getButtonStop().setEnabled(true);

                    startTask(indicator);
                } catch (Throwable e) {
                    LogUtil.error(e.getMessage(), e);
                    decompilerUtil = null;
                } finally {
                    //  getButtonStart().setEnabled(true);
                    //getOKAction().setEnabled(true);
                    //   getCancelAction().setEnabled(true);
                    //   getButtonStop().setEnabled(false);
                }
            }
        };
        backgroundable.setCancelText("停止任务");
        backgroundable.setCancelTooltipText("停止这个任务");
        ProgressManager.getInstance().run(backgroundable);
    }

    DecompilerUtil decompilerUtil;

    private void startTask(ProgressIndicator indicator) {
        decompilerUtil = new DecompilerUtil()
                .setDialog(this)
                .setNchome(home.getText())
                .setOut(out.getText())
                .setENCODING(encoding.getText())
                .setOutlog(outlog.isSelected())
                .setJarNameUseOut(jarname.isSelected())
                .setIndicator(indicator)
                .setTextArea(getTextFieldSerach())
                .setAutoLogScr(isAutoLogScr())
                .setDoubleThread(isDoubleThread())
                .setThreadNum(getThreadNum().get());

        if (StrUtil.isBlank(decompilerUtil.getNchome())) {
            textFieldSerach.append("请输入NCHOME文件夹路径");
            indicator.setText("请输入NCHOME文件夹路径");
            return;
        }
        if (StrUtil.isBlank(decompilerUtil.getOut())) {
            textFieldSerach.append("请输入输出文件夹路径");
            indicator.setText("请输入输出文件夹路径");
            return;
        }

        if (decompilerUtil.run()) {
            //  dispose();
        }
    }

    private void stopTask() {
        if (decompilerUtil != null) {
            decompilerUtil.getIndicator().cancel();

            //getButtonStart().setEnabled(true);
            //getOKAction().setEnabled(true);
            // getCancelAction().setEnabled(true);
            //  getButtonStop().setEnabled(false);
        }
    }

    @Override
    public void doCancelAction() {
        stopTask();
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
            home.setText(ProjectNCConfigUtil.getNCHomePath(getProject()));
            out.setText(home.getText() + File.separatorChar + "decom");
            encoding.setText("GBK");
            textFieldSerach.setText("");
            labelInfo.setText("点击启动反编译按钮，开始执行(下面日志窗口内右键 有更多菜单哦)");

            loadPaths();
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

    @Data
    @AllArgsConstructor
    public static class MySelectFileTableMouseAdpaterImpl extends MouseAdapter {
        NCDecompilerDialog patcherDialog;
        JTable table;

        @Override
        public void mousePressed(MouseEvent me) {
            if (SwingUtilities.isRightMouseButton(me)) {
                final int row = table.rowAtPoint(me.getPoint());
                if (row < 0) {
                    return;
                }

                final int column = table.columnAtPoint(me.getPoint());
                boolean isCheckbox = false;
                Component tableCellRendererComponent =
                        table.getColumnModel().getColumn(column).getCellRenderer().getTableCellRendererComponent(table
                                , table.getValueAt(row, column)
                                , true
                                , true
                                , row
                                , column);
                if (tableCellRendererComponent != null) {
                    if (JCheckBox.class.isAssignableFrom(tableCellRendererComponent.getClass())
                            || Checkbox.class.isAssignableFrom(tableCellRendererComponent.getClass())) {
                        isCheckbox = true;
                    }
                }

                final JPopupMenu popup = new JPopupMenu();
                JMenuItem item = new JMenuItem("查找");
                popup.add(item);
                item.addActionListener(event -> {
                    new SearchTableFieldDialog(getPatcherDialog().getProject(), table).show();
                });

                JMenuItem selectAllOrNot = new JMenuItem("反选");
                final boolean isCheckboxFinal = isCheckbox;
                selectAllOrNot.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        //高亮选择指定的行
                        DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
                        for (int i = 0; i < tableModel.getRowCount(); i++) {
                            if ("true".equals(tableModel.getDataVector().get(i).get(column))
                                    || Boolean.TRUE.equals(tableModel.getDataVector().get(i).get(column))) {
                                table.setValueAt(false, i, isCheckboxFinal ? column : 0);
                            } else {
                                table.setValueAt(true, i, isCheckboxFinal ? column : 0);
                            }
                        }
                        // table.setRowSelectionInterval(row, row);
                    }
                });
                popup.add(selectAllOrNot);

                selectAllOrNot = new JMenuItem("全选/全消");
                selectAllOrNot.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        //高亮选择指定的行
                        DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
                        boolean is = "true".equals(tableModel.getDataVector().get(0).get(column))
                                || Boolean.TRUE.equals(tableModel.getDataVector().get(0).get(column));
                        for (int i = 0; i < tableModel.getRowCount(); i++) {
                            table.setValueAt(!is, i, isCheckboxFinal ? column : 0);
                        }
                        // table.setRowSelectionInterval(row, row);
                    }
                });
                popup.add(selectAllOrNot);
                popup.add(new JSeparator());

                JMenuItem edit = new JMenuItem("编辑");
                popup.add(edit);
                edit.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        table.clearSelection(); //清除高亮选择状态
                        table.editCellAt(row, column); //设置某列为可编辑
                    }
                });

                JMenuItem calcel = new JMenuItem("删行");
                calcel.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        ((DefaultTableModel) table.getModel()).removeRow(row);
                    }
                });
                popup.add(new JSeparator());
                popup.add(calcel);

                final int[] selectedRows = table.getSelectedRows();
                final HashSet<Integer> selectedRowSet = Sets.newHashSet();
                for (int selectedRow : selectedRows) {
                    selectedRowSet.add(selectedRow);
                }
                selectAllOrNot = new JMenuItem("选中行批量反选");
                selectAllOrNot.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        //高亮选择指定的行
                        DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
                        for (int i = 0; i < tableModel.getRowCount(); i++) {
                            if (!selectedRowSet.contains(i)) {
                                continue;
                            }

                            if ("true".equals(tableModel.getDataVector().get(i).get(column))
                                    || Boolean.TRUE.equals(tableModel.getDataVector().get(i).get(column))) {
                                table.setValueAt(false, i, isCheckboxFinal ? column : 0);
                            } else {
                                table.setValueAt(true, i, isCheckboxFinal ? column : 0);
                            }
                        }
                        // table.setRowSelectionInterval(row, row);
                    }
                });
                popup.add(selectAllOrNot);

                popup.show(me.getComponent(), me.getX(), me.getY());
            }
        }
    }

}
