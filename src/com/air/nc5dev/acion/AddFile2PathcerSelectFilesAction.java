package com.air.nc5dev.acion;

import com.air.nc5dev.acion.base.AbstractIdeaAction;
import com.air.nc5dev.ui.PatcherDialog;
import com.air.nc5dev.util.CollUtil;
import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.vo.PatcherSelectFileVO;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JOptionPane;
import java.util.ArrayList;
import java.util.List;

public class AddFile2PathcerSelectFilesAction extends AbstractIdeaAction {
    @Override
    protected void doHandler(AnActionEvent e) {
        List<VirtualFile> vs = getSelectedFile(e);
        if (CollUtil.isNotEmpty(vs)) {
            String txt = StringUtil.get(JOptionPane.showInputDialog("请输入备注内容(取消视为空内容)"));

            for (VirtualFile v : vs) {
                PatcherDialog.addForceAddSelectFile(e.getProject()
                        , PatcherSelectFileVO.builder()
                                .file(v)
                                .path(v.getPath())
                                .memo(txt)
                                .build()
                );
            }
        }
    }

    @Nullable
    public List<VirtualFile> getSelectedFile(AnActionEvent e) {
        Project project = e.getProject();
        ArrayList<VirtualFile> fs = new ArrayList<>();
        final Editor selectedTextEditor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (selectedTextEditor != null) {
            VirtualFile selectedFile = FileDocumentManager.getInstance().getFile(selectedTextEditor.getDocument());
            if (selectedFile != null && !fs.contains(selectedFile)) {
                fs.add(selectedFile);
            }
        }

        // this is the preferred solution, but it doesn't respect the focus of split editors at present
        final VirtualFile[] selectedFiles = FileEditorManager.getInstance(project).getSelectedFiles();
        if (selectedFiles.length > 0) {
            for (VirtualFile selectedFile : selectedFiles) {
                if (selectedFile != null && !fs.contains(selectedFile)) {
                    fs.add(selectedFile);
                }
            }
        }

        VirtualFile[] vs = e.getDataContext().getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
        if (vs != null) {
            for (VirtualFile selectedFile : vs) {
                if (selectedFile != null && !fs.contains(selectedFile)) {
                    fs.add(selectedFile);
                }
            }
        }

        return fs;
    }

    @Override
    public void update(final @NotNull AnActionEvent e) {
        super.update(e);

        VirtualFile f = CollUtil.getFirst(getSelectedFile(e));

        if (f == null) {
            return ;
        }

        e.getPresentation().setEnabled(
                !PatcherDialog.containsForceAddSelectFile(e.getProject()
                        , PatcherSelectFileVO.builder()
                                .file(f)
                                .path(f.getPath())
                                .build()
                )
        );
    }
}
