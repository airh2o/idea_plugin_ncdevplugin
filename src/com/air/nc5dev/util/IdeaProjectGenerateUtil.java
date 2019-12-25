package com.air.nc5dev.util;

import com.air.nc5dev.util.idea.ApplicationLibraryUtil;
import com.air.nc5dev.util.idea.ProjectUtil;
import com.air.nc5dev.util.idea.RunConfigurationUtil;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.application.ApplicationConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.impl.RunManagerImpl;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/***
  *    生成新项目里 NC必须的东西       </br>
  *     1.NC库依赖      </br>
  *     2.NC 3个源文件夹 和 1个xml     </br>
  *     3.NC 2个运行选项 服务端+客户端      </br>
  * @author air Email: 209308343@qq.com
  * @date 2019/12/25 0025 9:02
 */
public class IdeaProjectGenerateUtil {
    /**
     *  生成NC默认 几个个文件夹 src和META-INF
     *
     * @param project
     */
    public static final void generateSrcDir(@Nullable Project project) {
        Project project1 = project == null ? ProjectUtil.getDefaultProject() : project;
        File projectHome = new File(project1.getBasePath());
        File src = new File(projectHome, "src");
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
        File umpDir = new File(projectHome, "META-INF");
        if (!umpDir.exists()) {
            umpDir.mkdirs();
        }
        File umpFile = new File(umpDir, "module.xml");
        if (!umpFile.exists()) {
            try {
                PrintWriter out =new PrintWriter(new FileOutputStream(umpFile));
                out.print("<?xml version=\"1.0\" encoding=\"gb2312\"?>\n" +
                        "<module name=\""
                        + project.getName()
                        + "\">\n" +
                        "\t<public>\n" +
                        "\t</public>\n" +
                        "\t<private>\n" +
                        "\t</private>\n" +
                        "</module>");
                out.flush();
                out.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }


         /*try {
           File readme = new File(projectHome, "插件使用帮助-看完可删除.txt");
            PrintWriter out =new PrintWriter(new FileOutputStream(readme));
            StringBuilder stringBuilder = new StringBuilder();

            //ConsoleViewContentType.SYSTEM_OUTPUT
            //ConsoleView out = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
            stringBuilder.append("第一次新建项目-必须步骤：\n" );
            stringBuilder.append("1. Tools -> 配置NC HOME   进行NC HOME配置！ \n" );
            stringBuilder.append("2. 第一步保存后，如果没有选更新依赖，请在 Tools -> 更新NC 库依赖 执行依赖更新 \n" );
            stringBuilder.append("3. 第2步后，请在 Tools -> 生成默认NC运行配置 执行Idea的运行配置,注意 执行后请运行时候根据提示手工修改里面的modelu项目名 \n");
            stringBuilder.append("常见问题：\n " );
            stringBuilder.append("1. Intellij IDEA运行报Command line is too long解法 ：" )
                    .append( " 修改项目下 .idea\\workspace.xml，找到标签 <component name=\"PropertiesComponent\"> ， " )
                    .append(  "在标签里加一行  <property name=\"dynamic.classpath\" value=\"true\" /> " );
            out.write(stringBuilder.toString());
            out.flush();
            out.close();
        }catch (Exception e){
        }*/
    }

    /**
     * 生成 项目的 NC2个运行配置
     *
     * @param project
     */
    public static final void generateRunMenu(@NotNull Project project) {
        if(!checkNCHomeSetPass()){
            return ;
        }

        File ncHome = ProjectNCConfigUtil.getNCHome();

        RunManagerImpl runManager = RunConfigurationUtil.getRunManagerImpl(ProjectUtil.getDefaultProject());
        List<RunConfiguration> configurationsList = runManager.getConfigurationsList(ApplicationConfigurationType.getInstance());

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
            ApplicationConfiguration conf = new ApplicationConfiguration("NC服务端", project, ApplicationConfigurationType.getInstance());
            conf.setMainClassName(serverClass);

            HashMap<String, String> envs = new HashMap<>();
            envs.put("FIELD_NC_HOME", ProjectNCConfigUtil.getNCHomePath());
            conf.setEnvs(envs);

            conf.setVMParameters("-Dnc.exclude.modules= " //${FIELD_EX_MODULES}
                  +  " -Dnc.runMode=develop -Dnc.server.location=$FIELD_NC_HOME$" +
                    " -DEJBConfigDir=$FIELD_NC_HOME$/ejbXMLs" +
                    " -DExtServiceConfigDir=$FIELD_NC_HOME$/ejbXMLs" +
                    " -Xmx768m -XX:MaxPermSize=256m -DEnableSqlDebug=true -XX:+HeapDumpOnOutOfMemoryError ");
            conf.setWorkingDirectory(ncHome.getPath());
            conf.setModule(ModuleManager.getInstance(project).getModules()[0]);

           RunConfigurationUtil.addRunJavaApplicationMenu(ProjectUtil.getDefaultProject(), conf);
        }

        if (0 == hasNc[1]) {
            //新增客户端
            ApplicationConfiguration conf = new ApplicationConfiguration("NC客户端", project, ApplicationConfigurationType.getInstance());
            conf.setMainClassName(clientClass);
            HashMap<String, String> envs = new HashMap<>();
            envs.put("FIELD_CLINET_IP", ProjectNCConfigUtil.getNCClientIP());
            envs.put("FIELD_CLINET_PORT", ProjectNCConfigUtil.getNCClientPort());
            envs.put("FIELD_NC_HOME", ProjectNCConfigUtil.getNCHomePath());
            conf.setEnvs(envs);

            conf.setVMParameters("-Dnc.runMode=develop" +
                    " -Dnc.jstart.server=$FIELD_CLINET_IP$" +
                    " -Dnc.jstart.port=$FIELD_CLINET_PORT$ -Xmx768m -XX:MaxPermSize=256m -Dnc.fi.autogenfile=N ");
            conf.setModule(ModuleManager.getInstance(project).getModules()[0]);
            conf.setWorkingDirectory(ncHome.getPath());

            RunConfigurationUtil.addRunJavaApplicationMenu(ProjectUtil.getDefaultProject(), conf);
        }
    }
    public static final boolean checkNCHomeSetPass(){
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
        if(!checkNCHomeSetPass()){
            return ;
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


    }



    private IdeaProjectGenerateUtil() {
    }
}
