package com.air.nc5dev.acion;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.air.nc5dev.acion.base.AbstractIdeaAction;
import com.air.nc5dev.ui.actionurlsearch.ActionResultListTable;
import com.air.nc5dev.util.CollUtil;
import com.air.nc5dev.util.IdeaProjectGenerateUtil;
import com.air.nc5dev.util.IoUtil;
import com.air.nc5dev.util.ProjectNCConfigUtil;
import com.air.nc5dev.util.idea.LogUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

/**
 * 导出被修改的标品代码文件情况 <br>
 * <br>
 * <br>
 *
 * @author 唐粟 Email:209308343@qq.com
 * @date 2023/11/10 0010 16:17
 * @project
 * @Version
 */
@Data
public class ExportChangeCodeFilesAction extends AbstractIdeaAction {
    public static volatile String path = null;
    public static volatile Map<String, List<FileInfoDTO>> infos = new ConcurrentHashMap<>();
    public static volatile Map<String, Map<String, FileInfoDTO>> infoMap = new ConcurrentHashMap<>();
    public static volatile ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();

    @Override
    protected void doHandler(AnActionEvent e) {
        try {
            //选择路径
            if (path == null) {
                path = e.getProject().getBasePath();
            }

            JFileChooser fileChooser = new JFileChooser(path);
            fileChooser.setDialogTitle("保存到文件夹:");
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setMultiSelectionEnabled(false);
            int flag = fileChooser.showOpenDialog(null);
            if (flag != JFileChooser.APPROVE_OPTION) {
                return;
            }
            File outDir = fileChooser.getSelectedFile();
            path = outDir.getPath();
            final File outDirFile = outDir;

            relaod(e.getProject(), (indicator) -> {
                List<FileInfoDTO> vs = ExportChangeCodeFilesAction.infos.get(e.getProject().getBasePath());
                if (vs.isEmpty()) {
                    LogUtil.infoAndHide("完成: 没有任何结果!");
                    return;
                }
                makeExcel(vs, outDirFile, indicator);
            });
        } catch (Throwable ex) {
        }
    }

