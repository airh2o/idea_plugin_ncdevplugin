package com.air.nc5dev.listeners;

import com.air.nc5dev.component.SubscribeEventAutoCopyNccClientFilesComponent;
import com.air.nc5dev.service.ui.IMeassgeConsole;
import com.air.nc5dev.util.ExceptionUtil;
import com.air.nc5dev.util.IdeaProjectGenerateUtil;
import com.air.nc5dev.util.ProjectNCConfigUtil;
import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.util.idea.ProjectUtil;
import com.intellij.execution.ui.ConsoleViewContentType;
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
        try {
            ProjectUtil.setProject(project);
            ProjectNCConfigUtil.initConfigFile(project);
            if (StringUtil.isBlank(ProjectNCConfigUtil.getNCHomePath())) {
                //没有配置NC home！
                return;
            }

            run(project);
        } catch (Throwable e) {
            //不要弹框报错！
            try {
                IMeassgeConsole service = ProjectUtil.getService(IMeassgeConsole.class, project);
                service.getConsoleView().print(ExceptionUtil.getExcptionDetall(e) + "\n", ConsoleViewContentType.ERROR_OUTPUT);
            } catch (Throwable ex) {
            }
        }
    }

    private void run(@NotNull Project project) {
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
