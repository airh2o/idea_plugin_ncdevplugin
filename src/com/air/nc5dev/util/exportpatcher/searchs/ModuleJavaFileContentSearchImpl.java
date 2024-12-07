package com.air.nc5dev.util.exportpatcher.searchs;

import com.air.nc5dev.enums.NcVersionEnum;
import com.air.nc5dev.util.IoUtil;
import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.vo.FileContentVO;
import com.air.nc5dev.util.idea.ProjectUtil;
import com.air.nc5dev.vo.ExportConfigVO;
import com.air.nc5dev.vo.ExportContentVO;
import com.air.nc5dev.vo.ModuleWarpVO;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * </br>
 * </br>
 * </br>
 *
 * @author 唐粟 Email:209308343@qq.com
 * @date 2024/11/26 0026 19:28
 * @project
 * @Version
 */
@Data
public abstract class ModuleJavaFileContentSearchImpl extends AbstractContentSearchImpl {
    @Override
    public void search(ExportContentVO contentVO) {
        //循环模块，根据编译对象情况，输出补丁
        CompilerModuleExtension compilerModuleExtension;
        Iterator<Map.Entry<String, Module>> moduleIterator = contentVO.moduleHomeDir2ModuleMap.entrySet().iterator();
        Map.Entry<String, Module> entry;
        //模块 自定义输出配置信息
        ExportConfigVO configVO;
        //src 源文件夹 顶级集合
        VirtualFile[] sourceRoots;
        //生产class输出文件夹
        VirtualFile classDir;
        //test class 输出文件夹
        String testClassDirPath;

        String moduleName;
        ModuleWarpVO module;
        while (moduleIterator.hasNext() && !contentVO.indicator.isCanceled()) {
            //循环所有的模块
            module = new ModuleWarpVO(moduleIterator.next().getValue());
            if (contentVO.getModuleName2ExportModuleNameMap().containsKey(module.getModule().getName())) {
                module.setConfigName(contentVO.getModuleName2ExportModuleNameMap().get(module.getModule().getName()));
            }
            if (!contentVO.getSelectExportModules().contains(module.getModule())) {
                //需要跳过的模块
                continue;
            }

            compilerModuleExtension = CompilerModuleExtension.getInstance(module.getModule());

            //读取自定义配置文件
            configVO = contentVO.module2ExportConfigVoMap.get(module.getModule());
            if (configVO.isIgnoreModule()) {
                return; //忽略模块!
            }

            sourceRoots = ModuleRootManager.getInstance(module.getModule()).getSourceRoots();
            classDir = compilerModuleExtension.getCompilerOutputPath();
            testClassDirPath = null == compilerModuleExtension.getCompilerOutputPathForTests()
                    ? null : compilerModuleExtension.getCompilerOutputPathForTests().getPath();
            for (VirtualFile sourceRoot : sourceRoots) {
                if (contentVO.indicator.isCanceled()) {
                    return;
                }

                //循环输出 NC 3大文件夹
                if (null == classDir) {
                    ProjectUtil.warnNotification("编译输出路径不存在!请重新build或者" +
                            "模块配置Paths-Compiler output选项必须要勾选的 Use module cpmpile output path！", null);
                    continue;
                }

                if (contentVO.exportModules && sourceRoot.getName().equals(NC_TYPE_PUBLIC)) {
                    contentVO.indicator.setText("public:" + sourceRoot.getPath());
                    export(NC_TYPE_PUBLIC, contentVO.outPath, module
                            , sourceRoot.getPath(), classDir.getPath(), testClassDirPath, contentVO);
                } else if (contentVO.exportModules && sourceRoot.getName().equals(NC_TYPE_PRIVATE)) {
                    contentVO.indicator.setText("private:" + sourceRoot.getPath());
                    export(NC_TYPE_PRIVATE, contentVO.outPath, module
                            , sourceRoot.getPath(), classDir.getPath(), testClassDirPath, contentVO);
                } else if (contentVO.exportHotwebsClass && sourceRoot.getName().equals(NC_TYPE_CLIENT)) {
                    contentVO.indicator.setText("client:" + sourceRoot.getPath());
                    export(NC_TYPE_CLIENT, contentVO.outPath, module
                            , sourceRoot.getPath(), classDir.getPath(), testClassDirPath, contentVO);
                } else if (null != testClassDirPath
                        && !configVO.noTest
                        && sourceRoot.getName().equals(NC_TYPE_TEST)) {
                    contentVO.indicator.setText("test:" + sourceRoot.getPath());
                    export(NC_TYPE_TEST, contentVO.outPath, module
                            , sourceRoot.getPath(), classDir.getPath(), testClassDirPath, contentVO);
                }
                //其他无视掉
            }


            //检查是否需要把代码打包成 jar文件
            if (configVO.toJar && !contentVO.indicator.isCanceled()) {
                File manifest = null;
                if (StringUtil.notEmpty(configVO.manifestFilePath)) {
                    manifest = new File(configVO.manifestFilePath);
                    if (!manifest.isFile()) {
                        manifest = null;
                    }
                }
                contentVO.indicator.setText("代码打包成jar:" + module.getConfigName());
                compressJar(new File(contentVO.outPath + File.separatorChar + module.getConfigName()),
                        configVO.toJarThenDelClass, manifest, contentVO);
            }

            //模块循环结束
        }
    }

