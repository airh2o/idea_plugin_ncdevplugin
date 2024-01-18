package com.air.nc5dev.acion;

import com.air.nc5dev.acion.base.AbstractIdeaAction;
import com.air.nc5dev.ui.datadictionary.NCDataDictionaryDialog;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class NCDataDictionaryAction extends AbstractIdeaAction {
    @Override
    protected void doHandler(AnActionEvent e) {
        NCDataDictionaryDialog dialog = new NCDataDictionaryDialog(e.getProject());
        dialog.setModal(false);
        dialog.show();
    }
}
