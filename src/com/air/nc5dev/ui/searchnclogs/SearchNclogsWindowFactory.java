package com.air.nc5dev.ui.searchnclogs;

import com.air.nc5dev.ui.ToolWindowFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;

public class SearchNclogsWindowFactory extends ToolWindowFactory {
    @Override
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        SearchNclogsDialog view = new SearchNclogsDialog(project);
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(view.getContentPane(), "执行SQL脚本", true);
        toolWindow.getContentManager().addContent(content);
    }
}