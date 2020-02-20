package com.air.nc5dev.util.idea;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ModuleRootModificationUtil;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.vfs.VirtualFileManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 项目 依赖路径管理 工具</br>
 * </br>
 * </br>
 * </br>
 *
 * @author air Email: 209308343@qq.com
 * @version NC505, JDK1.5+, V1.0
 * @date 2019/12/25 0025 8:55
 * @project
 */
public class ApplicationLibraryUtil {


    /**
     * 往 IDEA 的项目 jar依赖增加一个 依赖库，如果依赖库已存在 就删除在增加
     *
     * @param theProject
     * @param libraryName
     * @param files
     */
    public static final void addApplicationLibrary(@Nullable Project theProject, @Nonnull String libraryName, @Nonnull List<File> files) {
        Project project = null == theProject ? ProjectUtil.getDefaultProject() : theProject;
        final LibraryTable.ModifiableModel model = LibraryTablesRegistrar.getInstance().getLibraryTable(project).getModifiableModel();

        LibraryEx library = (LibraryEx) model.getLibraryByName(libraryName);
        // 库存在创建新的
        if (library != null) {
            model.removeLibrary(library);
        }
        library = (LibraryEx) model.createLibrary(libraryName);

        final LibraryEx.ModifiableModelEx libraryModel = library.getModifiableModel();

        //参数转换成路径集合
        List<String> classesRoots = files.stream().map(file -> file.getPath()).collect(Collectors.toList());

        // 加入新的依赖路径
        for (String root : classesRoots) {
            if (root.toLowerCase().endsWith("_src.jar")) {
                libraryModel.addRoot(VirtualFileManager.constructUrl("jar", root + "!/"), OrderRootType.SOURCES);
            } else if (root.toLowerCase().endsWith(".jar")) {
                // 注意jar格式jar:{path_to_jar}.jar!/
                libraryModel.addRoot(VirtualFileManager.constructUrl("jar", root + "!/"), OrderRootType.CLASSES);
            } else if (root.toLowerCase().endsWith(".class")) {
                libraryModel.addRoot(VirtualFileManager.constructUrl("file", root), OrderRootType.CLASSES);
                libraryModel.addRoot(VirtualFileManager.constructUrl("file", root), OrderRootType.SOURCES);
            } else if (root.toLowerCase().endsWith("resources")) {
                libraryModel.addRoot(VirtualFileManager.constructUrl("file", root), OrderRootType.CLASSES);
                libraryModel.addRoot(VirtualFileManager.constructUrl("file", root), OrderRootType.SOURCES);
            } else {
                libraryModel.addRoot(VirtualFileManager.constructUrl("file", root), OrderRootType.CLASSES);
                libraryModel.addRoot(VirtualFileManager.constructUrl("file", root), OrderRootType.SOURCES);
            }
        }

        // 提交库变更
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            @Override
            public void run() {
                libraryModel.commit();
                model.commit();
            }
        });

        // 向项目模块依赖中增加新增的库
        Module[] modules = ModuleManager.getInstance(project).getModules();
        for (Module module : modules) {
            if (ModuleRootManager.getInstance(module).getModifiableModel().findLibraryOrderEntry(library) == null) {
                ModuleRootModificationUtil.addDependency(module, library);
            }
        }
    }


    private ApplicationLibraryUtil() {
        throw new RuntimeException("cannot instance Util Class!");
    }
}
