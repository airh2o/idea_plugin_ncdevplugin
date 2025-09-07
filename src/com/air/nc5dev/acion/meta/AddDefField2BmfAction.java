package com.air.nc5dev.acion.meta;

import com.air.nc5dev.acion.AddFile2PathcerSelectFilesAction;
import com.air.nc5dev.ui.adddeffiled.AddDefField2BmfDialog;
import com.air.nc5dev.util.idea.LogUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

/**
 * 元数据文件快速 新增自定义项
 */
public class AddDefField2BmfAction extends AddFile2PathcerSelectFilesAction {

    @Override
    protected void doHandler(AnActionEvent e) {
        VirtualFile f = getSelectedFileTree(e);

        if (f == null || !f.getName().toLowerCase().endsWith(".bmf")) {
            LogUtil.infoAndHide("请选中左侧项目树中的bmf文件点击右键!");
            return;
        }

        AddDefField2BmfDialog dialog = new AddDefField2BmfDialog(e.getProject(), f);
        dialog.setModal(false);
        dialog.show();
    }

    @Override
    public void update(final @NotNull AnActionEvent e) {
        super.update(e);

       // VirtualFile f = getSelectedFileTree(e);

      //  e.getPresentation().setEnabled(f != null && f.getName().toLowerCase().endsWith(".bmf"));
    }

    public VirtualFile getSelectedFileTree(AnActionEvent e) {
        return e.getDataContext().getData(CommonDataKeys.VIRTUAL_FILE);
    }
}