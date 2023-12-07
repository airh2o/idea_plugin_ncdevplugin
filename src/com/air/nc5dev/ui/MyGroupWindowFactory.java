package com.air.nc5dev.ui;

import com.air.nc5dev.ui.actionurlsearch.NCCActionURLSearchWindowFactory;
import com.air.nc5dev.ui.datadictionary.NCDataDictionaryWindowFactory;
import com.air.nc5dev.ui.nccjsondecode.NCCJsonDecodeWindowFactory;
import com.air.nc5dev.ui.searchdatabasefull.SearchFullDataBaseWindowFactory;
import com.air.nc5dev.util.idea.ProjectUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;

public class MyGroupWindowFactory extends ToolWindowFactory {
    @Override
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        ProjectUtil.setProject(project);

        new MstscManggerWindowFactory().createToolWindowContent(project, toolWindow);

        new SearchFullDataBaseWindowFactory().createToolWindowContent(project, toolWindow);

        new NCCActionURLSearchWindowFactory().createToolWindowContent(project, toolWindow);

        new NCCJsonDecodeWindowFactory().createToolWindowContent(project, toolWindow);

        new ToolWindowFactory().createToolWindowContent(project, toolWindow);

        new NCDataDictionaryWindowFactory().createToolWindowContent(project, toolWindow);
    }
}