    public static void relaod(Project project, Consumer<ProgressIndicator> after) {
        init(project);

        Task.Backgroundable backgroundable = new Task.Backgroundable(project
                , "正在导出被修改的标品代码文件情况中...") {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {
                    Module[] modules = IdeaProjectGenerateUtil.getProjectModules(project);
                    File ncHome = ProjectNCConfigUtil.getNCHome(project);
                    List<FileInfoDTO> vs = infos.get(project.getBasePath());

                    vs.clear();
                    infoMap.get(project.getBasePath()).clear();

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

                            List<File> all = RenameHomeClassIfInProjectSrcAction.getAll(new File(sourceRoot.toNioPath().toUri()), ".java");
                            for (File sourceFile : all) {
                                if (indicator.isCanceled()) {
                                    return;
                                }

                                loadFile(project, vs, sourceFile, indicator, module.getName());
                            }
                        }
                    }

                    if (after != null) {
                        after.accept(indicator);
                    }
                } catch (Throwable ex) {
                    LogUtil.error("被修改的标品代码文件情况导出失败:" + ex.getLocalizedMessage(), ex);
                }
            }
        };
        backgroundable.setCancelText("停止");
        backgroundable.setCancelTooltipText("立即停止操作");
        ProgressManager.getInstance().run(backgroundable);
    }

    private void makeExcel(List<FileInfoDTO> infos, File outFile, ProgressIndicator indicator) {
        if (!StrUtil.contains(outFile.getName(), ".xls")) {
            outFile = new File(outFile, "被修改的标品代码文件情况_" + DateUtil.format(new Date(), "yyyy-MM-dd_HH_mm_ss") + ".xls");
        }
        indicator.setText("开始导出excel: " + outFile.getPath());

        ExcelWriter ew = cn.hutool.poi.excel.ExcelUtil.getWriter(outFile);
        CellStyle setBorder = ew.createCellStyle();
        setBorder.setBorderBottom(BorderStyle.MEDIUM); //下边框
        setBorder.setBorderLeft(BorderStyle.MEDIUM);//左边框
        setBorder.setBorderTop(BorderStyle.MEDIUM);//上边框
        setBorder.setBorderRight(BorderStyle.MEDIUM);//右边框
        setBorder.setWrapText(true);//设置自动换行

        ew.setSheet(0);
        ew.renameSheet("情况");
        ew.getCell(0, 0, true).setCellValue("文件路径");
        ew.getCell(1, 0, true).setCellValue("类名称");
        ew.getCell(2, 0, true).setCellValue("模块");
        ew.getCell(3, 0, true).setCellValue("标品类文件路径");

        ew.getCell(0, 0, true).setCellStyle(setBorder);
        ew.getCell(1, 0, true).setCellStyle(setBorder);
        ew.getCell(2, 0, true).setCellStyle(setBorder);
        ew.getCell(3, 0, true).setCellStyle(setBorder);

        ew.setColumnWidth(0, 100);
        ew.setColumnWidth(1, 50);
        ew.setColumnWidth(2, 10);
        ew.setColumnWidth(3, 200);

        for (int i = 0; i < infos.size(); i++) {
            int y = i + 1;
            FileInfoDTO v = infos.get(i);
            if (CollUtil.isEmpty(v.getOrgins())) {
                continue;
            }

            indicator.setText("导出excel内容: " + v.getClassFullName());

            ew.getCell(0, y, true).setCellValue(v.getSourceFile().getPath());
            ew.getCell(1, y, true).setCellValue(v.getClassFullName());
            ew.getCell(2, y, true).setCellValue(v.getModel());
            Cell cell = ew.getCell(3, y, true);
            StringBuilder sb = new StringBuilder(1000);
            for (VirtualFile of : v.getOrgins()) {
                if (sb.length() > 0) {
                    sb.append('\n');
                }
                sb.append(of.getPath());
            }
            cell.setCellValue(sb.toString());

            ew.getCell(0, y, true).setCellStyle(setBorder);
            ew.getCell(1, y, true).setCellStyle(setBorder);
            ew.getCell(2, y, true).setCellStyle(setBorder);
            ew.getCell(3, y, true).setCellStyle(setBorder);
        }

        ew.close();

        IoUtil.tryOpenFileExpolor(outFile);
    }

    public static void loadFile(Project project
            , List<FileInfoDTO> vs
            , File sourceFile
            , ProgressIndicator indicator
            , String modelName) {
        String path = sourceFile.getPath();
        path = StrUtil.replaceChars(path, new char[]{'\\', '/', File.separatorChar}, ".");
        String key = "src.public";
        int i = path.indexOf(key);
        if (i < 0) {
            key = "src.private";
            i = path.indexOf(key);
        }
        if (i < 0) {
            key = "src.client";
            i = path.indexOf(key);
        }
        if (i < 0) {
            return;
        }

        String classFullName = path.substring(i + key.length() + 1, path.lastIndexOf('.'));

        if (indicator != null) {
            indicator.setText("开始搜索类:" + classFullName);
        }

        Collection<VirtualFile> fs = ActionResultListTable.findClass(project
                , classFullName
                , true
                , false
                , false
        );

        if (CollUtil.isEmpty(fs)) {
            return;
        }

        String projectDir = StrUtil.replaceChars(
                project.getBasePath()
                , new char[]{'\\', '/', File.separatorChar}
                , File.separator
        );

        ArrayList<VirtualFile> fs2 = new ArrayList<>();
        for (VirtualFile f : fs) {
            if (StrUtil.replaceChars(
                    f.getPath()
                    , new char[]{'\\', '/', File.separatorChar}
                    , File.separator
            ).startsWith(projectDir)) {
                continue;
            }
            fs2.add(f);
        }

        if (CollUtil.isEmpty(fs2)) {
            return;
        }

        FileInfoDTO v = new FileInfoDTO()
                .setSourceFile(sourceFile)
                .setClassFullName(classFullName)
                .setModel(modelName)
                .setOrgins(fs2);
        init(project);
        if (vs == null) {
            vs = infos.get(project.getBasePath());
        }

        vs.add(v);
        infoMap.get(project.getBasePath()).put(v.getSourceFile().getPath(), v);
    }

    @Accessors(chain = true)
    @Data
    public static class FileInfoDTO {
        File sourceFile;
        Collection<VirtualFile> orgins;
        String model;
        String classFullName;
    }

    public static void init(Project project) {
        String key = project.getBasePath();
        if (infoMap.get(key) != null
                && infos.get(key) != null) {
            return;
        }

        try {
            reentrantReadWriteLock.writeLock().lock();
            if (infoMap.get(key) == null) {
                infoMap.put(key, new ConcurrentHashMap<>());
            }
            if (infos.get(key) == null) {
                infos.put(key, new CopyOnWriteArrayList<>());
            }
        } finally {
            reentrantReadWriteLock.writeLock().unlock();
        }
    }
}
