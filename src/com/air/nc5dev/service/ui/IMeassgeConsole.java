package com.air.nc5dev.service.ui;

import com.air.nc5dev.util.idea.ProjectUtil;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * 插件的 日志消息console
 *
 * @Author 唐粟 Email: 209308343@qq.com
 * @Description
 * @Date 2020/11/8 8:46
 **/
public interface IMeassgeConsole {
    public static IMeassgeConsole get(@NotNull Project p) {
        return ProjectUtil.getService(p, IMeassgeConsole.class);
    }

    public void debug(String msg);

    public boolean debugEnabled();

    public void info(String msg);

    public void infoAndHide(String msg);

    public void error(String msg);

    public void error(String msg, Throwable t, boolean showDig);

    public void clear();

    public ConsoleView getConsoleView();
}
