package com.air.nc5dev.util;

import com.air.nc5dev.bean.NCDataSourceVO;
import com.air.nc5dev.util.idea.ProjectUtil;
import com.intellij.openapi.ui.Messages;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.annotation.Nullable;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/***
 *     NC 的prop.xml工具类，提供数据源读取 修改等     </br>
 *           </br>
 *           </br>
 *          </br>
 * @author air Email: 209308343@qq.com
 * @version NC505, JDK1.5+, V1.0
 * @date 2019/12/25 0025 9:34
 * @project
 */
public class NCPropXmlUtil {
    /***** prop.xml 默认的相对NC HOME位置 ***/
    public static final String DEFUAL_NC_PROP_PATH = File.separatorChar + "ierp" + File.separatorChar + "bin" + File.separatorChar + "prop.xml";
   /****  配置的 数据源列表 ****/
    private static List<NCDataSourceVO> dataSourceVOS;

    static{
        loadConfFromFile(ProjectNCConfigUtil.getNCHomePath());
    }

    /***
      *    是否 数据源为空，true空！       </br>
      *     1.还没有读取数据源       </br>
      *     2.读取了 但是xml里是空的     </br>
      *           </br>
      * @author air Email: 209308343@qq.com
      * @date 2019/12/25 0025 9:36
      * @Param []
      * @return boolean
     */
    public static final boolean isDataSourceEmpty(){
        return null == dataSourceVOS || dataSourceVOS.isEmpty();
    }
    /***
      *    获取第几个 数据源       </br>
      *           </br>
      *           </br>
      *           </br>
      * @author air Email: 209308343@qq.com
      * @date 2019/12/25 0025 9:38
      * @Param [index]
      * @return com.air.nc5dev.bean.NCDataSourceVO 获取不到返回null
     */
    public static final NCDataSourceVO get(final int index){
        if(isDataSourceEmpty()){
            return null;
        }

        if(index > dataSourceVOS.size() - 1){
            return null;
        }

        return dataSourceVOS.get(index);
    }
    /***
     *    获取 指定数据源名字的 数据源       </br>
     *           </br>
     *           </br>
     *           </br>
     * @author air Email: 209308343@qq.com
     * @date 2019/12/25 0025 9:38
     * @Param [dataSourceName]
     * @return com.air.nc5dev.bean.NCDataSourceVO 获取不到返回null
     */
    public static final NCDataSourceVO get(final String dataSourceName){
        if(isDataSourceEmpty()){
            return null;
        }

        return stream().filter(e -> {
            return e.getDataSourceName().equals(dataSourceName);
        }).findFirst().get();
    }
    /***
     *    获得数据源操作流       </br>
     *           </br>
     *           </br>
     *           </br>
     * @author air Email: 209308343@qq.com
     * @date 2019/12/25 0025 9:40
     * @Param []
     * @return java.util.stream.DoubleStream
     */
    public static final Stream<NCDataSourceVO> stream() {
        return dataSourceVOS.stream();
    }
    /***
     *     增加一个数据源      </br>
     *           </br>
     *           </br>
     *           </br>
     * @author air Email: 209308343@qq.com
     * @date 2019/12/25 0025 9:41
     * @Param [ds]
     * @return void
     */
    public static void add(NCDataSourceVO ds) {
        if(null == dataSourceVOS){
            dataSourceVOS = new ArrayList<>();
        }

        dataSourceVOS.add(ds);
    }
    /**
     * 获得 NC HOME中 prop.xml 文件路径
     *
     * @return 没设置NCHOME或者文件不存在返回null
     */
    public static final  File getPropFile(@Nullable  String ncHome) {
        if (StringUtil.isEmpty(ProjectNCConfigUtil.getNCHomePath())) {
            return null;
        }
        File ncHomeFile = null;

        if(StringUtil.notEmpty(ncHome)){
            ncHomeFile = new File(ncHome);
        }else{
            ncHomeFile = ProjectNCConfigUtil.getNCHome();
        }

        if(!ncHomeFile.exists() || !ncHomeFile.isDirectory()){
            Messages.showErrorDialog(ProjectUtil.getDefaultProject(), ncHomeFile.getPath() + " NC Home不存在！", "读取NC数据配置错误");
            return null;
        }

        final String propPath = StringUtil.isEmpty(ProjectNCConfigUtil.getConfigValue(ProjectNCConfigUtil.KEY_PROJECT_NC_CONFIG_PROP_PATH))
                ? DEFUAL_NC_PROP_PATH : ProjectNCConfigUtil.getConfigValue(ProjectNCConfigUtil.KEY_PROJECT_NC_CONFIG_PROP_PATH);

        File prop = new File(ncHomeFile  , propPath);
        if (!prop.exists() && !prop.isFile()) {
            Messages.showErrorDialog(ProjectUtil.getDefaultProject(), prop.getPath() + " 配置文件不存在！", "读取NC数据配置错误");
            return null;
        }

        return prop;
    }
    /**
     * 从prop文件中 重新读取数据源信息
     */
    public static final void loadConfFromFile(@Nullable  String ncHome) {
        NCPropXmlUtil.dataSourceVOS = new ArrayList<>();
        final File propFile = getPropFile(ncHome);

        if(null == propFile || !propFile.exists()){
            return ;
        }

        Document document = XmlUtil.xmlFile2Document(propFile);
        Element root = document.getDocumentElement();
        NodeList dataSources = root.getElementsByTagName("dataSource");
        Element element;
        for (int i = 0; i < dataSources.getLength(); i++) {
            element = (Element) dataSources.item(i);
            dataSourceVOS.add(new NCDataSourceVO(element));
        }
    }

