package com.air.nc5dev.acion;

import com.air.nc5dev.acion.base.AbstractIdeaAction;
import com.air.nc5dev.util.ProjectNCConfigUtil;
import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.util.idea.LogUtil;
import com.air.nc5dev.util.idea.ProjectUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;

import java.io.File;
import java.io.IOException;

public class OpenNCConfigWindowAction extends AbstractIdeaAction {
    @Override
    protected void doHandler(AnActionEvent e) throws IOException {
        if (StringUtil.isEmpty(ProjectNCConfigUtil.getNCHomePath())) {
            ProjectUtil.warnNotification("未配置NC HOME", e.getProject());
            return;
        }

        File binDir = new File(ProjectNCConfigUtil.getNCHomePath() + File.separatorChar + "bin");
        File bat = new File(binDir, isWindows() ? "sysConfig.bat" : "sysConfig.sh");

        if (bat.isFile()) {
            bat = new File(binDir, isWindows() ? "u8cSysConfig.bat" : "u8cSysConfig.sh");
        }

        if (bat.isFile()) {
            bat = new File(binDir, isWindows() ? "ncSysConfig.bat" : "ncSysConfig.sh");
        }



        if (bat == null) {
            LogUtil.error("脚本路径不存在!请把配置窗口启动脚本命名成: ncSysConfig.bat 和 ncSysConfig.sh");
            return;
        }

        Runtime.getRuntime().exec(bat.getPath());

    }
}
