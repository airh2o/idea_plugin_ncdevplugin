package com.air.nc5dev.acion;

import com.air.nc5dev.acion.base.AbstractIdeaAction;
import com.air.nc5dev.ui.searchnclogs.SearchNclogsDialog;
import com.air.nc5dev.util.CollUtil;
import com.air.nc5dev.util.IoUtil;
import com.air.nc5dev.util.idea.LogUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

import javax.swing.JFileChooser;
import java.io.File;

/**
 * <br>
 * <br>
 * <br>
 * <br>
 *
 * @author air Email: 209308343@qq.com
 * @date 2020/4/1 0001 22:03
 * @Param
 * @return
 */
public class SearchNclogsAction extends AbstractIdeaAction {
    @Override
    protected void doHandler(AnActionEvent e) {
        SearchNclogsDialog dialog = null;
        if (SearchNclogsDialog.me == null) {
            dialog = new SearchNclogsDialog(e.getProject());
            //  SearchFullDataBaseDialog.me = dialog;
        } else {
            dialog = SearchNclogsDialog.me;
            dialog.getButton_execute().setEnabled(true);
        }

        dialog.setModal(false);
        dialog.show();
    }
}
