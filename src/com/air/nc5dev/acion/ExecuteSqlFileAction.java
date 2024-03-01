package com.air.nc5dev.acion;

import com.air.nc5dev.acion.base.AbstractIdeaAction;
import com.air.nc5dev.ui.ExecuteSqlFile.ExecuteSqlFileDialog;
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
 * </br>
 * </br>
 * </br>
 * </br>
 *
 * @author air Email: 209308343@qq.com
 * @date 2020/4/1 0001 22:03
 * @Param
 * @return
 */
public class ExecuteSqlFileAction extends AbstractIdeaAction {
    @Override
    protected void doHandler(AnActionEvent e) {
        ExecuteSqlFileDialog dialog = null;
        if (ExecuteSqlFileDialog.me == null) {
            dialog = new ExecuteSqlFileDialog(e.getProject());
            //  SearchFullDataBaseDialog.me = dialog;
        } else {
            dialog = ExecuteSqlFileDialog.me;
            dialog.getButton_execute().setEnabled(true);
        }

        dialog.setModal(false);
        dialog.show();
    }

    protected void doHandler222(AnActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择SQL文件(可多选):");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(true);
        int flag = fileChooser.showOpenDialog(null);
        if (flag != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File[] fs = fileChooser.getSelectedFiles();

        if (CollUtil.isEmpty(fs)) {
            return;
        }

        boolean ignorErrorLine = Messages.showYesNoDialog("是否自动忽略出错的行?"
                , "询问", Messages.getQuestionIcon()) == Messages.OK;
        boolean saveSucessSqls = Messages.showYesNoDialog("是否导出成功SQL到临时文件?"
                , "询问", Messages.getQuestionIcon()) == Messages.OK;
        boolean saveErrorSqls = Messages.showYesNoDialog("是否导出失败SQL到临时文件?"
                , "询问", Messages.getQuestionIcon()) == Messages.OK;
        File sucessFile =
                new File(System.getProperty("user.dir") + File.separator + "sucessSql_" + System.currentTimeMillis() + ".sql");
        File errorFile =
                new File(System.getProperty("user.dir") + File.separator + "errorSql_" + System.currentTimeMillis() + ".sql");
        try {
            Task.Backgroundable backgroundable = new Task.Backgroundable(e.getProject(), "正在执行sql脚本...") {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    try {
                        for (File f : fs) {
                            indicator.setText(f.getPath() + "   正在执行sql脚本...");
                            indicator.setText2(f.getName());

                            execute(fs, ignorErrorLine, saveSucessSqls, saveErrorSqls, sucessFile, errorFile);
                        }

                        LogUtil.infoAndHide("完成： " + sucessFile.getPath());
                        indicator.setText("完成： " + sucessFile.getPath());

                        if (sucessFile.isFile()) {
                            IoUtil.tryOpenFileExpolor(sucessFile);
                        } else if (errorFile.isFile()) {
                            IoUtil.tryOpenFileExpolor(errorFile);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            };
            backgroundable.setCancelText("放弃吧,没有卵用的按钮");
            backgroundable.setCancelTooltipText("这是一个没有卵用的按钮");
            ProgressManager.getInstance().run(backgroundable);
        } catch (Throwable iae) {
            LogUtil.error("失败: " + iae.toString(), iae);
        }
    }

    private void execute(File[] fs
            , boolean ignorErrorLine
            , boolean saveSucessSqls
            , boolean saveErrorSqls
            , File sucessFile
            , File errorFile) {

    }
}
