package com.air.nc5dev.acion;

import com.air.nc5dev.acion.base.AbstractIdeaAction;
import com.air.nc5dev.util.IoUtil;
import com.air.nc5dev.util.ProjectNCConfigUtil;
import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.util.idea.LogUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class OpenNCHomeIerpBinAction extends AbstractIdeaAction {
    @Override
    protected void doHandler(AnActionEvent e) throws IOException {
        if (StringUtil.isEmpty(ProjectNCConfigUtil.getNCHomePath())) {
            LogUtil.error("未配置NC HOME");
            return;
        }

        Desktop desktop = Desktop.getDesktop();
        File dirToOpen = new File(ProjectNCConfigUtil.getNCHomePath() + File.separatorChar + "ierp" + File
                .separatorChar + "bin");
        IoUtil.tryOpenFileExpolor(dirToOpen);

    }
}
