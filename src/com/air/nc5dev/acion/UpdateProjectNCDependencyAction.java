package com.air.nc5dev.acion;

import com.air.nc5dev.util.IdeaProjectGenerateUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class UpdateProjectNCDependencyAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        IdeaProjectGenerateUtil.updateApplicationNCLibrarys(e.getProject());
    }
}
