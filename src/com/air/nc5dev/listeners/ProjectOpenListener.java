package com.air.nc5dev.listeners;

import com.air.nc5dev.component.SubscribeEventAutoCopyNccClientFilesComponent;
import com.air.nc5dev.util.IdeaProjectGenerateUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

/**
 * 项目打开
 * </br>
 * </br>
 * </br>
 *
 * @author 唐粟 Email:209308343@qq.com
 * @date 2022/7/20 0020 16:36
 * @project
 * @Version
 */
public class ProjectOpenListener implements StartupActivity.DumbAware {
    @Override
    public void runActivity(@NotNull Project project) {
        //自动生成NC几个默认文件夹！
        autoCreateNCSrcDirs(project);

        initEventListener(project);
    }

    /**
     * 初始化 事件分发监听
     *
     * @param project
     */
    private void initEventListener(Project project) {
        new SubscribeEventAutoCopyNccClientFilesComponent().init(project);
    }

    private void autoCreateNCSrcDirs(Project project) {
        IdeaProjectGenerateUtil.generateSrcDir(project);
    }
}
