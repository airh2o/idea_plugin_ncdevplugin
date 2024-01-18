package com.air.nc5dev.ui.actionurlsearch.action;

import com.air.nc5dev.ui.actionurlsearch.NCCActionURLSearchUI;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class SearchAction extends AnAction {
    NCCActionURLSearchUI nCCActionURLSearchView;

    public SearchAction(NCCActionURLSearchUI nCCActionURLSearchView) {
        super("搜索", "立即搜索全部", null //IconLoader.getIcon("/icons/send.svg")
        );
        this.nCCActionURLSearchView = nCCActionURLSearchView;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

    }


}
