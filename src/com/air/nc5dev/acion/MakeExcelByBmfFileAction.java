package com.air.nc5dev.acion;

import com.air.nc5dev.acion.base.AbstractIdeaAction;
import com.air.nc5dev.util.CollUtil;
import com.air.nc5dev.util.IoUtil;
import com.air.nc5dev.util.idea.LogUtil;
import com.air.nc5dev.util.ncutils.BmfTableInfo2ExcelUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
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
public class MakeExcelByBmfFileAction extends AbstractIdeaAction {
    public static volatile String path = null;

    @Override
    protected void doHandler(AnActionEvent e) {
        //选择路径
        if (path == null) {
            path = e.getProject().getBasePath();
        }
        JFileChooser fileChooser = new JFileChooser(path);
        fileChooser.setDialogTitle("选择BMF文件(可多选):");
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

        path = (fs[0].isDirectory() ? fs[0] : fs[0].getParentFile()).getPath();

        //选择保存位置
        File outDir = new File(fs[0].isDirectory() ? fs[0] : fs[0].getParentFile(), "xls");
        fileChooser = new JFileChooser();
        fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        fileChooser.setCurrentDirectory(outDir.getParentFile());
        fileChooser.setSelectedFile(outDir);
        fileChooser.setDialogTitle("选择保存路径(选择文件夹或者具体的xls文件):");
        fileChooser.showDialog(null, null);
        if (flag != JFileChooser.APPROVE_OPTION
                && fileChooser.getSelectedFile() != null) {
            outDir = fileChooser.getSelectedFile();
        }

        try {
            File out = outDir;
            Task.Backgroundable backgroundable = new Task.Backgroundable(e.getProject(), "正在生成Excel字段文档...") {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    try {
                        for (File f : fs) {
                            indicator.setText(f.getPath() + "   正在生成Excel字段文档...");
                            indicator.setText2(f.getName());

                            out(f, out);
                        }

                        LogUtil.infoAndHide("生成成功： " + out.getPath());
                        indicator.setText("生成成功： " + out.getPath());

                        IoUtil.tryOpenFileExpolor(out);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            };
            backgroundable.setCancelText("放弃吧,没有卵用的按钮");
            backgroundable.setCancelTooltipText("这是一个没有卵用的按钮");
            ProgressManager.getInstance().run(backgroundable);
        } catch (Throwable iae) {
            LogUtil.error("失败: " + outDir.getPath(), iae);
        }
    }

    public void out(File f, File outDir) throws Exception {
        if (f.isFile()) {
            if (!f.getName().toLowerCase().endsWith(".bmf")) {
                return;
            }

            BmfTableInfo2ExcelUtil.toExcel(f,
                    outDir.getName().toLowerCase().endsWith(".xls") || outDir.getName().toLowerCase().endsWith(".xlsx")
                            ? outDir : new File(outDir, f.getName() + ".xls")
            );

            return;
        }

        File[] fs = f.listFiles();
        if (fs != null) {
            for (File fe : fs) {
                out(fe, outDir);
            }
        }
    }
}
