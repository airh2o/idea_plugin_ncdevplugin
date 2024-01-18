package com.air.nc5dev.acion;

import com.air.nc5dev.acion.base.AbstractIdeaAction;
import com.air.nc5dev.util.IoUtil;
import com.air.nc5dev.util.ProjectNCConfigUtil;
import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.util.idea.LogUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;

import java.io.File;

public class DeleteNCLogsAction extends AbstractIdeaAction {
    @Override
    protected void doHandler(AnActionEvent e) {
        if (StringUtil.isEmpty(ProjectNCConfigUtil.getNCHomePath())) {
            LogUtil.error("未配置NC HOME");
            return;
        }
        File dir = new File(ProjectNCConfigUtil.getNCHomePath() + File.separatorChar + "nclogs");

        if (!dir.exists()) {
            dir = new File(ProjectNCConfigUtil.getNCHomePath() + File.separatorChar + "u8clogs");
        }

        if (!dir.exists()) {
            LogUtil.error("日志文件夹路径不存在，请使用NC标准文件夹名: " + dir.getPath());
        }

        IoUtil.deleteFileAll(dir);
        dir.mkdir();

        LogUtil.error("日志文件清理完成: " + dir.getPath());
    }
}
