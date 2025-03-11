package com.air.nc5dev.util.exportpatcher.searchs;

import cn.hutool.core.collection.CollUtil;
import com.air.nc5dev.enums.NcVersionEnum;
import com.air.nc5dev.util.IdeaProjectGenerateUtil;
import com.air.nc5dev.util.IoUtil;
import com.air.nc5dev.util.ProjectNCConfigUtil;
import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.vo.FileContentVO;
import com.air.nc5dev.util.idea.ProjectUtil;
import com.air.nc5dev.vo.ExportConfigVO;
import com.air.nc5dev.vo.ExportContentVO;
import com.air.nc5dev.vo.ModuleWarpVO;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.roots.CompilerModuleExtension;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.jar.Manifest;

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
public abstract class AbstractContentSearchImpl implements IFileContentSearch {
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

    public String javapCommandPath = null;

    @Override
    public void process(ExportContentVO contentVO) {
        initJavap(contentVO);
        initModules(contentVO);


        search(contentVO);
    }

    public abstract void search(ExportContentVO contentVO);

    public static void initModules(ExportContentVO contentVO) {
        if (contentVO.modules != null) {
            return;
        }
        //获得所有的 模块
        Module[] modules = IdeaProjectGenerateUtil.getProjectModules(contentVO.project);
        contentVO.modules = CollUtil.toList(modules);
        if (contentVO.moduleHomeDir2ModuleMap == null
                || contentVO.moduleHomeDir2ModuleMap.isEmpty()) {
            contentVO.moduleHomeDir2ModuleMap = new HashMap<>();
            contentVO.initSelectModules();
        }
    }

    public void initJavap(ExportContentVO contentVO) {
        if (javapCommandPath != null) {
            return;
        }

        final String exe = "bin" + File.separatorChar + "javap.exe";

        File javaHomePathFile = IoUtil.getJavaHomePathFile(ProjectNCConfigUtil.getNCHomePath(), exe);
        if (javaHomePathFile != null) {
            javapCommandPath = javaHomePathFile.getPath();
        }
    }

    /**
     * 根据 补丁模块配置文件 获得模块名        <br>
     * <br>
     * <br>
     * <br>
     *
     * @return java.lang.String
     * @author air Email: 209308343@qq.com
     * @date 2020/3/30 0030 16:56
     * @Param [ contentVO, javaFullClassName,guass]
     */
    public static String getOutModuleName(ExportContentVO contentVO, String javaFullClassName
            , File classFile, ModuleWarpVO module, String defalut) {
        if (StringUtil.isEmpty(javaFullClassName)) {
            return defalut;
        }

        ExportConfigVO configVO = contentVO.module2ExportConfigVoMap.get(module.getModule());

        String outModuleName = null;
        //最优先 使用类全路径
        outModuleName = configVO.getProperty(javaFullClassName);
        if (StringUtil.notEmpty(outModuleName)) {
            return outModuleName;
        }

        //其实优先通配符
        String cn = javaFullClassName;
        int left = 0;
        while ((left = cn.lastIndexOf('.')) > 0) {
            cn = cn.substring(0, cn.lastIndexOf('.'));
            outModuleName = configVO.getProperty(cn);

            if (StringUtil.notEmpty(outModuleName)) {
                return outModuleName;
            }
        }

        //最后如果没有，就看看是否启用了 模块名猜测
        if (null != classFile
                && configVO.guessModule
                && StringUtil.isEmpty(outModuleName)
                && !classFile.getName().startsWith("CHG")
                && !classFile.getName().startsWith("N_")) {
            //猜测模块
            outModuleName = getPackgeName(javaFullClassName, 3, ".");

            if (StringUtil.notEmpty(outModuleName)) {
                return outModuleName;
            }
        }

        //都没有，返回默认
        return defalut;
    }


