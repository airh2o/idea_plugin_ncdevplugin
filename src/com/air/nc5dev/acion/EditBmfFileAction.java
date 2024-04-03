package com.air.nc5dev.acion;

import com.air.nc5dev.acion.base.AbstractIdeaAction;
import com.air.nc5dev.editor.bmf.languageinfo.BmfMDPFileType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * 编辑元数据      <br>
 * <br>
 * <br>
 * <br>
 *
 * @author air Email: 209308343@qq.com
 * @date 2020/1/17 0017 19:47
 * @Param
 * @return
 */
public class EditBmfFileAction extends AbstractIdeaAction {
    private Project mProject;
    @Override
    protected void doHandler(AnActionEvent event) {
        mProject = event.getData(PlatformDataKeys.PROJECT);
        DataContext dataContext = event.getDataContext();
        if (BmfMDPFileType.INSTANCE.getDefaultExtension().equals(getFileExtension(dataContext))) {
            //根据扩展名判定是否进行下面的处理
            //获取选中的文件
            VirtualFile selectFile = DataKeys.VIRTUAL_FILE.getData(event.getDataContext());
            if (selectFile == null) {
                Messages.showErrorDialog(event.getProject(), "请选中要编辑的元数据bmf文件后右键编辑元数据!", "Error");
                return ;
            }
        }
    }

    public void aaa(AnActionEvent event) {
        //根据扩展名是否是bmf，显示隐藏此Action,无用
        String extension = getFileExtension(event.getDataContext());
        this.getTemplatePresentation().setVisible(extension != null
                && BmfMDPFileType.INSTANCE.getDefaultExtension().equals(extension));
    }

    public static String getFileExtension(DataContext dataContext) {
        VirtualFile file = DataKeys.VIRTUAL_FILE.getData(dataContext);
        return file == null ? null : file.getExtension();
    }

}
