package com.air.nc5dev.ui;

import com.intellij.ui.components.JBCheckBox;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
@Accessors(chain = true)
public class MyTableRenderer extends DefaultTableCellRenderer {
    Collection<Integer> jCheckBoxIndexs = new ArrayList<>();
    Map<Integer, Color> selectColorIndex = new HashMap<>();
    Map<Integer, Color> unSelectColorIndex = new HashMap<>();
    Map<Integer, String> column2ToolTipTextMap = new HashMap<>();
    //  Map<String, JBCheckBox> rowColumn2CheckboxMap = new HashMap<>();
    private static final long serialVersionUID = 1L;

    public MyTableRenderer() {
        jCheckBoxIndexs.add(0);
        selectColorIndex.put(0, new Color(217, 217, 217));
        unSelectColorIndex.put(0, new Color(115, 115, 115));
    }

    /**
     * 对表格进行渲染的时候单元格默认返回的是JLabel，可以根据需要返回不同的控件
     */
    @Override
    public Component getTableCellRendererComponent(JTable table
            , Object value
            , boolean isSelected
            , boolean hasFocus
            , int row
            , int column) {
        // 第一列渲染成复选框
        if (jCheckBoxIndexs.contains(column)) {
            boolean valu = (Boolean) value;
            JBCheckBox box = null; //rowColumn2CheckboxMap.get(row + ":" + column); // 复选框
            if (box == null) {
                box = new JBCheckBox();
                box.setOpaque(true);  // 设置成不透明
                // rowColumn2CheckboxMap.put(row + ":" + column, box);
            }
            box.setToolTipText(column2ToolTipTextMap.get(column));
            // box.setHorizontalAlignment(SwingConstants.LEFT);
            if (isSelected) {//点击表格的时候改变点击的行的背景色
                box.setBackground(new Color(141, 214, 255, 64));
            } else {
                if (valu && selectColorIndex.containsKey(column)) {
                    box.setBackground(selectColorIndex.get(column));
                } else if (!valu && unSelectColorIndex.containsKey(column)) {
                    box.setBackground(unSelectColorIndex.get(column));
                } else {
                    if (row % 2 == 0) { // 偶数行的前景、背景颜色
                        box.setBackground(new Color(0, 0, 0));
                        box.setForeground(table.getForeground());
                    } else { // 奇数行的背景颜色
                        box.setBackground(table.getBackground());
                    }
                }
            }
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
                //  label.setBackground(new Color(0, 0, 0));
                label.setForeground(table.getForeground());
            } else {
                label.setBackground(table.getBackground());
            }
        }
        label.setText(value != null ? value.toString() : "");
        return label;
    }
}
