package com.air.nc5dev.ui.actionurlsearch;

import com.air.nc5dev.nccrequstsearch.RequestMappingItemProvider;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.openapi.project.DumbAware;
import com.intellij.ui.TextFieldWithAutoCompletionListProvider;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

@Getter
@Setter
public class NCCActionCompletionProvider extends TextFieldWithAutoCompletionListProvider<String> implements DumbAware {
    NCCActionURLSearchUI nccActionURLSearchUI;
    int size;

    public NCCActionCompletionProvider(int size, NCCActionURLSearchUI nccActionURLSearchUI) {
        super(null);
        this.size = size;
        this.nccActionURLSearchUI = nccActionURLSearchUI;
    }

    @Override
    public @NotNull
    Collection<String> getItems(String prefix, boolean cached, CompletionParameters parameters) {
        RequestMappingItemProvider pr = RequestMappingItemProvider.getMe();

        return pr.suggest(prefix
                , nccActionURLSearchUI.getProject()
                , !nccActionURLSearchUI.getCheckBox_onlyProject().isSelected()
                , size);
    }

    @Override
    protected @NotNull
    String getLookupString(@NotNull String s) {
        return s;
    }
}