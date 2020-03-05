package com.air.nc5dev.acion;

import com.air.nc5dev.util.IdeaProjectGenerateUtil;
import com.air.nc5dev.util.ProjectNCConfigUtil;
import com.air.nc5dev.util.idea.ProjectUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * 生成默认的2个NC运行配置，一个服务端，一个客户端(如果已经有了会跳过)，生成默认文件夹和xml
 */
public class AddProjectNCRunConfig extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        if(!ProjectNCConfigUtil.getNCHome().exists()){
            ProjectUtil.warnNotification("未配置NC HOME 无法执行此操作！ ", e.getProject());
            return ;
        }

        IdeaProjectGenerateUtil.generateSrcDir(e.getProject());
        IdeaProjectGenerateUtil.generateRunMenu(e.getProject());
        ProjectUtil.infoNotification("操作完成", e.getProject());
    }
}
