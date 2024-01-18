package com.air.nc5dev.ui.adddeffiled;

import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.vo.meta.PropertyDTO;
import com.intellij.ui.table.JBTable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import nc.vo.pub.VOStatus;

import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.border.BevelBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.Enumeration;

@Getter
@Setter
public class ProperteListTable extends JBTable {
    MyDefaultTableCellRenderer myDefaultTableCellRenderer;
    AddDefField2BmfDialog addDefField2BmfDialog;

    public ProperteListTable(TableModel tableModel, AddDefField2BmfDialog addDefField2BmfDialog) {
        super(tableModel);
        this.addDefField2BmfDialog = addDefField2BmfDialog;
        this.myDefaultTableCellRenderer = new MyDefaultTableCellRenderer(addDefField2BmfDialog);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return true;
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
         /*   if (AddDefField2BmfDialog.tableAttrs[col].equals("version")) {
                column.setWidth(50);
                header.setResizingColumn(column);
                continue;
            } else if (AddDefField2BmfDialog.tableAttrs[col].equals("name")) {
                column.setWidth(60);
                header.setResizingColumn(column);
                continue;
            } else if (AddDefField2BmfDialog.tableAttrs[col].equals("displayName")) {
                column.setWidth(60);
                header.setResizingColumn(column);
                continue;
            } else if (AddDefField2BmfDialog.tableAttrs[col].equals("order1")) {
                column.setWidth(30);
                header.setResizingColumn(column);
                continue;
            } else if (AddDefField2BmfDialog.tableAttrs[col].equals("fileVersion")) {
                column.setWidth(50);
                header.setResizingColumn(column);
                continue;
            } else if (AddDefField2BmfDialog.tableAttrs[col].equals("filePath")) {
                column.setWidth(400);
                header.setResizingColumn(column);
                continue;
            } else if (AddDefField2BmfDialog.tableAttrs[col].equals("id")) {
                column.setWidth(200);
                header.setResizingColumn(column);
                continue;
            } else {
                column.setWidth(50);
                header.setResizingColumn(column);
                continue;
            }*/

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
        AddDefField2BmfDialog addDefField2BmfDialog;

        @Override
        public Component getTableCellRendererComponent(JTable table
                , Object value
                , boolean isSelected
                , boolean hasFocus
                , int row
                , int column) {
            Color backgroundColor = null;

            PropertyDTO p = addDefField2BmfDialog.getRow(row);

            if (p != null && p.getState() == VOStatus.NEW) {
                backgroundColor = new Color(255, 255, 255);
            } else if (p != null && p.getState() == VOStatus.DELETED) {
                backgroundColor = new Color(245, 187, 187);
            } else if (p != null && p.getState() == VOStatus.UPDATED) {
                backgroundColor = new Color(166, 190, 171);
            } else if (row % 2 == 0) { // 偶数
                backgroundColor = new Color(128, 128, 128);//偶数行的背影色
            } else {
                backgroundColor = new Color(93, 93, 93);//奇数行的背影色
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
