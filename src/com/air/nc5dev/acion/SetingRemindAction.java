package com.air.nc5dev.acion;

import com.air.nc5dev.acion.base.AbstractIdeaAction;
import com.air.nc5dev.ui.PatcherDialog;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class SetingRemindAction extends AbstractIdeaAction {
    @Override
    protected void doHandler(AnActionEvent e) {
        PatcherDialog dialog = new PatcherDialog(e);
        if(dialog.showAndGet()){
            dialog.onOK();
        }
    }
}
