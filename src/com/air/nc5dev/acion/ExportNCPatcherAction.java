package com.air.nc5dev.acion;

import com.air.nc5dev.ui.PatcherDialog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class ExportNCPatcherAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        PatcherDialog dialog = new PatcherDialog(e);
        if(dialog.showAndGet()){
            dialog.onOK();
        }
    }
}
