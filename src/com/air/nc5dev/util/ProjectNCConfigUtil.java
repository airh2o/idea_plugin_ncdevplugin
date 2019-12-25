package com.air.nc5dev.util;

import com.air.nc5dev.util.idea.ProjectUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
  *    项目的NC总体配置 读取 维护   工具类       </br>
  *           </br>
  *           </br>
  *           </br>
  * @author air Email: 209308343@qq.com
  * @date 2019/12/25 0025 9:05
 */
public class ProjectNCConfigUtil {
    /*** NC配置文件在项目中文件的名字 ***/
    public static final String DEFUAL_NC_CONFIG_PROJECT_FILENAME = "nc.prop";
    public static final  String[] dsTypes = new String[]{"ORACLE11G", "ORACLE10G", "SQLSERVER2008", "DB297"};
    public static final  String[] dsTypeClasss = new String[]{"oracle.jdbc.OracleDriver", "oracle.jdbc.OracleDriver", "com.microsoft.sqlserver.jdbc.SQLServerDriver", "com.ibm.db2.jcc.DB2Driver"};

    /**** NC项目里配置文件 属性集合 ***/
    private static Properties configPropertis;
    /**** NC项目里配置文件 ***/
    private static File configFile;

    static{
        //init config
        initConfigFile();
    }
    /**
     *   获取 NC主目录        </br>
     *           </br>
     *           </br>
     *           </br>
     * @author air Email: 209308343@qq.com
     * @date 2019/12/25 0025 9:11
     * @Param []
     * @return java.lang.String
     */
    public static final File getNCHome(){
        return new File(getConfigValue(KEY_PROJECT_NC_CONFIG_NCHOME));
    }
    /**
      *   获取 NC主目录        </br>
      *           </br>
      *           </br>
      *           </br>
      * @author air Email: 209308343@qq.com
      * @date 2019/12/25 0025 9:11
      * @Param []
      * @return java.lang.String
     */
    public static final String getNCHomePath(){
        return getConfigValue(KEY_PROJECT_NC_CONFIG_NCHOME);
    }
    /**
     *    设置NC HOME路径       </br>
     *           </br>
     *           </br>
     *           </br>
     * @author air Email: 209308343@qq.com
     * @date 2019/12/25 0025 9:13
     * @Param [path]
     * @return void
     */
    public static final void setNCHomePath(String path){
        setNCConfigPropertice(KEY_PROJECT_NC_CONFIG_NCHOME, path);
    }
    /**
     *   获取 NC客户端要链接的服务器ip       </br>
     *           </br>
     *           </br>
     *           </br>
     * @author air Email: 209308343@qq.com
     * @date 2019/12/25 0025 9:11
     * @Param []
     * @return java.lang.String
     */
    public static final String getNCClientIP(){
        return getConfigValue(KEY_PROJECT_NC_CONFIG_CLIENT_IP);
    }
    /**
     *    设置NC 客户端要链接的服务器ip      </br>
     *           </br>
     *           </br>
     *           </br>
     * @author air Email: 209308343@qq.com
     * @date 2019/12/25 0025 9:13
     * @Param [path]
     * @return void
     */
    public static final void setNCClientIP(String path){
        setNCConfigPropertice(KEY_PROJECT_NC_CONFIG_CLIENT_IP, path);
    }
    /**
     *   获取 NC客户端要链接的服务器 端口      </br>
     *           </br>
     *           </br>
     *           </br>
     * @author air Email: 209308343@qq.com
     * @date 2019/12/25 0025 9:11
     * @Param []
     * @return java.lang.String
     */
    public static final String getNCClientPort(){
        return getConfigValue(KEY_PROJECT_NC_CONFIG_CLIENT_PORT);
    }
    /**
     *    设置NC 客户端要链接的服务器  端口  </br>
     *           </br>
     *           </br>
     *           </br>
     * @author air Email: 209308343@qq.com
     * @date 2019/12/25 0025 9:13
     * @Param [port]
     * @return void
     */
    public static final void setNCClientPort(String port){
        setNCConfigPropertice(KEY_PROJECT_NC_CONFIG_CLIENT_PORT, port);
    }



    /**
      *  根据key获取NC项目配置值       </br>
      *           </br>
      *           </br>
      *           </br>
            * @author air Email: 209308343@qq.com
      * @date 2019/12/25 0025 9:12
            * @Param [key]
            * @return java.lang.String
     */
    public static final String getConfigValue(@Nonnull String key){
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
    public static final void saveConfig2File(){
        IoUtil.wirtePropertis(ProjectNCConfigUtil.configPropertis, ProjectNCConfigUtil.configFile);
    }
    /**
      *    设置NC项目配置       </br>
      *           </br>
      *           </br>
      *           </br>
      * @author air Email: 209308343@qq.com
      * @date 2019/12/25 0025 9:13
      * @Param [key, value]
      * @return void
     */
    public static final void setNCConfigPropertice(@Nonnull String key, String value){
        configPropertis.setProperty(key, value);
    }
    /**
     * 检查项目是否有 NC配置文件，没有就初始化
     */
    public static void initConfigFile() {
        File configFile = new File("");
        try {
            File projectHome = new File(ProjectUtil.getDefaultProject().getBasePath());
            File ideaDir = new File(projectHome, Project.DIRECTORY_STORE_FOLDER);
            configFile = new File(ideaDir, DEFUAL_NC_CONFIG_PROJECT_FILENAME);
            if (!configFile.exists()) {
                configFile.createNewFile();
            }

            //读取
            Properties ncConf = new Properties();
            ncConf.load(new FileInputStream(configFile));
            ProjectNCConfigUtil.configPropertis = ncConf;
            ProjectNCConfigUtil.configFile = configFile;
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
    public static final String LIB_Ant_Library = "nc.uap.mde.library.container/Ant_Library";
    /*****    NC 依赖库： NC公共LIB         ****/
    public static final String LIB_Product_Common_Library = "nc.uap.mde.library.container/Product_Common_Library";
    /*****    NC 依赖库： NC Middleware_Library  ****/
    public static final String LIB_Middleware_Library = "nc.uap.mde.library.container/Middleware_Library";
    /*****    NC 依赖库： NC Framework_Library  ****/
    public static final String LIB_Framework_Library = "nc.uap.mde.library.container/Framework_Library";
    /*****    NC 依赖库： NC模块  Module_Public_Library     ****/
    public static final String LIB_NC_Module_Public_Library = "nc.uap.mde.library.container/Module_Public_Library";
    /*****    NC 依赖库： NC模块  Module_Client_Library     ****/
    public static final String LIB_Module_Client_Library = "nc.uap.mde.library.container/Module_Client_Library";
    /*****    NC 依赖库： NC模块  Module_Private_Library     ****/
    public static final String LIB_Module_Private_Library = "nc.uap.mde.library.container/Module_Private_Library";
    /*****    NC 依赖库： NC模块  Module_Lang_Library     ****/
    public static final String LIB_Module_Lang_Library = "nc.uap.mde.library.container/Module_Lang_Library";
    /*****    NC 依赖库： NC模块  Generated_EJB     ****/
    public static final String LIB_Generated_EJB = "nc.uap.mde.library.container/Generated_EJB";


    private ProjectNCConfigUtil() {
        throw new RuntimeException("cannot instance Util Class!");
    }
}