package com.air.nc5dev.util.idea;

import cn.hutool.core.lang.UUID;
import com.air.nc5dev.util.CollUtil;
import com.air.nc5dev.util.IoUtil;
import com.air.nc5dev.util.XmlUtil;
import com.air.nc5dev.vo.NCDataSourceVO;
import com.google.common.collect.Lists;
import com.intellij.notification.*;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.XMLWriter;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * 项目 工具类</br>
 * </br>
 * </br>
 * </br>
 *
 * @author air Email: 209308343@qq.com
 * @version NC505, JDK1.5+, V1.0
 * @date 2019/12/25 0025 8:43
 * @project
 */
public class ProjectUtil {
    private static final Logger LOG = Logger.getInstance(ProjectUtil.class);
    private static Project project;
    /**
     * 本插件 默认的 非模态提醒的 统一 组id
     */
    public static final NotificationGroup NOTIFICATION_GROUP = new NotificationGroup("com.air.nc5dev.tool.plugin" +
            ".nc5devtool",
            NotificationDisplayType.STICKY_BALLOON, false);

    /**
     * 显示一个 错误的 非模态提醒      </br>
     * </br>
     * </br>
     * </br>
     *
     * @return void
     * @author air Email: 209308343@qq.com
     * @date 2020/2/20 0020 17:05
     * @Param [msg]
     */
    public static void errorNotification(String msg, Project project) {
        Notification notification = NOTIFICATION_GROUP.createNotification(msg, NotificationType.ERROR);
        notification.setSubtitle("错误:");
        Notifications.Bus.notify(notification, project == null ? getDefaultProject() : project);
    }

    /**
     * 显示一个 警告的 非模态提醒      </br>
     * </br>
     * </br>
     * </br>
     *
     * @return void
     * @author air Email: 209308343@qq.com
     * @date 2020/2/20 0020 17:05
     * @Param [msg]
     */
    public static void warnNotification(String msg, Project project) {
        Notification notification = NOTIFICATION_GROUP.createNotification(msg, NotificationType.WARNING);
        notification.setSubtitle("警告:");
        Notifications.Bus.notify(notification, project == null ? getDefaultProject() : project);
    }

    /**
     * 显示一个 正常消息提醒的 非模态提醒      </br>
     * </br>
     * </br>
     * </br>
     *
     * @return void
     * @author air Email: 209308343@qq.com
     * @date 2020/2/20 0020 17:05
     * @Param [msg]
     */
    public static void infoNotification(String msg, Project project) {
        Notification notification = NOTIFICATION_GROUP.createNotification(msg, NotificationType.INFORMATION);
        notification.setSubtitle("提醒:");
        Notifications.Bus.notify(notification, project == null ? getDefaultProject() : project);
    }

    /* *
     *     获取默认的项目      </br>
     *           </br>
     *           </br>
     *           </br>
     * @author air Email: 209308343@qq.com
     * @date 2019/12/25 0025 8:44
     * @Param []
     * @return com.intellij.openapi.project.Project
     */
    public static Project getDefaultProject() {
        //因为IDEA可能打开多个项目，所以这里 2个办法 先根据之前按钮触发的最后一次 项目如果不是null 直接返回
        if (project != null) {
            return project;
        }
        //实在没办法了，直接返回最后一个打开的项目
        Project[] openProjects = getProjectMannager().getOpenProjects();
        return null == openProjects || openProjects.length < 1 ? project : openProjects[openProjects.length - 1];
    }

    /* *
     *    TODO  根据项目名字获取项目,未实现，返回默认项目      </br>
     *           </br>
     *           </br>
     *           </br>
     * @author air Email: 209308343@qq.com
     * @date 2019/12/25 0025 8:47
     * @Param [name]
     * @return com.intellij.openapi.project.Project
     */
    @Deprecated
    public static Project getProjectByName(String name) {
        return getDefaultProject();
    }

    /* *
     *    获取项目管理器       </br>
     *           </br>
     *           </br>
     *           </br>
     * @author air Email: 209308343@qq.com
     * @date 2019/12/25 0025 8:46
     * @Param []
     * @return com.intellij.openapi.project.ProjectManager
     */
    @Nonnull
    public static ProjectManager getProjectMannager() {
        return ProjectManager.getInstance();
    }

    private ProjectUtil() {
        throw new RuntimeException("cannot instance Util Class!");
    }

    public static void setProject(Project project) {
        ProjectUtil.project = project;
    }

    public static Project getProject() {
        return project;
    }

