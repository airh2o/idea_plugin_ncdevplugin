package com.air.nc5dev.acion;

import com.air.nc5dev.acion.base.AbstractIdeaAction;
import com.air.nc5dev.component.BuildManagerListenerImpl;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class ReDeployAction extends AbstractIdeaAction {

    @Override
    protected void doHandler(AnActionEvent e) {
        BuildManagerListenerImpl.getInstance().doTask(e.getProject());
    }
}
