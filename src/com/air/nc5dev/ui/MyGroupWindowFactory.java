package com.air.nc5dev.ui;

import com.air.nc5dev.ui.actionurlsearch.NCCActionURLSearchView;
import com.air.nc5dev.ui.actionurlsearch.NCCActionURLSearchWindowFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;

public class MyGroupWindowFactory extends ToolWindowFactory {
    @Override
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        new MstscManggerWindowFactory().createToolWindowContent(project, toolWindow);

        new SearchFullDataBaseWindowFactory().createToolWindowContent(project, toolWindow);

        new NCCActionURLSearchWindowFactory().createToolWindowContent(project, toolWindow);

        new ToolWindowFactory().createToolWindowContent(project, toolWindow);
    }
}