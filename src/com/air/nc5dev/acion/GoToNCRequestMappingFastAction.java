package com.air.nc5dev.acion;

import com.air.nc5dev.util.GoToNCRequestMappingFastActionSearchEverywhereContributor;
import com.air.nc5dev.util.ProjectNCConfigUtil;
import com.air.nc5dev.util.StringUtil;
import com.intellij.ide.actions.SearchEverywhereBaseAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class GoToNCRequestMappingFastAction extends SearchEverywhereBaseAction {
    public GoToNCRequestMappingFastAction() {
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        this.showInSearchEverywherePopup(
                GoToNCRequestMappingFastActionSearchEverywhereContributor.class.getSimpleName()
                , event
                , true
                , true);
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        super.update(event);
        if (event.getPresentation().isEnabledAndVisible()) {
            if (event.getProject() != null && StringUtil.isBlank(ProjectNCConfigUtil.getNCHomePath(event.getProject()))) {
                event.getPresentation().setEnabledAndVisible(false);
            }
        }
    }
}
