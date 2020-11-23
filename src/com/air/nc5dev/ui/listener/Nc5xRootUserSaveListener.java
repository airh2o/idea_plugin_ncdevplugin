package com.air.nc5dev.ui.listener;

import com.air.nc5dev.ui.ResetNCUserPassWordPanel;
import com.air.nc5dev.util.idea.LogUtil;
import com.intellij.openapi.ui.Messages;
import nc.hb.account.AccountXMLUtil;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.sm.config.SysAdm;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

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
public class Nc5xRootUserSaveListener implements ActionListener {
    private ResetNCUserPassWordPanel panel;

    public Nc5xRootUserSaveListener(ResetNCUserPassWordPanel panel) {
        this.panel = panel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int yesNo = Messages.showYesNoDialog("是否写入修改到文件?请确保文件已经备份!写入完成后必须重启后端服务!"
                , "询问", Messages.getQuestionIcon());
        if (yesNo != Messages.OK) {
            return;
        }


        try {
            ArrayList list = new ArrayList();

            if (panel.table_nc5x_rootuser.getRowCount() == 0) {
                Messages.showErrorDialog("请添加系统管理用户！", "错误:");
                return;
            }

            for (int i = 0; i < panel.table_nc5x_rootuser.getRowCount(); i++) {
                String rootName = panel.table_nc5x_rootuser.getValueAt(i, 0).toString();

                for (int j = i + 1; j < panel.table_nc5x_rootuser.getRowCount(); j++) {
                    if (rootName.equals(panel.table_nc5x_rootuser.getValueAt(j, 0).toString())) {
                        Messages.showErrorDialog("存在编码重复的用户，请修改！", "错误:");
                        return;
                    }
                }
            }


            for (int i = 0; i < panel.table_nc5x_rootuser.getRowCount(); i++) {
                if ((panel.table_nc5x_rootuser.getValueAt(i, 0) != null) && (!"".equals(panel.table_nc5x_rootuser
                        .getValueAt(i, 0)))) {
                    SysAdm adm = new SysAdm();
                    adm.setSysAdmCode(panel.table_nc5x_rootuser.getValueAt(i, 0).toString());
                    adm.setSysAdmName(panel.table_nc5x_rootuser.getValueAt(i, 0).toString());
                    adm.setPassword(panel.table_nc5x_rootuser.getValueAt(i, 1).toString());
                    adm.setDirty(false);
                    adm.setLocked(new UFBoolean(panel.table_nc5x_rootuser.getValueAt(i, 2).toString()));

                    list.add(adm);
                }
            }


            SysAdm[] adms = new SysAdm[list.size()];
            list.toArray(adms);

            panel.config5x.setArySysAdms(adms);

            AccountXMLUtil.saveConfigParameter(panel.config5x, panel.textField_nc5x_xmlpath.getText());
        } catch (Exception ex) {
            LogUtil.error(ex.toString(), ex);
            Messages.showErrorDialog("文件保存错误!请查看NC插件日志窗口!", ex.toString());
            return;
        }

        StringBuffer msg = new StringBuffer();
        msg.append("文件保存成功！\n\n");
        msg.append("请重启NC中间件！\n\n");

        Messages.showInfoMessage(msg.toString(), "提示:");
    }
}
