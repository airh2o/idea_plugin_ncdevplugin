package com.air.nc5dev.acion;

import com.air.nc5dev.acion.base.AbstractIdeaAction;
import com.air.nc5dev.ui.MstscDialog;
import com.air.nc5dev.util.idea.LogUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.tangsu.mstsc.dao.BaseDao;

import java.io.File;
import java.io.IOException;

public class MstscOpenLastAction extends AbstractIdeaAction {
    @Override
    protected void doHandler(AnActionEvent e) {
        File rdp = new File(BaseDao.getDefualtBaseDir(), "temp.rdp");

        if (!rdp.isFile()) {
            LogUtil.infoAndHide("未打开过MSTSC管理里的连接!因为不存在:" + rdp.getPath());
            return ;
        }

        try {
            Runtime.getRuntime().exec(
                    String.format("mstsc %s /console"
                            , rdp.getPath()));
        } catch (Throwable ex) {
        }
    }
}
