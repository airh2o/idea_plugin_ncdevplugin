package com.air.nc5dev.acion;

import com.air.nc5dev.acion.base.AbstractIdeaAction;
import com.air.nc5dev.ui.PatcherDialog;
import com.air.nc5dev.ui.patcherselectfile.PathcerSelectFilesDialog;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class ShowPathcerSelectFilesListAction extends AbstractIdeaAction {
    @Override
    protected void doHandler(AnActionEvent e) {
        PathcerSelectFilesDialog dialog = new PathcerSelectFilesDialog(e.getProject());
        dialog.setModal(false);
        dialog.show();
    }


    @Override
    public void update(final @NotNull AnActionEvent e) {
        super.update(e);

        e.getPresentation().setEnabled(!PatcherDialog.getForceAddSelectFiles(e.getProject()).isEmpty());
    }
}
