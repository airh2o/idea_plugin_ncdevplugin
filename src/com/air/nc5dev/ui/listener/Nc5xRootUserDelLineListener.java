package com.air.nc5dev.ui.listener;

import com.air.nc5dev.ui.ResetNCUserPassWordPanel;
import com.intellij.openapi.ui.Messages;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * <br/>
 * <br/>
 * <br/>
 *
 * @author 唐粟 Email:209308343@qq.com
 * @date 2020/11/23 16:01
 * @project
 * @Version
 */
public class Nc5xRootUserDelLineListener implements ActionListener {
    private ResetNCUserPassWordPanel panel;

    public Nc5xRootUserDelLineListener(ResetNCUserPassWordPanel panel) {
        this.panel = panel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int i = panel.table_nc5x_rootuser.getSelectedRow();
        if (i < 0) {
            return;
        }

        Object userName = panel.tableModel_nc5x_rootuser.getValueAt(i, 0);

        int yesNo = Messages.showYesNoDialog("是否确认删除用户: " + userName + "?"
                , "询问", Messages.getQuestionIcon());
        if (yesNo != Messages.OK) {
            return;
        }

        panel.tableModel_nc5x_rootuser.removeRow(i);
    }
}
