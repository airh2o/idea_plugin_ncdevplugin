package com.air.nc5dev.acion;

import com.air.nc5dev.util.ExceptionUtil;
import com.air.nc5dev.util.ProjectNCConfigUtil;
import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.util.idea.ProjectUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import java.io.File;

public class OpenNCConfigWindowAction extends AbstractIdeaAction {
    @Override
    protected void doHandler(AnActionEvent e) {
        try {
            if(StringUtil.isEmpty(ProjectNCConfigUtil.getNCHomePath())){
                ProjectUtil.warnNotification("未配置NC HOME", e.getProject());
                return ;
            }

            File binDir = new File(ProjectNCConfigUtil.getNCHomePath() + File.separatorChar + "bin");
            File sysConfigbat = new File(binDir, isWindows() ? "sysConfig.bat" : "sysConfig.sh");
            File u8cConfigbat = new File(binDir, isWindows() ? "u8cSysConfig.bat" : "u8cSysConfig.sh");

            File bat = sysConfigbat == null ? u8cConfigbat : sysConfigbat;

            if(bat == null){
                ProjectUtil.warnNotification("脚本路径不存在!", e.getProject());
                return ;
            }

            Runtime.getRuntime().exec(bat.getPath());
        } catch (Exception iae) {
            ProjectUtil.errorNotification(ExceptionUtil.getExcptionDetall(iae), e.getProject());
        }
    }
}