    /**
     * 导出一个包       <br>
     * <br>
     * <br>
     * <br>
     *
     * @return void
     * @author air Email: 209308343@qq.com
     * @date 2020/1/16 0016 19:34
     * @Param [ncType, exportDir, Module, sourceRoot, classDir , testClassDir, hasJavaFile, noOutTestClass ,
     * contentVO]    NC3大文件夹，导出文件夹路径，模块, java源码路径，生产代码路径，test代码路径，
     */
    public void export(@NotNull String ncType, @NotNull String exportDir
            , @NotNull ModuleWarpVO module, @NotNull String sourceRoot
            , @NotNull String classDir, @Nullable String testClassDir, @NotNull ExportContentVO contentVO) {
        File sourceBaseDirFile = new File(sourceRoot);

        File classBaseDirFile;
        if (NC_TYPE_TEST.equals(ncType)) {
            classBaseDirFile = new File(testClassDir);
        } else {
            classBaseDirFile = new File(classDir);
        }

        //获取所有的 有源码的包路径文件夹！
        List<File> allSourcePackges = IoUtil.getAllLastPackges(sourceBaseDirFile);

        for (final File sourcePackge : allSourcePackges) {
            String packgePath = sourcePackge.getPath().substring(sourceBaseDirFile.getPath().length());
            //class文件位置
            File classFileDir = new File(classBaseDirFile, packgePath);

            copyClassAndJavaSourceFiles(sourcePackge, sourceBaseDirFile
                    , contentVO, exportDir, module
                    , ncType, packgePath, classFileDir, classDir);

            copyClassPathOtherFile(sourcePackge, exportDir, module
                    , ncType, packgePath, contentVO, classDir);
        }
    }

