package com.air.nc5dev.util.subscribe.impl;

import com.air.nc5dev.acion.LogMoveDownAction;
import com.air.nc5dev.service.ui.IMeassgeConsole;
import com.air.nc5dev.util.idea.ProjectUtil;
import com.air.nc5dev.util.subscribe.itf.ISubscriber;
import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleView;

/**
 * <br/>
 * <br/>
 * <br/>
 *
 * @author 唐粟 Email:209308343@qq.com
 * @date 2020/11/8 11:16
 * @project
 * @Version
 */
public class LogWinAutoTail implements ISubscriber {
    LogMoveDownAction action;

    public LogWinAutoTail(LogMoveDownAction tailLineAction) {
        action = tailLineAction;
    }

    @Override
    public void accept(Object msg) {
        if (msg instanceof Throwable && LogMoveDownAction.isAuto()) {
            ConsoleView consoleView = ProjectUtil.getService(IMeassgeConsole.class, ProjectUtil.getDefaultProject()).getConsoleView();
            ConsoleViewImpl c = (ConsoleViewImpl) consoleView;
            c.scrollToEnd();
        }
    }

}
