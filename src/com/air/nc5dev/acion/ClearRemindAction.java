package com.air.nc5dev.acion;

import com.air.nc5dev.acion.base.AbstractIdeaAction;
import com.air.nc5dev.util.ProjectNCConfigUtil;
import com.air.nc5dev.util.RemindUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;

public class ClearRemindAction extends AbstractIdeaAction {

    @Override
    protected void doHandler(AnActionEvent e) {
        RemindUtil.clear();
    }
}
