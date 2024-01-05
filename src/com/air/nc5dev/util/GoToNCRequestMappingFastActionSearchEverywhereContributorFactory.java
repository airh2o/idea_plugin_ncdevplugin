package com.air.nc5dev.util;

import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributor;
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributorFactory;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class GoToNCRequestMappingFastActionSearchEverywhereContributorFactory implements SearchEverywhereContributorFactory<Object> {
    public GoToNCRequestMappingFastActionSearchEverywhereContributorFactory() {
    }

    @Override
    public @NotNull
    SearchEverywhereContributor<Object> createContributor(@NotNull AnActionEvent anActionEvent) {
        return new GoToNCRequestMappingFastActionSearchEverywhereContributor(anActionEvent);
    }
}