package com.air.nc5dev.nccrequstsearch;

import com.air.nc5dev.acion.GoToNCRequestMappingAction;
import com.air.nc5dev.ui.actionurlsearch.ActionResultDTO;
import com.air.nc5dev.ui.actionurlsearch.ActionResultListTable;
import com.air.nc5dev.util.ReflectUtil;
import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.vo.NCCActionInfoVO;
import com.google.common.collect.Lists;
import com.intellij.ide.DataManager;
import com.intellij.ide.actions.SearchEverywherePsiRenderer;
import com.intellij.ide.actions.searcheverywhere.AbstractGotoSEContributor;
import com.intellij.ide.actions.searcheverywhere.FoundItemDescriptor;
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributor;
import com.intellij.ide.actions.searcheverywhere.WeightedSearchEverywhereContributor;
import com.intellij.ide.util.gotoByName.FilteringGotoByModel;
import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.Processor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

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
@Data
public class RequestMappingGoToContributor
        implements com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributorFactory
        , WeightedSearchEverywhereContributor<NCCActionInfoVO> {
    AnActionEvent event;

    public RequestMappingGoToContributor(AnActionEvent event) {
        this.event = event;
    }

    private static AnActionEvent getAnActionEvent(AnActionEvent event) {
        if (event != null) {
            return event;
        }

        DataContext dataContext = DataManager.getInstance().getDataContext();
        return new AnActionEvent(null, dataContext
                , RequestMappingGoToContributor.class.getSimpleName(), new Presentation()
                , ActionManager.getInstance(), 0);
    }


    @Override
    public @NotNull
    String getSearchProviderId() {
        return this.getClass().getName();
    }

    @NotNull
    @Override
    public String getGroupName() {
        return "搜索 NC Action或接口";
    }

    @Override
    public int getSortWeight() {
        return 0;
    }

    @Override
    public boolean showInFindResults() {
        return false;
    }

    @Override
    public boolean processSelectedItem(@NotNull NCCActionInfoVO v, int i, @NotNull String s) {
        ActionResultListTable.openClass(null, getEvent().getProject(), ReflectUtil.copy2VO(v, ActionResultDTO.class));
        return true;
    }

    @Override
    public void fetchWeightedElements(@NotNull String key, @NotNull ProgressIndicator progressIndicator,
                                      @NotNull Processor<? super FoundItemDescriptor<NCCActionInfoVO>> processor) {
        List<NCCActionInfoVO> vos = RequestMappingItemProvider.getMe().search(getEvent().getProject()
                , StringUtil.trim(key), true);

        for (NCCActionInfoVO vo : vos) {
            if (!processor.process(new FoundItemDescriptor<>(vo, 0))) {
                return;
            }
        }
    }

    @Override
    public @NotNull
    ListCellRenderer<? super NCCActionInfoVO> getElementsRenderer() {
        return new SearchEverywherePsiRenderer(this);
    }

    @Override
    public @Nullable
    Object getDataForItem(@NotNull NCCActionInfoVO nccActionInfoVO, @NotNull String s) {
        return null;
    }

    @Override
    public @NotNull
    SearchEverywhereContributor createContributor(@NotNull AnActionEvent anActionEvent) {
        return new RequestMappingGoToContributor(anActionEvent);
    }
}
