package com.air.nc5dev.util;

import cn.hutool.core.util.ArrayUtil;
import com.air.nc5dev.enums.NcVersionEnum;
import com.air.nc5dev.util.idea.ProjectUtil;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

/**
 * 项目的NC总体配置 读取 维护   工具类       </br>
 * </br>
 * </br>
 * </br>
 *
 * @author air Email: 209308343@qq.com
 * @date 2019/12/25 0025 9:05
 */
@Getter
public class ProjectNCConfigUtil {
    //这是插件实例的唯一标识
    private static final int PLUGIN_RUNTIME_MARK = new Object().hashCode();
    /*** NC配置文件在项目中文件的名字 ***/
    public static final String DEFUAL_NC_CONFIG_PROJECT_FILENAME = "nc.prop";
    public static final String[] dsTypes = new String[]{"ORACLE11G", "ORACLE10G", "SQLSERVER2008", "DB297"};
    public static final String[] dsTypeClasss = new String[]{"oracle.jdbc.OracleDriver", "oracle.jdbc.OracleDriver"
            , "com.microsoft.sqlserver.jdbc.SQLServerDriver", "com.ibm.db2.jcc.DB2Driver"};
    private static boolean isInit = false;
    /**** NC项目里配置文件 属性集合 ***/
    private static Properties configPropertis;
    /**** NC项目里配置文件 ***/
    private static File configFile;

    static {
        //init config
        initConfigFile();
    }

    /**
     * 获取 NC主目录        </br>
     * </br>
     * </br>
     * </br>
     *
     * @return java.lang.String
     * @author air Email: 209308343@qq.com
     * @date 2019/12/25 0025 9:11
     * @Param []
     */
    public static final File getNCHome() {
        return new File(getConfigValue(KEY_PROJECT_NC_CONFIG_NCHOME));
    }

    /**
     * 获取 NC主目录        </br>
     * </br>
     * </br>
     * </br>
     *
     * @return java.lang.String
     * @author air Email: 209308343@qq.com
     * @date 2019/12/25 0025 9:11
     * @Param []
     */
    public static final String getNCHomePath() {
        return getConfigValue(KEY_PROJECT_NC_CONFIG_NCHOME);
    }

    /**
     * 设置NC HOME路径       </br>
     * </br>
     * </br>
     * </br>
     *
     * @return void
     * @author air Email: 209308343@qq.com
     * @date 2019/12/25 0025 9:13
     * @Param [path]
     */
    public static final void setNCHomePath(String path) {
        setNCConfigPropertice(KEY_PROJECT_NC_CONFIG_NCHOME, path);
    }

    /**
     * 获取 NC客户端要链接的服务器ip       </br>
     * </br>
     * </br>
     * </br>
     *
     * @return java.lang.String
     * @author air Email: 209308343@qq.com
     * @date 2019/12/25 0025 9:11
     * @Param []
     */
    public static final String getNCClientIP() {
        return getConfigValue(KEY_PROJECT_NC_CONFIG_CLIENT_IP);
    }

    /**
     * 设置NC 客户端要链接的服务器ip      </br>
     * </br>
     * </br>
     * </br>
     *
     * @return void
     * @author air Email: 209308343@qq.com
     * @date 2019/12/25 0025 9:13
     * @Param [path]
     */
    public static final void setNCClientIP(String path) {
        setNCConfigPropertice(KEY_PROJECT_NC_CONFIG_CLIENT_IP, path);
    }

    /**
     * 获取 NC客户端要链接的服务器 端口      </br>
     * </br>
     * </br>
     * </br>
     *
     * @return java.lang.String
     * @author air Email: 209308343@qq.com
     * @date 2019/12/25 0025 9:11
     * @Param []
     */
    public static final String getNCClientPort() {
        return getConfigValue(KEY_PROJECT_NC_CONFIG_CLIENT_PORT);
    }

