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
        File bat = null;
        if (isWindows()) {
            bat = new File(binDir, "sysConfig.bat");

            if (!bat.isFile()) {
                bat = new File(binDir, "u8cSysConfig.bat");
            }

            if (!bat.isFile()) {
                bat = new File(binDir, "ncSysConfig.bat");
            }

            if (!bat.isFile()) {
                bat = new File(binDir, "sysConfig.sh");
            }

            if (!bat.isFile()) {
                bat = new File(binDir, "u8cSysConfig.sh");
            }

            if (!bat.isFile()) {
                bat = new File(binDir, "ncSysConfig.sh");
            }
        }

        if (!bat.isFile()) {
            bat = new File(binDir, "sysConfig.sh");

            if (!bat.isFile()) {
                bat = new File(binDir, "u8cSysConfig.sh");
            }

            if (!bat.isFile()) {
                bat = new File(binDir, "ncSysConfig.sh");
            }
        }

        if (!bat.isFile()) {
            LogUtil.error("脚本路径不存在!请把配置窗口启动脚本命名成: ncSysConfig.bat 和 ncSysConfig.sh");
            return;
        }

        Runtime.getRuntime().exec(bat.getPath());

        LogUtil.infoAndHide("如果没有打开且是win系统,脚本应该是sh文件,可以安装git后 设置默认git打开sh文件");
    }
}
