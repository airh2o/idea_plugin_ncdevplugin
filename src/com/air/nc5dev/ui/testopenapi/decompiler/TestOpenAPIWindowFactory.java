package com.air.nc5dev.ui.testopenapi.decompiler;

import com.air.nc5dev.ui.ToolWindowFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;

public class TestOpenAPIWindowFactory extends ToolWindowFactory {
    @Override
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        TestOpenAPIDialog view = new TestOpenAPIDialog(project);
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(view.getContentPane(), "NC目录一键反编译代码(仅做学习测试用,请勿违法使用,本人不负任何责任!)", true);
        toolWindow.getContentManager().addContent(content);
    }
}