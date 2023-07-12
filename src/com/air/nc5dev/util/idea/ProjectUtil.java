package com.air.nc5dev.util.idea;

import cn.hutool.core.lang.UUID;
import com.air.nc5dev.util.CollUtil;
import com.air.nc5dev.util.IoUtil;
import com.air.nc5dev.util.ProjectNCConfigUtil;
import com.air.nc5dev.util.XmlUtil;
import com.air.nc5dev.vo.NCDataSourceVO;
import com.google.common.collect.Lists;
import com.intellij.notification.*;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.impl.java.stubs.index.JavaFullClassNameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectAndLibrariesScope;
import com.intellij.util.Alarm;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.XMLWriter;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

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
    private static volatile Project project;
    /**
     * 本插件 默认的 非模态提醒的 统一 组id
     */
  /*  public static final NotificationGroup NOTIFICATION_GROUP = new NotificationGroup(
            "com.air.nc5dev.tool.plugin.nc5devtool"
            , NotificationDisplayType.STICKY_BALLOON, false);*/

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
        Notification notification = createNotification(msg, NotificationType.ERROR);
        notification.setSubtitle("错误:");
        Notifications.Bus.notify(notification, project == null ? getDefaultProject() : project);
    }

    private static Notification createNotification(String msg, NotificationType notificationType) {
        return NotificationGroupManager.getInstance()
                .getNotificationGroup("com.air.nc5dev.tool.plugin.nc5devtool")
                .createNotification(msg, notificationType)
                ;
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
        Notification notification = createNotification(msg, NotificationType.WARNING);
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
        Notification notification = createNotification(msg, NotificationType.INFORMATION);
        notification.setSubtitle("提醒:");
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
    public static void notifyAndHide(String msg, Project project) {
        Notification notification = createNotification(msg, NotificationType.INFORMATION);
        notification.setSubtitle("提醒:");
        Notifications.Bus.notifyAndHide(notification, project == null ? getDefaultProject() : project);
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
    public static void notifyAndHide(String msg, Project project, int delayMillis) {
        if (delayMillis < 500) {
            delayMillis = (int) TimeUnit.SECONDS.toMillis(5L);
        }

        Notification notification = createNotification(msg, NotificationType.INFORMATION);
        notification.setSubtitle("提醒:");
        Notifications.Bus.notify(notification, project == null ? getDefaultProject() : project);

        Alarm alarm = new Alarm((Disposable) (project == null ? ApplicationManager.getApplication() : project));
        alarm.addRequest(() -> {
            notification.expire();
            Disposer.dispose(alarm);
        }, delayMillis);
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
        if (project != null && project.isOpen() && !project.isDisposed()) {
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
        Project[] openProjects = getProjectMannager().getOpenProjects();
        if (openProjects == null || openProjects.length < 1) {
            return null;
        }

        for (Project p : openProjects) {
            if (p.getName().equals(name)) {
                return p;
            }
        }
        return null;
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
    @NotNull
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


    static Map<Project, Map<Class, Object>> cacheMap = new ConcurrentHashMap<>();

    public static <T> T getService(Project project, Class<T> clazz) {
        Map<Class, Object> map = cacheMap.get(project);
        if (map == null) {
            synchronized (cacheMap) {
                map = cacheMap.get(project);
                if (map == null) {
                    cacheMap.put(project, new ConcurrentHashMap<>());
                    map = cacheMap.get(project);
                }
            }
        }

        T t = (T) map.get(clazz);
        if (t == null) {
            synchronized (cacheMap) {
                if (map.get(clazz) != null) {
                    return (T) map.get(clazz);
                }

                try {
                    //  ApplicationManager.getApplication().getComponent(EditorFactory.class);
                    t = ServiceManager.getService(project, clazz);
                    if (t != null) {
                        map.put(clazz, t);
                    }
                } catch (Throwable e) {
                    return null;
                }
            }
        }

        return t;
    }

    public static <T> T getService(Class<T> clazz, Project project) {
        try {
            return getService(project, clazz);
        } catch (Throwable e) {
            return null;
        }
    }

    public static File getResourceTemplates(String name) {
        return new File(getResource().getPath()
                + File.separatorChar + "templates", name);
    }

    public static String getResourceTemplatesUtf8Txt(String name) {
        InputStream input = ProjectUtil.class.getResourceAsStream("/" + "templates" + "/" + name);
        if (input == null) {
            return null;
        }

        try {
            return IoUtil.readUtf8(input);
        } catch (Exception e) {
            return null;
        }
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
    public static void addDatabaseToolLinks(String basePath, List<NCDataSourceVO> dataSourceVOS) throws IOException {
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

    /**
     * 获取整个项目依赖库
     *
     * @param project
     * @param vf
     * @param row
     * @param column
     */
    public static void openFile(Project project, VirtualFile vf, int row, int column) {
        com.intellij.openapi.application.ApplicationManager.getApplication()
                .runReadAction(() -> openFile0(project, vf, row, column));
    }

    /**
     * 获取某个 类的psi
     *
     * @param className
     * @param project
     * @param scope
     * @return
     */
    public static Collection<PsiClass> findClassByFullName(String className, Project project, GlobalSearchScope scope) {
        if (scope == null) {
            scope = getGlobalSearchScope(project);
        }

        JavaFullClassNameIndex nameIndex = JavaFullClassNameIndex.getInstance();
        Collection<PsiClass> pcs = null;

        try {
            pcs = nameIndex.get(className.hashCode()
                    , project
                    , scope);
        } catch (Throwable exception) {
            //兼容版本
            Method m = null;
            try {
                m = JavaFullClassNameIndex.class.getMethod("get", CharSequence.class, Project.class,
                        GlobalSearchScope.class);
            } catch (Throwable e) {
                Method[] ms = JavaFullClassNameIndex.class.getMethods();
                for (Method m1 : ms) {
                    if (m1.getName().equals("get")) {
                        m = m1;
                        break;
                    }
                }
            }

            if (m != null) {
                try {
                    pcs = (Collection<PsiClass>) m.invoke(nameIndex, className, project, scope);
                } catch (Throwable e1) {
                }
            }
        }

        return pcs;
    }

    public static GlobalSearchScope getGlobalSearchScope(Project project) {
        GlobalSearchScope scope = null;
        try {
            scope = new ProjectAndLibrariesScope(project, true);
        } catch (Throwable exception) {
            try {
                scope = new ProjectAndLibrariesScope(project);
            } catch (Throwable e) {
                Module[] ms = ModuleManager.getInstance(project).getModules();
                Module module = ms[0];
                final LibraryTable.ModifiableModel model =
                        LibraryTablesRegistrar.getInstance().getLibraryTable(project).getModifiableModel();
                for (Module m : ms) {
                    LibraryEx library = (LibraryEx) model.getLibraryByName(ProjectNCConfigUtil.LIB_Middleware_Library);
                    if (library != null) {
                        module = m;
                        break;
                    }
                }
                scope = module.getModuleWithLibrariesScope();
            }
        }

        return scope;
    }

    private static void openFile0(Project project, VirtualFile vf, int row, int column) {
        FileNavigator fileNavigator = FileNavigator.getInstance();
        try {
            if (row < 1 && column < 1) {
                fileNavigator.navigate(new OpenFileDescriptor(project, vf), true);
                return;
            }
        } catch (Throwable exception) {
        }

        final FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        ApplicationManager.getApplication().invokeLater(() -> {
            final FileEditor[] editor = fileEditorManager.openFile(vf, true);

            if (editor.length < 1) {
                return;
            }

            for (FileEditor e : editor) {
                if (e instanceof TextEditor) {
                    final Editor textEditor = ((TextEditor) e).getEditor();

                    final LogicalPosition problemPos = new LogicalPosition(row < 0 ? 0 : row, column < 0 ? 0 : column);
                    textEditor.getCaretModel().moveToLogicalPosition(problemPos);

                    textEditor.getScrollingModel().scrollToCaret(ScrollType.CENTER);
                    textEditor.getCaretModel().getCurrentCaret().selectLineAtCaret();
                }
            }
        }, ModalityState.NON_MODAL);
    }
}