    /**
     * 把数据源信息最新的情况 写入到 prop.xml
     *
     */
    public static final  void saveDataSources() {
        final  File propFile = getPropFile(ProjectNCConfigUtil.getNCHomePath());
        final Document document = XmlUtil.xmlFile2Document(propFile);
        final Element root = document.getDocumentElement();
        NodeList dataSources = root.getElementsByTagName("dataSource");
        ArrayList<Node> dataSourceNodes = new ArrayList<>();
        for (int i = 0; i < dataSources.getLength(); i++) {
            dataSourceNodes.add(dataSources.item(i));
        }
        dataSourceNodes.forEach(node -> root.removeChild(node));
        dataSourceVOS.forEach(ds -> {
            Element dataSource = document.createElement("dataSource");
            Element dataSourceName = document.createElement("dataSourceName");
            dataSourceName.setTextContent(ds.getDataSourceName());
            dataSource.appendChild(dataSourceName);

            Element oidMark = document.createElement("oidMark");
            oidMark.setTextContent(ds.getOidMark());
            dataSource.appendChild(oidMark);

            Element databaseUrl = document.createElement("databaseUrl");
            databaseUrl.setTextContent(ds.getDatabaseUrl());
            dataSource.appendChild(databaseUrl);

            Element user = document.createElement("user");
            user.setTextContent(ds.getUser());
            dataSource.appendChild(user);

            Element password = document.createElement("password");
            password.setTextContent(ds.getPassword());
            dataSource.appendChild(password);

            Element driverClassName = document.createElement("driverClassName");
            driverClassName.setTextContent(ds.getDriverClassName());
            dataSource.appendChild(driverClassName);

            Element databaseType = document.createElement("databaseType");
            databaseType.setTextContent(ds.getDatabaseType());
            dataSource.appendChild(databaseType);

            Element maxCon = document.createElement("maxCon");
            maxCon.setTextContent(ds.getMaxCon());
            dataSource.appendChild(maxCon);

            Element minCon = document.createElement("minCon");
            minCon.setTextContent(ds.getMinCon());
            dataSource.appendChild(minCon);

            Element dataSourceClassName = document.createElement("dataSourceClassName");
            dataSourceClassName.setTextContent(ds.getDataSourceClassName());
            dataSource.appendChild(dataSourceClassName);

            Element xaDataSourceClassName = document.createElement("xaDataSourceClassName");
            xaDataSourceClassName.setTextContent(ds.getXaDataSourceClassName());
            dataSource.appendChild(xaDataSourceClassName);

            Element conIncrement = document.createElement("conIncrement");
            conIncrement.setTextContent(ds.getConIncrement());
            dataSource.appendChild(conIncrement);

            Element conInUse = document.createElement("conInUse");
            conInUse.setTextContent(ds.getConInUse());
            dataSource.appendChild(conInUse);

            Element conIdle = document.createElement("conIdle");
            conIdle.setTextContent(ds.getConIdle());
            dataSource.appendChild(conIdle);

            Element isBase = document.createElement("isBase");
            isBase.setTextContent(ds.getIsBase());
            dataSource.appendChild(isBase);

            root.appendChild(dataSource);
        });

        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            // 换行
            transformer.setOutputProperty(OutputKeys.INDENT, "YES");
            // 文档字符编码
            transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
            // 可随意指定文件的后缀,效果一样,但xml比较好解析,比如: E:\\person.txt等
            transformer.transform(new DOMSource(document), new StreamResult(propFile));
        } catch (TransformerException e) {
            e.printStackTrace();
            Messages.showErrorDialog(ProjectUtil.getDefaultProject(), e.toString(), "更新NC 数据源配置文件错误");
        }
    }

    public static List<NCDataSourceVO> getDataSourceVOS() {
        return dataSourceVOS;
    }

    private NCPropXmlUtil() {
        throw new RuntimeException("cannot instance Util Class!");
    }

}
