package com.air.nc5dev.util;

import b.k.F;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.MD5;
import com.air.nc5dev.enums.NcVersionEnum;
import com.air.nc5dev.util.exportpatcher.searchs.AbstractContentSearchImpl;
import com.air.nc5dev.util.exportpatcher.searchs.ModuleJavaClientFileContentSearchImpl;
import com.air.nc5dev.util.exportpatcher.searchs.ModuleJavaPrivateFileContentSearchImpl;
import com.air.nc5dev.util.exportpatcher.searchs.ModuleJavaPublicFileContentSearchImpl;
import com.air.nc5dev.util.exportpatcher.searchs.NccloudHotwebsResourcesFileContentSearchImpl;
import com.air.nc5dev.vo.FileContentVO;
import com.air.nc5dev.util.exportpatcher.output.IOutPutFile;
import com.air.nc5dev.util.exportpatcher.output.SimpleCopyOutPutFileImpl;
import com.air.nc5dev.util.exportpatcher.beforafter.AbstarctAfterRule;
import com.air.nc5dev.util.exportpatcher.beforafter.AbstarctBeforRule;
import com.air.nc5dev.util.exportpatcher.beforafter.EmptyAfterRule;
import com.air.nc5dev.util.exportpatcher.beforafter.EmptyBeforRule;
import com.air.nc5dev.util.exportpatcher.beforafter.HotwebsModuleClassBeforRule;
import com.air.nc5dev.util.exportpatcher.searchs.BmfFileContentSearchImpl;
import com.air.nc5dev.util.exportpatcher.searchs.IFileContentSearch;
import com.air.nc5dev.util.exportpatcher.searchs.ModuleJavaFileContentSearchImpl;
import com.air.nc5dev.util.exportpatcher.searchs.ModuleLibFileContentSearchImpl;
import com.air.nc5dev.util.exportpatcher.searchs.ModuleResourcesFileContentSearchImpl;
import com.air.nc5dev.util.exportpatcher.searchs.ModuleUpmFileContentSearchImpl;
import com.air.nc5dev.util.idea.LogUtil;
import com.air.nc5dev.util.jdbc.ConnectionUtil;
import com.air.nc5dev.vo.ExportConfigVO;
import com.air.nc5dev.vo.ExportContentVO;
import com.air.nc5dev.vo.ItemsItemVO;
import com.air.nc5dev.vo.ModuleWarpVO;
import com.air.nc5dev.vo.NCDataSourceVO;
import com.alibaba.fastjson.JSON;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

