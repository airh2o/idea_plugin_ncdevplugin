package com.air.nc5dev.acion;

import com.air.nc5dev.acion.base.AbstractIdeaAction;
import com.air.nc5dev.util.ProjectNCConfigUtil;
import com.air.nc5dev.util.idea.LogUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.tangsu.mstsc.dao.BaseDao;

import java.io.File;
import java.util.ArrayList;
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
public class RenameHomeClassIfInProjectSrcAction extends AbstractIdeaAction {
    @Override
    public String getConformTxt(AnActionEvent e) {
        File ncHome = ProjectNCConfigUtil.getNCHome(e.getProject());
        return "你确定对NCHOME目录进行 重命名工程源码在NCHOME中的Class文件 操作? " + ncHome;
    }

    @Override
    protected void doHandler(AnActionEvent e) {
        try {
            Module[] modules = ModuleManager.getInstance(e.getProject()).getModules();
            File ncHome = ProjectNCConfigUtil.getNCHome(e.getProject());

            List<File> cs = new ArrayList<>(5000);

            File f = new File(ncHome, "modules");
            if (f.isDirectory()) {
                cs.addAll(getAll(f, ".class"));
            }
            f = new File(ncHome, "hotwebs");
            if (f.isDirectory()) {
                cs.addAll(getAll(f, ".class"));
            }
            f = new File(ncHome, "external");
            if (f.isDirectory()) {
                cs.addAll(getAll(f, ".class"));
            }

            for (Module module : modules) {
                VirtualFile[] sourceRoots = ModuleRootManager.getInstance(module).getSourceRoots();
                for (VirtualFile sourceRoot : sourceRoots) {
                    List<File> all = getAll(new File(sourceRoot.toNioPath().toUri()), ".java");
                    for (File ff : all) {
                        renameHome(cs, ff);
                    }
                }
            }

            LogUtil.infoAndHide("重命名完成: " + ncHome);
        } catch (Throwable ex) {
        }
    }

    private static void renameHome(List<File> cs, File ff) {
        boolean has = false;
        String n = ff.getName();
        n = n.substring(0, n.lastIndexOf('.'));
        for (File c : cs) {
            String cn = c.getName();
            cn = cn.substring(0, cn.lastIndexOf('.'));

            if (cn.equals(n)) {
                if (c.getParentFile().getName().equals(ff.getParentFile().getName())) {
                    has = true;
                    LogUtil.info("\t" + c.getPath() + " 重命名");
                    c.renameTo(new File(c.getParentFile(), c.getName() + "1"));
                }
            }
        }

        if (has) {
            LogUtil.info("=========   " + ff.getPath() + " -> 搜索重命名完成=============");
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
                renameHome(cs, ff);
            }
        }

    }

}
