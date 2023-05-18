package com.air.nc5dev.acion;

import com.air.nc5dev.acion.base.AbstractIdeaAction;
import com.air.nc5dev.ui.PatcherDialog;
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

public class RemoveFile2PathcerSelectFilesAction extends AbstractIdeaAction {


    @Override
    protected void doHandler(AnActionEvent e) {
        VirtualFile f = getSelectedFile(e.getProject());

        PatcherDialog.FORCEADDSELECTFILES.putIfAbsent(e.getProject(), new CopyOnWriteArrayList<>());
        List<VirtualFile> vs = PatcherDialog.FORCEADDSELECTFILES.get(e.getProject());

        vs.remove(f);
    }

    @Nullable
    private VirtualFile getSelectedFile(final Project project) {
        VirtualFile selectedFile = null;
        final Editor selectedTextEditor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (selectedTextEditor != null) {
            selectedFile = FileDocumentManager.getInstance().getFile(selectedTextEditor.getDocument());
        }

        if (selectedFile == null) {
            // this is the preferred solution, but it doesn't respect the focus of split editors at present
            final VirtualFile[] selectedFiles = FileEditorManager.getInstance(project).getSelectedFiles();
            if (selectedFiles.length > 0) {
                selectedFile = selectedFiles[0];
            }
        }

        return selectedFile;
    }

    @Override
    public void update(final @NotNull AnActionEvent e) {
        super.update(e);

        VirtualFile f = getSelectedFile(e.getProject());

        PatcherDialog.FORCEADDSELECTFILES.putIfAbsent(e.getProject(), new CopyOnWriteArrayList<>());
        List<VirtualFile> vs = PatcherDialog.FORCEADDSELECTFILES.get(e.getProject());

        e.getPresentation().setEnabled(vs.contains(f));
    }
}
