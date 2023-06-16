package com.air.nc5dev.util;

import cn.hutool.core.util.ArrayUtil;
import com.air.nc5dev.enums.NcVersionEnum;
import com.air.nc5dev.util.idea.ProjectUtil;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
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
    public static final String DEFUAL_NC_CONFIG_PROJECT_FILENAME = "nc.properties";
    public static final String[] dsTypes = new String[]{"ORACLE11G", "ORACLE10G", "SQLSERVER2008", "DB297"};
    public static final String[] dsTypeClasss = new String[]{"oracle.jdbc.OracleDriver", "oracle.jdbc.OracleDriver"
            , "com.microsoft.sqlserver.jdbc.SQLServerDriver", "com.ibm.db2.jcc.DB2Driver"};
    private static Map<String, Boolean> isInit = Maps.newConcurrentMap();
    /**** NC项目里配置文件 属性集合 ***/
    private static Map<String, Properties> configPropertis = Maps.newConcurrentMap();
    /**** NC项目里配置文件 ***/
    private static Map<String, File> configFile = Maps.newConcurrentMap();

    static {
        //init config
        //   initConfigFile(ProjectUtil.getProject());
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
        String configValue = getConfigValue(KEY_PROJECT_NC_CONFIG_NCHOME);
        if (StringUtil.isBlank(configValue)) {
            return null;
        }
        return new File(configValue);
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
    public static final String getNCHomePath(Project project) {
        return getConfigValue(project, KEY_PROJECT_NC_CONFIG_NCHOME);
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
        return getConfigValue(ProjectUtil.getProject(), KEY_PROJECT_NC_CONFIG_NCHOME);
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
    public static final String getConfigValue(@NotNull String key, String ifnull) {
        return getConfigValue(ProjectUtil.getProject(), key, ifnull);
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
    public static final String getConfigValue(Project project, @NotNull String key, String ifnull) {
        String s = getConfigValue(project, key);
        return s == null ? ifnull : s;
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
        return getConfigValue(ProjectUtil.getProject(), key);
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
    public static final String getConfigValue(Project project, @NotNull String key) {
        if (project == null) {
            return null;
        }

        if (!isInited(project)) {
            initConfigFile(project);
        }

        return configPropertis.get(project.getBasePath()).getProperty(key);
    }

    @Deprecated
    public static final void saveConfig2File() {
        saveConfig2File(ProjectUtil.getProject());
    }

    public static boolean isInited(Project project) {
        return Boolean.TRUE.equals(isInit.get(project.getBasePath()));
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
    public static final void saveConfig2File(Project project) {
        if (!isInited(project)) {
            initConfigFile(project);
        }

        Properties prop = configPropertis.get(project.getBasePath());

        if (!prop.containsKey("close_client_copy")) {
            prop.put("close_client_copy", "false");
        }
        if (!prop.containsKey("buildAfterNCCodeCheck")) {
            prop.put("buildAfterNCCodeCheck", "false");
        }
        if (!prop.containsKey("nc.version")) {
            prop.put("nc.version", "");
        }
        if (!prop.containsKey("filtersql")) {
            prop.put("filtersql", "true");
        }
        if (!prop.containsKey("rebuildsql")) {
            prop.put("rebuildsql", "false");
        }
        if (!prop.containsKey("data_source_index")) {
            prop.put("data_source_index", "0");
        }
        if (!prop.containsKey("enableSubResultSet")) {
            prop.put("enableSubResultSet", "true");
        }
        if (!prop.containsKey("includeDeletes")) {
            prop.put("includeDeletes", "false");
        }
        if (!prop.containsKey("reNpmBuild")) {
            prop.put("reNpmBuild", "true");
        }
        if (!prop.containsKey("format4Ygj")) {
            prop.put("format4Ygj", "true");
        }
        if (!prop.containsKey("selectExport")) {
            prop.put("selectExport", "false");
        }
        if (!prop.containsKey("reWriteSourceFile")) {
            prop.put("reWriteSourceFile", "false");
        }
        if (!prop.containsKey("deleteDir")) {
            prop.put("deleteDir", "true");
        }
        if (!prop.containsKey("zip")) {
            prop.put("zip", "true");
        }
        if (!prop.containsKey("exportResources")) {
            prop.put("exportResources", "true");
        }
        if (!prop.containsKey("exportSql")) {
            prop.put("exportSql", "true");
        }
        if (!prop.containsKey("onleyFullSql")) {
            prop.put("onleyFullSql", "true");
        }
        IoUtil.wirtePropertis(prop, ProjectNCConfigUtil.configFile.get(project.getBasePath()));
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
        setNCConfigPropertice(ProjectUtil.getProject(), key, value);
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
    public static final void setNCConfigPropertice(Project project, @NotNull String key, String value) {
        if (!isInited(project)) {
            initConfigFile(project);
        }

        configPropertis.get(project.getBasePath()).setProperty(key, value);
    }

    /**
     * 检查项目是否有 NC配置文件，没有就初始化
     */
    public static void initConfigFile(Project project) {
        if (project == null) {
            return;
        }

        File configFile = new File("");
        try {
            File projectHome = new File(ProjectUtil.getDefaultProject().getBasePath());
            File ideaDir = new File(projectHome, Project.DIRECTORY_STORE_FOLDER);
            configFile = new File(ideaDir, DEFUAL_NC_CONFIG_PROJECT_FILENAME);
            if (!configFile.isFile()) {
                configFile = new File(ideaDir, "nc.prop");
                if (configFile.isFile()) {
                    configFile.renameTo(new File(ideaDir, DEFUAL_NC_CONFIG_PROJECT_FILENAME));
                    configFile = new File(ideaDir, DEFUAL_NC_CONFIG_PROJECT_FILENAME);
                }
            }
            if (!configFile.exists()) {
                configFile.createNewFile();
            }
            isInit.put(project.getBasePath(), true);
            //读取
            Properties ncConf = new Properties();
            ncConf.load(new FileInputStream(configFile));
            configPropertis.put(project.getBasePath(), ncConf);
            ProjectNCConfigUtil.configFile.put(project.getBasePath(), configFile);

            if (StringUtils.isBlank(getNCClientIP())) {
                setNCClientIP("127.0.0.1");
            }
            if (StringUtils.isBlank(getNCClientPort())) {
                setNCClientPort("80");
            }
        } catch (IOException e) {
            Messages.showErrorDialog(project
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
    /*****    NC 依赖库： NC模块  Module_Public_Hyext_Library     ****/
    public static final String LIB_NC_Module_Public_Hyext_Library = "NC_LIBS/Module_Public_Hyext_Library";
    /*****    NC 依赖库： NC模块  Module_Client_Library     ****/
    public static final String LIB_Module_Client_Library = "NC_LIBS/Module_Client_Library";
    /*****    NC 依赖库： NC模块  Module_Client_Hyext_Library     ****/
    public static final String LIB_Module_Client_Hyext_Library = "NC_LIBS/Module_Client_Hyext_Library";
    /*****    NC 依赖库： NC模块  Module_Private_Library     ****/
    public static final String LIB_Module_Private_Library = "NC_LIBS/Module_Private_Library";
    /*****    NC 依赖库： NC模块  Module_Private_Hyext_Library     ****/
    public static final String LIB_Module_Private_Hyext_Library = "NC_LIBS/Module_Private_Hyext_Library";
    /*****    NC 依赖库： NC模块  Module_Private_Extra_Library     ****/
    public static final String LIB_Module_Private_Extra_Library = "NC_LIBS/Module_Private_Extra_Library";
    /*****    NC 依赖库： NC模块  Module_Lang_Library     ****/
    public static final String LIB_Module_Lang_Library = "NC_LIBS/Module_Lang_Library";
    /*****    NC 依赖库： NC模块  Generated_EJB     ****/
    public static final String LIB_Generated_EJB = "NC_LIBS/Generated_EJB";
    /*****    NC 依赖库： NC模块  NCC     ****/
    public static final String LIB_NCCloud_Library = "NC_LIBS/Module_NCCloud_Library";
    /*****    NC 依赖库： NC模块  NCCHR     ****/
    public static final String LIB_NCCHR_Library = "NC_LIBS/Module_NCCHR_Library";
    /*****    NC 依赖库： Resources     ****/
    public static final String LIB_RESOURCES = "NC_LIBS/Resources";

    /*****    U8C 依赖库： 解决启用优先级         ****/
    public static final String U8C_RUN_FIRST_DEPEND = "U8C_RUN_FIRST_DEPEND/Run_Library";

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

    /**
     * NC是否为行业版
     * 
     * @return
     */
    public static boolean isHyVersion() {
        // 读取配置文件
        File f = new File(getNCHome(), "ierp"
                + File.separatorChar + "bin"
                + File.separatorChar + "industry_config.xml"
        );
        if (!f.isFile()) {
            return false;
        }
        // 解析配置文件
        Document document = XmlUtil.xmlFile2Document2(f);
        // 产品编码
        String industryCode = document.getDocumentElement().getElementsByTagName("entry").item(0).getTextContent();
        // 非0，为行业版
        return !"0".equals(industryCode);
    }

}