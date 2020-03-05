package com.air.nc5dev.acion;

import com.air.nc5dev.util.*;
import com.air.nc5dev.util.idea.ProjectUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

import java.io.File;

public class OpenJconsoleAction extends AnAction {

    @Override
    public void actionPerformed(final AnActionEvent e) {
        try {
            final String exe = "bin" + File.separatorChar + "jconsole.exe";
            File javaBin = IoUtil.getJavaHomePathFile(ProjectNCConfigUtil.getNCHomePath()
                    , exe);
            if (null == javaBin) {
                ProjectUtil.warnNotification("路径不存在: " + javaBin.getPath(), e.getProject());
                return;
            }

            Runtime.getRuntime().exec(javaBin.getPath());
        } catch (Exception iae) {
            ProjectUtil.errorNotification(ExceptionUtil.getExcptionDetall(iae), e.getProject());
        }
    }

    private boolean isWindows() {
        return StringUtil.get(System.getProperty("os.name")).toLowerCase().contains("windows");
    }
}
