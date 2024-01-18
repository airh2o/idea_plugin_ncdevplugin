package com.air.nc5dev.ui.listener;

import com.air.nc5dev.ui.ResetNCUserPassWordPanel;

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
public class Nc5xRootUserAddLineListener implements ActionListener {
    private ResetNCUserPassWordPanel panel;

    public Nc5xRootUserAddLineListener(ResetNCUserPassWordPanel panel) {
        this.panel = panel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        panel.tableModel_nc5x_rootuser.addRow(new Object[]{"", "", false});
    }
}
