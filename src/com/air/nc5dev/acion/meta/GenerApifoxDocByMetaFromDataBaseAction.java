package com.air.nc5dev.acion.meta;

import com.air.nc5dev.acion.base.AbstractIdeaAction;
import com.air.nc5dev.ui.exportapifoxdoc.ExportbmfDialog ;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 *
 */
public class GenerApifoxDocByMetaFromDataBaseAction extends AbstractIdeaAction {

    @Override
    protected void doHandler(AnActionEvent e) {
        ExportbmfDialog dialog = new ExportbmfDialog(e.getProject());
        dialog.setModal(false);
        dialog.show();
    }

}
