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
            if (StringUtil.isEmpty(ProjectNCConfigUtil.getNCHomePath())) {
                ProjectUtil.warnNotification("未配置NC HOME", e.getProject());
                return;
            }
            File dir = new File(ProjectNCConfigUtil.getNCHomePath() + File.separatorChar + "nclogs");

            if (!dir.exists()) {
                dir = new File(ProjectNCConfigUtil.getNCHomePath() + File.separatorChar + "u8clogs");
            }

            if (!dir.exists()) {
                ProjectUtil.warnNotification("日志文件夹路径不存在，请使用NC标准文件夹名: " + dir.getPath(), e.getProject());
            }

            IoUtil.deleteFileAll(dir);
            dir.mkdir();

            ProjectUtil.infoNotification("日志文件清理完成: " + dir.getPath(), e.getProject());
        } catch (Exception iae) {
            ProjectUtil.errorNotification(ExceptionUtil.getExcptionDetall(iae), e.getProject());
        }
    }
}
