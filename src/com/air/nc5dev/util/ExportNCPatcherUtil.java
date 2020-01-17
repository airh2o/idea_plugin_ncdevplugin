package com.air.nc5dev.util;

import com.air.nc5dev.util.idea.ProjectUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 导出NC补丁 工具类      </br>
 * </br>
 * </br>
 * </br>
 *
 * @author air Email: 209308343@qq.com
 * @version NC505, JDK1.5+, V1.0
 * @date 2020/1/16 0016 18:28
 * @project
 */
public class ExportNCPatcherUtil {
    /**
     * 模块补丁输出配置文件名字
     **/
    public static final String MODULE_OUTPATCHER_CONFIG_FILENAME = "patcherconfig.properties";
    /**
     * NC 源码 3大类型
     **/
    public static final String NC_TYPE_PUBLIC = "public";
    /**
     * NC 源码 3大类型
     **/
    public static final String NC_TYPE_PRIVATE = "private";
    /**
     * NC 源码 3大类型
     **/
    public static final String NC_TYPE_CLIENT = "client";
    /**
     * test 代码
     **/
    public static final String NC_TYPE_TEST = "test";

    /**
     * 导出补丁到指定的文件夹   </br>
     * </br>
     * </br>
     * </br>
     *
     * @return void
     * @author air Email: 209308343@qq.com
     * @date 2020/1/16 0016 18:32
     * @Param [outPath, project] 输出文件夹，项目对象
     */
    public static final void export(@NotNull String outPath, @NotNull Project project) {
        //获得所有的 模块
        Module[] modules = ModuleManager.getInstance(project).getModules();
        //模块文件夹根路径 ： 模块对象
        HashMap<String, Module> moduleHomeDir2ModuleMap = new HashMap<>();
        for (Module module : modules) {
            moduleHomeDir2ModuleMap.put(new File(module.getModuleFilePath()).getParent(), module);
        }

        //循环模块，根据编译对象情况，输出补丁
        CompilerModuleExtension compilerModuleExtension;
        Iterator<Map.Entry<String, Module>> moduleIterator = moduleHomeDir2ModuleMap.entrySet().iterator();
        Map.Entry<String, Module> entry;
        //模块 自定义输出配置信息
        Properties modulePatcherConfig;
        //不输出 test
        boolean noOutTestClass = true;
        //是否导出 源文件
        boolean hasJavaFile = true;
        //src 源文件夹 顶级集合
        VirtualFile[] sourceRoots;
        //生产class输出文件夹
        VirtualFile classDir;
        //test class 输出文件夹
        VirtualFile testClassDir;
        while (moduleIterator.hasNext()) {
            entry = moduleIterator.next();
            compilerModuleExtension = CompilerModuleExtension.getInstance(entry.getValue());

            //读取自定义配置文件
            modulePatcherConfig = readModuleOutConfigFile(entry.getKey());
            hasJavaFile = !"false".equals(modulePatcherConfig.getProperty("exportsourcefile"));
            noOutTestClass = !"false".equals(modulePatcherConfig.getProperty("config-notest"));

            sourceRoots = ModuleRootManager.getInstance(entry.getValue()).getSourceRoots();
            classDir = compilerModuleExtension.getCompilerOutputPath();
            testClassDir = compilerModuleExtension.getCompilerOutputPathForTests();

            //循环输出 NC 3大文件夹
            for (VirtualFile sourceRoot : sourceRoots) {
                if (sourceRoot.getName().equals(NC_TYPE_PUBLIC)) {
                    export(NC_TYPE_PUBLIC, outPath, entry.getValue().getName()
                            , sourceRoot.getPath(), classDir.getPath(), testClassDir.getPath()
                            , hasJavaFile, noOutTestClass, modulePatcherConfig);
                } else if (sourceRoot.getName().equals(NC_TYPE_PRIVATE)) {
                    export(NC_TYPE_PRIVATE, outPath, entry.getValue().getName()
                            , sourceRoot.getPath(), classDir.getPath(), testClassDir.getPath()
                            , hasJavaFile, noOutTestClass, modulePatcherConfig);
                } else if (sourceRoot.getName().equals(NC_TYPE_CLIENT)) {
                    export(NC_TYPE_CLIENT, outPath, entry.getValue().getName()
                            , sourceRoot.getPath(), classDir.getPath(), testClassDir.getPath()
                            , hasJavaFile, noOutTestClass, modulePatcherConfig);
                } else if (!noOutTestClass
                        && sourceRoot.getName().equals(NC_TYPE_TEST)) {
                    export(NC_TYPE_TEST, outPath, entry.getValue().getName()
                            , sourceRoot.getPath(), classDir.getPath(), testClassDir.getPath()
                            , hasJavaFile, noOutTestClass, modulePatcherConfig);
                }
                //其他无视掉
            }

            //复制模块配置文件！
            IoUtil.copyAllFile(new File(new File(entry.getValue().getModuleFilePath()).getParentFile(), "META-INF")
                    , new File(outPath + File.separatorChar + entry.getValue().getName() + File.separatorChar + "META-INF"));
        }
    }

