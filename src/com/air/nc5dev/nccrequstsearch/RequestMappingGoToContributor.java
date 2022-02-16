package com.air.nc5dev.nccrequstsearch;

import com.air.nc5dev.acion.GoToNCRequestMappingAction;
import com.intellij.ide.actions.searcheverywhere.AbstractGotoSEContributor;
import com.intellij.ide.util.gotoByName.FilteringGotoByModel;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * </br>
 * </br>
 * </br>
 *
 * @author 唐粟 Email:209308343@qq.com
 * @date 2022/2/15 0015 11:07
 * @project
 * @Version
 */
public class RequestMappingGoToContributor extends AbstractGotoSEContributor {
    protected RequestMappingGoToContributor(@Nullable Project project
            , @Nullable PsiElement context) {
        super(project, context);
    }

    @NotNull
    @Override
    protected FilteringGotoByModel<?> createModel(@NotNull Project project) {
        return new RequestMappingModel(project, GoToNCRequestMappingAction.EXTENSION_NAME.getExtensionList(), null);
    }

    @NotNull
    @Override
    public String getGroupName() {
        return "NCC_RequestMappingGoToContributor";
    }

    @Override
    public int getSortWeight() {
        return 0;
    }
}
