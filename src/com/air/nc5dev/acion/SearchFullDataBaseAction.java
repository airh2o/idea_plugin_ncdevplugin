package com.air.nc5dev.acion;

import com.air.nc5dev.acion.base.AbstractIdeaAction;
import com.air.nc5dev.ui.searchdatabasefull.SearchFullDataBaseDialog;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class SearchFullDataBaseAction extends AbstractIdeaAction {
    @Override
    protected void doHandler(AnActionEvent e) {
        SearchFullDataBaseDialog dialog = null;
        if (SearchFullDataBaseDialog.me == null) {
            dialog = new SearchFullDataBaseDialog(e.getProject());
          //  SearchFullDataBaseDialog.me = dialog;
        } else {
            dialog = SearchFullDataBaseDialog.me;
            dialog.getButton_Search().setEnabled(true);
        }

        dialog.setModal(false);
        dialog.show();
    }
}
