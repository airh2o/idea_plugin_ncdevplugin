package com.air.nc5dev.ui;

import cn.hutool.core.io.IoUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * 插件的 console 窗口
 *
 * @Author 唐粟 Email: 209308343@qq.com
 * @Description
 * @Date 2020/11/6 18:22
 **/
public class PluginConsoleWindow implements ToolWindowFactory {
    //主面板
    private JPanel mainPanel;
    ///构造一个 有滚动条的面板
    private JBScrollPane scrollPane;
    private JEditorPane jEditorPane;
    //右键菜单
    private JPopupMenu popup;
    public static PluginConsoleWindow me;

    public PluginConsoleWindow() {
        me = this;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize(); //得到屏幕的尺寸
        mainPanel = new JPanel();
        //设置主面板的大小
        /*mainPanel.setPreferredSize(new Dimension((int) screenSize.getWidth() - 50
                , (int) screenSize.getHeight() / 3 * 2));*/

        //构建右键菜单
        popup = new JPopupMenu();
        popup.add(createPopupItem("搜索", "search", e -> {
            try {
                jEditorPane.setText(IoUtil.read(new FileInputStream("C:\\Users\\Administrator\\" +
                        ".IntelliJIdea2019.3\\system\\log\\threadDumps-freeze-20200829-185232-IU-193.6015.39" +
                        "-ComponentContainer.startComponents-16sec\\threadDump-20200829-185237.txt"), "utf-8"));
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            }
        }));
        popup.addSeparator();
        popup.add(createPopupItem("清空", "clear", e -> {
            jEditorPane.setText("");
        }));
        popup.addSeparator();
        popup.add(createPopupItem("首行", "headLine", e -> {
            jEditorPane.setCaretPosition(0);
            jEditorPane.moveCaretPosition(0);
        }));
        popup.add(createPopupItem("尾行", "tailLine", e -> {
            jEditorPane.setCaretPosition(jEditorPane.getText().length());
            jEditorPane.moveCaretPosition(jEditorPane.getText().length());
        }));

        //构建内容日志文本框
        jEditorPane = new JEditorPane();
        jEditorPane.setEditable(true);
        scrollPane = new JBScrollPane(jEditorPane);
        //设置滚动条面板位置
        /*scrollPane.setPreferredSize(new Dimension((int) screenSize.getWidth() - 50
                , (int) screenSize.getHeight() / 3 * 2 - 50));*/
        //将滚动条面板设置 可见
        scrollPane.setVisible(true);
        //设置滚动条的滚动速度
        scrollPane.getVerticalScrollBar().setUnitIncrement(15);
        //解决闪烁问题
        scrollPane.getVerticalScrollBar().setDoubleBuffered(true);

        jEditorPane.add(popup);
        jEditorPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getModifiers() == InputEvent.BUTTON1_MASK) {
                    //鼠标左键
                    popup.setVisible(false);
                }
                if (e.getModifiers() == InputEvent.BUTTON2_MASK) {
                    //滚轮点击
                }
                if (e.getModifiers() == InputEvent.BUTTON3_MASK) {
                    //右键
                    popup.show(mainPanel, e.getX(), e.getY());
                }
            }
        });
        mainPanel.add(scrollPane);
    }

    /**
     * new一个右键菜单项
     *
     * @param name
     * @param action
     * @param actionListener
     * @return
     */
    private JMenuItem createPopupItem(String name, String action, ActionListener actionListener) {
        JMenuItem item = new JMenuItem(name);
        item.setActionCommand(action);
        item.addActionListener(actionListener);

        return item;
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(mainPanel, "", false);
        toolWindow.getContentManager().addContent(content);
    }

    //返回光标所在列
    public static int getColumnAtCaret(JTextComponent component)
    {
        int caretPosition = component.getCaretPosition();
        Element root = component.getDocument().getDefaultRootElement();
        int line = root.getElementIndex( caretPosition );
        int lineStart = root.getElement( line ).getStartOffset();

        return caretPosition - lineStart + 1;
    }

    //获取指定行的第一个字符位置
    public static int getLineStart(JTextComponent component,int line)
    {
        int lineNumber = line - 1;
        Element root = component.getDocument().getDefaultRootElement();
        int lineStart = root.getElement( lineNumber ).getStartOffset();
        return lineStart;
    }

    //返回选中的字符数
    public static int getSelectedNumber(JTextComponent component)
    {
        if( component.getSelectedText() == null )
            return 0;
        else
            return component.getSelectedText().length();
    }

    //返回光标所在行
    public static int getLineAtCaret(JTextComponent component)
    {
        int caretPosition = component.getCaretPosition();
        Element root = component.getDocument().getDefaultRootElement();

        return root.getElementIndex( caretPosition ) + 1;
    }

    //返回文本行数
    public static int getLines(JTextComponent component)
    {
        Element root = component.getDocument().getDefaultRootElement();
        return root.getElementCount();
    }

    //返回文本框的字符总数
    public static int getCharNumber(JTextComponent component)
    {
        Document doc = component.getDocument();
        return doc.getLength();
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public JBScrollPane getScrollPane() {
        return scrollPane;
    }

    public JEditorPane getjEditorPane() {
        return jEditorPane;
    }

    public JPopupMenu getPopup() {
        return popup;
    }

    public static PluginConsoleWindow getMe() {
        return me;
    }

}