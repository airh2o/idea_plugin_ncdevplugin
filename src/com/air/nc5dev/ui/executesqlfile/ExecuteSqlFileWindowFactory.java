package com.air.nc5dev.ui.executesqlfile;

import com.air.nc5dev.ui.ToolWindowFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;

public class ExecuteSqlFileWindowFactory extends ToolWindowFactory {
    @Override
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        ExecuteSqlFileDialog view = new ExecuteSqlFileDialog(project);
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(view.getContentPane(), "执行SQL脚本", true);
        toolWindow.getContentManager().addContent(content);
    }
}