package com.air.nc5dev.acion;

import com.air.nc5dev.acion.base.AbstractIdeaAction;
import com.air.nc5dev.util.IdeaProjectGenerateUtil;
import com.air.nc5dev.util.ProjectNCConfigUtil;
import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.util.idea.ProjectUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

/**
 *
 */
public class ReConfigProjectAction extends AbstractIdeaAction {

    @Override
    protected void doHandler(AnActionEvent e) {
        Project project = e.getProject();
        ProjectUtil.setProject(project);
        ProjectNCConfigUtil.initConfigFile(project);
        if (StringUtil.isBlank(ProjectNCConfigUtil.getNCHomePath())) {
            //没有配置NC home！
            return;
        }

        int re = Messages.showYesNoDialog("是否自动生成整个项目的结构和NC默认文件夹?"
                , "询问", Messages.getQuestionIcon());
        if (re != Messages.OK) {
            return;
        }

        Module[] modules = IdeaProjectGenerateUtil.getProjectModules(project);
        for (Module module1 : modules) {
            ReConfigModuleAction.setModuel(module1);
        }
    }
}
