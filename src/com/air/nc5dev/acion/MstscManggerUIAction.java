package com.air.nc5dev.acion;

import com.air.nc5dev.acion.base.AbstractIdeaAction;
import com.air.nc5dev.ui.MstscDialog;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class MstscManggerUIAction extends AbstractIdeaAction {
    @Override
    protected void doHandler(AnActionEvent e) {
        MstscDialog dialog = new MstscDialog(e.getProject());
        dialog.setModal(false);
        dialog.show();
    }
}
