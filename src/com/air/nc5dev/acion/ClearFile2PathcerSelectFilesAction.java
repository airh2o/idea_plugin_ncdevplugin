package com.air.nc5dev.acion;

import com.air.nc5dev.ui.PatcherDialog;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class ClearFile2PathcerSelectFilesAction extends AddFile2PathcerSelectFilesAction {
    @Override
    protected void doHandler(AnActionEvent e) {
        PatcherDialog.clearForceAddSelectFile(e.getProject());
    }

    @Override
    public boolean showConform() {
        return true;
    }

    @Override
    public void update(final @NotNull AnActionEvent e) {
        super.update(e);

        e.getPresentation().setEnabled(!PatcherDialog.getForceAddSelectFiles(e.getProject()).isEmpty());
    }
}