    public static boolean canExport(ExportContentVO contentVO, File file, @Nullable File sourcePackge
            , @Nullable File sourceBaseDirFile, @Nullable String exportDir, @Nullable ModuleWarpVO module
            , @Nullable String packgePath, @Nullable File classFileDir, String ncType) {
        if (!contentVO.isSelectExport() || file == null) {
            return true;
        }

        String path = file.getPath();
        path = StringUtil.replaceChars(path, "/", File.separator);
        path = StringUtil.replaceChars(path, "\\", File.separator);
        String orginPath = path;
        if (contentVO.getSelectFiles().contains(path)) {
            return true;
        }

        String out = null;
        String modulePath = null;
        if (module != null) {
            CompilerModuleExtension compilerModuleExtension = CompilerModuleExtension.getInstance(module.getModule());
            out = compilerModuleExtension.getCompilerOutputPath().getPath();
            out = StringUtil.replaceChars(out, "/", File.separator);
            out = StringUtil.replaceChars(out, "\\", File.separator);
            if (out.indexOf(out.length() - 1) != File.separatorChar) {
                out = out + File.separatorChar;
            }

            modulePath = IdeaProjectGenerateUtil.getModuleBaseDir(module.getModule());
            modulePath = StringUtil.replaceChars(modulePath, "/", File.separator);
            modulePath = StringUtil.replaceChars(modulePath, "\\", File.separator);
        }

        for (String key : contentVO.getSelectFiles()) {
            String s = key;
            if (sourceBaseDirFile != null) {
                s = StringUtil.removeAll(key, sourceBaseDirFile.getPath() + File.separatorChar);
            }
            if (StringUtil.endsWith(s.toLowerCase(), ".java")) {
                s = s.substring(0, s.length() - 5);
            }
            if (out != null) {
                path = StringUtil.removeAll(path, out);
            }

            if (StringUtil.startsWith(path, s)) {
                //选择了这个文件或者这个文件所属的文件夹或上级文件夹！
                return true;
            }

            if (StringUtil.equals(s, modulePath)) {
                //选择了整个模块！
                return true;
            }

            //3个文件夹
            if (StringUtil.endsWith(s, File.separatorChar + NC_TYPE_PRIVATE)
                    || StringUtil.endsWith(s, File.separatorChar + NC_TYPE_PUBLIC)
                    || StringUtil.endsWith(s, File.separatorChar + NC_TYPE_CLIENT)
                    || StringUtil.endsWith(s, File.separatorChar + NC_TYPE_TEST)
            ) {
                String s2 = s;
                boolean is = false;
                if (StringUtil.endsWith(s2, File.separatorChar + NC_TYPE_PRIVATE)) {
                    s2 = StringUtil.removeEnd(s2, File.separatorChar + NC_TYPE_PRIVATE);
                    is = NC_TYPE_PRIVATE.equals(ncType);
                } else if (StringUtil.endsWith(s2, File.separatorChar + NC_TYPE_PUBLIC)) {
                    s2 = StringUtil.removeEnd(s2, File.separatorChar + NC_TYPE_PUBLIC);
                    is = NC_TYPE_PUBLIC.equals(ncType);
                } else if (StringUtil.endsWith(s2, File.separatorChar + NC_TYPE_CLIENT)) {
                    s2 = StringUtil.removeEnd(s2, File.separatorChar + NC_TYPE_CLIENT);
                    is = NC_TYPE_CLIENT.equals(ncType);
                } else if (StringUtil.endsWith(s2, File.separatorChar + NC_TYPE_TEST)) {
                    s2 = StringUtil.removeEnd(s2, File.separatorChar + NC_TYPE_TEST);
                    is = NC_TYPE_TEST.equals(ncType);
                }
                s2 = StringUtil.removeEnd(s2, File.separatorChar + "src");
                s2 = StringUtil.removeEnd(s2, File.separatorChar + "java");
                if (is && StringUtil.equals(s2, modulePath)) {
                    //选择了模块的3个文件夹某一个
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 把一个文件 复制到指定的文件夹里 <br>
     * 会自动创建不存在的文件夹      <br>
     * <br>
     * <br>
     *
     * @return void
     * @author air Email: 209308343@qq.com
     * @date 2020/1/16 0016 20:07
     * @Param [from, toDir]
     */
    public static void copyFile(@NotNull File from, @NotNull final File dir
            , ExportContentVO contentVO
            , ModuleWarpVO module
            , String ncType
            , File sourceBaseDirFile
            , String classDir) {
        contentVO.addOutFiles(new FileContentVO()
                .setModule(module)
                .setFile(from.getPath())
                .setFileTo(dir.getPath())
                .setFilter(f -> {
                    if (Objects.nonNull(contentVO) && Objects.nonNull(module)) {
                        //检查是否配置了 文件不复制！
                        ExportConfigVO cf = contentVO.module2ExportConfigVoMap.get(module.getModule());
                        String path = f.getPath();
                        if (cf.ignoreFiles.stream().anyMatch(reg -> isMatch(path, reg))) {
                            return false;
                        }
                    }

                    return true;
                })
                .setName(ncType)
                .setSrcTop(classDir != null ? classDir : (sourceBaseDirFile == null ? null : sourceBaseDirFile.getPath()))
        );
    }

    /**
     * 判断一个 全路径的文件，他 是否 符合 配置文件里 配置的 包或精确路径
     *
     * @param path
     * @param regx
     * @return
     */
    public static boolean isMatch(String file, String regx) {
        File f = new File(file);
        if (!f.exists()) {
            return false;
        }

        if (f.isFile()) {

        }

        String s = StringUtil.replaceAll(f.getPath(), "\\", ".");
        s = StringUtil.replaceAll(s, "/", ".");

        return s.contains(regx);
    }

    /**
     * 获得第几个包名字       <br>
     * <br>
     * <br>
     * <br>
     *
     * @return java.lang.String
     * @author air Email: 209308343@qq.com
     * @date 2020/3/24 0024 13:23
     * @Param [p, packgeIndex, split]
     */
    public static String getPackgeName(String p, int packgeIndex, String split) {
        String packgeThreeName = p;

        for (int i = 1; i < packgeIndex; i++) {
            packgeThreeName = packgeThreeName.substring(packgeThreeName.indexOf(split) + 1);
        }

        return packgeThreeName.substring(0, packgeThreeName.indexOf(split));
    }


    /**
     * 打包模块class到jar文件       <br>
     * <br>
     * <br>
     * <br>
     *
     * @return void
     * @author air Email: 209308343@qq.com
     * @date 2020/2/9 0009 14:50
     * @Param [moduleHomeDir, compressEndDeleteClass, contentVO]  模块路径， 是否不保留class文件  true删除class文件,模块配置文件
     */
    public static List<File> compressJar(File moduleHomeDir, boolean compressEndDeleteClass, File manifest,
                                         ExportContentVO contentVO) {
        ArrayList<File> fs = new ArrayList<>();
        //public
        fs.addAll(compressJar(new File(moduleHomeDir
                        , "classes"
                )
                , new File(moduleHomeDir, "lib")
                , "public_" + moduleHomeDir.getName()
                , manifest));
        //private
        fs.addAll(compressJar(new File(moduleHomeDir
                        , "META-INF"
                        + File.separatorChar + "classes"
                )
                , new File(moduleHomeDir, "META-INF"
                        + File.separatorChar + "lib"
                )
                , "private_" + moduleHomeDir.getName()
                , manifest));
        //client
        if (!NcVersionEnum.isNCCOrBIP(contentVO.ncVersion)) {
            fs.addAll(compressJar(new File(moduleHomeDir
                            , "client"
                            + File.separatorChar + "classes"
                    )
                    , new File(moduleHomeDir, "client"
                            + File.separatorChar + "lib"
                    )
                    , "ui_" + moduleHomeDir.getName()
                    , manifest));
        }

        //删除打包前文件
        if (compressEndDeleteClass) {
            //public
            IoUtil.cleanUpDirFiles(new File(moduleHomeDir, "classes"));
            //private
            IoUtil.cleanUpDirFiles(new File(moduleHomeDir
                            , "META-INF"
                            + File.separatorChar + "classes"
                    )
            );
            //client
            if (!NcVersionEnum.isNCCOrBIP(contentVO.ncVersion)) {
                IoUtil.cleanUpDirFiles(new File(moduleHomeDir
                                , "client"
                                + File.separatorChar + "classes"
                        )
                );
            }
        }

        return fs;
    }

    /**
     * 打包成jar文件   <br>
     * <br>
     * <br>
     * <br>
     *
     * @return java.io.File
     * @author air Email: 209308343@qq.com
     * @date 2020/2/9 0009 14:53
     * @Param [dir, outDir, jarName,manifest] 要打包的文件，要输出到的文件夹，jar文件名（注意，源码会自动加_src后缀），manifest
     */
    public static List<File> compressJar(File dir, File outDir, String jarName, File manifest) {
        try {
            if (dir.listFiles() == null
                    || dir.listFiles().length < 1
                    || (dir.listFiles().length < 2 && dir.listFiles()[0].getName().equals("META-INF"))) {
                return new ArrayList<>();
            }
            IoUtil.makeDirs(outDir);
            //创建MANIFEST.MF

            Manifest maniFest = getManiFest(manifest, dir.getName());

            File jarFile = new File(outDir, jarName + ".jar");
            File jarSrcFile = new File(outDir, jarName + "_src.jar");
            IoUtil.makeJar(dir, jarFile, maniFest, new String[]{".java"});
            IoUtil.makeJar(dir, jarSrcFile, maniFest, new String[]{".class"});
            return CollUtil.toList(jarFile, jarSrcFile);
        } catch (Exception e) {
            e.printStackTrace();
            RuntimeException ex = new RuntimeException(e.getMessage(), e);
            ex.setStackTrace(e.getStackTrace());
            throw ex;
        } finally {
        }
    }

    /**
     * 获得jar的manifest文件，如果穿的file是null 就够就默认的格式      <br>
     * <br>
     * <br>
     * <br>
     *
     * @return void
     * @author air Email: 209308343@qq.com
     * @date 2020/4/1 0001 22:31
     * @Param [manifest]
     */
    public static Manifest getManiFest(File manifest, String name) {
        Manifest minf = null;
        PrintWriter printWriter = null;
        try {
            if (null == manifest) {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                printWriter = new PrintWriter(byteArrayOutputStream);
                printWriter.println("Manifest-Version: " + "1.0");
                printWriter.println("Class-Path: " + "");
                printWriter.println("Main-Class: " + "");
                printWriter.println("Name: " + name);
                printWriter.println("Specification-Title: " + "");
                printWriter.println("Specification-Version: " + "1.0");
                printWriter.println("Specification-Vendor: " + "");
                printWriter.println("Implementation-Title: " + "");
                printWriter.println("Implementation-Version: " + "");
                printWriter.println("Implementation-Vendor: " + "");
                printWriter.println("CreateDate: " + DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                        .format(LocalDateTime.now()));
                printWriter.println("Created-By: " + (
                        ProjectUtil.getProject() != null
                                ? ProjectUtil.getProject().getName() : ""
                ));
                printWriter.println("Created-ByIde: power by air QQ:209308343@qq.com 微信:yongyourj");
                printWriter.println("IDEA-Plugin: https://gitee.com/yhlx/idea_plugin_nc5devplugin");
                printWriter.flush();
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream
                        .toByteArray());
                minf = new Manifest(byteArrayInputStream);
            } else {
                minf = new Manifest(new FileInputStream(manifest));
            }
        } catch (IOException e) {
            RuntimeException ex = new RuntimeException(e.getMessage(), e);
            ex.setStackTrace(e.getStackTrace());
            throw ex;
        } finally {
            IoUtil.close(printWriter);
        }

        return minf;
    }

}
