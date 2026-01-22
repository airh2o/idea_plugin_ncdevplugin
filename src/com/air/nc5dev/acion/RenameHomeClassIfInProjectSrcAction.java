package com.air.nc5dev.acion;

import com.air.nc5dev.acion.base.AbstractIdeaAction;
import com.air.nc5dev.util.IdeaProjectGenerateUtil;
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
 * 根据工程里的src java源码， 重命名掉 home中的class文件 <br>
 * <br>
 * <br>
 *
 * @author 唐粟 Email:209308343@qq.com
 * @date 2023/11/10 0010 16:17
 * @project
 * @Version
 */
public class RenameHomeClassIfInProjectSrcAction extends AbstractIdeaAction {
    @Override
    public String getConformTxt(AnActionEvent e) {
        File ncHome = ProjectNCConfigUtil.getNCHome(e.getProject());
        return "你确定对NCHOME目录进行 重命名工程源码在NCHOME中的class、java文件 操作? " + ncHome;
    }

    @Override
    protected void doHandler(AnActionEvent e) {
        try {
            Task.Backgroundable backgroundable = new Task.Backgroundable(e.getProject()
                    , "正在重命名工程源码在NCHOME中的class、java文件中...") {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    Module[] modules = IdeaProjectGenerateUtil.getProjectModules(e.getProject());
                    File ncHome = ProjectNCConfigUtil.getNCHome(e.getProject());

                    List<File> cs = new ArrayList<>(5000);

                    File f = new File(ncHome, "modules");
                    if (f.isDirectory()) {
                        if (indicator.isCanceled()) {
                            return;
                        }
                        cs.addAll(getAll(f, ".class"));
                        cs.addAll(getAll(f, ".java"));
                    }
                    f = new File(ncHome, "hotwebs");
                    if (f.isDirectory()) {
                        if (indicator.isCanceled()) {
                            return;
                        }
                        cs.addAll(getAll(f, ".class"));
                        cs.addAll(getAll(f, ".java"));
                    }
                    f = new File(ncHome, "external");
                    if (f.isDirectory()) {
                        if (indicator.isCanceled()) {
                            return;
                        }
                        cs.addAll(getAll(f, ".class"));
                        cs.addAll(getAll(f, ".java"));
                    }

                    indicator.setText("文件数量: " + cs.size());

                    for (Module module : modules) {
                        if (indicator.isCanceled()) {
                            return;
                        }

                        VirtualFile[] sourceRoots = ModuleRootManager.getInstance(module).getSourceRoots();
                        if (sourceRoots == null) {
                            continue;
                        }

                        indicator.setText("处理模块:" + module.getName() + " ,源码目录数量: " + sourceRoots.length);
                        for (VirtualFile sourceRoot : sourceRoots) {
                            if (indicator.isCanceled()) {
                                return;
                            }

                            List<File> all = getAll(new File(sourceRoot.toNioPath().toUri()), ".java");
                            for (File ff : all) {
                                if (indicator.isCanceled()) {
                                    return;
                                }

                                renameHome(cs, ff, indicator);
                            }
                        }
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

    private static void renameHome(List<File> cs, File ff, ProgressIndicator indicator) {
        boolean has = false;
        String n = ff.getName();
        n = n.substring(0, n.lastIndexOf('.'));
        for (File c : cs) {
            if (indicator.isCanceled()) {
                return;
            }

            String cn = c.getName();
            cn = cn.substring(0, cn.lastIndexOf('.'));

            if (cn.equals(n)
                    || cn.startsWith(n + "$")) {
                if (c.getParentFile().getName().equals(ff.getParentFile().getName())) {
                    has = true;
                    String msg = "\t" + c.getPath() + " 重命名";
                    LogUtil.info(msg);
                    indicator.setText(msg);
                    c.renameTo(new File(c.getParentFile(), c.getName() + ".idea_plugin_rename"));
                }
            }
        }

        if (has) {
            String msg = "=========   " + ff.getPath() + " -> 搜索重命名完成=============";
            LogUtil.info(msg);
            indicator.setText(msg);
        }
    }

    public static List<File> getAll(File src, String s) {
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

    public static void main(String[] args) {
        File m = new File("E:\\runtimes\\NCC1909_ZTNRNY_HOME\\modules");

        File p = new File("E:\\projects\\iuap\\NCC1909_ZTNRNY");

        List<File> cs = getAll(m, ".class");

        File[] ps = p.listFiles();
        for (File f : ps) {
            File src = new File(f, "src");
            if (!src.isDirectory()) {
                continue;
            }

            List<File> all = getAll(src, ".java");
            for (File ff : all) {
                renameHome(cs, ff, null);
            }
        }

    }

}
