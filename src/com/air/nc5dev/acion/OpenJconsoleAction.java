package com.air.nc5dev.acion;

import com.air.nc5dev.util.ExceptionUtil;
import com.air.nc5dev.util.ProjectNCConfigUtil;
import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.util.idea.ProjectUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

import java.io.File;

public class OpenJconsoleAction extends AnAction {

    @Override
    public void actionPerformed(final AnActionEvent e) {
        try {
            final String exe = "jconsole.exe";
            String javaHome = System.getProperty("java.home");
            if(StringUtil.isEmpty(javaHome)){
                if(StringUtil.isEmpty(ProjectNCConfigUtil.getNCHomePath())){
                    ProjectUtil.warnNotification("未配置JAVA_HOME 环境变量, 也没有配置NC HOME", e.getProject());
                    return ;
                }
                javaHome = ProjectNCConfigUtil.getNCHomePath() + File.separatorChar + "ufjdk";
            }else{
                //尝试智能匹配路径 like : C:\Program Files\Java\jdk1.8.0_191\jre\bin\jconsole.exe -> to正确路径 (最多往上跳2级)
                if(!new File(javaHome, "bin" + File.separatorChar + exe).exists()){
                    javaHome = new File(javaHome).getParent();
                }
                if(!new File(javaHome, "bin" + File.separatorChar + exe).exists()){
                    javaHome = new File(javaHome).getParent();
                }
            }

            File javaBin = new File(javaHome, "bin" + File.separatorChar + exe);

            if(!javaBin.exists()){
                ProjectUtil.warnNotification("路径不存在: " + javaBin.getPath(), e.getProject());
                return ;
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
