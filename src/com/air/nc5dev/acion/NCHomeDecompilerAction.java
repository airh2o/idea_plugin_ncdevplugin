package com.air.nc5dev.acion;

import com.air.nc5dev.acion.base.AbstractIdeaAction;
import com.air.nc5dev.ui.datadictionary.NCDataDictionaryDialog;
import com.air.nc5dev.ui.decompiler.NCDecompilerDialog;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class NCHomeDecompilerAction extends AbstractIdeaAction {
    @Override
    protected void doHandler(AnActionEvent e) {
        NCDecompilerDialog dialog = new NCDecompilerDialog(e.getProject());
        dialog.setModal(false);
        dialog.show();
    }
}
