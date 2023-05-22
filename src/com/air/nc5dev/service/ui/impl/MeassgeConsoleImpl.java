package com.air.nc5dev.service.ui.impl;

import com.air.nc5dev.service.ui.IMeassgeConsole;
import com.air.nc5dev.util.idea.ProjectUtil;
import com.air.nc5dev.util.subscribe.PubSubUtil;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 默认的消息console实现 <br/>
 * <br/>
 * <br/>
 *
 * @author 唐粟 Email:209308343@qq.com
 * @date 2020/11/8 8:47
 * @project
 * @Version
 */
public class MeassgeConsoleImpl implements IMeassgeConsole, Disposable {
    public static final String KEY = "ERROR_LOG";
    private ConsoleView consoleView;
    private final Project myProject;

    public MeassgeConsoleImpl(Project project) {
        this.myProject = project;
    }

    @Override
    public void debug(String msg) {
        if (debugEnabled()) {
            getConsoleView().print(msg + "\n", ConsoleViewContentType.NORMAL_OUTPUT);
            ProjectUtil.infoNotification(msg, ProjectUtil.getProject());
        }
    }

    @Override
    public boolean debugEnabled() {
        return true;
    }

    @Override
    public void info(String msg) {
        getConsoleView().print(msg + "\n", ConsoleViewContentType.NORMAL_OUTPUT);
        ProjectUtil.infoNotification(msg, ProjectUtil.getProject());
    }
    @Override
    public void infoAndHide(String msg) {
        getConsoleView().print(msg + "\n", ConsoleViewContentType.NORMAL_OUTPUT);
        ProjectUtil.notifyAndHide(msg, ProjectUtil.getProject());
    }

    @Override
    public void error(String msg) {
        getConsoleView().print(msg + "\n", ConsoleViewContentType.ERROR_OUTPUT);
        PubSubUtil.publishAsync(KEY, new RuntimeException());
        ProjectUtil.errorNotification(msg, ProjectUtil.getProject());
    }

    @Override
    public void error(String msg, Throwable t, boolean showDig) {
        t = null == t ? new RuntimeException() : t;

        if (showDig) {
            error(msg);
            StringWriter errors = new StringWriter();
            t.printStackTrace(new PrintWriter(errors));
            error(errors.toString());
        }else{
            getConsoleView().print(msg + "\n", ConsoleViewContentType.ERROR_OUTPUT);
            StringWriter errors = new StringWriter();
            t.printStackTrace(new PrintWriter(errors));
            getConsoleView().print(errors.toString() + "\n", ConsoleViewContentType.ERROR_OUTPUT);
        }

        PubSubUtil.publishAsync(KEY, t);
    }

    @Override
    public void clear() {
        if (consoleView != null) {
            getConsoleView().clear();
        }
    }

    @Override
    public ConsoleView getConsoleView() {
        if (consoleView == null) {
            consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(myProject).getConsole();
        }
        return this.consoleView;
    }

    @Override
    public void dispose() {
        if (consoleView != null) {
            Disposer.dispose(consoleView);
        }
    }
}
