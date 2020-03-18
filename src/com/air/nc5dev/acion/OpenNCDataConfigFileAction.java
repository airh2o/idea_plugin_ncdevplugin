package com.air.nc5dev.acion;

import com.air.nc5dev.util.ExceptionUtil;
import com.air.nc5dev.util.ProjectNCConfigUtil;
import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.util.idea.ProjectUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import java.awt.*;
import java.io.File;

public class OpenNCDataConfigFileAction extends AbstractIdeaAction {
    @Override
    protected void doHandler(AnActionEvent e) {
        try {
            if(StringUtil.isEmpty(ProjectNCConfigUtil.getNCHomePath())){
                ProjectUtil.warnNotification("未配置NC HOME", e.getProject());
                return ;
            }

            Desktop desktop = Desktop.getDesktop();
            File dirToOpen = new File(ProjectNCConfigUtil.getNCHomePath() + File.separatorChar
                    + "ierp" + File.separatorChar + "bin"
                    + File.separatorChar + "prop.xml");
            desktop.open(dirToOpen);
        } catch (Exception iae) {
            ProjectUtil.errorNotification(ExceptionUtil.getExcptionDetall(iae), e.getProject());
        }
    }
}
