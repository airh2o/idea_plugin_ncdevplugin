package com.air.nc5dev.ui.actionurlsearch.action;

import com.air.nc5dev.ui.actionurlsearch.NCCActionURLSearchUI;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class SearchProjectAction extends AnAction {
    NCCActionURLSearchUI nCCActionURLSearchView;

    public SearchProjectAction(NCCActionURLSearchUI nCCActionURLSearchView) {
        super("搜索项目本身", "立即搜索项目自身源码里的url映射", null //IconLoader.getIcon("/icons/send.svg")
        );
        this.nCCActionURLSearchView = nCCActionURLSearchView;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

    }


}
