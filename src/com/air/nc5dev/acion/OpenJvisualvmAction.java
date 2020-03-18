package com.air.nc5dev.acion;

import com.air.nc5dev.util.ExceptionUtil;
import com.air.nc5dev.util.IoUtil;
import com.air.nc5dev.util.ProjectNCConfigUtil;
import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.util.idea.ProjectUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import java.io.File;

public class OpenJvisualvmAction extends AbstractIdeaAction {
    @Override
    protected void doHandler(AnActionEvent e) {
        try {
            final String exe = "bin" + File.separatorChar + "jvisualvm.exe";
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
}
