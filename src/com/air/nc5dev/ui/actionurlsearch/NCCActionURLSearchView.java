package com.air.nc5dev.ui.actionurlsearch;

import com.air.nc5dev.ui.actionurlsearch.action.SearchAction;
import com.air.nc5dev.ui.actionurlsearch.action.SearchProjectAction;
import com.intellij.ide.actions.searcheverywhere.CheckBoxSearchEverywhereToggleAction;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.CheckboxAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.components.JBScrollPane;
import lombok.Data;
import org.jdesktop.swingx.JXButton;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

@Data
public class NCCActionURLSearchView extends SimpleToolWindowPanel {
    NCCActionURLSearchUI nCCActionURLSearchView;
    Project project;

    public NCCActionURLSearchView(Project project) {
        super(false, true);
        this.project = project;
        this.nCCActionURLSearchView = new NCCActionURLSearchUI(project);

       /* DefaultActionGroup barGroup = new DefaultActionGroup();
        barGroup.addSeparator();
        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar(
                "toolbar", (ActionGroup) barGroup, true);
        toolbar.setTargetComponent((JComponent) this);
        setToolbar(toolbar.getComponent());*/

        setContent(nCCActionURLSearchView.createUI());
    }

}
