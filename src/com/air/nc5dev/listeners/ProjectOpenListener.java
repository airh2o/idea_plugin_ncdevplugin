package com.air.nc5dev.listeners;

import com.air.nc5dev.acion.HelpMeAction;
import com.air.nc5dev.component.SubscribeEventAutoCopyNccClientFilesComponent;
import com.air.nc5dev.nccrequstsearch.RequestMappingItemProvider;
import com.air.nc5dev.service.ui.IMeassgeConsole;
import com.air.nc5dev.util.ExceptionUtil;
import com.air.nc5dev.util.IdeaProjectGenerateUtil;
import com.air.nc5dev.util.ProjectNCConfigUtil;
import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.util.idea.LogUtil;
import com.air.nc5dev.util.idea.ProjectUtil;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.ide.BrowserUtil;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

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
        ProjectUtil.setProject(project);
        ProjectNCConfigUtil.initConfigFile(project);
        ProjectUtil.notifyAndHide("(15秒后自动消失)欢迎使用IDEA-NC插件,有关插件使用信息,您可以参考 NC 开发插件配置" +
                        " 菜单里的 关于我(https://gitee.com/yhlx/idea_plugin_nc5devplugin    QQ209308343)." +
                        "\n当前项目配置的NCHOME路径:" + ProjectNCConfigUtil.getNCHomePath(), project
                , (int) TimeUnit.SECONDS.toMillis(15L));


        if (!"Y".equalsIgnoreCase(ProjectNCConfigUtil.getConfigValue(project, "what_new_showed"))) {
            try {
                BrowserUtil.browse(new URL(HelpMeAction.URL));
            } catch (Exception e) {
            }
            ProjectNCConfigUtil.setNCConfigPropertice("what_new_showed", "Y");
            ProjectNCConfigUtil.saveConfig2File(project);
        }

        try {
            ProjectUtil.setProject(project);
            ProjectNCConfigUtil.initConfigFile(project);
            if (StringUtil.isBlank(ProjectNCConfigUtil.getNCHomePath())) {
                //没有配置NC home！
                return;
            }

//            int re = Messages.showYesNoDialog("是否自动生成整个项目的结构和NC默认文件夹?"
//                    , "询问", Messages.getQuestionIcon());
//            if (re != Messages.OK) {
//                return;
//            }

            run(project);

            RequestMappingItemProvider.getMe().initScan(project);
        } catch (Throwable e) {
            //不要弹框报错！
            try {
                IMeassgeConsole service = ProjectUtil.getService(IMeassgeConsole.class, project);
                service.getConsoleView().print(ExceptionUtil.getExcptionDetall(e) + "\n",
                        ConsoleViewContentType.ERROR_OUTPUT);
            } catch (Throwable ex) {
            }
        }

    }

    public static void run(@NotNull Project project) {
        //自动生成NC几个默认文件夹！
        autoCreateNCSrcDirs(project);

        initEventListener(project);
    }

    /**
     * 初始化 事件分发监听
     *
     * @param project
     */
    public static void initEventListener(Project project) {
        new SubscribeEventAutoCopyNccClientFilesComponent().init(project);
    }

    public static void autoCreateNCSrcDirs(Project project) {
        if (!ProjectNCConfigUtil.hasSetNCHOME(project)) {
            return;
        }

        IdeaProjectGenerateUtil.generateSrcDir(project);
    }
}