    /**
     * 设置NC 客户端要链接的服务器  端口  </br>
     * </br>
     * </br>
     * </br>
     *
     * @return void
     * @author air Email: 209308343@qq.com
     * @date 2019/12/25 0025 9:13
     * @Param [port]
     */
    public static final void setNCClientPort(String port) {
        setNCConfigPropertice(KEY_PROJECT_NC_CONFIG_CLIENT_PORT, port);
    }


    /**
     * 根据key获取NC项目配置值       </br>
     * </br>
     * </br>
     * </br>
     *
     * @return java.lang.String
     * @author air Email: 209308343@qq.com
     * @date 2019/12/25 0025 9:12
     * @Param [key]
     */
    public static final String getConfigValue(@NotNull String key) {
        if (!isInit) {
            initConfigFile();
        }

        return configPropertis.getProperty(key);
    }

    /***
     *     马上把配置信息更新到配置文件里      </br>
     *           </br>
     *           </br>
     *           </br>
     * @author air Email: 209308343@qq.com
     * @date 2019/12/25 0025 9:16
     * @Param []
     * @return void
     */
    public static final void saveConfig2File() {
        if (!isInit) {
            initConfigFile();
        }

        IoUtil.wirtePropertis(ProjectNCConfigUtil.configPropertis, ProjectNCConfigUtil.configFile);
    }

    /**
     * 设置NC项目配置       </br>
     * </br>
     * </br>
     * </br>
     *
     * @return void
     * @author air Email: 209308343@qq.com
     * @date 2019/12/25 0025 9:13
     * @Param [key, value]
     */
    public static final void setNCConfigPropertice(@NotNull String key, String value) {
        if (!isInit) {
            initConfigFile();
        }

        configPropertis.setProperty(key, value);
    }

    /**
     * 检查项目是否有 NC配置文件，没有就初始化
     */
    public static void initConfigFile() {
        if (ProjectUtil.getDefaultProject() == null) {
            throw new RuntimeException("project not finish yet, try restart idea!");
        }
        File configFile = new File("");
        try {
            File projectHome = new File(ProjectUtil.getDefaultProject().getBasePath());
            File ideaDir = new File(projectHome, Project.DIRECTORY_STORE_FOLDER);
            configFile = new File(ideaDir, DEFUAL_NC_CONFIG_PROJECT_FILENAME);
            if (!configFile.exists()) {
                configFile.createNewFile();
            }
            isInit = true;
            //读取
            Properties ncConf = new Properties();
            ncConf.load(new FileInputStream(configFile));
            ProjectNCConfigUtil.configPropertis = ncConf;
            ProjectNCConfigUtil.configFile = configFile;

            if (StringUtils.isBlank(getNCClientIP())) {
                setNCClientIP("127.0.0.1");
            }
            if (StringUtils.isBlank(getNCClientPort())) {
                setNCClientPort("80");
            }
        } catch (IOException e) {
            Messages.showErrorDialog(ProjectUtil.getDefaultProject()
                    , "文件路径: " + configFile.getPath() + " ,异常: " + e.toString()
                    , "实例化项目NC配置文件错误");
        }
    }

    /**** projectNCConfigFilePropertis 对应的项目NC配置文件中的 NC HOME的key  ***/
    public static final String KEY_PROJECT_NC_CONFIG_NCHOME = "home";
    /**** projectNCConfigFilePropertis 对应的项目NC配置文件中的 NC porp.xml 相对路径 的key  ***/
    public static final String KEY_PROJECT_NC_CONFIG_PROP_PATH = "porppath";
    /**** projectNCConfigFilePropertis  运行时候 client ip  ***/
    public static final String KEY_PROJECT_NC_CONFIG_CLIENT_IP = "clientip";
    /**** projectNCConfigFilePropertis  运行时候 client port  ***/
    public static final String KEY_PROJECT_NC_CONFIG_CLIENT_PORT = "clientport";


