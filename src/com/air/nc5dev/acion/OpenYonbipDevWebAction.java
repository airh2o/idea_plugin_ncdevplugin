package com.air.nc5dev.acion;

import com.air.nc5dev.acion.base.AbstractIdeaAction;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;

import java.io.IOException;
import java.net.URISyntaxException;

public class OpenYonbipDevWebAction extends AbstractIdeaAction {
    @Override
    protected void doHandler(AnActionEvent e) throws IOException, URISyntaxException {
        String url = "https://developer.yonyou.com";
 /*       Desktop desktop = Desktop.getDesktop();
        desktop.browse(new URI(url));*/

        BrowserUtil.open(url);
    }
}
