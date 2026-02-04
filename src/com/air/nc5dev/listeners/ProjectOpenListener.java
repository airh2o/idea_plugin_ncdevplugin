package com.air.nc5dev.listeners;

import com.air.nc5dev.acion.ExportChangeCodeFilesAction;
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
import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.melloware.jintellitype.JIntellitype;
import org.jetbrains.annotations.NotNull;

import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * 项目打开
 * <br>
 * <br>
 * <br>
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
                //  BrowserUtil.browse(new URL(HelpMeAction.URL));
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

            try {
                run(project);
            } catch (Throwable e) {
                LogUtil.error(e.getMessage(), e);
            }

            try {
                RequestMappingItemProvider.getMe().initScan(project);
            } catch (Throwable e) {
                LogUtil.error(e.getMessage(), e);
            }

            try {
                ExportChangeCodeFilesAction.relaod(project, (indicator) -> {
                    ProjectView.getInstance(project).refresh();
                });
            } catch (Throwable e) {
                // LogUtil.error(e.getMessage(), e);
            }


            try {
                registerGlobalHotKey();
            } catch (Throwable e) {
                LogUtil.error(e.getMessage(), e);
            }
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

    public void registerGlobalHotKey() {
        // 注册全局热键 Ctrl+Alt+A
        registerHotkey(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK | KeyEvent.ALT_DOWN_MASK);
    }

    /**
     * ©著作权归作者所有：来自51CTO博客作者mob64ca12f831ae的原创作品，请联系作者获取转载授权，否则将追究法律责任
     * java注册全局热键
     * https://blog.51cto.com/u_16213464/7497771
     *
     * @param keyCode
     * @param modifiers
     */
    public static void registerHotkey(int keyCode, int modifiers) {
        try {
            final int KEY_F3 = 10086;
            final int KEY_F4 = 10087;
            JIntellitype.getInstance().registerHotKey(KEY_F3, "F3");
            JIntellitype.getInstance().registerHotKey(KEY_F4, "F4");

            JIntellitype.getInstance().addHotKeyListener(identifier -> {
                System.out.println("JIntellitype-identifier=" + identifier);

                if (identifier == KEY_F3) {
                    try {
                        Process p = Runtime.getRuntime().exec(StringUtil.splitTrim(ProjectNCConfigUtil.getConfigValue("GLOBAL_HOTKEY_F3_RUN")
                                , " ").toArray(new String[0]));
                        Scanner scanner = new Scanner(p.getInputStream());
                        while (scanner.hasNextLine()) {
                            System.out.println(scanner.nextLine());
                        }

                        p.getInputStream().close();
                        p.destroy();
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                } else if (identifier == KEY_F4) {
                    try {
                        Process p = Runtime.getRuntime().exec(StringUtil.splitTrim(ProjectNCConfigUtil.getConfigValue("GLOBAL_HOTKEY_F4_RUN")
                                , " ").toArray(new String[0]));
                        Scanner scanner = new Scanner(p.getInputStream());
                        while (scanner.hasNextLine()) {
                            System.out.println(scanner.nextLine());
                        }

                        p.getInputStream().close();
                        p.destroy();
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }

            });
            JIntellitype.getInstance().addIntellitypeListener(command -> {
                System.out.println("JIntellitype-command=" + command);
            });
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

}
