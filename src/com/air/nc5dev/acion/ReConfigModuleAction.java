package com.air.nc5dev.acion;

import com.air.nc5dev.acion.base.AbstractIdeaAction;
import com.air.nc5dev.util.RemindUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

/**
 *
 */
public class ReConfigModuleAction extends AbstractIdeaAction {

    @Override
    protected void doHandler(AnActionEvent e) {
        Project project = e.getProject();

    }
}
