/*
 * SonarLint for IntelliJ IDEA
 * Copyright (C) 2015-2020 SonarSource
 * sonarlint@sonarsource.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package com.air.nc5dev.ui;

import com.air.nc5dev.service.ui.impl.MeassgeConsoleImpl;
import com.air.nc5dev.util.idea.LogUtil;
import com.air.nc5dev.util.subscribe.PubSubUtil;
import com.air.nc5dev.util.subscribe.impl.LogWinAutoShowWhenErr;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowType;
import com.intellij.ui.content.Content;

/**
 * Factory of SonarLint tool window.
 * Nothing can be injected as it runs in the root pico container.
 */
public class ToolWindowFactory implements com.intellij.openapi.wm.ToolWindowFactory {
    public static final String TOOL_WINDOW_ID = "NC插件日志";
    public static final String TAB_LOGS = "Log";
    public static LogPanel logPanel;

    @Override
    public void createToolWindowContent(Project project, final ToolWindow toolWindow) {
        addLogTab(project, toolWindow);
        toolWindow.setType(ToolWindowType.DOCKED, null);
        toolWindow.setTitle("(QQ 209308343 有业务可联系 感谢支持)");
        try {
            toolWindow.setIcon(AllIcons.General.Information);
        } catch (Throwable e) {
        }
        toolWindow.setToHideOnEmptyContent(true);
        toolWindow.setStripeTitle(TOOL_WINDOW_ID);

        PubSubUtil.subscribe(MeassgeConsoleImpl.KEY, new LogWinAutoShowWhenErr(toolWindow));
    }

    private static void addLogTab(Project project, ToolWindow toolWindow) {
        try {
            logPanel = new LogPanel(toolWindow, project);
            Content logContent = toolWindow.getContentManager().getFactory().createContent(logPanel, TAB_LOGS, false);
            toolWindow.getContentManager().addContent(logContent);
        } catch (Throwable e) {
            LogUtil.error(e.toString(), e);
        }
    }

    @Override
    public boolean isDoNotActivateOnStart() {
        return false;
    }
}
