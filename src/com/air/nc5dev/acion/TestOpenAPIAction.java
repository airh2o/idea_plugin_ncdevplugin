package com.air.nc5dev.acion;

import com.air.nc5dev.acion.base.AbstractIdeaAction;
import com.air.nc5dev.ui.decompiler.NCDecompilerDialog;
import com.air.nc5dev.ui.testopenapi.decompiler.TestOpenAPIDialog;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class TestOpenAPIAction extends AbstractIdeaAction {
    @Override
    protected void doHandler(AnActionEvent e) {
        TestOpenAPIDialog dialog = new TestOpenAPIDialog(e.getProject());
        dialog.setModal(false);
        dialog.show();
    }
}
