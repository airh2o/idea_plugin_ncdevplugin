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
package com.air.nc5dev.util;

import com.air.nc5dev.acion.LogCleanConsoleAction;
import com.air.nc5dev.acion.LogErrAutoShowAction;
import com.air.nc5dev.acion.LogMoveDownAction;
import com.air.nc5dev.acion.LogMoveUpAction;
import com.air.nc5dev.util.idea.LogUtil;
import com.air.nc5dev.util.idea.ProjectUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;

/**
 * Creates and keeps a single instance of actions used by SonarLint.
 * Some actions are created programmatically instead of being declared in plugin.xml so that they are not registered in
 * ActionManager, becoming accessible from the action search.
 */
public class Actions {
    private final AnAction cleanConsoleAction;
    private final AnAction topLineAction;
    private final LogMoveDownAction tailLineAction;
    private final AnAction showWhenErrorAction;

    public Actions() {
        this(ActionManager.getInstance());
    }

    /**
     * TODO Replace @Deprecated with @NonInjectable when switching to 2019.3 API level
     *
     * @deprecated in 4.2 to silence a check in 2019.3
     */
    @Deprecated
    Actions(ActionManager actionManager) {
        cleanConsoleAction = new LogCleanConsoleAction("清空NC插件日志窗口内容",
                "清空NC插件日志窗口内容",
                AllIcons.Actions.Close);
        topLineAction = new LogMoveUpAction("首行",
                "跳转到首行",
                AllIcons.Actions.MoveUp);
        tailLineAction = new LogMoveDownAction("尾行",
                "跳转到尾行",
                AllIcons.Actions.MoveDown);
        showWhenErrorAction = new LogErrAutoShowAction("自动显示",
                "出现error日志自动显示窗口",
                AllIcons.Actions.Resume);
    }

    public static Actions getInstance() {
        try {
            return ProjectUtil.getService(Actions.class);
        } catch (Exception e) {
            LogUtil.error(e.toString(), e);
        }
        return null;
    }

    public AnAction cleanConsole() {
        return cleanConsoleAction;
    }

    public AnAction getTopLineAction() {
        return topLineAction;
    }

    public LogMoveDownAction getTailLineAction() {
        return tailLineAction;
    }
    public AnAction getShowWhenErrorAction() {
        return showWhenErrorAction;
    }
}