    /**
     * 把一个包里的 非代码文件 复制到补丁 对应位置里      <br>
     * 比如 wsdl json 各种类路径配置文件等     <br>
     * <br>
     * <br>
     *
     * @return void
     * @author air Email: 209308343@qq.com
     * @date 2020/2/4 0004 17:28
     * @Param [sourcePackge, exportDir, moduleName, ncType, packgePath]
     */
    public void copyClassPathOtherFile(File sourcePackge
            , String exportDir, ModuleWarpVO module, String ncType
            , String packgePath, ExportContentVO contentVO, String classDir) {
        //复制包路径文件夹内所有其他文件，比如 wsdl文件 配置文件等等
        Stream.of(sourcePackge.listFiles()).forEach(file -> {
            if (!file.isFile()) {
                return;
            }
            final String fileName = file.getName().toLowerCase();
            if (fileName.endsWith(".class")
                    || fileName.endsWith(".java")) {
                return;
            }

            if (!canExport(contentVO, file, sourcePackge, null, exportDir
                    , module, packgePath, null, ncType)) {
                return;
            }

            //检查是否有特殊模块路径设置
            String configKey = packgePath + '.' + file.getName();
            if (configKey.charAt(0) == '/'
                    || configKey.charAt(0) == '\\'
                    || configKey.charAt(0) == File.separatorChar) {
                configKey = configKey.substring(1);
            }
            configKey = StringUtil.replaceAll(configKey, "/", ".");
            configKey = StringUtil.replaceAll(configKey, "\\", ".");
            configKey = StringUtil.replaceAll(configKey, File.separator, ".");

            String outModuleName = getOutModuleName(contentVO, configKey, null, module, module.getConfigName());
            String outClasPathOtherFileDirPath = exportDir + File.separatorChar + (StringUtil.isEmpty(outModuleName)
                    ? module.getConfigName() : outModuleName);
            File outClasPathOtherFileDir = null;
            if (NC_TYPE_PUBLIC.equals(ncType)) {
                outClasPathOtherFileDir = new File(outClasPathOtherFileDirPath, "classes");
            } else if (NC_TYPE_PRIVATE.equals(ncType)) {
                outClasPathOtherFileDir = new File(outClasPathOtherFileDirPath, "META-INF" + File.separatorChar +
                        "classes");
            } else if (NC_TYPE_CLIENT.equals(ncType)) {
                outClasPathOtherFileDir = new File(outClasPathOtherFileDirPath, "client" + File.separatorChar +
                        "classes");
            } else if (NC_TYPE_TEST.equals(ncType)) {
                outClasPathOtherFileDir = new File(outClasPathOtherFileDirPath, "test");
            }

            File outClasPathOtherFileDirFinal = new File(outClasPathOtherFileDir, packgePath);

            copyFile(file, outClasPathOtherFileDirFinal, contentVO, module, ncType, sourcePackge, null);
        });
    }

    /**
     * 把一个包 当前的文件夹内所有 java源代码和类文件 复制到补丁对应位置（不包含下级包）      <br>
     * <br>
     * <br>
     * <br>
     *
     * @return void
     * @author air Email: 209308343@qq.com
     * @date 2020/2/4 0004 17:31
     * @Param [sourcePackge, sourceBaseDirFile,  contentVO, exportDir, moduleName, ncType, packgePath,
     * classFileDir]
     */
    public void copyClassAndJavaSourceFiles(File sourcePackge, File sourceBaseDirFile
            , ExportContentVO contentVO, String exportDir, ModuleWarpVO module
            , String ncType, String packgePath, final File classFileDir, String classDir) {
        ExportConfigVO configVO = contentVO.module2ExportConfigVoMap.get(module.getModule());

        if (null == javapCommandPath
                || configVO.closeJavaP) {
            copyClassAndJavaSourceFilesBySource(sourcePackge, sourceBaseDirFile
                    , contentVO, exportDir, module
                    , ncType, packgePath, classFileDir, classDir);
        } else {
            copyClassAndJavaSourceFilesByClass(sourcePackge, sourceBaseDirFile
                    , contentVO, exportDir, module
                    , ncType, packgePath, classFileDir, classDir);
        }
    }