    public static <T> T getService(@NotNull Project project, Class<T> clazz) {
        T t = ServiceManager.getService(project, clazz);
        if (t == null) {
            LOG.error("Could not find service: " + clazz.getName());
            throw new IllegalArgumentException("Class not found: " + clazz.getName());
        }

        return t;
    }

    public static <T> T getService(Class<T> clazz) {
        return getService(getDefaultProject(), clazz);
    }

    public static File getResourceTemplates(String name) {
        return new File(getResource().getPath()
                + File.separatorChar + "templates", name);
    }

    public static File getResource() {
        return new File(ProjectUtil.class.getResource("/").getPath());
    }

    /**
     * 往IDEA的数据库database管理工具 新增数据连接
     *
     * @param basePath      项目ROOT文件夹路径
     * @param dataSourceVOS 数据源们
     * @throws IOException
     */
    public static void addDatabaseToolLinks(String basePath, List<NCDataSourceVO> dataSourceVOS) throws IOException  {
        File dsxml = new File(basePath + File.separatorChar + ".idea", "dataSources.xml");
        File dslxml = new File(basePath + File.separatorChar + ".idea", "dataSources.local.xml");

        if (!dsxml.isFile()) {
            IoUtil.copyFile(ProjectUtil.getResourceTemplates("dataSources.xml")
                    , dsxml.getParentFile());
        }
        if (!dslxml.isFile()) {
            IoUtil.copyFile(ProjectUtil.getResourceTemplates("dataSources.local.xml")
                    , dslxml.getParentFile());
        }

        Document document = XmlUtil.xmlFile2DocumentSax(dsxml);
        Element rootElement = document.getRootElement();
        Element component = rootElement.element("component");

        Document documentLocal = XmlUtil.xmlFile2DocumentSax(dslxml);
        Element rootElementLocal = documentLocal.getRootElement();
        Element componentLocal = rootElementLocal.element("component");

        final String dataSourceElementName = "data-source";
        List<Element> elementDs = component.elements(dataSourceElementName);

        if (CollUtil.isEmpty(elementDs)) {
            elementDs = Lists.newArrayListWithCapacity(dataSourceVOS.size());
        }

        List<NCDataSourceVO> newDs = Lists.newArrayListWithCapacity(dataSourceVOS.size());
        for (NCDataSourceVO dataSourceVO : dataSourceVOS) {
            if (elementDs.stream().noneMatch(element ->
                    getDsName(dataSourceVO).equals(element.attributeValue("name")))) {
                newDs.add(dataSourceVO);
            }
        }

        if (newDs.isEmpty()) {
            LogUtil.info("过滤掉已有同名数据源的连接,没有可以新增的连接!");
            return;
        }

        LogUtil.info("新增连接: " + newDs.toString());
        Element dele;
        Element deleLocal;
        String id;
        String name;
        for (NCDataSourceVO d : newDs) {
            dele = component.addElement(dataSourceElementName);
            dele.addAttribute("source", "LOCAL");
            id = UUID.fastUUID().toString();
            dele.addAttribute("uuid", id);
            name = getDsName(d);
            dele.addAttribute("name", name);
            dele.addElement("driver-ref").setText(d.getDatabaseType().toLowerCase());
            dele.addElement("synchronize").setText("true");
            dele.addElement("remarks").setText(d.getUser() + '@' + d.getPassword());
            dele.addElement("auto-commit").setText("false");
            dele.addElement("jdbc-driver").setText(d.getDriverClassName());
            dele.addElement("jdbc-url").setText(d.getDatabaseUrl());
            dele.addElement("driver-properties")
                    .addElement("property")
                    .addAttribute("name", "v$session.program")
                    .addAttribute("value", "DataGrip");


            deleLocal = componentLocal.addElement("data-source")
                    .addAttribute("name", name)
                    .addAttribute("uuid", id);
            deleLocal.addElement("database-info")
                    .addAttribute("product", "")
                    .addAttribute("version", "")
                    .addAttribute("jdbc-version", "")
                    .addAttribute("driver-name", "")
                    .addAttribute("driver-version", "");
            deleLocal.addElement("secret-storage").setText("master_key");
            deleLocal.addElement("first-sync").setText("true");
            deleLocal.addElement("user-name").setText(d.getUser());
            deleLocal.addElement("introspection-schemas").setText("*:");
        }

        //保存
        XMLWriter writer = new XMLWriter(new FileOutputStream(dsxml));
        writer.write(document);
        writer.close();

        writer = new XMLWriter(new FileOutputStream(dslxml));
        writer.write(documentLocal);
        writer.close();

        LogUtil.info("生成数据库连接成功");
    }

    private static String getDsName(NCDataSourceVO d) {
        return d.getDataSourceName() + '_' + d.getDatabaseUrl();
    }
}
