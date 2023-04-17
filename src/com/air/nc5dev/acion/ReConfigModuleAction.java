package com.air.nc5dev.acion;

import com.air.nc5dev.acion.base.AbstractIdeaAction;
import com.air.nc5dev.util.IdeaProjectGenerateUtil;
import com.air.nc5dev.util.ProjectNCConfigUtil;
import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.util.idea.ApplicationLibraryUtil;
import com.air.nc5dev.util.idea.ProjectUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

/**
 *
 */
public class ReConfigModuleAction extends AbstractIdeaAction {

    @Override
    protected void doHandler(AnActionEvent e) {
        Project project = e.getProject();
        ProjectUtil.setProject(project);
        ProjectNCConfigUtil.initConfigFile(project);
        if (StringUtil.isBlank(ProjectNCConfigUtil.getNCHomePath())) {
            //没有配置NC home！
            return;
        }

        Module module = LangDataKeys.MODULE.getData(e.getDataContext());

        if (module == null) {
            int updateClassPath = Messages.showYesNoDialog(String.format("是否确定重新部署项目所有模块?%s", project.getBasePath())
                    , "询问", Messages.getQuestionIcon());
            if (updateClassPath != Messages.OK) {
                return;
            }

            Module[] modules = ModuleManager.getInstance(project).getModules();
            for (Module module1 : modules) {
                setModuel(module1);
            }
        } else {
            int updateClassPath = Messages.showYesNoDialog(String.format("是否确定重新部署模块%s?", module.getModuleFilePath())
                    , "询问", Messages.getQuestionIcon());
            if (updateClassPath != Messages.OK) {
                return;
            }
            setModuel(module);
        }
    }

    private void setModuel(Module module) {
        IdeaProjectGenerateUtil.generateSrcDir4Modules(module);
        ApplicationLibraryUtil.addLibs2Module(module);
        //设置模块 源文件结构 ModulesStructureConfigurable
        IdeaProjectGenerateUtil.setModuleStructureConfigurable(module);
    }
}
