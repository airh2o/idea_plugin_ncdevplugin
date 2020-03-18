package com.air.nc5dev.acion;

import com.air.nc5dev.util.IdeaProjectGenerateUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class UpdateProjectNCDependencyAction extends AbstractIdeaAction {

    @Override
    protected void doHandler(AnActionEvent e) {
        IdeaProjectGenerateUtil.updateApplicationNCLibrarys(e.getProject());
    }
}
