package com.air.nc5dev.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import com.air.nc5dev.enums.NcVersionEnum;
import com.air.nc5dev.util.idea.LogUtil;
import com.air.nc5dev.util.idea.ProjectUtil;
import com.air.nc5dev.util.jdbc.ConnectionUtil;
import com.air.nc5dev.vo.ExportConfigVO;
import com.air.nc5dev.vo.ExportContentVO;
import com.air.nc5dev.vo.ItemsItemVO;
import com.air.nc5dev.vo.NCDataSourceVO;
import com.google.common.base.Splitter;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.jar.Manifest;
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

    public static String javapCommandPath = null;

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
    public static final void export(@NotNull ExportContentVO contentVO) {
        if (null == javapCommandPath) {
            initJavap();
        }

        //获得所有的 模块
        Module[] modules = ModuleManager.getInstance(contentVO.project).getModules();
        contentVO.modules = CollUtil.toList(modules);
        //模块文件夹根路径 ： 模块对象
        for (Module module : modules) {
            contentVO.moduleHomeDir2ModuleMap.put(new File(module.getModuleFilePath()).getParent(), module);
        }

        //排除配置文件里设置的 不需要的模块
        contentVO.moduleHomeDir2ModuleMap.forEach((path, module) -> {
            ExportConfigVO configVO = loadExportConfig(path, module);
            contentVO.module2ExportConfigVoMap.put(module, configVO);

            if (configVO.ignoreModule) {
                contentVO.ignoreModules.add(module);
            }
        });

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
        while (moduleIterator.hasNext()) {
            //循环所有的模块
            entry = moduleIterator.next();

            if (contentVO.ignoreModules.contains(entry.getValue())) {
                //需要跳过的模块
                continue;
            }

            compilerModuleExtension = CompilerModuleExtension.getInstance(entry.getValue());

            //读取自定义配置文件
            configVO = contentVO.module2ExportConfigVoMap.get(entry.getValue());

            sourceRoots = ModuleRootManager.getInstance(entry.getValue()).getSourceRoots();
            classDir = compilerModuleExtension.getCompilerOutputPath();
            testClassDirPath = null == compilerModuleExtension.getCompilerOutputPathForTests()
                    ? null : compilerModuleExtension.getCompilerOutputPathForTests().getPath();
            for (VirtualFile sourceRoot : sourceRoots) {
                //循环输出 NC 3大文件夹
                if (null == classDir) {
                    ProjectUtil.warnNotification("编译输出路径不存在!请重新build或者" +
                            "模块配置Paths-Compiler output选项必须要勾选的 Use module cpmpile output path！", null);
                    continue;
                }

                if (sourceRoot.getName().equals(NC_TYPE_PUBLIC)) {
                    contentVO.indicator.setText("public:" + sourceRoot.getPath());
                    export(NC_TYPE_PUBLIC, contentVO.outPath, entry.getValue()
                            , sourceRoot.getPath(), classDir.getPath(), testClassDirPath, contentVO);
                } else if (sourceRoot.getName().equals(NC_TYPE_PRIVATE)) {
                    contentVO.indicator.setText("private:" + sourceRoot.getPath());
                    export(NC_TYPE_PRIVATE, contentVO.outPath, entry.getValue()
                            , sourceRoot.getPath(), classDir.getPath(), testClassDirPath, contentVO);
                } else if (sourceRoot.getName().equals(NC_TYPE_CLIENT)) {
                    contentVO.indicator.setText("client:" + sourceRoot.getPath());
                    export(NC_TYPE_CLIENT, contentVO.outPath, entry.getValue()
                            , sourceRoot.getPath(), classDir.getPath(), testClassDirPath, contentVO);
                } else if (null != testClassDirPath
                        && !configVO.noTest
                        && sourceRoot.getName().equals(NC_TYPE_TEST)) {
                    contentVO.indicator.setText("test:" + sourceRoot.getPath());
                    export(NC_TYPE_TEST, contentVO.outPath, entry.getValue()
                            , sourceRoot.getPath(), classDir.getPath(), testClassDirPath, contentVO);
                }
                //其他无视掉
            }

            //复制模块配置文件！
            File umpDir = new File(new File(entry.getValue().getModuleFilePath()).getParentFile(), "META-INF");
            if (umpDir.isDirectory()) {
                contentVO.indicator.setText("导出模块配置文件:" + umpDir.getPath());
                IoUtil.copyAllFile(umpDir
                        , new File(contentVO.outPath + File.separatorChar + entry.getValue().getName() + File
                                .separatorChar +
                                "META-INF"));
            }

            File bmfDir = new File(new File(entry.getValue().getModuleFilePath()).getParentFile(), "METADATA");

            //复制模块元数据
            if (bmfDir.isDirectory()) {
                contentVO.indicator.setText("导出模块元数据:" + umpDir.getPath());
                IoUtil.copyAllFile(bmfDir
                        , new File(contentVO.outPath + File.separatorChar + entry.getValue().getName() + File
                                .separatorChar +
                                "METADATA"));
            }

            //检查是否需要把代码打包成 jar文件
            if (configVO.toJar) {
                File manifest = null;
                if (StringUtil.notEmpty(configVO.manifestFilePath)) {
                    manifest = new File(configVO.manifestFilePath);
                    if (!manifest.isFile()) {
                        manifest = null;
                    }
                }
                contentVO.indicator.setText("代码打包成jar:" + entry.getValue().getName());
                compressJar(new File(contentVO.outPath + File.separatorChar + entry.getValue().getName()),
                        configVO.toJarThenDelClass, manifest, contentVO);
            }

            //模块循环结束
        }

        //处理NCC特殊的模块补丁结构
        //if (NcVersionEnum.NCC.equals(ProjectNCConfigUtil.getNCVerSIon())) {
        if (contentVO.rebuildsql) {
            contentVO.indicator.setText("强制直接连接数据库生成SQL合并文件...");
            reBuildNCCSqlAndFrontFiles(contentVO);
        } else {
            contentVO.indicator.setText("使用用友开发工具导出的SQL脚本文件进行SQL合并...");
            buildNCCSqlAndFrontFiles(contentVO);
        }
        //}
    }

    /**
     * 强制直接连接数据库 读取 构建 SQL文件
     *
     * @param contentVO
     */
    public static void reBuildNCCSqlAndFrontFiles(ExportContentVO contentVO) {
        buildNCCHotwebs(new File(contentVO.getProject().getBasePath(), "hotwebs"), contentVO);

        HashMap<String, Module> moduleHomeDir2ModuleMap = contentVO.getModuleHomeDir2ModuleMap();
        File sqldir = new File(new File(contentVO.getOutPath()).getParentFile(), "SQL脚本");
        ArrayList<File> moduleOneSqls = new ArrayList<>();
        //已存在的sql
        Set exsitSqlSet = null;
        if (contentVO.filtersql) {
            exsitSqlSet = new HashSet<>(60000);
        }

        NCPropXmlUtil.loadConfFromFile(ProjectNCConfigUtil.getNCHomePath());
        NCDataSourceVO ds = NCPropXmlUtil.get(contentVO.data_source_index);
        if (ds == null) {
            return;
        }
        Connection con = null;
        String module = null;
        try {
            con = ConnectionUtil.getConn(ds);
            if (con == null) {
                return;
            }

            for (String modulePath : moduleHomeDir2ModuleMap.keySet()) {
                if (contentVO.ignoreModules.contains(moduleHomeDir2ModuleMap.get(modulePath))) {
                    //需要跳过的模块
                    continue;
                }
                module = modulePath;

                File script = new File(modulePath, "script");
                if (!script.isDirectory() || script.listFiles().length < 1) {
                    continue;
                }
                File conf = new File(script, "conf");
                File initdata = new File(conf, "initdata");
                File itemXml = new File(initdata, "items.xml");
                if (!itemXml.isFile()) {
                    continue;
                }

                File moduleSqlDir = new File(sqldir, moduleHomeDir2ModuleMap.get(modulePath).getName());
                //根据模块 合并SQL文件们
                File moduleSqlOne = new File(moduleSqlDir, moduleHomeDir2ModuleMap.get(modulePath).getName() + "_模块汇总.sql");

                moduleSqlOne.deleteOnExit();
                moduleOneSqls.add(moduleSqlOne);
                StringBuilder txt = new StringBuilder(80_0000);

                //读取items.xml文件
                List<ItemsItemVO> itemVOs = ItemsItemVO.read(itemXml);
                for (ItemsItemVO itemVO : itemVOs) {
                    if (StringUtil.isBlank(itemVO.getItemKey())) {
                        continue;
                    }

                    ConnectionUtil.toInserts(con, itemVO, txt, exsitSqlSet, contentVO);
                }

                FileUtil.writeUtf8String(txt.toString(), moduleSqlOne);
            }

            //合并成完全一个文件
            File sqlOne = new File(sqldir, "全量汇总.sql");
            sqlOne.deleteOnExit();
            for (File moduleOneSql : moduleOneSqls) {
                FileUtil.appendUtf8String("\n-- 模块SQL:  " + moduleOneSql.getPath() + "\n"
                        + FileUtil.readUtf8String(moduleOneSql)
                        + "\n\n\n\n\n", sqlOne);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            LogUtil.error("重建导出SQL:" + e.toString() + ",模块:" + module, e);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            LogUtil.error("重建导出SQL:" + e.toString() + ",模块:" + module, e);
        } finally {
            IoUtil.close(con);
        }
    }

    /**
     * 构建 NCC特殊的 前端js文件和SQL文件
     *
     * @param contentVO
     */
    public static void buildNCCSqlAndFrontFiles(ExportContentVO contentVO) {
        buildNCCHotwebs(new File(contentVO.getProject().getBasePath(), "hotwebs"), contentVO);

        HashMap<String, Module> moduleHomeDir2ModuleMap = contentVO.getModuleHomeDir2ModuleMap();
        File sqldir = new File(new File(contentVO.getOutPath()).getParentFile(), "SQL脚本");
        ArrayList<File> moduleOneSqls = new ArrayList<>();
        //已存在的sql
        Set exsitSqlSet = null;
        if (contentVO.filtersql) {
            exsitSqlSet = new HashSet<>(60000);
        }

        for (String modulePath : moduleHomeDir2ModuleMap.keySet()) {
            if (contentVO.ignoreModules.contains(moduleHomeDir2ModuleMap.get(modulePath))) {
                //需要跳过的模块
                continue;
            }

            File script = new File(modulePath, "script");
            if (!script.isDirectory() || script.listFiles().length < 1) {
                continue;
            }

            File moduleSqlDir = new File(sqldir, moduleHomeDir2ModuleMap.get(modulePath).getName());
            //根据模块 复制sql文件们
            IoUtil.copyFile(script, moduleSqlDir);

            //根据模块 合并SQL文件们
            File moduleSqlOne = new File(moduleSqlDir, moduleHomeDir2ModuleMap.get(modulePath).getName() + "_模块汇总.sql");
            File[] fs = moduleSqlDir.listFiles();
            if (fs == null) {
                continue;
            }

            contentVO.indicator.setText("SQL合并:" + modulePath);

            moduleSqlOne.deleteOnExit();
            moduleOneSqls.add(moduleSqlOne);
            StringBuilder txt = new StringBuilder(80_0000);
            for (File f : fs) {
                if (f.isDirectory() && (
                        f.getName().equals("dbcreate")
                                || f.getName().equals("conf")
                                || f.getName().equals("dbml")
                )) {
                    continue;
                }

                if (isSqlFile(f)) {
                    apendToSql(f, txt, exsitSqlSet);
                }

                meargSqlFiles(f, txt, exsitSqlSet);
            }

            FileUtil.writeUtf8String(txt.toString(), moduleSqlOne);
        }

        //合并成完全一个文件
        File sqlOne = new File(sqldir, "全量汇总.sql");
        sqlOne.deleteOnExit();
        for (File moduleOneSql : moduleOneSqls) {
            FileUtil.appendUtf8String("\n-- 模块SQL:  " + moduleOneSql.getPath() + "\n"
                    + FileUtil.readUtf8String(moduleOneSql)
                    + "\n\n\n\n\n", sqlOne);
        }
    }

    private static void meargSqlFiles(File dir, StringBuilder txt, Set exsitSqlSet) {
        File[] fs = dir.listFiles();
        if (fs == null) {
            return;
        }

        for (File f : fs) {
            if (isSqlFile(f)) {
                apendToSql(f, txt, exsitSqlSet);
            }

            meargSqlFiles(f, txt, exsitSqlSet);
        }
    }

    public static boolean isSqlFile(File f) {
        return IoUtil.isFile(f, ".sql");
    }

    private static void apendToSql(File f, StringBuilder txt, Set exsitSqlSet) {
        List<String> lines = FileUtil.readUtf8Lines(f);
        txt.append("\n\n-- SQL文件: ").append(f.getPath()).append("\n");
        String sqlfull = "";
        String sql;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i) == null) {
                continue;
            }

            sql = lines.get(i).trim();

            if (sql.toLowerCase().equals("go") || sql.toLowerCase().equals(";")) {
                if (exsitSqlSet != null && exsitSqlSet.contains(sqlfull)) {
                    txt.append(" -- 重复SQL发现(" + exsitSqlSet.size() + "):" + sqlfull).append(" ;\n ");
                    sqlfull = "";
                    continue;
                }

                txt.append(sqlfull).append(" ;\n");
                exsitSqlSet.add(sqlfull);
                sqlfull = "";
                continue;
            } else {
                sqlfull += ' ' + sql;
            }
        }

        if (StringUtil.isNotBlank(sqlfull)) {
            txt.append(" -- SQL文件结尾异常数据:  " + sqlfull).append(" \n");
        }
    }

    /**
     * 导出 hotwebs的前端 dist补丁
     *
     * @param hotwebs
     * @param contentVO
     */
    public static void buildNCCHotwebs(File hotwebs, ExportContentVO contentVO) {
        File dist = new File(hotwebs, "dist");
        if (!dist.isDirectory()) {
            return;
        }

        if (dist.listFiles() == null || dist.listFiles().length < 1) {
            return;
        }

        File p_hotwebs = new File(new File(contentVO.getOutPath()).getParentFile(), "hotwebs");
        File nccloud = new File(p_hotwebs, "nccloud");
        File resources = new File(nccloud, "resources");

        if (!resources.exists()) {
            try {
                resources.mkdirs();
            } catch (Exception e) {
                LogUtil.error(e.getMessage(), e);
                e.printStackTrace();
            }
        }

        File[] fs = dist.listFiles();
        for (File f : fs) {
            IoUtil.copyFile(f, resources);
        }
    }

    /**
     * 当NCC补丁导出后，马上处理 补丁结构 符合NCC要求
     *
     * @param sourceRoot
     * @param contentVO
     * @param entry
     * @param classDir
     * @param testClassDirPath
     */
    private static void processNCCPatchersWhenFinash(@NotNull String ncType, @NotNull String exportDir
            , @NotNull Module module, @NotNull String sourceRoot
            , @NotNull String classDir, @Nullable String testClassDir, @NotNull ExportContentVO contentVO) {
        //目前只有client包位置特殊
        if (!NC_TYPE_CLIENT.equals(ncType)) {
            return;
        }

        File exportDirF = new File(exportDir);
        File[] fs = new File[]{new File(exportDir, module.getName())};
        if (fs == null) {
            LogUtil.info(exportDir + " 文件夹为空，不执行 processNCCPatchersWhenFinash !");
            fs = new File[0];
        }

        File outBaseDir = new File(exportDirF.getParent(), "hotwebs"
                + File.separatorChar + "nccloud"
                + File.separatorChar + "WEB-INF"
        );

        //检查是否需要把代码打包成 jar文件
        ExportConfigVO configVO = contentVO.module2ExportConfigVoMap.get(module);

        String moduleKey = module.getName() + '_' + System.currentTimeMillis();

        //转移文件到hotwebs
        for (File f : fs) {
            if (f.isDirectory()) {
                File clientDir = new File(f, "client");

                File[] clientFs = clientDir.listFiles();
                if (com.air.nc5dev.util.CollUtil.isEmpty(clientFs)) {
                    FileUtil.del(clientDir);
                    continue;
                }
                for (File cf : clientFs) {
                    if (cf.getName().equals("classes")) {
                        //如果是classes 要特殊点，有配置文件！
                        File yyconfig = new File(cf, "yyconfig");
                        if (yyconfig.isDirectory()) {
                            File outf = new File(outBaseDir, "extend");
                            if (!outf.isDirectory()) {
                                outf.mkdirs();
                            }
                            IoUtil.copyFile(yyconfig, outf);
                            FileUtil.del(yyconfig);
                        }
                    }

                    File outf = outBaseDir;

                    if (configVO.toJar) {
                        outf = new File(outf, moduleKey);
                    }

                    if (!outf.isDirectory()) {
                        outf.mkdirs();
                    }
                    IoUtil.copyFile(cf, outf, configVO.getNccClientHotwebsPackges());
                    IoUtil.deleteAllEmptyDirs(cf);
                }
            }
        }

        //检查是否要求打包jar
        if (configVO.toJar) {
            File manifest = null;
            if (StringUtil.notEmpty(configVO.manifestFilePath)) {
                manifest = new File(configVO.manifestFilePath);
                if (!manifest.isFile()) {
                    manifest = null;
                }
            }
            contentVO.indicator.setText("NCC-client代码打包成jar:" + module.getName());
            List<File> jars = compressJar(
                    new File(contentVO.outPath
                            + File.separatorChar + module.getName()
                            + File.separatorChar + "client"
                            + File.separatorChar + "classes"
                    )
                    , new File(contentVO.outPath
                            + File.separatorChar + module.getName()
                            + File.separatorChar + "client", "lib")
                    , "ui_" + module.getName()
                    , manifest);
            //删除打包前文件
            if (configVO.toJarThenDelClass) {
                FileUtil.del(new File(contentVO.outPath
                        + File.separatorChar + module.getName()
                        + File.separatorChar + "client"
                        + File.separatorChar + "classes"
                ));
            }

            contentVO.indicator.setText("NCC-hotwebs代码打包成jar:" + module.getName());
            jars = jars = compressJar(
                    new File(outBaseDir, moduleKey)
                    , new File(outBaseDir, "lib")
                    , "ui_" + module.getName()
                    , manifest);
            //删除打包前文件
            IoUtil.copyFile(new File(outBaseDir, moduleKey), outBaseDir);
            FileUtil.del(new File(outBaseDir, moduleKey));

            if (configVO.toJarThenDelClass) {
                IoUtil.cleanUpDirFiles(new File(outBaseDir, "classes"));
            }
        }
    }

    /**
     * 根据 配置文件的路径(所在文件夹) 载入配置信息， module 不重要！
     *
     * @param path
     * @param module
     * @return
     */
    public static ExportConfigVO loadExportConfig(String path, Module module) {
        ExportConfigVO cf = new ExportConfigVO();
        Properties prop = readModuleOutConfigFile(path);
        cf.prop = prop;

        cf.hasSource = toBoolean(cf.getProperty("config-exportsourcefile"), true);
        cf.noTest = toBoolean(cf.getProperty("config-notest"), true);
        cf.toJar = toBoolean(cf.getProperty("config-compressjar"), true);
        cf.toJarThenDelClass = toBoolean(cf.getProperty("config-compressEndDeleteClass"), false);
        cf.guessModule = toBoolean(cf.getProperty("config-guessModule"), false);
        cf.ignoreModule = toBoolean(cf.getProperty("config-ignoreModule"), false);
        cf.manifestFilePath = cf.getProperty("config-ManifestFilePath");
        cf.closeJavaP = toBoolean(cf.getProperty("config-closeJavaP"), false);
        cf.nccClientHotwebsPackges = StringUtil.split2ListAndTrim(cf.getProperty("nccClientHotwebsPackges", ""), ",");

        String s = cf.getProperty("config-ignoreFiles");
        if (StringUtils.isNotBlank(s)) {
            cf.ignoreFiles.addAll(Splitter.on(',').omitEmptyStrings().trimResults().splitToList(s));
        }

        return cf;
    }

    private static boolean toBoolean(String s, boolean defual) {
        if (StringUtil.isEmpty(s)) {
            return defual;
        }

        s = s.trim().toLowerCase();

        if (s.equals("true")) {
            return true;
        }

        if (s.equals("y")) {
            return true;
        }

        if (s.equals("1")) {
            return true;
        }

        if (s.equals("false")) {
            return false;
        }

        if (s.equals("n")) {
            return false;
        }

        if (s.equals("0")) {
            return false;
        }

        return defual;
    }

    /**
     * 打包模块class到jar文件       </br>
     * </br>
     * </br>
     * </br>
     *
     * @return void
     * @author air Email: 209308343@qq.com
     * @date 2020/2/9 0009 14:50
     * @Param [moduleHomeDir, compressEndDeleteClass, contentVO]  模块路径， 是否不保留class文件  true删除class文件,模块配置文件
     */
    private static List<File> compressJar(File moduleHomeDir, boolean compressEndDeleteClass, File manifest, ExportContentVO contentVO) {
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
        if (!NcVersionEnum.NCC.equals(contentVO.ncVersion)) {
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
            if (!NcVersionEnum.NCC.equals(contentVO.ncVersion)) {
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
     * 打包成jar文件   </br>
     * </br>
     * </br>
     * </br>
     *
     * @return java.io.File
     * @author air Email: 209308343@qq.com
     * @date 2020/2/9 0009 14:53
     * @Param [dir, outDir, jarName,manifest] 要打包的文件，要输出到的文件夹，jar文件名（注意，源码会自动加_src后缀），manifest
     */
    private static List<File> compressJar(File dir, File outDir, String jarName, File manifest) {
        try {
            if (dir.listFiles() == null
                    || dir.listFiles().length < 1
                    || (dir.listFiles().length < 2 && dir.listFiles()[0].getName().equals("META-INF"))) {
                return new ArrayList<>();
            }
            outDir.mkdirs();
            //创建MANIFEST.MF

            Manifest maniFest = getManiFest(manifest, dir.getName());

            File jarFile = new File(outDir, jarName + ".jar");
            File jarSrcFile = new File(outDir, jarName + "_src.jar");
            IoUtil.makeJar(dir, jarFile, maniFest, new String[]{".java"});
            IoUtil.makeJar(dir, jarSrcFile, maniFest, new String[]{".class"});
            return CollUtil.toList(jarFile, jarSrcFile);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
        return new ArrayList<>();
    }

    /**
     * 获得jar的manifest文件，如果穿的file是null 就够就默认的格式      </br>
     * </br>
     * </br>
     * </br>
     *
     * @return void
     * @author air Email: 209308343@qq.com
     * @date 2020/4/1 0001 22:31
     * @Param [manifest]
     */
    public static Manifest getManiFest(File manifest, String name) {
        Manifest minf = null;
        try {
            if (null == manifest) {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                PrintWriter printWriter = new PrintWriter(byteArrayOutputStream);
                printWriter.println("Manifest-Version" + ": " + "1.0");
                printWriter.println("Class-Path" + ": " + "");
                printWriter.println("Main-Class" + ": " + "");
                printWriter.println("Name" + ": " + name);
                printWriter.println("Specification-Title" + ": " + "");
                printWriter.println("Specification-Version" + ": " + "1.0");
                printWriter.println("Specification-Vendor" + ": " + "");
                printWriter.println("Implementation-Title" + ": " + "");
                printWriter.println("Implementation-Version" + ": " + "");
                printWriter.println("Implementation-Vendor" + ": " + "");
                printWriter.println("CreateDate" + ": " + DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                        .format(LocalDateTime.now()));
                printWriter.println("Created-By" + ": " + "");
                printWriter.println("Created-ByIde" + ": " + "IDEA-plugin-nc5devtoolidea");
                printWriter.println("  by air Email 209308343@qq.com,QQ 209308343");
                printWriter.println("IDEA-plugin-nc5devtoolidea-github" + ": " + "https://");
                printWriter.println(" gitee.com/yhlx/idea_plugin_nc5devplugin");
                printWriter.println("");
                printWriter.flush();
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream
                        .toByteArray());
                minf = new Manifest(byteArrayInputStream);
                printWriter.close();
            } else {
                minf = new Manifest(new FileInputStream(manifest));
            }
        } catch (IOException e) {
        }

        return minf;
    }

    /**
     * 把一个包 当前的文件夹内所有 java源代码和类文件 复制到补丁对应位置（不包含下级包）      </br>
     * </br>
     * </br>
     * </br>
     *
     * @return void
     * @author air Email: 209308343@qq.com
     * @date 2020/2/4 0004 17:31
     * @Param [sourcePackge, sourceBaseDirFile,  contentVO, exportDir, moduleName, ncType, packgePath,
     * classFileDir]
     */
    public static final void copyClassAndJavaSourceFiles(File sourcePackge, File sourceBaseDirFile
            , ExportContentVO contentVO, String exportDir, Module module
            , String ncType, String packgePath, final File classFileDir) {
        ExportConfigVO configVO = contentVO.module2ExportConfigVoMap.get(module);

        if (null == javapCommandPath
                || configVO.closeJavaP) {
            copyClassAndJavaSourceFilesBySource(sourcePackge, sourceBaseDirFile
                    , contentVO, exportDir, module
                    , ncType, packgePath, classFileDir);
        } else {
            copyClassAndJavaSourceFilesByClass(sourcePackge, sourceBaseDirFile
                    , contentVO, exportDir, module
                    , ncType, packgePath, classFileDir);
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
     * @Param [ncType, exportDir, Module, sourceRoot, classDir , testClassDir, hasJavaFile, noOutTestClass ,
     * contentVO]    NC3大文件夹，导出文件夹路径，模块, java源码路径，生产代码路径，test代码路径，
     */
    public static void export(@NotNull String ncType, @NotNull String exportDir
            , @NotNull Module module, @NotNull String sourceRoot
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
                    , ncType, packgePath, classFileDir);

            copyClassPathOtherFile(sourcePackge, exportDir, module
                    , ncType, packgePath, contentVO);
        }

        //处理NCC特殊的模块补丁结构
        if (NcVersionEnum.NCC.equals(contentVO.ncVersion)) {
            processNCCPatchersWhenFinash(ncType, exportDir, module
                    , sourceRoot, classDir, testClassDir, contentVO);
        }
    }

    /**
     * 通过循环 class文件 方式复制包里补丁         </br>
     * </br>
     * </br>
     * </br>
     *
     * @return void
     * @author air Email: 209308343@qq.com
     * @date 2020/2/22 0022 12:14
     * @Param [sourcePackge, sourceBaseDirFile,  contentVO, exportDir, moduleName, ncType, packgePath,
     * hasJavaFile, classFileDir]
     */
    private static final void copyClassAndJavaSourceFilesByClass(File sourcePackge, File sourceBaseDirFile
            , ExportContentVO contentVO, String exportDir, Module module
            , String ncType, String packgePath, final File classFileDir) {
        ExportConfigVO configVO = contentVO.module2ExportConfigVoMap.get(module);
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
            String outModuleName = getOutModuleName(contentVO
                    , javaFullClassName, classFile, module, module.getName());

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
                copyFile(new File(sourcePackge, javaFullName.endsWith(".java") ? javaFullName : javaFullName + ".java")
                        , outDir, contentVO, module);
            }

            //复制class
            copyFile(classFile, outDir, contentVO, module);
        });
    }

    /**
     * 把一个文件 复制到指定的文件夹里 </br>
     * 会自动创建不存在的文件夹      </br>
     * </br>
     * </br>
     *
     * @return void
     * @author air Email: 209308343@qq.com
     * @date 2020/1/16 0016 20:07
     * @Param [from, toDir]
     */
    private static final void copyFile(@NotNull File from, @NotNull final File dir
            , ExportContentVO contentVO, Module module) {
        if (Objects.nonNull(contentVO) && Objects.nonNull(module)) {
            //检查是否配置了 文件不复制！
            ExportConfigVO cf = contentVO.module2ExportConfigVoMap.get(module);
            final String path = from.getPath();
            if (cf.ignoreFiles.stream().anyMatch(reg -> isMatch(path, reg))) {
                return;
            }
        }

        IoUtil.copyFile(from, dir);
    }

    /**
     * 根据 补丁模块配置文件 获得模块名        </br>
     * </br>
     * </br>
     * </br>
     *
     * @return java.lang.String
     * @author air Email: 209308343@qq.com
     * @date 2020/3/30 0030 16:56
     * @Param [ contentVO, javaFullClassName,guass]
     */
    private static String getOutModuleName(ExportContentVO contentVO, String javaFullClassName
            , File classFile, Module module, String defalut) {
        if (StringUtil.isEmpty(javaFullClassName)) {
            return defalut;
        }

        ExportConfigVO configVO = contentVO.module2ExportConfigVoMap.get(module);

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
     * 获得第几个包名字       </br>
     * </br>
     * </br>
     * </br>
     *
     * @return java.lang.String
     * @author air Email: 209308343@qq.com
     * @date 2020/3/24 0024 13:23
     * @Param [p, packgeIndex, split]
     */
    private static String getPackgeName(String p, int packgeIndex, String split) {
        String packgeThreeName = p;

        for (int i = 1; i < packgeIndex; i++) {
            packgeThreeName = packgeThreeName.substring(packgeThreeName.indexOf(split) + 1);
        }

        return packgeThreeName.substring(0, packgeThreeName.indexOf(split));
    }

    /**
     * 通过循环源文件方式复制 包里补丁        </br>
     * 警告：同一个java源文件内非匿名 非public class会跳过复制！</br>
     * </br>
     * </br>
     *
     * @return void
     * @author air Email: 209308343@qq.com
     * @date 2020/2/22 0022 12:14
     * @Param [sourcePackge, sourceBaseDirFile,  contentVO, exportDir, moduleName, ncType, packgePath,
     * hasJavaFile, classFileDir]
     */
    private static final void copyClassAndJavaSourceFilesBySource(File sourcePackge, File sourceBaseDirFile
            , ExportContentVO contentVO, String exportDir, Module module
            , String ncType, String packgePath, final File classFileDir) {
        ExportConfigVO configVO = contentVO.module2ExportConfigVoMap.get(module);
        Stream.of(sourcePackge.listFiles()).forEach(javaFile -> {
            //循环复制所有java文件 ,因为idea编译后 没有分NC这3个文件夹！
            if (!javaFile.isFile()) {
                return;
            }

            if (!javaFile.getName().toLowerCase().endsWith(".java")) {
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
            String outModuleName = getOutModuleName(contentVO
                    , javaFullClassName, javaFile, module, module.getName());

            final String baseOutDirPath = exportDir + File.separatorChar
                    + (StringUtil.isEmpty(outModuleName) ? module.getName() : outModuleName);

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
                copyFile(javaFile, outDir, contentVO, module);
            }

            //复制class 这个麻烦，要正确导出匿名类。 如果是 一个java多个类 反人类写法不考虑让用户手工处理！
            List<File> classFiles = getJavaClassByClassName(javaFullClassName, classFileDir);
            for (File classFile : classFiles) {
                copyFile(classFile, outDir, contentVO, module);
            }

        });
    }

    /**
     * 把一个包里的 非代码文件 复制到补丁 对应位置里      </br>
     * 比如 wsdl json 各种类路径配置文件等     </br>
     * </br>
     * </br>
     *
     * @return void
     * @author air Email: 209308343@qq.com
     * @date 2020/2/4 0004 17:28
     * @Param [sourcePackge, exportDir, moduleName, ncType, packgePath]
     */
    public static final void copyClassPathOtherFile(File sourcePackge
            , String exportDir, Module module, String ncType
            , String packgePath, ExportContentVO contentVO) {
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

            String outModuleName = getOutModuleName(contentVO, configKey, null, module, module.getName());
            String outClasPathOtherFileDirPath = exportDir + File.separatorChar + (StringUtil.isEmpty(outModuleName)
                    ? module.getName() : outModuleName);
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

            copyFile(file, outClasPathOtherFileDirFinal, contentVO, module);
        });
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

    /**
     * 根据class文件 获取他的源码文件名字： 会根据javap命令来        </br>
     * </br>
     * </br>
     * </br>
     *
     * @return java.lang.String
     * @author air Email: 209308343@qq.com
     * @date 2020/2/22 0022 12:25
     * @Param [path, sourcePackge]
     */
    public static String getClassFileSourceFileName(String path, File sourcePackge) {
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
     * 从Class文件 javap 读取 源文件名        </br>
     * FIXME 有效率问题，如果用二进制读取会更好        </br>
     * </br>
     * </br>
     *
     * @return java.lang.String
     * @author air Email: 209308343@qq.com
     * @date 2020/2/22 0022 13:04
     * @Param [classFilePath]
     */
    public static String readClassFileSourceFileName(String classFilePath) {
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

    public static void initJavap() {
        final String exe = "bin" + File.separatorChar + "javap.exe";

        File javaHomePathFile = IoUtil.getJavaHomePathFile(ProjectNCConfigUtil.getNCHomePath()
                , exe);
        if (javaHomePathFile != null) {
            javapCommandPath = javaHomePathFile.getPath();
        }
    }
}
