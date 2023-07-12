package com.air.nc5dev.acion.meta;

import com.air.nc5dev.acion.base.AbstractIdeaAction;
import com.air.nc5dev.ui.MakeBmfDialog;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageUtil;
import com.intellij.openapi.ui.Messages;

import java.io.File;

/**
 *
 */
public class TakeBmfFromDatabaseAction extends AbstractIdeaAction {

    @Override
    protected void doHandler(AnActionEvent e) {
        Project project = e.getProject();
        Module module = LangDataKeys.MODULE.getData(e.getDataContext());

        Messages.showInfoMessage(
                "功能暂未实现，如果您有工具类实现" +
                        "，可以发给我 我整理到插件里面！", "QQ209308343:");

    }

}
