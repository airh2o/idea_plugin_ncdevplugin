package com.air.nc5dev.component;

import com.air.nc5dev.util.IdeaProjectGenerateUtil;
import com.air.nc5dev.util.ProjectNCConfigUtil;
import com.air.nc5dev.util.idea.LogUtil;
import com.air.nc5dev.util.idea.ProjectUtil;
import com.intellij.debugger.impl.DebuggerManagerListener;
import com.intellij.debugger.impl.DebuggerSession;
import com.intellij.execution.Executor;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunContentWithExecutorListener;
import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebuggerManagerListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;


public class BuildManagerListenerImpl implements com.intellij.compiler.server.BuildManagerListener
        , DebuggerManagerListener, RunContentWithExecutorListener, XDebuggerManagerListener {
    private static BuildManagerListenerImpl instance;

    public static BuildManagerListenerImpl getInstance() {
        if (instance == null) {
            instance = new BuildManagerListenerImpl();
        }
        return instance;
    }

    private BuildManagerListenerImpl() {
    }

    @Override
    public void buildFinished(@NotNull Project project, @NotNull UUID sessionId, boolean isAutomake) {
        doTask(project);
        LogUtil.tryInfo("com.air.nc5dev.component.BuildManagerListenerImpl.buildFinished 触发.");
    }

    @Override
    public void sessionCreated(DebuggerSession session) {
        doTask(null);
        LogUtil.tryInfo("com.air.nc5dev.component.BuildManagerListenerImpl.sessionCreated 触发.");
    }

    public void doTask(Project project) {
        project = null == project ? ProjectUtil.getDefaultProject() : project;
        LogUtil.tryInfo(project.getBasePath()
                + " 项目编译或运行等，触发NC 配置文件文件复制到NCHOME:"
                + ProjectNCConfigUtil.getNCHomePath());

        IdeaProjectGenerateUtil.copyProjectMetaInfFiles2NCHomeModules();
    }

    @Override
    public void contentSelected(@Nullable RunContentDescriptor runContentDescriptor
            , @NotNull Executor executor) {
        doTask(null);
        LogUtil.tryInfo("com.air.nc5dev.component.BuildManagerListenerImpl.contentSelected 触发.");
    }

    @Override
    public void contentRemoved(@Nullable RunContentDescriptor runContentDescriptor, @NotNull Executor executor) {

    }

    @Override
    public void processStarted(@NotNull XDebugProcess debugProcess) {
        doTask(debugProcess.getSession().getProject());
        LogUtil.tryInfo("com.air.nc5dev.component.BuildManagerListenerImpl.processStarted 触发.");
    }
}
