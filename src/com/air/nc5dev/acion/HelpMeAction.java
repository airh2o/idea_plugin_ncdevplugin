package com.air.nc5dev.acion;

import com.air.nc5dev.util.ExceptionUtil;
import com.air.nc5dev.util.idea.ProjectUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

import java.awt.*;
import java.io.IOException;
import java.net.URI;

/**
 * 打开使用帮助（gitee网址）        </br>
 * </br>
 * </br>
 * </br>
 *
 * @author air Email: 209308343@qq.com
 * @date 2020/2/9 0009 14:38
 * @Param
 * @return
 */
public class HelpMeAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        final String url = "https://gitee.com/yhlx/idea_plugin_nc5devplugin";
        try {
            Desktop.getDesktop().browse(URI.create(url));
        } catch (IOException e1) {
            ProjectUtil.errorNotification("无法自动打开浏览器，请手工打开网址：" + url
                    + "  ,ERROR=" + ExceptionUtil.getExcptionDetall(e1), e.getProject());
        }
    }
}
