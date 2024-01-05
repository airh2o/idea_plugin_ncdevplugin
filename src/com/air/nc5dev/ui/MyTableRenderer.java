package com.air.nc5dev.ui;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Color;
import java.awt.Component;

public class MyTableRenderer extends DefaultTableCellRenderer {

    private static final long serialVersionUID = 1L;

    /**
     * 对表格进行渲染的时候单元格默认返回的是JLabel，可以根据需要返回不同的控件
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {
        // 第一列渲染成复选框
        if (column == 0) {
            JCheckBox box = new JCheckBox(); // 复选框
            box.setOpaque(true);  // 设置成不透明
            box.setHorizontalAlignment(SwingConstants.LEFT); //
            if (isSelected) {//点击表格的时候改变点击的行的背景色
                box.setBackground(new Color(141, 214, 255, 64));
            } else {
                if (row % 2 == 0) { // 偶数行的前景、背景颜色
                    box.setBackground(new Color(0, 0, 0));
                    box.setForeground(table.getForeground());
                } else { // 奇数行的背景颜色
                    box.setBackground(table.getBackground());
                }
            }
            boolean valu = (Boolean) value;
            box.setSelected(valu);
            return box;
        }
        // 其它列渲染成普通标签
        JLabel label = new JLabel();
        label.setOpaque(true);
        label.setHorizontalAlignment(SwingConstants.LEFT);
        if (isSelected) { // 点击表格的时候改变点击的行的背景色
            label.setBackground(new Color(141, 214, 255, 64));
        } else {
            if (row % 2 == 0) {
                label.setBackground(new Color(0, 0, 0));
                label.setForeground(table.getForeground());
            } else {
                label.setBackground(table.getBackground());
            }
        }
        label.setText(value != null ? value.toString() : "");
        return label;
    }
}
