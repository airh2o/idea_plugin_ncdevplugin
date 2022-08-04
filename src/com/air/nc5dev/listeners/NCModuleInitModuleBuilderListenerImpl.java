package com.air.nc5dev.listeners;

import com.air.nc5dev.util.IdeaProjectGenerateUtil;
import com.air.nc5dev.util.ProjectNCConfigUtil;
import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.util.idea.ApplicationLibraryUtil;
import com.air.nc5dev.util.idea.ProjectUtil;
import com.intellij.ide.util.projectWizard.ModuleBuilderListener;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.ModuleListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import org.jetbrains.annotations.NotNull;

/**
 * 监听模块创建事件
 * </br>
 * </br>
 * </br>
 *
 * @author 唐粟 Email:209308343@qq.com
 * @date 2022/7/20 0020 17:11
 * @project
 * @Version
 */
public class NCModuleInitModuleBuilderListenerImpl implements ModuleListener {
    public static volatile NCModuleInitModuleBuilderListenerImpl me = new NCModuleInitModuleBuilderListenerImpl();

    public static NCModuleInitModuleBuilderListenerImpl getMe() {
        return me;
    }

    @Override
    public void moduleAdded(@NotNull Project project, @NotNull Module module) {
       /* ProjectUtil.setProject(project);
        ProjectNCConfigUtil.initConfigFile();
        if (StringUtil.isBlank(ProjectNCConfigUtil.getNCHomePath())) {
            //没有配置NC home！
            return;
        }*/

        onAdd(module);
    }

    private void onAdd(@NotNull Module module) {
        IdeaProjectGenerateUtil.generateSrcDir4Modules(module);

        ApplicationLibraryUtil.addLibs2Module(module);

        //设置模块 源文件结构 ModulesStructureConfigurable
        IdeaProjectGenerateUtil.setModuleStructureConfigurable(module);
    }
}
