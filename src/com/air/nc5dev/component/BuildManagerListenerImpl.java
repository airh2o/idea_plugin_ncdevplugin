package com.air.nc5dev.component;

import com.air.nc5dev.util.CollUtil;
import com.air.nc5dev.util.IdeaProjectGenerateUtil;
import com.air.nc5dev.util.idea.LogUtil;
import com.air.nc5dev.util.idea.ProjectUtil;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileDocumentManagerAdapter;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;


public class BuildManagerListenerImpl implements com.intellij.compiler.server.BuildManagerListener {
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
     //   LogUtil.info(project.getBasePath() + " 项目编译，触发文件复制到NCHOME!");

        IdeaProjectGenerateUtil.copyProjectMetaInfFiles2NCHomeModules();
    }
}
