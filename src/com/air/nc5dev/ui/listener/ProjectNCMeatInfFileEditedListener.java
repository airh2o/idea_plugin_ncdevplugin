package com.air.nc5dev.ui.listener;

import com.air.nc5dev.util.IdeaProjectGenerateUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.util.messages.MessageBusConnection;

import org.jetbrains.annotations.NotNull;
import java.io.File;
import java.util.HashSet;
import java.util.List;

/***
 *    监控 项目里 META-INF 文件夹里文件的修改
 *    ，修改了 马上更新复制到 NC HOME对应的模块里      </br>
 *           </br>
 *           </br>
 *          </br>
 * @author air Email: 209308343@qq.com
 * @version NC505, JDK1.5+, V1.0
 * @date 2019/12/25 0025 15:07
 * @project
 */
@Deprecated //暂时关闭使用 @see com.air.nc5dev.component.Component
public class ProjectNCMeatInfFileEditedListener implements ApplicationComponent, BulkFileListener {
    @SuppressWarnings("WeakerAccess")
    public static HashSet<String> dirtyProjects = new HashSet<>();

    private final MessageBusConnection connection;

    public ProjectNCMeatInfFileEditedListener() {
        connection = ApplicationManager.getApplication().getMessageBus().connect();
    }

    @Override
    public void initComponent() {
        connection.subscribe(VirtualFileManager.VFS_CHANGES, this);
    }

    @Override
    public void disposeComponent() {
        connection.disconnect();
    }

    @Override
    public void before(@NotNull List<? extends VFileEvent> events) {

    }

    @Override
    public void after(@NotNull List<? extends VFileEvent> events) {
        try {
            boolean isMeta = false;
            for (VFileEvent event : events) {
                if (isMetaInfoDir(event)) {
                    isMeta = true;
                    break;
                }
            }

            IdeaProjectGenerateUtil.copyProjectMetaInfFiles2NCHomeModules();
        } catch (Exception e) {
            //is ok
            e.printStackTrace();
        }
    }

    private boolean isMetaInfoDir(VFileEvent event) {
        File file = new File(event.getPath());
        if(null == file || !file.exists()){
            return false;
        }

        return event.getPath().contains("META-INF");
    }

    private String getProjectName(VFileEvent event) {
        String[] paths = event.getPath().split("/");
        if (paths.length > 3) {
            return paths[paths.length - 3];
        }

        return "";
    }

    @SuppressWarnings("WeakerAccess")
    public static boolean projectIsDirty(String projectName) {
        return dirtyProjects.contains(projectName);
    }

}
