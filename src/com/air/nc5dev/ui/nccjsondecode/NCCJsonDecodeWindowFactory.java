package com.air.nc5dev.ui.nccjsondecode;

import com.air.nc5dev.ui.ToolWindowFactory;
import com.air.nc5dev.ui.searchdatabasefull.SearchFullDataBaseDialog;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;

public class NCCJsonDecodeWindowFactory extends ToolWindowFactory {
    @Override
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        NCCJsonDecodeDialog view = new NCCJsonDecodeDialog(project);
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(view.getContentPane(), "NCC(BIP)Action请求参数解密&加密", true);
        toolWindow.getContentManager().addContent(content);
    }
}