package com.air.nc5dev.util.subscribe.impl;

import com.air.nc5dev.acion.LogErrAutoShowAction;
import com.air.nc5dev.ui.ToolWindowFactory;
import com.air.nc5dev.util.subscribe.itf.ISubscriber;
import com.intellij.openapi.wm.ToolWindow;

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
public class LogWinAutoShowWhenErr implements ISubscriber {
    private ToolWindow toolWindow;

    public LogWinAutoShowWhenErr(ToolWindow toolWindow) {
        this.toolWindow = toolWindow;
    }

    @Override
    public void accept(Object msg) {
        if (msg instanceof Throwable && LogErrAutoShowAction.isAuto()) {
            toolWindow.setAvailable(true, null);
        }
    }

    public ToolWindow getToolWindow() {
        return toolWindow;
    }

    public void setToolWindow(ToolWindow toolWindow) {
        this.toolWindow = toolWindow;
    }
}