    /**
     * 通过循环 class文件 方式复制包里补丁         <br>
     * <br>
     * <br>
     * <br>
     *
     * @return void
     * @author air Email: 209308343@qq.com
     * @date 2020/2/22 0022 12:14
     * @Param [sourcePackge, sourceBaseDirFile,  contentVO, exportDir, moduleName, ncType, packgePath,
     * hasJavaFile, classFileDir]
     */
    public void copyClassAndJavaSourceFilesByClass(File sourcePackge, File sourceBaseDirFile
            , ExportContentVO contentVO, String exportDir, ModuleWarpVO module
            , String ncType, String packgePath, final File classFileDir, String classDir) {
        ExportConfigVO configVO = contentVO.module2ExportConfigVoMap.get(module.getModule());
        if (com.air.nc5dev.util.CollUtil.isEmpty(classFileDir.listFiles())) {
            return;
        }
        Stream.of(classFileDir.listFiles()).forEach(classFile -> {
            //循环复制所有java文件 ,因为idea编译后 没有分NC这3个文件夹！
            if (!classFile.isFile()) {
                return;
            }

            if (!classFile.getName().toLowerCase().endsWith(".class")) {
                return;
            }

            if (!canExport(contentVO, classFile, sourcePackge, sourceBaseDirFile, exportDir
                    , module, packgePath, classFile, ncType)) {
                return;
            }

            //获得源文件名字 C:\Users\airh2o\IdeaProjects\ncctest\out\production\mmmpsxj\nc\bs\pub\action\N_36D1_SIGNAL.class

            String javaFullName = StringUtil.get(getClassFileSourceFileName(classFile.getPath(), sourcePackge), "");

            //这里判断一下， 解决 如果 public新建了一个 和 private一模一样的包路径，会导致 private的class可能跑到了 public里！
            if (!new File(sourcePackge, javaFullName).isFile()
                    && !new File(sourcePackge, javaFullName + ".java").isFile()
            ) {
                return;
            }

            String pageJava = packgePath;
            if (pageJava.startsWith(File.separator)) {
                pageJava = pageJava.substring(File.separator.length());
            } else if (pageJava.startsWith("\\")) {
                pageJava = pageJava.substring("\\".length());
            } else if (pageJava.startsWith("/")) {
                pageJava = pageJava.substring("/".length());
            }
            String javaFullClassName = StringUtil.replaceAll(pageJava + "/" + javaFullName, File.separator, ".");
            javaFullClassName = StringUtil.replaceAll(javaFullClassName, "\\", ".");
            javaFullClassName = StringUtil.replaceAll(javaFullClassName, "/", ".");
            if (javaFullClassName.endsWith(".java")) {
                //移除后面的 .java 后缀
                javaFullClassName = javaFullClassName.substring(0, javaFullClassName.lastIndexOf('.'));
            }

            //获得类对于的模块名字
            String outModuleName = NC_TYPE_CLIENT.equals(ncType) && NcVersionEnum.isNCCOrBIP(contentVO.ncVersion) ?
                    module.getConfigName() : getOutModuleName(contentVO
                    , javaFullClassName, classFile, module, module.getConfigName());

            final String baseOutDirPath = exportDir + File.separatorChar + outModuleName;

            File outDir = null;
            if (NC_TYPE_PUBLIC.equals(ncType)) {
                outDir = new File(baseOutDirPath, "classes");
            } else if (NC_TYPE_PRIVATE.equals(ncType)) {
                outDir = new File(baseOutDirPath, "META-INF" + File.separatorChar + "classes");
            } else if (NC_TYPE_CLIENT.equals(ncType)) {
                outDir = new File(baseOutDirPath, "client" + File.separatorChar + "classes");
            } else if (NC_TYPE_TEST.equals(ncType)) {
                outDir = new File(baseOutDirPath, "test");
            }

            outDir = new File(outDir, packgePath);

            if (configVO.hasSource && StringUtil.notEmpty(javaFullName)) {
                //复制源码
                copyFile(new File(sourcePackge, javaFullName.endsWith(".java")
                                ? javaFullName : javaFullName + ".java")
                        , outDir
                        , contentVO
                        , module
                        , ncType
                        , sourceBaseDirFile
                        , null
                );
            }

            //复制class
            copyFile(classFile, outDir, contentVO, module, ncType, sourceBaseDirFile, classDir);
        });
    }


