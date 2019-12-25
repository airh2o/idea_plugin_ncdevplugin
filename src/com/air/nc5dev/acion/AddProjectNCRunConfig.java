package com.air.nc5dev.acion;

import com.air.nc5dev.util.IdeaProjectGenerateUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;

/**
 * 生成默认的2个NC运行配置，一个服务端，一个客户端(如果已经有了会跳过)，生成默认文件夹和xml
 */
public class AddProjectNCRunConfig extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        IdeaProjectGenerateUtil.generateSrcDir(e.getProject());
        IdeaProjectGenerateUtil.generateRunMenu(e.getProject());
        Messages.showMessageDialog("操作完成", "提示", Messages.getInformationIcon());
    }
}
