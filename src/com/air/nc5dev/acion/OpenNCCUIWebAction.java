package com.air.nc5dev.acion;

import com.air.nc5dev.acion.base.AbstractIdeaAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class OpenNCCUIWebAction extends AbstractIdeaAction {
    @Override
    protected void doHandler(AnActionEvent e) throws IOException, URISyntaxException {
        Desktop desktop = Desktop.getDesktop();
        desktop.browse(new URI("https://yondesign.yonyou.com/homepage/#/"));
    }
}
