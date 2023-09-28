package com.air.nc5dev.acion;

import com.air.nc5dev.acion.base.AbstractIdeaAction;
import com.air.nc5dev.ui.SearchNCRequestMappingDialog;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class SearchNCRequestMappingAction extends AbstractIdeaAction {
    @Override
    protected void doHandler(AnActionEvent e) {
        SearchNCRequestMappingDialog dialog = new SearchNCRequestMappingDialog(e.getProject());
        dialog.setModal(false);
        dialog.show();
    }
}
