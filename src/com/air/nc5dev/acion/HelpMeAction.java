package com.air.nc5dev.acion;

import com.air.nc5dev.acion.base.AbstractIdeaAction;
import com.air.nc5dev.util.idea.LogUtil;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;

import java.awt.Desktop;
import java.net.URI;
import java.net.URL;

/**
 * 打开使用帮助（gitee网址）        <br>
 * <br>
 * <br>
 * <br>
 *
 * @author air Email: 209308343@qq.com
 * @date 2020/2/9 0009 14:38
 * @Param
 * @return
 */
public class HelpMeAction extends AbstractIdeaAction {
    public static String URL = "https://gitee.com/yhlx/idea_plugin_nc5devplugin";

    @Override
    protected void doHandler(AnActionEvent e) {
        try {
            BrowserUtil.browse(new URL(HelpMeAction.URL));
        } catch (Exception exception) {
            try {
                Desktop.getDesktop().browse(URI.create(URL));
            } catch (Exception e1) {
                LogUtil.error("无法自动打开浏览器，请手工打开网址：" + URL, e1);
            }
        }
    }
}
