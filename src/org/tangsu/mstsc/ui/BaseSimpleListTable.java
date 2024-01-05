package org.tangsu.mstsc.ui;

import com.air.nc5dev.util.StringUtil;
import com.intellij.ui.table.JBTable;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Enumeration;

@Getter
@Setter
public class BaseSimpleListTable extends JBTable {
    public BaseSimpleListTable() {
        super();
    }

    public BaseSimpleListTable(TableModel tableModel) {
        super(tableModel);
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

    public static class MyDefaultTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Color backgroundColor = null;
            if (row % 2 == 0) { // 偶数
                backgroundColor = new Color(245, 245, 245);//偶数行的背影色
            } else {
                backgroundColor = Color.white;
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
