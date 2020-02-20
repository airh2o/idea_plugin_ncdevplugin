package com.air.nc5dev.util.idea;

import com.intellij.notification.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;

import javax.annotation.Nonnull;

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
    private static Project project;
    /**
      *        本插件 默认的 非模态提醒的 统一 组id
     */
    public static final NotificationGroup NOTIFICATION_GROUP = new NotificationGroup("com.air.nc5dev.tool.plugin.nc5devtool",
                                                                         NotificationDisplayType.STICKY_BALLOON, false);
    /**
      *     显示一个 错误的 非模态提醒      </br>
      *           </br>
      *           </br>
      *           </br>
      * @author air Email: 209308343@qq.com
      * @date 2020/2/20 0020 17:05
      * @Param [msg]
      * @return void
     */
    public static void errorNotification(String msg, Project project){
        Notification notification = NOTIFICATION_GROUP.createNotification(msg, NotificationType.ERROR);
        notification.setSubtitle("错误:");
        Notifications.Bus.notify(notification, project == null ? getDefaultProject() : project);
    }

    /**
     *     显示一个 警告的 非模态提醒      </br>
     *           </br>
     *           </br>
     *           </br>
     * @author air Email: 209308343@qq.com
     * @date 2020/2/20 0020 17:05
     * @Param [msg]
     * @return void
     */
    public static void warnNotification(String msg, Project project){
        Notification notification = NOTIFICATION_GROUP.createNotification(msg, NotificationType.WARNING);
        notification.setSubtitle("警告:");
        Notifications.Bus.notify(notification, project == null ? getDefaultProject() : project);
    }

    /**
     *     显示一个 正常消息提醒的 非模态提醒      </br>
     *           </br>
     *           </br>
     *           </br>
     * @author air Email: 209308343@qq.com
     * @date 2020/2/20 0020 17:05
     * @Param [msg]
     * @return void
     */
    public static void infoNotification(String msg, Project project){
        Notification notification = NOTIFICATION_GROUP.createNotification(msg, NotificationType.INFORMATION);
        notification.setSubtitle("提醒:");
        Notifications.Bus.notify(notification, project == null ? getDefaultProject() : project);
    }

    public static void setBaseDefaultProject(Project project){
        ProjectUtil.project = project;
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
    @Nonnull
    public static Project getDefaultProject(){
        Project[] openProjects = getProjectMannager().getOpenProjects();
        return null == openProjects || openProjects.length < 1 ? project : openProjects[0];
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
    public static Project getProjectByName(String name){
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
    public static ProjectManager getProjectMannager(){
        return ProjectManager.getInstance();
    }
    private ProjectUtil() {
        throw new RuntimeException("cannot instance Util Class!");
    }
}
