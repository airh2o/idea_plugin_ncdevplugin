package com.air.nc5dev.ui.exportbmf;

import com.air.nc5dev.util.CollUtil;
import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.vo.meta.SearchComponentVO;
import com.intellij.lang.java.JavaDocumentationProvider;
import com.intellij.psi.impl.compiled.ClsClassImpl;
import com.intellij.psi.impl.java.stubs.JavaStubElementTypes;
import com.intellij.psi.impl.java.stubs.impl.PsiClassStubImpl;
import com.intellij.ui.table.JBTable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Enumeration;

@Getter
@Setter
public class BmfListTable extends JBTable {
    MyDefaultTableCellRenderer myDefaultTableCellRenderer;
    ExportbmfDialog exportbmfDialog;

    public BmfListTable(TableModel tableModel, ExportbmfDialog exportbmfDialog) {
        super(tableModel);
        this.exportbmfDialog = exportbmfDialog;
        this.myDefaultTableCellRenderer = new MyDefaultTableCellRenderer(exportbmfDialog);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        int row = rowAtPoint(event.getPoint());
        int column = columnAtPoint(event.getPoint());

        String txt = "";

        if (row > -1 && column > -1) {
            txt = StringUtil.getSafeString(getValueAt(row, column));
        }

        return txt;
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
            if (ExportbmfDialog.tableAttrs[col].equals("version")) {
                column.setWidth(50);
                header.setResizingColumn(column);
                continue;
            } else if (ExportbmfDialog.tableAttrs[col].equals("name")) {
                column.setWidth(60);
                header.setResizingColumn(column);
                continue;
            } else if (ExportbmfDialog.tableAttrs[col].equals("displayName")) {
                column.setWidth(60);
                header.setResizingColumn(column);
                continue;
            } else if (ExportbmfDialog.tableAttrs[col].equals("order1")) {
                column.setWidth(30);
                header.setResizingColumn(column);
                continue;
            }else if (ExportbmfDialog.tableAttrs[col].equals("fileVersion")) {
                column.setWidth(50);
                header.setResizingColumn(column);
                continue;
            } else if (ExportbmfDialog.tableAttrs[col].equals("filePath")) {
                column.setWidth(400);
                header.setResizingColumn(column);
                continue;
            } else if (ExportbmfDialog.tableAttrs[col].equals("id")) {
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

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        return myDefaultTableCellRenderer;
    }

    @AllArgsConstructor
    public static class MyDefaultTableCellRenderer extends DefaultTableCellRenderer {
        ExportbmfDialog exportbmfDialog;

        @Override
        public Component getTableCellRendererComponent(JTable table
                , Object value
                , boolean isSelected
                , boolean hasFocus
                , int row
                , int column) {
            Color backgroundColor = null;
            SearchComponentVO vo = CollUtil.get(exportbmfDialog.getResult(), row);
            if (vo != null && !StringUtil.equalsIgnoreCase(vo.getVersion(), vo.getFileVersion())) {
                backgroundColor = new Color(246, 209, 209, 255);//版本不一致行的背影色
            } else {
                if (row % 2 == 0) { // 偶数
                    backgroundColor = new Color(128, 128, 128);//偶数行的背影色
                } else {
                    backgroundColor = new Color(93, 93, 93);//奇数行的背影色
                }
            }

            setBackground(backgroundColor);
            Color fontColor = new Color(51, 51, 51);
            setForeground(fontColor);
            Component cm = super.getTableCellRendererComponent(table, value, isSelected,
                    hasFocus, row, column);

            setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));

            return cm;
        }
    }
}