    /**
     * 通过循环源文件方式复制 包里补丁        <br>
     * 警告：同一个java源文件内非匿名 非public class会跳过复制！<br>
     * <br>
     * <br>
     *
     * @return void
     * @author air Email: 209308343@qq.com
     * @date 2020/2/22 0022 12:14
     * @Param [sourcePackge, sourceBaseDirFile,  contentVO, exportDir, moduleName, ncType, packgePath,
     * hasJavaFile, classFileDir]
     */
    public void copyClassAndJavaSourceFilesBySource(File sourcePackge, File sourceBaseDirFile
            , ExportContentVO contentVO, String exportDir, ModuleWarpVO module
            , String ncType, String packgePath, final File classFileDir, String classDir) {
        ExportConfigVO configVO = contentVO.module2ExportConfigVoMap.get(module.getModule());
        Stream.of(sourcePackge.listFiles()).forEach(javaFile -> {
            //循环复制所有java文件 ,因为idea编译后 没有分NC这3个文件夹！
            if (!javaFile.isFile()) {
                return;
            }

            if (!javaFile.getName().toLowerCase().endsWith(".java")) {
                return;
            }

            if (!canExport(contentVO, javaFile, sourcePackge, sourceBaseDirFile, exportDir
                    , module, packgePath, classFileDir, ncType)) {
                return;
            }

            String javaFullName = javaFile.getPath().substring((sourceBaseDirFile.getPath()).length() + 1);
            javaFullName = javaFullName.substring(0, javaFullName.length() - ".java".length());
            if (javaFullName.startsWith(File.separator)) {
                javaFullName = javaFullName.substring(File.separator.length());
            } else if (javaFullName.startsWith("\\")) {
                javaFullName = javaFullName.substring("\\".length());
            } else if (javaFullName.startsWith("/")) {
                javaFullName = javaFullName.substring("/".length());
            }
            String javaFullClassName = StringUtil.replaceAll(javaFullName, File.separator, ".");
            javaFullClassName = StringUtil.replaceAll(javaFullClassName, "\\", ".");
            javaFullClassName = StringUtil.replaceAll(javaFullClassName, "/", ".");
            //检查是否配置了 特殊输出路径
            String outModuleName = NC_TYPE_CLIENT.equals(ncType) && NcVersionEnum.isNCCOrBIP(contentVO.ncVersion) ?
                    module.getConfigName() : getOutModuleName(contentVO, javaFullClassName, javaFile, module
                    , module.getConfigName());

            final String baseOutDirPath = exportDir + File.separatorChar
                    + (StringUtil.isEmpty(outModuleName) ? module.getConfigName() : outModuleName);

            File outDir = null;
            if (NC_TYPE_PUBLIC.equals(ncType)) {
                outDir = new File(baseOutDirPath, "classes");
            } else if (NC_TYPE_PRIVATE.equals(ncType)) {
                outDir = new File(baseOutDirPath, "META-INF" + File.separatorChar + "classes");
            } else if (NC_TYPE_CLIENT.equals(ncType)) {
                outDir = new File(baseOutDirPath, "client" + File.separatorChar + "classes");
            } else if (NC_TYPE_TEST.equals(ncType)) {
                outDir = new File(baseOutDirPath, "test");
            }

            outDir = new File(outDir, packgePath);

            if (configVO.hasSource) {
                //复制源码
                contentVO.addOutFiles(
                        new FileContentVO()
                                .setModule(module)
                                .setSrcFile(javaFile.getPath())
                                .setSrcFileTo(new File(outDir, javaFile.getName()).getPath())
                                .setName(ncType)
                                .setSrcTop(sourceBaseDirFile == null ? null : sourceBaseDirFile.getPath())
                );
                // copyFile(javaFile, outDir, contentVO, module);
            }

            //复制class 这个麻烦，要正确导出匿名类。 如果是 一个java多个类 反人类写法不考虑让用户手工处理！
            File topClassOutDir = new File(CompilerModuleExtension.getInstance(module.getModule()).getCompilerOutputPath().getPath());
            List<File> classFiles = getJavaClassByClassName(javaFullClassName, classFileDir);
            for (File classFile : classFiles) {
                contentVO.addOutFiles(
                        new FileContentVO()
                                .setModule(module)
                                .setSrcFile(javaFile.getPath())
                                .setFile(classFile.getPath())
                                .setFileTo(new File(outDir, classFile.getName()).getPath())
                                .setName(ncType)
                                .setSrcTop(classDir)
                );
                //  copyFile(classFile, outDir, contentVO, module);
            }

        });
    }

