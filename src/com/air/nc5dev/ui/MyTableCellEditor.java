package com.air.nc5dev.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.EventObject;
import javax.swing.AbstractCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.table.TableCellEditor;

public class MyTableCellEditor extends AbstractCellEditor implements TableCellEditor {

    protected JComponent editorComponent; // 编辑组件
    protected EditorDelegate delegate;    // 编辑代表
    protected int clickCountToStart = 1;

    public MyTableCellEditor(final JTextField textField) {
        editorComponent = textField;
        this.clickCountToStart = 1;
        delegate = new EditorDelegate() {
            public void setValue(Object value) {
                textField.setText((value != null) ? value.toString().trim() : "");
            }

            public Object getCellEditorValue() {
                return textField.getText();
            }
        };
        textField.addActionListener(delegate);
    }

    public MyTableCellEditor(final JCheckBox checkBox) {
        editorComponent = checkBox;
        checkBox.setSelected(false);
        checkBox.setHorizontalAlignment(JCheckBox.CENTER);
        delegate = new EditorDelegate() {
            public void setValue(Object value) {
                boolean selected = false;
                if (value instanceof Boolean) {
                    selected = ((Boolean) value).booleanValue();
                } else if (value instanceof String) {
                    selected = value.equals("true");
                }
                checkBox.setSelected(selected);
            }

            public Object getCellEditorValue() {
                return Boolean.valueOf(checkBox.isSelected());
            }
        };
        checkBox.addActionListener(delegate);
        checkBox.setRequestFocusEnabled(false);
    }

    public MyTableCellEditor(final JComboBox comboBox) {
        editorComponent = comboBox;
        comboBox.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
        delegate = new EditorDelegate() {
            public void setValue(Object value) {
                comboBox.setSelectedItem(value);
            }

            public Object getCellEditorValue() {
                return comboBox.getSelectedItem();
            }

            public boolean shouldSelectCell(EventObject anEvent) {
                if (anEvent instanceof MouseEvent) {
                    MouseEvent e = (MouseEvent) anEvent;
                    return e.getID() != MouseEvent.MOUSE_DRAGGED;
                }
                return true;
            }

            public boolean stopCellEditing() {
                if (comboBox.isEditable()) {
                    // Commit edited value.
                    comboBox.actionPerformed(new ActionEvent(
                            MyTableCellEditor.this, 0, ""));
                }
                return super.stopCellEditing();
            }
        };
        comboBox.addActionListener(delegate);
    }

    public Component getComponent() {
        return editorComponent;
    }

    public void setClickCountToStart(int count) {
        clickCountToStart = count;
    }

    public int getClickCountToStart() {
        return clickCountToStart;
    }

    @Override
    public Object getCellEditorValue() {
        return delegate.getCellEditorValue();
    }

    @Override
    public boolean isCellEditable(EventObject anEvent) {
        if (anEvent != null && delegate != null) {
            return delegate.isCellEditable(anEvent);
        } else {
            return false;
        }
    }

    @Override
    public boolean shouldSelectCell(EventObject anEvent) {
        return delegate.shouldSelectCell(anEvent);
    }

    @Override
    public boolean stopCellEditing() {
        return delegate.stopCellEditing();
    }

    @Override
    public void cancelCellEditing() {
        delegate.cancelCellEditing();
    }

    public Component getTreeCellEditorComponent(JTree tree, Object value,
            boolean isSelected,
            boolean expanded,
            boolean leaf, int row) {
        String stringValue = tree.convertValueToText(value, isSelected,
                expanded, leaf, row, false);

        delegate.setValue(stringValue);
        return editorComponent;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected,
            int row, int column) {
        delegate.setValue(value);
        return editorComponent;
    }

    protected class EditorDelegate implements ActionListener, ItemListener, Serializable {

        /**
         * The value of this cell.
         */
        protected Object value;

        public Object getCellEditorValue() {
            return value;
        }
        public void setValue(Object value) {
            this.value = value;
        }

        public boolean isCellEditable(EventObject anEvent) {
            if (anEvent instanceof MouseEvent) {
                return ((MouseEvent) anEvent).getClickCount() >= clickCountToStart;
            }
            return true;
        }
        public boolean shouldSelectCell(EventObject anEvent) {
            return true;
        }
        public boolean startCellEditing(EventObject anEvent) {
            return true;
        }
        public boolean stopCellEditing() {
            fireEditingStopped();
            return true;
        }
        public void cancelCellEditing() {
            fireEditingCanceled();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            MyTableCellEditor.this.stopCellEditing();
        }
        @Override
        public void itemStateChanged(ItemEvent e) {
            MyTableCellEditor.this.stopCellEditing();
        }
    }
}
