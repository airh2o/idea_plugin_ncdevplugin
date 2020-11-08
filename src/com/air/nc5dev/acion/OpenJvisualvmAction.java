package com.air.nc5dev.acion;

import com.air.nc5dev.acion.base.AbstractIdeaAction;
import com.air.nc5dev.util.ExceptionUtil;
import com.air.nc5dev.util.IoUtil;
import com.air.nc5dev.util.ProjectNCConfigUtil;
import com.air.nc5dev.util.idea.LogUtil;
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
                LogUtil.error("路径不存在: " + javaBin.getPath());
                return;
            }

            Runtime.getRuntime().exec(javaBin.getPath());
        } catch (Exception iae) {
            LogUtil.error(ExceptionUtil.getExcptionDetall(iae), iae);
        }
    }
}
