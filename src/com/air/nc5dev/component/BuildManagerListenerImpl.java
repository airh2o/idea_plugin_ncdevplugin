package com.air.nc5dev.component;

import com.air.nc5dev.codecheck.NCApplicationInspectionProfileManager;
import com.air.nc5dev.codecheck.NCDefualtLocalInspectionTool;
import com.air.nc5dev.codecheck.NCGlobalInspectionContextImpl;
import com.air.nc5dev.codecheck.NCInspectionContextImpl;
import com.air.nc5dev.util.*;
import com.air.nc5dev.util.idea.LogUtil;
import com.air.nc5dev.util.idea.ProjectUtil;
import com.google.common.collect.Lists;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.ex.*;
import com.intellij.compiler.server.CustomBuilderMessageHandler;
import com.intellij.debugger.impl.DebuggerManagerListener;
import com.intellij.debugger.impl.DebuggerSession;
import com.intellij.execution.Executor;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunContentWithExecutorListener;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.profile.codeInspection.InspectionProjectProfileManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebuggerManagerListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;


public class BuildManagerListenerImpl implements com.intellij.compiler.server.BuildManagerListener
        , DebuggerManagerListener, RunContentWithExecutorListener, XDebuggerManagerListener,
        CustomBuilderMessageHandler {
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

        if ("true".equals(ProjectNCConfigUtil.getConfigValue(project, "buildAfterNCCodeCheck"))) {
            try {
                checkNCCode(project);
            } catch (Throwable e) {
                //is ok
            }
        }

        LogUtil.tryInfo(project.getBasePath()
                + " 项目编译或运行等，触发NC 配置文件文件复制到NCHOME:"
                + ProjectNCConfigUtil.getNCHomePath());

        try {
            IdeaProjectGenerateUtil.copyProjectMetaInfFiles2NCHomeModules();
        } catch (Throwable e) {
            //is ok
        }

        //刷新 ncc路径
        try {
            NCCActionRefreshUtil.reloadProjectAction(project);
        } catch (Throwable e) {
            //is ok
        }
    }

    public static volatile boolean isRuning = false;

    /**
     * 检查NC的代码是否符合NC架构要求
     *
     * @param project
     */
    public static void checkNCCode(Project project) {
        if (isRuning) {
            return;
        }

        Task.Backgroundable backgroundable = new Task.Backgroundable(project, "检查中...请等待...") {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                isRuning = true;
                indicator.setText("正在检查代码引用是否有错误...");
                indicator.setText2("检查中... ");
                indicator.setIndeterminate(true);

                try {
                    long s = System.currentTimeMillis();

                    VirtualFile[] files = ProjectRootManager.getInstance(project).getContentRoots();
                    if (CollUtil.isEmpty(files)) {
                        return;
                    }
                    indicator.setText("模块共有:" + files.length);
                    final PsiManager psiManager = PsiManager.getInstance(project);

                    LinkedList<PsiFile> psis = Lists.newLinkedList();
                    for (int i = 0; i < files.length; i++) {
                        indicator.setText(String.format("收集文件中...模块:%s,剩余:%s, %s"
                                , i + 1
                                , files.length - 1 - i
                                , files[i].getPath()));
                        psis.addAll(buildFilesList(psiManager, files[i]));
                    }

                    if (CollUtil.isEmpty(psis)) {
                        return;
                    }

                    indicator.setText(String.format("文件共计 %s 个", psis.size()));
                    for (PsiFile psi : psis) {
                        FileType fileType = psi.getFileType();
                        indicator.setText(String.format("处理文件...: ", psi.getVirtualFile().getPath()));
                        System.out.println();
                    }

                    long e = System.currentTimeMillis();
                } catch (Throwable iae) {
                    LogUtil.error("自动打开路径失败: " + ExceptionUtil.getExcptionDetall(iae));
                } finally {
                    isRuning = false;
                }
            }
        };

        backgroundable.setCancelText("放弃吧,没有卵用的按钮");
        backgroundable.setCancelTooltipText("这是一个没有卵用的按钮");
        ProgressManager.getInstance().run(backgroundable);
        // createGlobalInspectionContextImpl(project).doInspections(new AnalysisScope(project));
    }

    public static List<PsiFile> buildFilesList(final PsiManager psiManager, final VirtualFile virtualFile) {
        return ReadAction.compute(() -> {
            final FindChildFiles visitor = new FindChildFiles(virtualFile, psiManager);
            VfsUtilCore.visitChildrenRecursively(virtualFile, visitor);
            return visitor.locatedFiles;
        });
    }

    @Override
    public void messageReceived(String s, String s1, String s2) {

    }

    public static class FindChildFiles extends VirtualFileVisitor {

        private final VirtualFile virtualFile;
        private final PsiManager psiManager;

        private final List<PsiFile> locatedFiles = new ArrayList<>();

        FindChildFiles(final VirtualFile virtualFile, final PsiManager psiManager) {
            this.virtualFile = virtualFile;
            this.psiManager = psiManager;
        }

        @Override
        public boolean visitFile(@NotNull final VirtualFile file) {
            if (!file.isDirectory() && file.getPath().toLowerCase().endsWith(".java")) {
                final PsiFile psiFile = psiManager.findFile(file);
                if (psiFile != null) {
                    locatedFiles.add(psiFile);
                }
            }

            return true;
        }
    }

    public static Map<Object, LocalInspectionToolWrapper> localInspectionToolWrapperMap = new ConcurrentHashMap<>();

    public static GlobalInspectionContextImpl createGlobalInspectionContextImpl(Project project) {
        InspectionProfileImpl inspectionProfile =
                (InspectionProfileImpl) InspectionProjectProfileManager.getInstance(project).getInspectionProfile();

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
        doTask(null);
    }

    @Override
    public void processStarted(@NotNull XDebugProcess debugProcess) {
        doTask(debugProcess.getSession().getProject());
        LogUtil.tryInfo("com.air.nc5dev.component.BuildManagerListenerImpl.processStarted 触发.");
    }
}
