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
package com.air.nc5dev.acion;

import com.air.nc5dev.acion.base.AbstractIdeaAction;
import com.air.nc5dev.util.idea.LogUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

import javax.swing.Icon;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 按钮： 错误自动显示 日志窗口
 *
 * @Author 唐粟 Email: 209308343@qq.com
 * @Description
 * @Date 2020/11/8 11:06
 **/
public class LogErrAutoShowAction extends AbstractIdeaAction {
    private static volatile AtomicBoolean AUTO_FLAG = new AtomicBoolean(false);

    public LogErrAutoShowAction(String name, String desc, Icon icon) {
        super(name, desc, icon);
    }

    public LogErrAutoShowAction() {
        super();
    }

    @Override
    public void doHandler(AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        setAuto(!isAuto());
        LogUtil.info((isAuto() ? "已开启" : "已关闭") + " 插件日志窗口出现错误日志 自动显示.");
    }


    public static boolean isAuto() {
        return AUTO_FLAG.get();
    }

    public static boolean setAuto(boolean auto) {
        return LogErrAutoShowAction.AUTO_FLAG.getAndSet(auto);
    }
}
