package com.air.nc5dev.acion;

import com.air.nc5dev.util.ExceptionUtil;
import com.air.nc5dev.util.ProjectNCConfigUtil;
import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.util.idea.ProjectUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

import java.io.File;

public class OpenJvisualvmAction extends AnAction {

    @Override
    public void actionPerformed(final AnActionEvent e) {
        try {
            final String exe = "jvisualvm.exe";
            File javaHome = new File(System.getProperty("java.home"));
            File ufjdkHome = new File(ProjectNCConfigUtil.getNCHomePath() + File.separatorChar + "ufjdk");

            //尝试智能匹配路径 like : C:\Program Files\Java\jdk1.8.0_191\jre\bin\jconsole.exe -> to正确路径 (最多往上跳2级)
            if (!new File(javaHome, "bin" + File.separatorChar + exe).exists()) {
                javaHome = javaHome.getParentFile();
            }
            if (!new File(javaHome, "bin" + File.separatorChar + exe).exists()) {
                javaHome = javaHome.getParentFile();
            }


            File javaBin = new File(javaHome, "bin" + File.separatorChar + exe);
            javaBin = javaBin.exists() ? javaBin : new File(ufjdkHome, "bin" + File.separatorChar + exe);

            if (!javaBin.exists()) {
                ProjectUtil.warnNotification("路径不存在: " + javaBin.getPath(), e.getProject());
                return;
            }

            Runtime.getRuntime().exec(javaBin.getPath());
        } catch (Exception iae) {
            ProjectUtil.errorNotification(ExceptionUtil.getExcptionDetall(iae), e.getProject());
        }
    }

    private boolean isWindows(){
        return StringUtil.get(System.getProperty("os.name")).toLowerCase().contains("windows");
    }
}
