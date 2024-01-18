package com.air.nc5dev.acion.meta;

import com.air.nc5dev.acion.base.AbstractIdeaAction;
import com.air.nc5dev.ui.exportbmf.ExportbmfDialog;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 *
 */
public class TakeBmfFromDatabaseAction extends AbstractIdeaAction {

    @Override
    protected void doHandler(AnActionEvent e) {
        ExportbmfDialog dialog = new ExportbmfDialog(e.getProject());
        dialog.setModal(false);
        dialog.show();
    }

}
