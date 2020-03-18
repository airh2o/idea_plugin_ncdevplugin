package com.air.nc5dev.acion;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * 新增WSDL接口动作      </br>
 * </br>
 * </br>
 * </br>
 *
 * @author air Email: 209308343@qq.com
 * @date 2020/1/17 0017 19:47
 * @Param
 * @return
 */
public class NewWsdlFilesAction extends AbstractIdeaAction {
    private Project mProject;

    @Override
    protected void doHandler(AnActionEvent event) {
        mProject = event.getData(PlatformDataKeys.PROJECT);
        DataContext dataContext = event.getDataContext();
        VirtualFile selectFile = DataKeys.VIRTUAL_FILE.getData(dataContext);
    }
}