    /*****    NC 依赖库： NC ant jars         ****/
    public static final String LIB_Ant_Library = "NC_LIBS/Ant_Library";
    /*****    NC 依赖库： NC公共LIB         ****/
    public static final String LIB_Product_Common_Library = "NC_LIBS/Product_Common_Library";
    /*****    NC 依赖库： NC Middleware_Library  ****/
    public static final String LIB_Middleware_Library = "NC_LIBS/Middleware_Library";
    /*****    NC 依赖库： NC Framework_Library  ****/
    public static final String LIB_Framework_Library = "NC_LIBS/Framework_Library";
    /*****    NC 依赖库： NC模块  Module_Public_Library     ****/
    public static final String LIB_NC_Module_Public_Library = "NC_LIBS/Module_Public_Library";
    /*****    NC 依赖库： NC模块  Module_Client_Library     ****/
    public static final String LIB_Module_Client_Library = "NC_LIBS/Module_Client_Library";
    /*****    NC 依赖库： NC模块  Module_Private_Library     ****/
    public static final String LIB_Module_Private_Library = "NC_LIBS/Module_Private_Library";
    /*****    NC 依赖库： NC模块  Module_Lang_Library     ****/
    public static final String LIB_Module_Lang_Library = "NC_LIBS/Module_Lang_Library";
    /*****    NC 依赖库： NC模块  Generated_EJB     ****/
    public static final String LIB_Generated_EJB = "NC_LIBS/Generated_EJB";
    /*****    NC 依赖库： NC模块  NCC     ****/
    public static final String LIB_NCCloud_Library = "NC_LIBS/Module_NCCloud_Library";

    private ProjectNCConfigUtil() {
        throw new RuntimeException("cannot instance Util Class!");
    }

    /**
     * 获取这个插件的运行实例的唯一标识int        </br>
     * </br>
     * </br>
     * </br>
     *
     * @return int
     * @author air Email: 209308343@qq.com
     * @date 2020/3/18 0018 9:28
     * @Param []
     */
    public static int getPluginRuntimeMark() {
        return PLUGIN_RUNTIME_MARK;
    }

    /**
     * 获取NC的 hotwebs里面的文件夹名字列表 ,隔开
     *
     * @return
     */
    public static String getNcHotWebsList() {
        String s = "lfw,portal,fs ";
        File ncHome = getNCHome();
        if (ncHome == null) {
            return s;
        }

        File hw = new File(ncHome, "hotwebs");
        if (!hw.isDirectory()) {
            return s;
        }

        File[] fs = hw.listFiles();
        if (ArrayUtil.isEmpty(fs)) {
            return s;
        }

        ArrayList<String> ns = Lists.newArrayListWithCapacity(fs.length);
        for (File f : fs) {
            if (f.isDirectory()) {
                ns.add(f.getName());
            }
        }

        return Joiner.on(',').skipNulls().join(ns);
    }

    /**
     * 获取NC的版本
     *
     * @return
     */
    public static NcVersionEnum getNCVerSIon() {
        //先看看是否配置文件强行指定了NC版本！
        String version = getConfigValue("nc.version");
        if (StringUtil.isNotEmpty(version)) {
            return NcVersionEnum.valueOf(version);
        }

        File f = new File(getNCHome(), "webapps"
                + File.separatorChar + "u8c_web"
                + File.separatorChar + "webapps"
                + File.separatorChar + "Client"
                + File.separatorChar + "appletjar"
                + File.separatorChar + "U8C_Login.jar"
        );
        if (f.isFile()) {
            return NcVersionEnum.U8Cloud;
        }

        f = new File(getNCHome(), "webapps"
                + File.separatorChar + "nc_web"
                + File.separatorChar + "Client"
                + File.separatorChar + "appletjar"
                + File.separatorChar + "NC_Login_v507.jar"
        );
        if (f.isFile()) {
            return NcVersionEnum.NC5;
        }

        f = new File(getNCHome(), "hotwebs"
                + File.separatorChar + "nccloud"
        );

        if (f.isDirectory()) {
            return NcVersionEnum.NCC;
        }

        return NcVersionEnum.NC6;
    }

}