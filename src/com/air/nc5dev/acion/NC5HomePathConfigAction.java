package com.air.nc5dev.acion;

import com.air.nc5dev.ui.NC5HomeConfigDialogUI;
import com.air.nc5dev.util.IdeaProjectGenerateUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

/***
 *  点击  配置NC HOME  的action       </br>
 *           </br>
 *           </br>
 *           </br>
 * @author air Email: 209308343@qq.com
 * @date 2019/12/25 0025 14:41
 * @Param
 * @return
 */
public class NC5HomePathConfigAction extends AbstractIdeaAction {
    public static NC5HomeConfigDialogUI ui = null;

    @Override
    protected void doHandler(AnActionEvent e) {
        Project project = e.getProject();
        if (null != ui && ui.isShowing()) {
            ui.dispose();
            ui = null;
        }
        ui = new NC5HomeConfigDialogUI(project);
        ui.setSize(700, 490);
        if (ui.showAndGet()) {
          /*  int updateClassPath = Messages.showYesNoDialog("你是否确定保存配置?"
                +"请提前备份好NCHOME/ierp/bin/prop.xml文件!", "询问", Messages.getQuestionIcon());
            if (updateClassPath != Messages.OK) {
                return ;
            }
*/
            ui.getCenterPanel().getNc5HomeConfigDialogUIListner().OnSave(null);

            int updateClassPath = Messages.showYesNoDialog("是否立即更新项目 Dependencies 中NC包依赖?"
                    , "询问", Messages.getQuestionIcon());
            if (updateClassPath == Messages.OK) {
                IdeaProjectGenerateUtil.updateApplicationNCLibrarys(e.getProject());
            }
        }
    }
}
