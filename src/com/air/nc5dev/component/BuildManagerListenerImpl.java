package com.air.nc5dev.component;

import com.air.nc5dev.codecheck.NCApplicationInspectionProfileManager;
import com.air.nc5dev.codecheck.NCDefualtLocalInspectionTool;
import com.air.nc5dev.codecheck.NCGlobalInspectionContextImpl;
import com.air.nc5dev.codecheck.NCInspectionContextImpl;
import com.air.nc5dev.util.IdeaProjectGenerateUtil;
import com.air.nc5dev.util.NCCActionRefreshUtil;
import com.air.nc5dev.util.ProjectNCConfigUtil;
import com.air.nc5dev.util.idea.LogUtil;
import com.air.nc5dev.util.idea.ProjectUtil;
import com.intellij.analysis.AnalysisScope;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.ex.*;
import com.intellij.debugger.impl.DebuggerManagerListener;
import com.intellij.debugger.impl.DebuggerSession;
import com.intellij.execution.Executor;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunContentWithExecutorListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.profile.codeInspection.InspectionProjectProfileManager;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebuggerManagerListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;


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

        if ("true".equals(ProjectNCConfigUtil.getConfigValue("buildAfterNCCodeCheck"))) {
            checkNCCode(project);
        }

        LogUtil.tryInfo(project.getBasePath()
                + " 项目编译或运行等，触发NC 配置文件文件复制到NCHOME:"
                + ProjectNCConfigUtil.getNCHomePath());

        IdeaProjectGenerateUtil.copyProjectMetaInfFiles2NCHomeModules();

        //刷新 ncc路径
        NCCActionRefreshUtil.reloadProjectAction(project);
    }

    /**
     * 检查NC的代码是否符合NC架构要求
     *
     * @param project
     */
    public static void checkNCCode(Project project) {
        createGlobalInspectionContextImpl(project).doInspections(new AnalysisScope(project));
    }

    public static Map<Object, LocalInspectionToolWrapper> localInspectionToolWrapperMap = new ConcurrentHashMap<>();

    public static GlobalInspectionContextImpl createGlobalInspectionContextImpl(Project project) {
        InspectionProfileImpl inspectionProfile = (InspectionProfileImpl) InspectionProjectProfileManager.getInstance(project).getInspectionProfile();

        InspectionManagerEx inspectionManagerEx = (InspectionManagerEx) InspectionManager.getInstance(project);
        NCInspectionContextImpl ncInspectionContext = new NCInspectionContextImpl(project
                , inspectionManagerEx.getContentManager());

        LocalInspectionToolWrapper inspectionToolWrapper;
        synchronized (localInspectionToolWrapperMap) {
            inspectionToolWrapper = localInspectionToolWrapperMap.get("project");
            if (inspectionToolWrapper == null) {
                inspectionToolWrapper = new LocalInspectionToolWrapper(new NCDefualtLocalInspectionTool());
                localInspectionToolWrapperMap.put("project", inspectionToolWrapper);
            }
        }

        if (!InspectionToolRegistrar.getInstance().createTools().contains(inspectionToolWrapper)) {
            try {
                Field f = InspectionToolRegistrar.class.getDeclaredField("myInspectionToolFactories");
                f.setAccessible(true);
                NotNullLazyValue<Collection> myInspectionToolFactories
                        = (NotNullLazyValue<Collection>) f.get(InspectionToolRegistrar.getInstance());
                final LocalInspectionToolWrapper finalinspectionToolWrapper = inspectionToolWrapper;
                myInspectionToolFactories.getValue().add(new Supplier<>() {
                    @Override
                    public Object get() {
                        return finalinspectionToolWrapper;
                    }
                });
            } catch (Throwable e) {
            }
        }
        ncInspectionContext.setExternalProfile(new NCGlobalInspectionContextImpl("NCGlobalInspectionContextImpl"
                , InspectionToolRegistrar.getInstance()
                , new NCApplicationInspectionProfileManager()));
        return ncInspectionContext;
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
