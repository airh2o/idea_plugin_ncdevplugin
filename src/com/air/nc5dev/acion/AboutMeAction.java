package com.air.nc5dev.acion;

import com.air.nc5dev.util.ProjectNCConfigUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;

public class AboutMeAction extends AbstractIdeaAction {

    @Override
    protected void doHandler(AnActionEvent e) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("作者： air Email 209308343@qq.com,QQ 209308343 \n");
        stringBuilder.append("开源项目地址,欢迎贡献代码：\n " );
        stringBuilder.append("github：https://github.com/airh2o/idea_plugin_nc5devplugin (推送地址)\n " );
        stringBuilder.append("gitee：https://gitee.com/yhlx/idea_plugin_nc5devplugin  (同步github)\n " );
        Messages.showInfoMessage(stringBuilder.toString(), "关于我(插件实例标识: "
                + ProjectNCConfigUtil.getPluginRuntimeMark() + "): ");
    }
}
