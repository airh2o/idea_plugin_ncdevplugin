package com.air.nc5dev.acion;

import com.air.nc5dev.acion.base.AbstractIdeaAction;
import com.air.nc5dev.util.ProjectNCConfigUtil;
import com.air.nc5dev.util.idea.LogUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 根据工程里的src java源码， 重命名掉 home中的class文件 </br>
 * </br>
 * </br>
 *
 * @author 唐粟 Email:209308343@qq.com
 * @date 2023/11/10 0010 16:17
 * @project
 * @Version
 */
public class UnRenameHomeClassIfInProjectSrcAction extends AbstractIdeaAction {
    @Override
    public String getConformTxt(AnActionEvent e) {
        File ncHome = ProjectNCConfigUtil.getNCHome(e.getProject());
        return "你确定回滚对NCHOME目录进行 重命名工程源码在NCHOME中的class、java文件 操作? " + ncHome;
    }

    @Override
    protected void doHandler(AnActionEvent e) {
        try {
            Task.Backgroundable backgroundable = new Task.Backgroundable(e.getProject()
                    , "正在回滚重命名工程源码在NCHOME中的class、java文件中...") {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    Module[] modules = ModuleManager.getInstance(e.getProject()).getModules();
                    File ncHome = ProjectNCConfigUtil.getNCHome(e.getProject());

                    List<File> cs = new ArrayList<>(5000);

                    File f = new File(ncHome, "modules");
                    if (f.isDirectory()) {
                        if (indicator.isCanceled()) {
                            return;
                        }
                        cs.addAll(getAll(f, ".idea_plugin_rename"));
                    }
                    f = new File(ncHome, "hotwebs");
                    if (f.isDirectory()) {
                        if (indicator.isCanceled()) {
                            return;
                        }
                        cs.addAll(getAll(f, ".idea_plugin_rename"));
                    }
                    f = new File(ncHome, "external");
                    if (f.isDirectory()) {
                        if (indicator.isCanceled()) {
                            return;
                        }
                        cs.addAll(getAll(f, ".idea_plugin_rename"));
                    }

                    indicator.setText("文件数量: " + cs.size());
                    for (File c : cs) {
                        if (indicator.isCanceled()) {
                            return;
                        }

                        indicator.setText(c.getName());
                        c.renameTo(new File(c.getParentFile(), c.getName().substring(0
                                , c.getName().length() - ".idea_plugin_rename".length())));
                    }

                    LogUtil.infoAndHide("重命名完成: " + ncHome);
                }
            };
            backgroundable.setCancelText("停止");
            backgroundable.setCancelTooltipText("立即停止操作");
            ProgressManager.getInstance().run(backgroundable);
        } catch (Throwable ex) {
        }
    }

    private static List<File> getAll(File src, String s) {
        File[] fs = src.listFiles();
        ArrayList<File> ff = new ArrayList<>();
        if (fs == null) {
            return ff;
        }

        for (File f : fs) {
            if (f.isFile() && f.getName().toLowerCase().endsWith(s)) {
                ff.add(f);
                continue;
            }

            if (f.isDirectory()) {
                ff.addAll(getAll(f, s));
            }
        }
        return ff;
    }

    @Override
    public boolean showConform() {
        return true;
    }
}
