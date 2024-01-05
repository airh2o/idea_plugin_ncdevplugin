package com.air.nc5dev.ui;

import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.util.idea.LogUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTextField;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JViewport;
import java.awt.AWTEvent;
import java.awt.Point;
import java.awt.Rectangle;

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
public class SearchTableFieldDialog extends DialogWrapper {
    int height = 100;
    int width = 400;
    JBPanel contentPane;
    JBTextField textFieldText;
    JBLabel labelInfo;
    JTable table;

    public SearchTableFieldDialog(Project project, JTable table) {
        super(project);
        this.table = table;
        createCenterPanel();
        init();
        setOKButtonText("上一个");
        setOKButtonTooltip("往上搜索行");
        setCancelButtonText("下一个");
        setModal(false);
        setTitle("查找表格");
    }

    public void search(boolean up) {
        String str = StringUtil.trim(getTextFieldText().getText());
        if (StringUtil.isBlank(str)) {
            labelInfo.setText("请输入搜索内容");
            return;
        }

        if (table == null || table.getRowCount() < 1) {
            labelInfo.setText("表格没有行可以搜索");
            return;
        }

        int columnCount = table.getColumnCount();
        int rowCount = table.getRowCount();
        int selectedRow = table.getSelectedRow();
        for (int i = up ? selectedRow - 1 : selectedRow + 1; ; ) {
            if (up) {
                if (i < 0) {
                    break;
                }
            } else {
                if (i >= rowCount) {
                    break;
                }
            }

            for (int x = 0; x < columnCount; x++) {
                Object v = table.getValueAt(i, x);
                if (StringUtil.contains(StringUtil.getSafeString(v), str)) {
                    table.clearSelection();
                    table.setRowSelectionInterval(i, i);
                    labelInfo.setText(String.format("行 %s 列 %s 值 %s 匹配成功", i + 1, table.getColumnName(x), v));

                    //滚动条 滚动到当前行
                    JViewport viewport = (JViewport) table.getParent();
                    Rectangle rect = table.getCellRect(i, 0, true);
                    Point pt = viewport.getViewPosition();
                    rect.setLocation(rect.x - pt.x, rect.y - pt.y);
                    viewport.scrollRectToVisible(rect);

                    /*滚动条 滚动到当前列
                    JViewport viewport = (JViewport) table.getParent();
                    Rectangle r = table.getCellRect(rowIndex, 0, true);
                    int extentHeight = viewport.getExtentSize().height;
                    int viewHeight = viewport.getViewSize().height;

                    int y = Math.max(0, r.y - ((extentHeight - r.height) / 2));
                    y = Math.min(y, viewHeight - extentHeight);

                    viewport.setViewPosition(new Point(0, y));
                    * */

                    return;
                }
            }

            if (up) {
                --i;
                if (i < 0) {
                    break;
                }
            } else {
                ++i;
                if (i >= rowCount) {
                    break;
                }
            }
        }

        labelInfo.setText("没有任何行匹配");
    }

    private void createCenterPanel0() throws Exception {
        contentPane = new JBPanel();
        contentPane.setBounds(0, 0, getWidth(), getHeight());
        JBPanel panel_main = contentPane;
        panel_main.setLayout(null);
        {
            int x = 1;
            int y = 1;

            labelInfo = new JBLabel("");
            labelInfo.setBounds(x, y, getWidth(), 30);
            panel_main.add(labelInfo);

            JBLabel label = new JBLabel("搜索文本:");
            label.setBounds(x, y += labelInfo.getHeight() + 2, 60, 30);
            panel_main.add(label);
            textFieldText = new JBTextField();
            textFieldText.setBounds(x += label.getWidth() + 2, y, 380, 30);
            panel_main.add(textFieldText);

            initListeners();
        }

        //设置点默认值
        initDefualtValues();
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

    private void initListeners() {
    }

    @Override
    public void doCancelAction(AWTEvent source) {
        if (source.getID() == 201) {
            super.dispose();
            return;
        }

        search(false);
    }

    @Override
    protected void doOKAction() {
        search(true);
    }
}