    /**
     * 从Class文件 javap 读取 源文件名        <br>
     * FIXME 有效率问题，如果用二进制读取会更好        <br>
     * <br>
     * <br>
     *
     * @return java.lang.String
     * @author air Email: 209308343@qq.com
     * @date 2020/2/22 0022 13:04
     * @Param [classFilePath]
     */
    public String readClassFileSourceFileName(String classFilePath) {
        try {
            ProcessBuilder pb = new ProcessBuilder(javapCommandPath, "-v", classFilePath);
            Process p = pb.start();
            Scanner in = new Scanner(p.getInputStream());
            String s;
            String sourceFile = null;
            while (in.hasNextLine()) {
                s = in.nextLine().trim();
                if (s.length() > 0
                        && s.startsWith("SourceFile:")
                        && s.toLowerCase().endsWith(".java\"")) {
                    sourceFile = s.split(":")[1].trim();
                    sourceFile = sourceFile.substring(1, sourceFile.length() - 1);
                    break;
                }
            }
            in.close();
            p.destroy();
            return sourceFile;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 根据class文件 获取他的源码文件名字： 会根据javap命令来        <br>
     * <br>
     * <br>
     * <br>
     *
     * @return java.lang.String
     * @author air Email: 209308343@qq.com
     * @date 2020/2/22 0022 12:25
     * @Param [path, sourcePackge]
     */
    public String getClassFileSourceFileName(String path, File sourcePackge) {
        //先看看是否是 源文件的public类或public类里匿名类
        File classFile = new File(path);
        String classFileName = classFile.getName().substring(0, classFile.getName().lastIndexOf('.'));
        if (classFileName.indexOf('$') > 0) {
            classFileName = classFileName.substring(0, classFileName.indexOf('$'));
        }
        if (new File(sourcePackge, classFileName + ".java").exists()) {
            return classFileName;
        }

        String fileName = readClassFileSourceFileName(path);

        if (StringUtil.isEmpty(fileName)) {
            ProjectUtil.warnNotification(path + " class文件JAVAP方式无法找到源码,可以在配置文件关闭此方式!", null);
            return null;
        }

        if (new File(sourcePackge, fileName).exists()) {
            return fileName;
        }

        return fileName;
    }

    /**
     * 根据class全名，获得所有他的编译后class     <br>
     * <br>
     * <br>
     * <br>
     *
     * @return java.io.File
     * @author air Email: 209308343@qq.com
     * @date 2020/1/16 0016 20:42
     * @Param [classFullName, javaFileDir] class名 比如 nc.ui.gl.ErrorUI  , java源文件夹根目录
     */
    public List<File> getJavaClassByClassName(@NotNull String classFullName, @NotNull File classFileDir) {
        final String classPath = StringUtil.replaceAll(classFullName, ".", File.separator);
        if (com.air.nc5dev.util.CollUtil.isEmpty(classFileDir.listFiles())) {
            return com.air.nc5dev.util.CollUtil.emptyList();
        }

        return Stream.of(classFileDir.listFiles()).filter(file -> {
            if (file.getPath().lastIndexOf(classPath) < 0) {
                return false;
            }

            String classFullPath = file.getPath().substring(file.getPath().lastIndexOf(classPath), file.getPath()
                    .lastIndexOf('.'));
            return classFullPath.equals(classPath) || classFullPath.startsWith(classPath + '$');
        }).collect(Collectors.toList());
    }

}
