package com.air.nc5dev.ui.nccactionsearch;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBList;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * <br>
 * <br>
 * <br>
 *
 * @author 唐粟 Email:209308343@qq.com
 * @date 2022/8/12 0012 11:15
 * @project
 * @Version
 */
public class RestListForm {
    private JTextField requestSearch;
    private JScrollPane scrollPane;
    private JBList<RequestPath> jbList;
    private JPanel panel;
    private JButton reloadBtn;

    public RestListForm(Project project) {
        reloadBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<RequestPath> requestPath = RequestPathUtil.findAllRequestInProject(project);
                RestListForm restListForm = RequestPathUtil.getRestListForm(project);
                restListForm.getJbList().setModel(RequestPathUtil.getListModel(requestPath));
            }
        });
        jbList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                // 有 1，2，3。1代表鼠标左键，3代表鼠标右键
                if (3 == e.getButton()) {
                    int index = jbList.locationToIndex(e.getPoint());
                    RequestPath requestPath = jbList.getModel().getElementAt(index);

                    // 获取系统剪贴板
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    // 封装文本内容
                    Transferable trans = new StringSelection(requestPath.getPath());
                    // 把文本内容设置到系统剪贴板
                    clipboard.setContents(trans, null);

                    // 提示框
                    NotificationGroup balloonNotifications = new NotificationGroup("RequestList", NotificationDisplayType.TOOL_WINDOW, false);
                    Notification notification = balloonNotifications.createNotification("Copyed", requestPath.getPath(),
                            NotificationType.INFORMATION, (notification1, hyperlinkEvent) -> {
                            });
                    Notifications.Bus.notify(notification);
                }
            }
        });
    }

    public JTextField getRequestSearch() {
        return requestSearch;
    }

    public JScrollPane getScrollPane() {
        return scrollPane;
    }

    public JBList<RequestPath> getJbList() {
        return jbList;
    }

    public JPanel getPanel() {
        return panel;
    }
}
