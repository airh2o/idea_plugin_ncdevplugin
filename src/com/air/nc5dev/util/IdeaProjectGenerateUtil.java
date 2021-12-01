package com.air.nc5dev.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import com.air.nc5dev.enums.NcVersionEnum;
import com.air.nc5dev.util.idea.ApplicationLibraryUtil;
import com.air.nc5dev.util.idea.LogUtil;
import com.air.nc5dev.util.idea.ProjectUtil;
import com.air.nc5dev.util.idea.RunConfigurationUtil;
import com.air.nc5dev.vo.ExportConfigVO;
import com.air.nc5dev.vo.ExportContentVO;
import com.intellij.execution.ShortenCommandLine;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.application.ApplicationConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.impl.RunManagerImpl;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Stream;

/***
 *    生成新项目里 NC必须的东西  等    </br>
 *     1.NC库依赖      </br>
 *     2.NC 3个源文件夹 和 1个xml     </br>
 *     3.NC 2个运行选项 服务端+客户端
 *     4. 复制项目 ump文件到 NC HOME的工具方法 </br>
 * @author air Email: 209308343@qq.com
 * @date 2019/12/25 0025 9:02
 */
public class IdeaProjectGenerateUtil {
    /**
     * 生成NC默认 几个个文件夹 src和META-INF 到项目
     *
     * @param project
     */
    public static final void generateSrcDir(@Nullable Project project) {
        Project project1 = project == null ? ProjectUtil.getDefaultProject() : project;

        Module[] modules = ModuleManager.getInstance(project1).getModules();
        for (Module module : modules) {
            generateSrcDir(module);
            generatePatherConfigFile(module);
        }
    }

