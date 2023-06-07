package com.air.nc5dev.ui;

import com.air.nc5dev.ui.MstscDialog;
import com.air.nc5dev.ui.SearchFullDataBaseDialog;
import com.air.nc5dev.ui.ToolWindowFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;

public class MstscManggerWindowFactory extends ToolWindowFactory {
    @Override
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        MstscDialog view = new MstscDialog(project);
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(view.getContentPane(), "MSTSC远程管理", true);
        toolWindow.getContentManager().addContent(content);
    }
}