package com.air.nc5dev.ui;

import com.air.nc5dev.ui.SearchFullDataBaseDialog;
import com.air.nc5dev.ui.ToolWindowFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;

import javax.swing.*;

public class SearchFullDataBaseWindowFactory extends ToolWindowFactory {
    @Override
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        SearchFullDataBaseDialog view = new SearchFullDataBaseDialog(project);
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(view.getContentPane(), "数据库全局搜索", true);
        toolWindow.getContentManager().addContent(content);
    }
}