package com.air.nc5dev.acion;

import com.air.nc5dev.util.ExceptionUtil;
import com.air.nc5dev.util.IoUtil;
import com.air.nc5dev.util.ProjectNCConfigUtil;
import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.util.idea.ProjectUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

import java.io.File;

public class DeleteNCLogsAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        try {
            if(StringUtil.isEmpty(ProjectNCConfigUtil.getNCHomePath())){
                ProjectUtil.warnNotification("未配置NC HOME", e.getProject());
                return ;
            }
            File dir = new File(ProjectNCConfigUtil.getNCHomePath() + File.separatorChar + "nclogs" );

            IoUtil.deleteFileAll(dir);
        } catch (Exception iae) {
            ProjectUtil.errorNotification(ExceptionUtil.getExcptionDetall(iae), e.getProject());
        }
    }
}
