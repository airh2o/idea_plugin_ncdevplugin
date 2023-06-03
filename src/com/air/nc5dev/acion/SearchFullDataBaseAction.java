package com.air.nc5dev.acion;

import com.air.nc5dev.acion.base.AbstractIdeaAction;
import com.air.nc5dev.ui.PatcherDialog;
import com.air.nc5dev.ui.SearchFullDataBaseDialog;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class SearchFullDataBaseAction extends AbstractIdeaAction {
    @Override
    protected void doHandler(AnActionEvent e) {
        SearchFullDataBaseDialog dialog = null;
        if (SearchFullDataBaseDialog.me == null) {
            dialog = new SearchFullDataBaseDialog(e);
          //  SearchFullDataBaseDialog.me = dialog;
        } else {
            dialog = SearchFullDataBaseDialog.me;
            dialog.getButton_Search().setEnabled(true);
        }

        dialog.show();
    }
}
