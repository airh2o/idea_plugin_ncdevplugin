package com.air.nc5dev.acion;

import com.air.nc5dev.acion.base.AbstractIdeaAction;
import com.air.nc5dev.ui.PatcherDialog;
import com.air.nc5dev.util.CollUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class RemoveFile2PathcerSelectFilesAction extends AddFile2PathcerSelectFilesAction {
    @Override
    protected void doHandler(AnActionEvent e) {
        List<VirtualFile> vs = getSelectedFile(e);
        if (CollUtil.isNotEmpty(vs)) {
            for (VirtualFile v : vs) {
                PatcherDialog.removeForceAddSelectFile(e.getProject(), v);
            }
        }
    }

    @Override
    public boolean showConform() {
        return true;
    }

    @Override
    public void update(final @NotNull AnActionEvent e) {
        super.update(e);

        VirtualFile f = CollUtil.getFirst(getSelectedFile(e));

        e.getPresentation().setEnabled(PatcherDialog.containsForceAddSelectFile(e.getProject(), f));
    }
}
