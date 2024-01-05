package com.air.nc5dev.util;

import com.air.nc5dev.nccrequstsearch.RequestMappingItemProvider;
import com.air.nc5dev.ui.actionurlsearch.ActionResultDTO;
import com.air.nc5dev.ui.actionurlsearch.ActionResultListTable;
import com.air.nc5dev.util.idea.LogUtil;
import com.air.nc5dev.util.idea.ProjectUtil;
import com.air.nc5dev.vo.NCCActionInfoVO;
import com.intellij.ide.actions.searcheverywhere.AbstractGotoSEContributor;
import com.intellij.ide.actions.searcheverywhere.ClassSearchEverywhereContributor;
import com.intellij.ide.actions.searcheverywhere.FoundItemDescriptor;
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributor;
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributorFactory;
import com.intellij.ide.util.gotoByName.ChooseByNameInScopeItemProvider;
import com.intellij.ide.util.gotoByName.ChooseByNameItemProvider;
import com.intellij.ide.util.gotoByName.ChooseByNameModelEx;
import com.intellij.ide.util.gotoByName.ChooseByNameViewModel;
import com.intellij.ide.util.gotoByName.ChooseByNameWeightedItemProvider;
import com.intellij.ide.util.gotoByName.FilteringGotoByModel;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.util.ProgressIndicatorUtils;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.Processor;
import com.intellij.util.indexing.FindSymbolParameters;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class GoToNCRequestMappingFastActionSearchEverywhereContributor extends ClassSearchEverywhereContributor {
    public GoToNCRequestMappingFastActionSearchEverywhereContributor(@NotNull AnActionEvent event) {
        super(event);
    }

    @Override
    public void fetchWeightedElements(@NotNull String rawPattern, @NotNull ProgressIndicator progressIndicator
            , @NotNull Processor<? super FoundItemDescriptor<Object>> consumer) {
        if (StringUtil.isBlank(rawPattern)) {
            return;
        }

        Runnable fetchRunnable = () -> {
            if (this.isDumbAware() || !DumbService.isDumb(this.myProject)) {
                FilteringGotoByModel<?> model = this.createModel(this.myProject);
                if (!progressIndicator.isCanceled()) {
                    RequestMappingItemProvider.getMe().search(myProject
                            , rawPattern
                            , true
                            , progressIndicator
                            , consumer);
                }
            }
        };

        Application application = ApplicationManager.getApplication();
        if (application.isUnitTestMode() && application.isDispatchThread()) {
            fetchRunnable.run();
        } else {
            ProgressIndicatorUtils.yieldToPendingWriteActions();
            ProgressIndicatorUtils.runInReadActionWithWriteActionPriority(fetchRunnable, progressIndicator);
        }
    }


    @Override
    public String getGroupName() {
        return "搜索"+V.get(V.str4Obj(ProjectNCConfigUtil.getNCVersion(myProject)))+"Action";
    }

    @Override
    public boolean processSelectedItem(@NotNull Object selected, int modifiers, @NotNull String searchText) {
        if (selected instanceof NCCActionInfoVO) {
            NCCActionInfoVO v = (NCCActionInfoVO) selected;
            ActionResultListTable.openClass(null,myProject,ReflectUtil.copy2VO(v, ActionResultDTO.class));
        }

        return super.processSelectedItem(selected, modifiers, searchText);
    }
}