    /**
     * 导出一个包       </br>
     * </br>
     * </br>
     * </br>
     *
     * @return void
     * @author air Email: 209308343@qq.com
     * @date 2020/1/16 0016 19:34
     * @Param [ncType, exportDir, moduleName, sourceRoot, classDir , testClassDir, hasJavaFile, noOutTestClass , modulePatcherConfig]    NC3大文件夹，导出文件夹路径，模块名字, java源码路径，生产代码路径，test代码路径，是否导出源码，是否不输出test代码
     */
    public static void export(@NotNull String ncType, @NotNull String exportDir
            , @NotNull String moduleName, @NotNull String sourceRoot
            , @NotNull String classDir, @NotNull String testClassDir
            , boolean hasJavaFile, boolean noOutTestClass, @NotNull Properties modulePatcherConfig) {
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
            final String packgePath = sourcePackge.getPath().substring(sourceBaseDirFile.getPath().length());
            //class文件位置
            final File classFileDir = new File(classBaseDirFile, packgePath);

            //循环复制所有java ,因为idea编译后 没有分NC这3个文件夹！
            Stream.of(sourcePackge.listFiles()).forEach(javaFile -> {
                if (!javaFile.isFile()) {
                    return;
                }
                String javaFullName = javaFile.getPath().substring((classBaseDirFile.getPath() + ncType).length() + 1);
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
                String outModuleName = modulePatcherConfig.getProperty(javaFullClassName);
                final String baseOutDirPath = exportDir + File.separatorChar
                        + (StringUtil.isEmpty(outModuleName) ? moduleName : outModuleName);
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

                if (hasJavaFile) {
                    //复制源码
                    IoUtil.copyFile(javaFile, outDir);
                }
                //复制class 这个麻烦，要正确导出匿名类。 如果是 一个java多个类 反人类写法不考虑让用户手工处理！
                List<File> classFiles = getJavaClassByClassName(javaFullClassName, classFileDir);
                for (File classFile : classFiles) {
                    IoUtil.copyFile(classFile, outDir);
                }

            });
        }
    }

    /**
     * 根据class全名，获得所有他的编译后class     </br>
     * </br>
     * </br>
     * </br>
     *
     * @return java.io.File
     * @author air Email: 209308343@qq.com
     * @date 2020/1/16 0016 20:42
     * @Param [classFullName, javaFileDir] class名 比如 nc.ui.gl.ErrorUI  , java源文件夹根目录
     */
    private static List<File> getJavaClassByClassName(@NotNull String classFullName, @NotNull File classFileDir) {
        final String classPath = StringUtil.replaceAll(classFullName, ".", File.separator);
        return Stream.of(classFileDir.listFiles()).filter(file -> {
            if (file.getPath().lastIndexOf(classPath) < 0) {
                return false;
            }

            String classFullPath = file.getPath().substring(file.getPath().lastIndexOf(classPath), file.getPath().lastIndexOf('.'));
            return classFullPath.equals(classPath) || classFullPath.startsWith(classPath + '$');
        }).collect(Collectors.toList());
    }

    /**
     * 读取模块补丁输出设置的配置文件       </br>
     * </br>
     * </br>
     * </br>
     *
     * @return java.util.Properties
     * @author air Email: 209308343@qq.com
     * @date 2020/1/16 0016 18:47
     * @Param [moduleDir]
     */
    public static Properties readModuleOutConfigFile(@NotNull String moduleDir) {
        Properties configProperties = new Properties();
        File config = new File(moduleDir, MODULE_OUTPATCHER_CONFIG_FILENAME);
        if (!config.isFile()) {
            return configProperties;
        }

        try {
            FileInputStream fileInputStream = new FileInputStream(config);
            configProperties.load(fileInputStream);
            fileInputStream.close();
            return configProperties;
        } catch (IOException e) {
            return configProperties;
        }
    }
}