/**
 * 导出NC补丁 工具类      <br>
 * <br>
 * <br>
 * <br>
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

    public static String javapCommandPath = null;

    public static void saveConfig(Project pro, ExportContentVO contentVO) {
        try {
            ExportContentVO c = contentVO;
            FileUtil.writeUtf8String(JSON.toJSONString(c), new File(new File(pro.getBasePath(), ".idea"),
                    "ExportContentVO.json"));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static ExportContentVO readConfig(Project pro) {
        try {
            String str = FileUtil.readUtf8String(new File(new File(pro.getBasePath(), ".idea")
                    , "ExportContentVO.json"));
            if (StrUtil.isBlank(str)) {
                return null;
            }
            return JSON.parseObject(str, ExportContentVO.class);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 导出补丁到指定的文件夹   <br>
     * <br>
     * <br>
     * <br>
     *
     * @return void
     * @author air Email: 209308343@qq.com
     * @date 2020/1/16 0016 18:32
     * @Param [outPath, project] 输出文件夹，项目对象
     */
    public static final void export(@NotNull ExportContentVO contentVO) {
        ArrayList<IFileContentSearch> searchs = getSearchs(contentVO);
        for (IFileContentSearch search : searchs) {
            search.process(contentVO);
        }

        IOutPutFile simpleExecutor = new SimpleCopyOutPutFileImpl();
        AbstarctBeforRule beforRule = new EmptyBeforRule();
        AbstarctAfterRule afterRule = new EmptyAfterRule();

        if (NcVersionEnum.isNCCOrBIP(contentVO.ncVersion)) {
            beforRule.addNext(new HotwebsModuleClassBeforRule());
        }

        for (FileContentVO outFile : contentVO.getOutFiles()) {
            if (contentVO.indicator != null && contentVO.indicator.isCanceled()) {
                break;
            }

            if (outFile.getExecutor() == null) {
                outFile.setExecutor(simpleExecutor);
            }

            outFile.getExecutor().outPut(contentVO, outFile, beforRule, afterRule);
        }

        if (contentVO.indicator.isCanceled()) {
            return;
        }

        //处理NCC特殊的模块补丁结构
        if (contentVO.isExportSql()
                && !contentVO.indicator.isCanceled()) {
            if (contentVO.rebuildsql) {
                contentVO.indicator.setText("强制直接连接数据库生成SQL合并文件...");
                reBuildNCCSqlFiles(contentVO);
            } else {
                contentVO.indicator.setText("使用用友开发工具导出的SQL脚本文件进行SQL合并...");
                buildNCCSqlFiles(contentVO);
            }
        }

        if (contentVO.isFormat4Ygj()
                && !contentVO.indicator.isCanceled()) {
            //转换云管家格式！
            toYgjFormatExportStract(contentVO);
        }

        if (contentVO.isReWriteSourceFile()
                && !contentVO.indicator.isCanceled()) {
            reWriteSourceFile(contentVO);
        }
    }

    public static ArrayList<IFileContentSearch> getSearchs(ExportContentVO contentVO) {
        ArrayList<IFileContentSearch> shs = new ArrayList<>();

        if (contentVO.isExportHotwebsClass()) {
            shs.add(new ModuleJavaClientFileContentSearchImpl());
        }

        if (contentVO.isExportModules()) {
            shs.add(new ModuleJavaPublicFileContentSearchImpl());
            shs.add(new ModuleJavaPrivateFileContentSearchImpl());
        }

        if (contentVO.isExportModuleMeteinfo()) {
            shs.add(new ModuleUpmFileContentSearchImpl());
        }

        if (contentVO.isExportModuleResources()) {
            shs.add(new ModuleResourcesFileContentSearchImpl());
        }

        if (contentVO.isExportModuleLib()) {
            shs.add(new ModuleLibFileContentSearchImpl());
        }

        if (contentVO.isExportModuleMetadata()) {//元数据
            shs.add(new BmfFileContentSearchImpl());
        }

        if (contentVO.isExportResources()
                && NcVersionEnum.isNCCOrBIP(contentVO.getNcVersion())) {
            shs.add(new NccloudHotwebsResourcesFileContentSearchImpl());
        }

        return shs;
    }

    /**
     * 覆盖写入混淆字符串到源码文件 java和js的
     *
     * @param f
     */
    public static void reWriteSourceFile(File f) {
        if (f.isDirectory()) {
            for (File f1 : f.listFiles()) {
                reWriteSourceFile(f1);
            }
        } else {
            String name = f.getName().toLowerCase();
            boolean is = false;
            ArrayList<String> ends = com.air.nc5dev.util.CollUtil.toList(".java", ".js", "_src.jar");
            for (String end : ends) {
                if (StringUtil.endsWith(name, end)) {
                    is = true;
                    break;
                }
            }
            if (!is) {
                return;
            }

            int size = 30 + new Random().nextInt(200);

            String txt = "QQ:209308343@qq.com 微信:yongyourj 时间:" + V.nowDateTime();
            String md5 = "";
            try {
                BasicFileAttributes attributes = Files.readAttributes(f.toPath(), BasicFileAttributes.class);
                Date creationTime = new Date(attributes.creationTime().toMillis());
                Date lastModifiedTime = new Date(attributes.lastModifiedTime().toMillis());

                txt += "\n\n文件原始创建时间:" + V.formatDateTime(creationTime) + "\n\n"
                        + "文件原始修改时间:" + V.formatDateTime(lastModifiedTime) + "\n\n"
                        + "文件原始路径:" + f.getPath() + "\n\n"
                        + "文件原始大小:" + (f.length() / 1024.00) + "kb\n\n"
                        + "文件原始MD5:\n\n"
                ;

                md5 = MD5.create().digestHex(FileUtil.readBytes(f));
            } catch (Throwable e) {
            }

            for (int i = 0; i < size; i++) {
                txt += md5 + "\n";
            }

            if (name.endsWith(".jar")) {
                try {
                    IoUtil.writeUtf8String2Jar(txt, f, com.air.nc5dev.util.CollUtil.toList(".java", ".js"));
                } catch (Throwable e) {
                }
            } else {
                //classess
                FileUtil.writeUtf8String(txt, f);
            }
        }
    }

    /**
     * 覆盖写入混淆字符串到源码文件 java和js的
     *
     * @param contentVO
     */
    public static void reWriteSourceFile(ExportContentVO contentVO) {
        ArrayList<String> javas = Lists.newArrayList();
        ArrayList<String> jss = Lists.newArrayList();
        contentVO.indicator.setText("正在覆盖写入混淆字符串到源码文件...");

        File base = new File(contentVO.getOutPath());

        if (contentVO.isFormat4Ygj()) {
            //云管家格式的
            base = new File(base, "replacement");
        }

        javas.add(base.getPath() + File.separatorChar + "modules");
        jss.add(new File(base, "hotwebs"
                        + File.separatorChar + "nccloud" + File.separatorChar + "resources"
                        + File.separatorChar + "__SOURCE__CODE__" + File.separatorChar + "src"
                ).getPath()
        );

        File hotwebs = new File(base, "hotwebs");
        File[] fs = hotwebs.listFiles();
        if (fs != null) {
            for (File f : fs) {
                if (f.isDirectory()) {
                    javas.add(
                            f.getPath() + File.separatorChar + "WEB-INF" + File.separatorChar + "classes"
                    );
                }
            }
        }

        for (String java : javas) {
            File f = new File(java);
            if (!f.exists()) {
                continue;
            }
            reWriteSourceFile(f);
        }

        for (String java : jss) {
            File f = new File(java);
            if (!f.exists()) {
                continue;
            }
            reWriteSourceFile(f);
        }
    }

    /**
     * 把补丁转换成云管家格式
     *
     * @param contentVO
     */
    public static void toYgjFormatExportStract(ExportContentVO contentVO) {
        File orginDir = new File(contentVO.getOutPath());
        File patcherDir = orginDir.getParentFile();
        File base = new File(patcherDir.getParentFile(), patcherDir.getName() + "-云管家补丁");
        contentVO.indicator.setText("转换成云管家补丁格式..." + base.getPath());
        if (base.exists()) {
            IoUtil.deleteFileAll(base);
            base.deleteOnExit();
        }

        File replacement = new File(base, "replacement");
        /*   File readme = new File(base, "readme.txt");
        File packmetadata = new File(base, "packmetadata.xml");
        File installpatch = new File(base, "installpatch.xml");*/

        if (!patcherDir.isDirectory()) {
            LogUtil.error(String.format("转换成云管家补丁格式出错: 路径不存在: %s , orginDir: %s"
                    , patcherDir.getPath()
                    , orginDir.getParent()
            ));
            return;
        }

        for (File src : patcherDir.listFiles()) {
            FileUtil.move(src, replacement, true);
        }

        //输出 描述文件！
        try {
            new AutoGenerPathcherInfo4YunGuanJiaUtil()
                    .setV(contentVO.getV())
                    .setNo(contentVO.getNo())
                    .setApplyVersion(contentVO.getApplyVersion())
                    .setDepartment(contentVO.getDepartment())
                    .setProvider(contentVO.getProvider())
                    .setDeploy(contentVO.isDeploy())
                    .setAppleyjar(contentVO.isAppleyjar())
                    .run(
                            base.getPath()
                            , StringUtil.get(contentVO.getName(), base.getName())
                            , NcVersionEnum.isNCCOrBIP(contentVO.getNcVersion())
                            , contentVO
                    );

            contentVO.setOutPath(base.getPath());
        } catch (Throwable e) {
            e.printStackTrace();
            e.printStackTrace();
            LogUtil.error("转换成云管家补丁格式出错:" + e.toString(), e);
        }

        try {
            FileUtil.del(patcherDir);
        } catch (Exception e) {
        }
    }

    /**
     * 强制直接连接数据库 读取 构建 SQL文件
     *
     * @param contentVO
     */
    public static void reBuildNCCSqlFiles(ExportContentVO contentVO) {
        HashMap<String, Module> moduleHomeDir2ModuleMap = contentVO.getModuleHomeDir2ModuleMap();
        File sqldir = new File(new File(contentVO.getOutPath()).getParentFile(), "sql");
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
                if (contentVO.indicator.isCanceled()) {
                    return;
                }

                if (!contentVO.getSelectExportModules().contains(moduleHomeDir2ModuleMap.get(modulePath))) {
                    //需要跳过的模块
                    continue;
                }

                if (contentVO.isSelectExport()
                        && !contentVO.getModuleDirPath2ModuleMap().values().contains(moduleHomeDir2ModuleMap.get(modulePath))) {
                    //需要跳过的模块
                    continue;
                }

                module = modulePath;

                File script = new File(modulePath, "script");
                if (!script.isDirectory() || script.listFiles().length < 1) {
                    continue;
                }

                ArrayList<File> fs = Lists.newArrayList();
                File conf = new File(script, "conf");

                if (conf.isDirectory()) {
                    File[] files = conf.listFiles();
                    if (files != null) {
                        for (File f : files) {
                            if (contentVO.indicator.isCanceled()) {
                                return;
                            }

                            if (f.isFile() && f.getName().toLowerCase().endsWith(".xml")) {
                                fs.add(f);
                            }
                        }
                    }
                }

                File initdata = new File(conf, "initdata");
                if (initdata.isDirectory()) {
                    File[] files = initdata.listFiles();
                    if (files != null) {
                        for (File f : files) {
                            if (contentVO.indicator.isCanceled()) {
                                return;
                            }

                            if (f.isFile() && f.getName().toLowerCase().endsWith(".xml")) {
                                fs.add(f);
                            }
                        }
                    }
                }

                File moduleSqlDir = new File(sqldir, moduleHomeDir2ModuleMap.get(modulePath).getName());
                StringBuilder txt = new StringBuilder(80_0000);

                //根据模块 合并SQL文件们
                File moduleSqlOne;

                //读取items.xml文件
                if (com.air.nc5dev.util.CollUtil.isNotEmpty(fs)) {
                    for (File f : fs) {
                        if (contentVO.indicator.isCanceled()) {
                            return;
                        }

                        contentVO.indicator.setText("IDEA根据xml配置导出sql:" + f.getPath());

                        List<ItemsItemVO> vs = ItemsItemVO.read(f, contentVO.getProject(),
                                contentVO.moduleHomeDir2ModuleMap.get(modulePath));
                        if (com.air.nc5dev.util.CollUtil.isNotEmpty(vs)) {
                            if (txt.length() > 0) {
                                txt.delete(0, txt.length());
                            }

                            moduleSqlOne = new File(moduleSqlDir
                                    , moduleHomeDir2ModuleMap.get(modulePath).getName() + '_'
                                    + StringUtil.removeAll(FileNameUtil.getName(f), ".xml")
                                    + ".sql");
                            moduleSqlOne.deleteOnExit();
                            moduleOneSqls.add(moduleSqlOne);

                            for (ItemsItemVO itemVO : vs) {
                                if (contentVO.indicator.isCanceled()) {
                                    return;
                                }

                                if (StringUtil.isBlank(itemVO.getItemKey())) {
                                    continue;
                                }

                                ConnectionUtil.toInserts(con, itemVO, txt, exsitSqlSet, contentVO);
                            }

                            FileUtil.writeUtf8String(txt.toString(), moduleSqlOne);
                        }
                    }
                }
            }

            //合并成完全一个文件
            File sqlOne = new File(sqldir, "全量汇总.sql");
            sqlOne.deleteOnExit();
            for (File moduleOneSql : moduleOneSqls) {
                FileUtil.appendUtf8String("\n-- 模块SQL:  " + moduleOneSql.getPath() + "\n"
                        + FileUtil.readUtf8String(moduleOneSql)
                        + "\n\n\n\n\n", sqlOne);
            }

            if (contentVO.onleyFullSql) {
                File[] fs = sqlOne.getParentFile().listFiles();
                for (File f : fs) {
                    if (f.getName().equals(sqlOne.getName())) {
                        continue;
                    }
                    FileUtil.del(f);
                }
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
    public static void buildNCCSqlFiles(ExportContentVO contentVO) {
        if (contentVO.indicator.isCanceled()) {
            return;
        }

        HashMap<String, Module> moduleHomeDir2ModuleMap = contentVO.getModuleHomeDir2ModuleMap();
        File sqldir = new File(new File(contentVO.getOutPath()).getParentFile(), "sql");
        ArrayList<File> moduleOneSqls = new ArrayList<>();
        //已存在的sql
        Set exsitSqlSet = null;
        if (contentVO.filtersql) {
            exsitSqlSet = new HashSet<>(60000);
        }

        for (String modulePath : moduleHomeDir2ModuleMap.keySet()) {
            if (contentVO.indicator.isCanceled()) {
                return;
            }

            if (!contentVO.getSelectExportModules().contains(moduleHomeDir2ModuleMap.get(modulePath))) {
                //需要跳过的模块
                continue;
            }

            if (contentVO.isSelectExport()
                    && !contentVO.getModuleDirPath2ModuleMap().values().contains(moduleHomeDir2ModuleMap.get(modulePath))) {
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
                if (contentVO.indicator.isCanceled()) {
                    return;
                }

                contentVO.indicator.setText("处理sql文件:" + f.getPath());

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
                IoUtil.makeDirs(resources);
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
    private static void processNCCPatchersWhenFinash(@NotNull String exportDir
            , @NotNull ModuleWarpVO module, @NotNull ExportContentVO contentVO) {
        File exportDirF = new File(exportDir);
        File[] fs = new File[]{new File(exportDir, module.getConfigName())};
        if (fs == null) {
            LogUtil.infoAndHide(exportDir + " 文件夹为空，不执行 processNCCPatchersWhenFinash !");
            fs = new File[0];
        }

        File outBaseDir = new File(exportDirF.getParent(), "hotwebs"
                + File.separatorChar + "nccloud"
                + File.separatorChar + "WEB-INF"
        );

        //检查是否需要把代码打包成 jar文件
        ExportConfigVO configVO = contentVO.module2ExportConfigVoMap.get(module.getModule());

        String moduleKey = module.getConfigName() + '_' + System.currentTimeMillis();

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
                                IoUtil.makeDirs(outf);
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
                        IoUtil.makeDirs(outf);
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
            contentVO.indicator.setText("NCC-client代码打包成jar:" + module.getModule().getName());
            List<File> jars = AbstractContentSearchImpl.compressJar(
                    new File(contentVO.outPath
                            + File.separatorChar + module.getConfigName()
                            + File.separatorChar + "client"
                            + File.separatorChar + "classes"
                    )
                    , new File(contentVO.outPath
                            + File.separatorChar + module.getConfigName()
                            + File.separatorChar + "client", "lib")
                    , "ui_" + module.getConfigName()
                    , manifest);
            //删除打包前文件
            if (configVO.toJarThenDelClass) {
                FileUtil.del(new File(contentVO.outPath
                        + File.separatorChar + module.getConfigName()
                        + File.separatorChar + "client"
                        + File.separatorChar + "classes"
                ));
            }

            contentVO.indicator.setText("NCC-hotwebs代码打包成jar:" + module.getModule().getName());
            jars = jars = AbstractContentSearchImpl.compressJar(
                    new File(outBaseDir, moduleKey)
                    , new File(outBaseDir, "lib")
                    , "ui_" + module.getConfigName()
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
        cf.notExportModelueXml = toBoolean(cf.getProperty("not-export-modelue-xml"), false);

        cf.clientToModuleHotwebs = toBoolean(cf.getProperty("clientToModuleHotwebs"), NcVersionEnum.isNCCOrBIP(ProjectNCConfigUtil.getNCVersion(module.getProject())));
        cf.publicToModuleHotwebs = toBoolean(cf.getProperty("publicToModuleHotwebs"), false);
        cf.privateToModuleHotwebs = toBoolean(cf.getProperty("privateToModuleHotwebs"), false);
        cf.moduleHotwebsName = StringUtil.get(cf.getProperty("moduleHotwebsName"), "nccloud");

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
     * 读取模块补丁输出设置的配置文件       <br>
     * <br>
     * <br>
     * <br>
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


}
