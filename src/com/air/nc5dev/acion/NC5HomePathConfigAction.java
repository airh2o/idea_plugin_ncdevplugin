package com.air.nc5dev.acion;

import com.air.nc5dev.ui.NC5HomeConfigDialogUI;
import com.air.nc5dev.util.IdeaProjectGenerateUtil;
import com.intellij.execution.ui.RunContentManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.Messages;

public class NC5HomePathConfigAction extends AnAction {
    public static NC5HomeConfigDialogUI ui = null;
    static{
        NCRunManagerListener nCRunManagerListener = new NCRunManagerListener();
        ProjectManager.getInstance().getDefaultProject().getMessageBus().connect().subscribe(RunContentManager.TOPIC , nCRunManagerListener);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        if (null != ui && ui.isShowing()) {
            ui.dispose();
            ui = null;
        }
        ui = new NC5HomeConfigDialogUI(project);
        ui.setSize(700, 490);
        if (ui.showAndGet()) {
            ui.getCenterPanel().getNc5HomeConfigDialogUIListner().OnSave(null);

            int updateClassPath = Messages.showYesNoDialog("是否立即更新项目 Dependencies 中NC包依赖?", "询问", Messages.getQuestionIcon());
            if (updateClassPath == Messages.OK) {
                IdeaProjectGenerateUtil.updateApplicationNCLibrarys(e.getProject());
            }
        }
    }
}
