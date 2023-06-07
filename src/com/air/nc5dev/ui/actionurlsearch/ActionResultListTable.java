package com.air.nc5dev.ui.actionurlsearch;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.system.SystemUtil;
import com.air.nc5dev.acion.AboutMeAction;
import com.air.nc5dev.ui.PatcherDialog;
import com.air.nc5dev.util.CollUtil;
import com.air.nc5dev.util.IoUtil;
import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.util.idea.ProjectUtil;
import com.intellij.featureStatistics.FeatureUsageTracker;
import com.intellij.ide.DataManager;
import com.intellij.ide.IdeEventQueue;
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereManager;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.ui.table.JBTable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.tangsu.mstsc.entity.MstscEntity;
import org.tangsu.mstsc.ui.MainPanel;
import org.tangsu.mstsc.ui.listeners.TableMouseListenerImpl2;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

@Getter
public class ActionResultListTable extends JBTable {
    NCCActionURLSearchUI nccActionURLSearchUI;
    DefaultTableModel tableModel;
    List<ActionResultDTO> datas;

    public ActionResultListTable(DefaultTableModel tableModel, NCCActionURLSearchUI nccActionURLSearchUI) {
        super(tableModel);
        this.tableModel = tableModel;
        this.nccActionURLSearchUI = nccActionURLSearchUI;
        addMouseListener(new NccActionTableMouseListenerImpl(this));
        addMouseListener(new MySelectFileTableMouseAdpaterImpl(this));
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    public void fitTableColumns() {
        JTable myTable = this;
        JTableHeader header = myTable.getTableHeader();
        int rowCount = myTable.getRowCount();
        Enumeration columns = myTable.getColumnModel().getColumns();
        while (columns.hasMoreElements()) {
            TableColumn column = (TableColumn) columns.nextElement();
            int col = header.getColumnModel().getColumnIndex(
                    column.getIdentifier());
            if (MainPanel.tableAttrs[col].equals("pk")) {
                column.setWidth(50);
                header.setResizingColumn(column);
                continue;
            } else if (MainPanel.tableAttrs[col].equals("title")) {
                column.setWidth(400);
                header.setResizingColumn(column);
                continue;
            } else if (MainPanel.tableAttrs[col].equals("ip")) {
                column.setWidth(200);
                header.setResizingColumn(column);
                continue;
            } else {
                column.setWidth(50);
                header.setResizingColumn(column);
                continue;
            }

            /*int width = (int) myTable.getTableHeader().getDefaultRenderer()
                    .getTableCellRendererComponent(myTable,
                            column.getIdentifier(), false, false, -1, col)
                    .getPreferredSize().getWidth();
            for (int row = 0; row < rowCount; row++) {
                int preferedWidth = (int) myTable.getCellRenderer(row, col)
                        .getTableCellRendererComponent(myTable,
                                myTable.getValueAt(row, col), false, false,
                                row, col).getPreferredSize().getWidth();
                width = Math.max(width, preferedWidth);
            }
            header.setResizingColumn(column);
            column.setWidth(width + myTable.getIntercellSpacing().width);*/
        }

        //行背景色
       /* DefaultTableCellRenderer dtcr = new MyDefaultTableCellRenderer();
        // 对每行的每一个单元格
        int columnCount = myTable.getColumnCount();
        for (int i = 0; i < columnCount; i++) {
            myTable.getColumn(myTable.getColumnName(i)).setCellRenderer(dtcr);
        }*/
    }

    public void open() {
        int row = getSelectedRow();
        ActionResultDTO vo = getDataOfRow(row);
        if (vo == null) {
            return;
        }

        SearchEverywhereManager seManager = SearchEverywhereManager.getInstance(nccActionURLSearchUI.getProject());
        FeatureUsageTracker.getInstance().triggerFeatureUsed("SearchEverywhere");
        IdeEventQueue.getInstance().getPopupManager().closeAllPopups(false);
        DataContext dataContext = DataManager.getInstance().getDataContext(nccActionURLSearchUI.createUI());
        AnActionEvent anActionEvent = new AnActionEvent(null, dataContext
                , this.getClass().getSimpleName(), new Presentation()
                , ActionManager.getInstance(), 0);
        seManager.show("ClassSearchEverywhereContributor", vo.getClazz(), anActionEvent);
    }

    public void setRows(List<ActionResultDTO> results) {
        this.datas = results;
        removeAll();
        for (ActionResultDTO e : results) {
            Vector row = new Vector();
            for (String arr : NCCActionURLSearchUI.tableAttrs) {
                row.add(ReflectUtil.getFieldValue(e, arr));
            }
            tableModel.addRow(row);
        }

        if (nccActionURLSearchUI.getCheckBox_AutoColumnSize().isSelected()) {
            fitTableColumns();
        }
    }


    public void removeAll() {
        while (tableModel.getRowCount() > 0) {
            tableModel.removeRow(0);
        }
    }

    @Data
    @AllArgsConstructor
    public static class MySelectFileTableMouseAdpaterImpl extends MouseAdapter {
        ActionResultListTable table;

        @Override
        public void mousePressed(MouseEvent me) {
            if (SwingUtilities.isRightMouseButton(me)) {
                final int row = table.rowAtPoint(me.getPoint());
                if (row != -1) {
                    final int column = table.columnAtPoint(me.getPoint());

                    final JPopupMenu popup = new JPopupMenu();

                    JMenuItem openClass = new JMenuItem("定位Class文件");
                    popup.add(openClass);
                    openClass.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            table.open();
                        }
                    });

                    popup.add(new JSeparator());

                    JMenuItem openXml = new JMenuItem("打开XML");
                    popup.add(openXml);
                    openXml.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            ActionResultDTO re = table.getDataOfRow(row);
                            if (re == null) {
                                return;
                            }

                            // 直接 打开 文件 编辑
                            final FileEditorManager fileEditorManager = FileEditorManager.getInstance(
                                    table.getNccActionURLSearchUI().getProject());
                            String xml = re.getXmlPath();
                            if (xml.contains(".jar" + File.separatorChar)) {
                                xml = xml.substring(0, xml.indexOf(".jar" + File.separatorChar) + 4);
                            }

                            VirtualFile virtualFile = VirtualFileManager.getInstance().findFileByUrl(xml);
                            if (virtualFile == null) {
                                IoUtil.tryOpenFileExpolor(new File(xml));
                                return;
                            }

                            ApplicationManager.getApplication().invokeLater(() -> {
                                final FileEditor[] editor = fileEditorManager.openFile(virtualFile, true);

                                if (editor.length > 0 && editor[0] instanceof TextEditor) {
                                    final Editor textEditor = ((TextEditor) editor[0]).getEditor();
                                    textEditor.getScrollingModel().scrollToCaret(ScrollType.CENTER);
                                    textEditor.getCaretModel().getCurrentCaret().selectWordAtCaret(true);
                                }
                            }, ModalityState.NON_MODAL);
                        }
                    });

                    popup.add(new JSeparator());

                    openClass = new JMenuItem("复制类名称");
                    popup.add(openClass);
                    openClass.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            ActionResultDTO v = table.getDataOfRow(row);
                            if (v != null) {
                                StringUtil.setIntoClipboard(v.getClazz());
                            }
                        }
                    });

                    popup.add(new JSeparator());

                    openClass = new JMenuItem("复制XML路径");
                    popup.add(openClass);
                    openClass.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            ActionResultDTO v = table.getDataOfRow(row);
                            if (v != null) {
                                StringUtil.setIntoClipboard(v.getXmlPath());
                            }
                        }
                    });

                    popup.show(me.getComponent(), me.getX(), me.getY());
                }
            }
        }
    }

    public ActionResultDTO getDataOfRow(int row) {
        if (row < 0) return null;

        if (CollUtil.isEmpty(getDatas()) || getDatas().size() < row) {
            return null;
        }

        return getDatas().get(row);
    }
}