    /**
     * 生成NC默认 几个个文件夹 src和META-INF 到指定模块       </br>
     * </br>
     * </br>
     * </br>
     *
     * @return void
     * @author air Email: 209308343@qq.com
     * @date 2020/1/18 0018 12:25
     * @Param [module]
     */
    public static void generateSrcDir(@NotNull Module module) {
        File homeDir = new File(module.getModuleFilePath()).getParentFile();
        File src = new File(homeDir, "src");
        File publicf = new File(src, "public");
        if (!publicf.exists()) {
            publicf.mkdirs();
        }
        File client = new File(src, "client");
        if (!client.exists()) {
            client.mkdirs();
        }
        File privatef = new File(src, "private");
        if (!privatef.exists()) {
            privatef.mkdirs();
        }
        File testf = new File(src, "test");
        if (!testf.exists()) {
            testf.mkdirs();
        }
        File umpDir = new File(homeDir, "META-INF");
        if (!umpDir.exists()) {
            umpDir.mkdirs();
        }
        File umpFile = new File(umpDir, "module.xml");
        if (!umpFile.exists()) {
            try {
                PrintWriter out = new PrintWriter(new FileOutputStream(umpFile));
                out.print(
                        String.format("<?xml version=\"1.0\" encoding=\"gb2312\"?>\n"
                                        + "<module name=\"%s\">\n"
                                        + "\t<public>\n"
                                        + "\t</public>\n"
                                        + "\t<private>\n"
                                        + "\t</private>\n"
                                        + "</module>"
                                , module.getName())
                );
                out.flush();
                out.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        copyProjectMetaInfFiles2NCHomeModules();

    }

    /**
     * 生成 插件补丁 标准导出配置文件 到 到指定模块       </br>
     * </br>
     * </br>
     * </br>
     *
     * @return void
     * @author air Email: 209308343@qq.com
     * @date 2020/1/18 0018 12:25
     * @Param [module]
     */
    public static void generatePatherConfigFile(@NotNull Module module) {
        File homeDir = new File(module.getModuleFilePath()).getParentFile();
        File outFile = new File(homeDir, "patcherconfig.properties");
        if (!outFile.exists()) {
            try {
                PrintWriter out = new PrintWriter(new FileOutputStream(outFile));
                out.print(
                        "#模块 " + module.getName() + "(" + homeDir.getPath() + ")" + "导出补丁的配置文件\n" +
                                "#导出补丁选项参数\n" +
                                "config-notest=true\n" +
                                "#是否导出源码\n" +
                                "config-exportsourcefile=true\n" +
                                "#是否打包成jar(所有idea项目和模块 才打包，其他非模块和项目名字的不打包)\n" +
                                "config-compressjar=false\n" +
                                "#打包成jar的是否保留classess里的文件\n" +
                                "config-compressEndDeleteClass=true\n" +
                                "#如果打包jar，那么 META-INF.MF 文件模板磁盘全路径\n" +
                                "config-ManifestFilePath=\n" +
                                "#是否猜测模块，默认true，开启后 如果配置文件没有指明的类会根据包名第三个判断模块\n" +
                                "# （比如 nc.ui.pub.ButtonBar 第三个是pub 所以认为模块是 pub）\n" +
                                "config-guessModule=false\n" +
                                "#关闭使用JAVAP方式判断 源码文件对应,默认false\n" +
                                "config-closeJavaP=false\n" +
                                "\n" +
                                "#全类名匹配的输出模块\n" +
                                "nc.ui.glpub.UiManager=gl\n" +
                                "#非源码的文件输出模块\n" +
                                "nc.bs.arap.1.txt=arap  \n"
                                + "#支持包路径匹配（优先级低于 全类名匹配， 包路径优先匹配最精确的包）\n"
                                + "nc.bs.po=pu  \n "
                                + "# 是否跳过此配置文件所在模块，不导出这个模块\n"
                                + "config-ignoreModule=false \n"
                                + "# 不导出的文件列表 多个 用 英文,隔开\n"
                                + "# 第一个 根据class定位精确不导出， 第二个 根据包名和里面的文件名精确定位不导出， 第三个 根据包名路径下的 所有 都不导出\n"
                                + "config-ignoreFiles=nc.bs.some.Save2Impl, nc.bs.arap.2.txt, nc.bs.some.impl2 \n"
                );
                out.flush();
                out.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 生成 项目的 NC2个运行配置
     *
     * @param project
     */
    public static final void generateRunMenu(@NotNull Project project) {
        if (!checkNCHomeSetPass()) {
            return;
        }

        File ncHome = ProjectNCConfigUtil.getNCHome();

        RunManagerImpl runManager = RunConfigurationUtil.getRunManagerImpl(ProjectUtil.getDefaultProject());
        List<RunConfiguration> configurationsList = runManager.getConfigurationsList(ApplicationConfigurationType
                .getInstance());

        final byte[] hasNc = new byte[2];
        final String serverClass = "ufmiddle.start.tomcat.StartDirectServer";
        final String clientClass = "nc.starter.test.JStarter";

        if (null != configurationsList && !configurationsList.isEmpty()) {
            //已经有配置了，检查是否有了NC的，有的要更新，没有的要新增
            configurationsList.stream().forEach(rc -> {
                ApplicationConfiguration conf = (ApplicationConfiguration) rc;
                if (serverClass.equals(((ApplicationConfiguration) rc).MAIN_CLASS_NAME)) {
                    hasNc[0] = 1;
                    //更新配置
                    Map<String, String> envs = conf.getEnvs();
                    //FIELD_NC_HOME
                    //FIELD_EX_MODULES
                    envs.put("FIELD_NC_HOME", ProjectNCConfigUtil.getNCHomePath());
                    conf.setEnvs(envs);
                    conf.setWorkingDirectory(ncHome.getPath());
                } else if (clientClass.equals(((ApplicationConfiguration) rc).MAIN_CLASS_NAME)) {
                    hasNc[1] = 1;
                    //更新配置
                    Map<String, String> envs = conf.getEnvs();
                    //FIELD_CLINET_IP
                    //FIELD_CLINET_PORT
                    //FIELD_NC_HOME
                    envs.put("FIELD_CLINET_IP", ProjectNCConfigUtil.getNCClientIP());
                    envs.put("FIELD_CLINET_PORT", ProjectNCConfigUtil.getNCClientPort());
                    envs.put("FIELD_NC_HOME", ProjectNCConfigUtil.getNCHomePath());
                    conf.setEnvs(envs);
                    conf.setWorkingDirectory(ncHome.getPath());
                }
            });
        }

        if (0 == hasNc[0]) {
            //新增服务端
            ApplicationConfiguration conf = new ApplicationConfiguration("NC服务端", project,
                    ApplicationConfigurationType.getInstance());
            conf.setMainClassName(serverClass);

            HashMap<String, String> envs = new HashMap<>();
            envs.put("FIELD_NC_HOME", ProjectNCConfigUtil.getNCHomePath());
            conf.setEnvs(envs);

            conf.setVMParameters(
                    " -Dnc.http.port=" + ProjectNCConfigUtil.getNCClientPort()
                            + " -Dcom.sun.management.jmxremote "
                            + "-Dcom.sun.management.jmxremote.port=" + RandomUtil.randomInt(25000, 55000)
                            + " -Dcom.sun.management.jmxremote.ssl=false "
                            + "-Dcom.sun.management.jmxremote.authenticate=false "
                            + "-Dnc.exclude.modules1=可以配置你需要排除的模块" //${FIELD_EX_MODULES}
                            + " -Dnc.runMode=develop -Dnc.server.location=$FIELD_NC_HOME$ -Dorg.owasp.esapi.resources=$FIELD_NC_HOME$/ierp/bin/esapi "
                            + " -DEJBConfigDir=$FIELD_NC_HOME$/ejbXMLs"
                            + " -DExtServiceConfigDir=$FIELD_NC_HOME$/ejbXMLs"
                            + " -XX:+HeapDumpOnOutOfMemoryError "
                            + " -Duap.hotwebs=" + ProjectNCConfigUtil.getNcHotWebsList()
                            + " -Djava.awt.headless=true -Dlog4j.ignoreTCL=true -Duser.timezone=GMT+8 "
                            + (
                            NcVersionEnum.NCC.equals(ProjectNCConfigUtil.getNCVerSIon()) ?
                                    " -Dfile.encoding=UTF-8 " : " -Xmx768m -XX:MaxPermSize=256m "
                    )
            );
            conf.setWorkingDirectory(ncHome.getPath());
            conf.setModule(ModuleManager.getInstance(project).getModules()[0]);
            conf.setShowConsoleOnStdErr(true);
            conf.setShortenCommandLine(ShortenCommandLine.MANIFEST);

            RunConfigurationUtil.addRunJavaApplicationMenu(ProjectUtil.getDefaultProject(), conf, true, true);
        }

        if (0 == hasNc[1]) {
            //新增客户端
            ApplicationConfiguration conf = new ApplicationConfiguration("NC客户端", project,
                    ApplicationConfigurationType.getInstance());
            conf.setMainClassName(clientClass);
            HashMap<String, String> envs = new HashMap<>();
            envs.put("FIELD_CLINET_IP", ProjectNCConfigUtil.getNCClientIP());
            envs.put("FIELD_CLINET_PORT", ProjectNCConfigUtil.getNCClientPort());
            envs.put("FIELD_NC_HOME", ProjectNCConfigUtil.getNCHomePath());
            conf.setEnvs(envs);

            conf.setVMParameters(
                    " -Dcom.sun.management.jmxremote "
                            //    + "-Dcom.sun.management.jmxremote.port=" + RandomUtil.randomInt(25000, 55000)
                            + " -Dcom.sun.management.jmxremote.ssl=false "
                            + "-Dcom.sun.management.jmxremote.authenticate=false "
                            + "-Dnc.runMode=develop"
                            + " -Dnc.jstart.server=$FIELD_CLINET_IP$"
                            + " -Dnc.jstart.port=$FIELD_CLINET_PORT$"
                            + " -Dnc.fi.autogenfile=N "
            );
            conf.setModule(ModuleManager.getInstance(project).getModules()[0]);
            conf.setWorkingDirectory(ncHome.getPath());
            conf.setShowConsoleOnStdErr(true);
            conf.setShortenCommandLine(ShortenCommandLine.MANIFEST);
            RunConfigurationUtil.addRunJavaApplicationMenu(ProjectUtil.getDefaultProject(), conf, false, true);
        }
    }

    public static final boolean checkNCHomeSetPass() {
        if (StringUtil.isEmpty(ProjectNCConfigUtil.getNCHomePath())) {
            Messages.showInfoMessage("未配置NC HOME，请在 Tools 菜单下 配置NC HOME 菜单进行配置！", "警告");
            return false;
        }

        return true;
    }

    /**
     * 更新用户配置的 项目的NC 路径更新 NC的库依赖
     */
    public static final void updateApplicationNCLibrarys(@Nullable Project theProject) {
        if (!checkNCHomeSetPass()) {
            return;
        }

        Project project = null == theProject ? ProjectUtil.getDefaultProject() : theProject;

        File ncHome = new File(ProjectNCConfigUtil.getNCHomePath());

        if (!ncHome.exists() && !ncHome.isDirectory()) {
            Messages.showInfoMessage("NC HOME不正确，请在 Tools 菜单下 配置NC HOME 菜单进行配置！", "警告");
        }

        ApplicationLibraryUtil.addApplicationLibrary(project, ProjectNCConfigUtil.LIB_Ant_Library
                , IoUtil.serachAllNcAntJars(ncHome));

        ApplicationLibraryUtil.addApplicationLibrary(project, ProjectNCConfigUtil.LIB_Product_Common_Library
                , IoUtil.serachProduct_Common_LibraryJars(ncHome));

        ApplicationLibraryUtil.addApplicationLibrary(project, ProjectNCConfigUtil.LIB_Middleware_Library
                , IoUtil.serachMiddleware_LibraryJars(ncHome));

        ApplicationLibraryUtil.addApplicationLibrary(project, ProjectNCConfigUtil.LIB_Framework_Library
                , IoUtil.serachFramework_LibraryJars(ncHome));

        ApplicationLibraryUtil.addApplicationLibrary(project, ProjectNCConfigUtil.LIB_NC_Module_Public_Library
                , IoUtil.serachNC_Module_Public_Library(ncHome));

        ApplicationLibraryUtil.addApplicationLibrary(project, ProjectNCConfigUtil.LIB_Module_Client_Library
                , IoUtil.serachModule_Client_Library(ncHome));

        ApplicationLibraryUtil.addApplicationLibrary(project, ProjectNCConfigUtil.LIB_Module_Private_Library
                , IoUtil.serachModule_Private_Library(ncHome));

        ApplicationLibraryUtil.addApplicationLibrary(project, ProjectNCConfigUtil.LIB_Module_Lang_Library
                , IoUtil.serachModule_Lang_Library(ncHome));

        ApplicationLibraryUtil.addApplicationLibrary(project, ProjectNCConfigUtil.LIB_Generated_EJB
                , IoUtil.serachGenerated_EJB(ncHome));

        if (new File(ProjectNCConfigUtil.getNCHome(), "hotwebs" + File.separatorChar + "nccloud").isDirectory()) {
            ApplicationLibraryUtil.addApplicationLibrary(project, ProjectNCConfigUtil.LIB_NCCloud_Library
                    , IoUtil.serachNCCloud_Library(ncHome));
        }
    }

    /**
     * </br>
     * 马上把最新的项目里所有模块的 META-INF 里所有文件复制到(包括ncc项目的hotwebs的文件)
     * NC HOME里对应的项目模块文件夹，主要是 ejb部署xml       </br>
     * </br>
     * </br>
     *
     * @return void
     * @author air Email: 209308343@qq.com
     * @date 2019/12/25 0025 15:04
     * @Param []
     */
    public static final void copyProjectMetaInfFiles2NCHomeModules() {
        Project project = ProjectUtil.getDefaultProject();
        Module[] modules = ModuleManager.getInstance(project).getModules();
        for (Module module : modules) {
            LogUtil.tryInfo(module.getModuleFilePath()
                    + " 模块执行：NC 各种配置文件 复制到NCHOME:"
                    + ProjectNCConfigUtil.getNCHomePath());
            copyProjectMetaInfFiles2NCHomeModules(module);
        }
    }

    /**
     * </br>
     * 马上把指定模块的 META-INF 里所有文件复制到
     * NC HOME里对应的项目模块文件夹，主要是 ejb部署xml       </br>
     * </br>
     * </br>
     *
     * @return void
     * @author air Email: 209308343@qq.com
     * @date 2019/12/25 0025 15:04
     * @Param []
     */
    public static final void copyProjectMetaInfFiles2NCHomeModules(@NotNull Module module) {
        try {
            copyModuleMetainfoDir2NChome(module);

            if (NcVersionEnum.NCC.equals(ProjectNCConfigUtil.getNCVerSIon())) {
                copyModuleNccConfigDir2NChome(module);
                if (!"true".equals(ProjectNCConfigUtil.getConfigValue("close_client_copy"))) {
                    copyModuleNccActionDir2NChome(module);
                } else {
                    LogUtil.tryInfo(module.getModuleFilePath()
                            + " 模块执行：NCC-hotwebs文件复制到NCHOME. 配置了跳过。执行跳过。");
                }
            }
        } catch (Exception e) {
            LogUtil.error(e.toString(), e);
            e.printStackTrace();
        }
    }

    private static void copyModuleMetainfoDir2NChome(@NotNull Module module) {
        String ncHomePath = ProjectNCConfigUtil.getNCHomePath();
        if (null == ncHomePath || ncHomePath.trim().isEmpty()) {
            return;
        }

        File umpDir = new File(new File(module.getModuleFilePath()).getParentFile(), "META-INF");
        if (!umpDir.exists()) {
            return;
        }

        File nchome = new File(ncHomePath);
        if (!nchome.exists() || !nchome.isDirectory()) {
            return;
        }

        File modeluUmpDir = new File(ncHomePath, File.separatorChar + "modules"
                + File.separatorChar + module.getName() + File.separatorChar + "META-INF");
        if (!modeluUmpDir.exists() || !modeluUmpDir.isDirectory()) {
            modeluUmpDir.mkdirs();
        }

        //复制 ump 文件到这里
        File[] projectFiles = umpDir.listFiles(f -> f.isFile());
        Stream.of(projectFiles).forEach(f -> {
            try {
                //判断文件是否改变， 不改变的跳过复制
                File tof = new File(modeluUmpDir, f.getName());
                if (IoUtil.isNoChange(f, tof)) {
                    return;
                }
                Files.copy(f.toPath(), tof.toPath(), StandardCopyOption
                        .REPLACE_EXISTING);
            } catch (Exception e) {
            }
        });
    }

    /**
     * 复制NCC的配置文件：client
     *
     * @param module
     */
    private static void copyModuleNccConfigDir2NChome(@NotNull Module module) {
        String ncHomePath = ProjectNCConfigUtil.getNCHomePath();
        if (null == ncHomePath || ncHomePath.trim().isEmpty()) {
            return;
        }

        File clientDir = new File(new File(module.getModuleFilePath()).getParentFile(), "src"
                + File.separatorChar + "client");
        if (!clientDir.exists()) {
            return;
        }

        File yyconfigDir = new File(clientDir, "yyconfig");
        if (!yyconfigDir.exists()) {
            return;
        }

        File nchome = new File(ncHomePath);
        if (!nchome.exists() || !nchome.isDirectory()) {
            return;
        }

        // hotwebs\nccloud\WEB-INF\extend\yyconfig\modules\recr
        File ncHomeyyconfigDir = new File(ncHomePath, "hotwebs"
                + File.separatorChar + "nccloud"
                + File.separatorChar + "WEB-INF"
                + File.separatorChar + "extend"
                + File.separatorChar + "yyconfig"
        );
        if (!ncHomeyyconfigDir.exists() || !ncHomeyyconfigDir.isDirectory()) {
            ncHomeyyconfigDir.mkdirs();
        }

        //复制 ump 文件到这里
        LogUtil.tryInfo("开始复制：NCC-hotwebs yyconfig文件复制到NCHOME: " + yyconfigDir.getPath());
        List<File> fs = IoUtil.getAllFiles(yyconfigDir, true);
        for (File f : fs) {
            try {
                File tof = IoUtil.replaceDir(f, yyconfigDir, ncHomeyyconfigDir);
                if (!tof.getParentFile().isDirectory()) {
                    tof.getParentFile().mkdirs();
                }

                LogUtil.tryInfo(module.getModuleFilePath()
                        + " 模块执行：NCC-hotwebs yyconfig文件复制到NCHOME: "
                        + f.getPath() + "   ->  " + tof.getPath());

                Files.copy(f.toPath()
                        , tof.toPath()
                        , StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
            }
        }
    }

    /**
     * 复制NCC的client action代码到hotwebs
     *
     * @param module
     */
    private static void copyModuleNccActionDir2NChome(@NotNull Module module) {
        String ncHomePath = ProjectNCConfigUtil.getNCHomePath();
        if (null == ncHomePath || ncHomePath.trim().isEmpty()) {
            return;
        }

        File clientDir = new File(new File(module.getModuleFilePath()).getParentFile(), "src"
                + File.separatorChar + "client"
                //  + File.separatorChar + "nccloud"
        );
        if (!clientDir.exists()) {
            return;
        }


        File nchome = new File(ncHomePath);
        if (!nchome.exists() || !nchome.isDirectory()) {
            LogUtil.tryInfo("NCHOME无法找到，跳过NCC配置文件复制.");
            return;
        }

        // hotwebs\nccloud\WEB-INF\extend\yyconfig\modules\recr
        File nccClientClassDir = new File(ncHomePath, "hotwebs"
                + File.separatorChar + "nccloud"
                + File.separatorChar + "WEB-INF"
                + File.separatorChar + "classes"
        );
        if (!nccClientClassDir.exists() || !nccClientClassDir.isDirectory()) {
            nccClientClassDir.mkdirs();
        }

        //复制
        VirtualFile[] sourceRoots = ModuleRootManager.getInstance(module).getSourceRoots();
        File classBaseDirFile = null;
        try {
            classBaseDirFile = new File(CompilerModuleExtension.getInstance(module).getCompilerOutputPath().getPath());
        } catch (Exception e) {
        }

        File outPathRoot = nccClientClassDir;

        if (classBaseDirFile != null) {
            //获取所有的 有源码的包路径文件夹！
            List<File> allSourcePackges = IoUtil.getAllLastPackges(clientDir);
            ExportContentVO contentVO = new ExportContentVO();
            contentVO.setIgnoreModules(new ArrayList<>());
            contentVO.setModule2ExportConfigVoMap(new HashMap<>());
            ExportConfigVO exportConfigVO = new ExportConfigVO();
            exportConfigVO.setProp(new Properties());
            contentVO.getModule2ExportConfigVoMap().put(module, exportConfigVO);
            contentVO.setModuleHomeDir2ModuleMap(new HashMap<>());
            contentVO.getModuleHomeDir2ModuleMap().put(module.getModuleFilePath(), module);
            for (final File sourcePackge : allSourcePackges) {
                String packgePath = sourcePackge.getPath().substring(clientDir.getPath().length());
                //class文件位置
                File classFileDir = new File(classBaseDirFile, packgePath);

                LogUtil.tryInfo("复制NCC Action代码文件夹: " + nccClientClassDir.getPath());

                ExportNCPatcherUtil.copyClassAndJavaSourceFiles(sourcePackge, clientDir
                        , contentVO, outPathRoot.getPath(), module
                        , ExportNCPatcherUtil.NC_TYPE_CLIENT, packgePath, classFileDir);

                if(!StringUtil.replaceAll(StringUtil.replaceAll(sourcePackge.getPath(), File.separator, "/"), "\\", "/").contains("client/yyconfig/")){
                    ExportNCPatcherUtil.copyClassPathOtherFile(sourcePackge, outPathRoot.getPath(), module
                            , ExportNCPatcherUtil.NC_TYPE_CLIENT, packgePath, contentVO);
                }
            }
        }

        //移动文件到正确的文件夹结构
        File[] fs1 = nccClientClassDir.listFiles();
        if (CollUtil.isEmpty(fs1)) {
            return;
        }
        File f2;
        for (File f1 : fs1) {
            f2 = new File(f1, "client" + File.separatorChar + "classes");
            if (!f2.isDirectory()) {
                continue;
            }

            IoUtil.copyFile(f2, nccClientClassDir.getParentFile());
            FileUtil.del(f1);
        }
    }

    private IdeaProjectGenerateUtil() {
    }
}
