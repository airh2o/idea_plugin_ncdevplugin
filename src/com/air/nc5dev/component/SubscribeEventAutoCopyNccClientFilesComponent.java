package com.air.nc5dev.component;

import com.air.nc5dev.listeners.NCModuleInitModuleBuilderListenerImpl;
import com.air.nc5dev.util.idea.LogUtil;
import com.intellij.ProjectTopics;
import com.intellij.compiler.server.BuildManagerListener;
import com.intellij.compiler.server.CustomBuilderMessageHandler;
import com.intellij.debugger.impl.DebuggerManagerListener;
import com.intellij.execution.ui.RunContentManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.xdebugger.impl.XDebuggerManagerImpl;
import org.jetbrains.annotations.NotNull;

/**
 * The plugin entry class that instanciates (or reuse) and delegates to {@link BuildManagerListenerImpl}. This is not a
 * singleton, for java based ide the corresponding component will also get instanciated (check {@link JavaComponent}).
 *
 * @see BuildManagerListenerImpl
 */
public class SubscribeEventAutoCopyNccClientFilesComponent implements ApplicationComponent {
    public static volatile boolean inited = false;
    public static final String COMPONENT_NAME = "Build Manager";
    public static volatile Project myproject;

    @Override
    public void initComponent() {
        if (inited) {
            return;
        }

        synchronized (COMPONENT_NAME) {
            if (inited) {
                return;
            }
            inited = true;
            // LogUtil.info("Starting component: " + COMPONENT_NAME);
            try {
                BuildManagerListenerImpl manager = BuildManagerListenerImpl.getInstance();
                MessageBus bus = null;
                if (myproject == null) {
                    bus = ApplicationManager.getApplication().getMessageBus();
                } else {
                    bus = myproject.getMessageBus();
                }

                MessageBusConnection connection = bus.connect();

                //监听编译动作
                connection.subscribe(BuildManagerListener.TOPIC, manager);
                connection.subscribe(CustomBuilderMessageHandler.TOPIC, manager);
                //监听debug run动作
                connection.subscribe(DebuggerManagerListener.TOPIC, manager);
                //监听其他 运行
                connection.subscribe(RunContentManager.TOPIC, manager);
                connection.subscribe(XDebuggerManagerImpl.TOPIC, manager);

                //监听模块创建后
                connection.subscribe(ProjectTopics.MODULES, NCModuleInitModuleBuilderListenerImpl.getMe());
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void disposeComponent() {
        LogUtil.info("Stopping component: " + COMPONENT_NAME);
    }

    @NotNull
    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    public void init(Project project) {
        if (myproject != null && myproject != project) {
            synchronized (COMPONENT_NAME) {
                inited = false;
            }
        }
        myproject = project;
        initComponent();
    }
}
