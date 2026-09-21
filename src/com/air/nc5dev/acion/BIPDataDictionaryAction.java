package com.air.nc5dev.acion;

import com.air.nc5dev.acion.base.AbstractIdeaAction;
import com.air.nc5dev.ui.bipdatadictionary.BIPDataDictionaryDialog;
import com.air.nc5dev.ui.datadictionary.NCDataDictionaryDialog;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class BIPDataDictionaryAction extends AbstractIdeaAction {
    @Override
    protected void doHandler(AnActionEvent e) {
        BIPDataDictionaryDialog dialog = new BIPDataDictionaryDialog(e.getProject());
        dialog.setModal(false);
        dialog.show();
    }
}
