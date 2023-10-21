package com.air.nc5dev.acion.meta;

import com.air.nc5dev.acion.base.AbstractIdeaAction;
import com.air.nc5dev.ui.MakeBmfDialog;
import com.air.nc5dev.ui.exportbmf.ExportbmfDialog;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageUtil;
import com.intellij.openapi.ui.Messages;

import java.io.File;

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
