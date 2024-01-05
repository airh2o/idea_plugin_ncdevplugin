package com.air.nc5dev.acion;

import com.air.nc5dev.acion.base.AbstractIdeaAction;
import com.air.nc5dev.ui.nccjsondecode.NCCJsonDecodeDialog;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class NCCJsonDecodeAction extends AbstractIdeaAction {
    @Override
    protected void doHandler(AnActionEvent e) {
        NCCJsonDecodeDialog dialog = new NCCJsonDecodeDialog(e.getProject());
        dialog.setModal(false);
        dialog.show();
    }
}
