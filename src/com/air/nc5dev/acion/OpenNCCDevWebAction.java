package com.air.nc5dev.acion;

import com.air.nc5dev.acion.base.AbstractIdeaAction;
import com.air.nc5dev.util.ProjectNCConfigUtil;
import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.util.idea.LogUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class OpenNCCDevWebAction extends AbstractIdeaAction {
    @Override
    protected void doHandler(AnActionEvent e) throws IOException, URISyntaxException {
        Desktop desktop = Desktop.getDesktop();
        desktop.browse(new URI("https://nccdev.yonyou.com/"));
    }
}
