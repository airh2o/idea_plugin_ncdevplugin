package com.air.nc5dev.ui.listener;

import com.air.nc5dev.ui.ResetNCUserPassWordPanel;
import com.air.nc5dev.util.idea.LogUtil;
import com.air.nc5dev.util.ncutils.nc5x.NC5xEncode;
import com.intellij.openapi.ui.Messages;
import nc.bcmanage.vo.SuperAdminVO;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.sm.UserVO;
import nc.vo.uap.rbac.util.RbacUserPwdUtil;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

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
public class Nc6xRootUserSaveListener implements ActionListener {
    private ResetNCUserPassWordPanel panel;

    public Nc6xRootUserSaveListener(ResetNCUserPassWordPanel panel) {
        this.panel = panel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        try {
            ArrayList list = new ArrayList();

            int size = panel.nc6xAdminVOList.size();
            SuperAdminVO[] vos = new SuperAdminVO[size];
            for (int i = 0; i < size; i++) {
                vos[i] = panel.nc6xAdminVOList.get(i).clone();

                for (int k = 0; k < panel.table_nc6x_rootuser.getRowCount(); k++) {
                    if (panel.table_nc6x_rootuser.getValueAt(k, 0).equals(vos[i].getAdmCode())) {
                        vos[i].setPassword(getEncodeString(panel.table_nc6x_rootuser.getValueAt(k, 1).toString()));
                        vos[i].setIsLocked(new UFBoolean(panel.table_nc6x_rootuser.getValueAt(k, 2).toString())
                                .booleanValue());
                        list.add(vos[i]);
                        break;
                    }
                }
            }


            SuperAdminVO[] adms = new SuperAdminVO[list.size()];
            list.toArray(adms);

            updateCache(adms);
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

    private String getEncodeString(String password) throws Exception {
        UserVO user = new UserVO();
        user.setCuserid("superadminpk00000000");
        return RbacUserPwdUtil.getEncodedPassword(user, password);
    }

    public void updateCache(SuperAdminVO[] vos) {
        panel.nc6xAdminVOList.clear();
        if (vos != null) {
            panel.nc6xAdminVOList.addAll(Arrays.asList(vos));
        }
        serialized();
    }

    private void serialized() {
        File configFile = new File(panel.textField_nc6x_rootuserXmlPath.getText());
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(configFile, "GBK");
            SuperAdminVO[] vos = panel.nc6xAdminVOList.toArray(new SuperAdminVO[0]);
            String xmlContentBeforeEncoding = constructXMLContent(vos);

            pw.println(new NC5xEncode().encode(xmlContentBeforeEncoding));
        } catch (Exception ex) {
            LogUtil.error(ex.toString(), ex);
        } finally {
            if (pw != null) {
                pw.close();
            }
        }
    }

    private String constructXMLContent(SuperAdminVO[] vos) {
        StringBuffer sb = new StringBuffer("<?xml version=\"1.0\" encoding=\"gb2312\"?>");
        sb.append("<admins>");
        for (int i = 0; i < vos.length; i++) {
            SuperAdminVO vo = vos[i];
            sb.append("\t<admin>");
            sb.append("\t\t<code>" + (vo.getAdmCode() == null ? "" : vo.getAdmCode()) + "</code>");
            sb.append("\t\t<name>" + (vo.getAdmName() == null ? "" : vo.getAdmName()) + "</name>");
            sb.append("\t\t<password>" + (vo.getPassword() == null ? "" : vo.getPassword()) + "</password>");
            sb.append("\t\t<oldpwd1>" + (vo.getOldpwd1() == null ? "" : vo.getOldpwd1()) + "</oldpwd1>");
            sb.append("\t\t<oldpwd2>" + (vo.getOldpwd2() == null ? "" : vo.getOldpwd2()) + "</oldpwd2>");
            sb.append("\t\t<oldpwd3>" + (vo.getOldpwd3() == null ? "" : vo.getOldpwd3()) + "</oldpwd3>");
            sb.append("\t\t<pwdinuse>" + (vo.getPwdinuse() == null ? "" : vo.getPwdinuse()) + "</pwdinuse>");
            sb.append("\t\t<pwdlvl>" + (vo.getPwdlvl() == null ? "" : vo.getPwdlvl()) + "</pwdlvl>");
            sb.append("\t\t<identify>" + (vo.getIdentify() == null ? "" : vo.getIdentify()) + "</identify>");
            sb.append("\t\t<isLocked>" + (vo.getIsLocked() ? "Y" : "N") + "</isLocked>");
            sb.append("\t</admin>");
        }
        sb.append("</admins>");
        return sb.toString();
    }
}
