package com.air.nc5dev.acion;

import com.air.nc5dev.nccrequstsearch.RequestMappingModel;
import com.air.nc5dev.util.ProjectNCConfigUtil;
import com.air.nc5dev.util.idea.LogUtil;
import com.air.nc5dev.util.idea.ProjectUtil;
import com.intellij.ide.actions.GotoActionBase;
import com.intellij.ide.util.gotoByName.ChooseByNamePopup;
import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.extensions.ExtensionPointName;

import java.util.ArrayList;

/**
 * </br>
 * </br>
 * </br>
 *
 * @author 唐粟 Email:209308343@qq.com
 * @date 2022/2/15 0015 10:40
 * @project
 * @Version
 */
public class GoToNCRequestMappingAction extends GotoActionBase implements DumbAware {
    public static final ExtensionPointName EXTENSION_NAME = ExtensionPointName
            .create("com.air.nc5dev.acion.GoToNCRequestMappingAction.EXTENSION_NAME")
            ;

    @Override
    protected void gotoActionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getData(CommonDataKeys.PROJECT);
        if (project == null) {
            return;
        }

        init(e);

        RequestMappingModel requestMappingModel = new RequestMappingModel(project, new ArrayList(), e);
        showNavigationPopup(e, requestMappingModel, new MyGotoActionCallback()
                , null, true, false);
    }

    private void init(AnActionEvent e) {
        //设置默认项目
        ProjectUtil.setProject(e.getProject());
        try {
            // 自动重新加载一次 插件配置文件
            ProjectNCConfigUtil.initConfigFile();
        } catch (Exception e1) {
            LogUtil.error("emmmmm 出现异常,淡定不要惊慌,如果影响使用请QQ209308343通知我: ", e1);
        }
    }

    class MyGotoActionCallback extends GotoActionBase.GotoActionCallback{
        @Override
        public void elementChosen(ChooseByNamePopup chooseByNamePopup, Object element) {

        }
    }
}


