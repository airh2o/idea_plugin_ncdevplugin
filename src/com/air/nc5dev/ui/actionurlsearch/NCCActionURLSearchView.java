package com.air.nc5dev.ui.actionurlsearch;

import com.air.nc5dev.ui.actionurlsearch.action.SearchAction;
import com.air.nc5dev.ui.actionurlsearch.action.SearchProjectAction;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import lombok.Data;

import javax.swing.*;

@Data
public class NCCActionURLSearchView extends SimpleToolWindowPanel {
    NCCActionURLSearchUI nCCActionURLSearchView;
    Project project;

    public NCCActionURLSearchView(Project project) {
        super(false, true);
        this.project = project;
        this.nCCActionURLSearchView = new NCCActionURLSearchUI(project);
        DefaultActionGroup barGroup = new DefaultActionGroup();
        barGroup.add((AnAction) new SearchAction(nCCActionURLSearchView));
        barGroup.addSeparator();
        barGroup.add((AnAction) new SearchProjectAction(nCCActionURLSearchView));
        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar(
                "toolbar", (ActionGroup) barGroup, true);
        toolbar.setTargetComponent((JComponent) this);
        setToolbar(toolbar.getComponent());
        setContent(nCCActionURLSearchView.createUI());
    }

}
