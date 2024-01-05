package com.air.nc5dev.ui.actionurlsearch;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import lombok.Data;

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
