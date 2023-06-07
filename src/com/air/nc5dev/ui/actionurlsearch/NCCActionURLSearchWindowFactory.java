package com.air.nc5dev.ui.actionurlsearch;

import cn.hutool.core.io.FileUtil;
import com.air.nc5dev.acion.GoToNCRequestMappingAction;
import com.air.nc5dev.ui.ToolWindowFactory;
import com.air.nc5dev.util.ExceptionUtil;
import com.air.nc5dev.util.ExportNCPatcherUtil;
import com.air.nc5dev.util.idea.LogUtil;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class NCCActionURLSearchWindowFactory extends ToolWindowFactory {
    @Override
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        NCCActionURLSearchView view = new NCCActionURLSearchView(project);
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(view, "NCC Action URL搜索(双击行 打开Class,或对表格行点击右键)", true);
        toolWindow.getContentManager().addContent(content);

        init2(project);
    }

    //这种会 显示 右下角 非模态 进度条
    public static void init2(Project project) {
        Task.Backgroundable backgroundable = new Task.Backgroundable(project, "初始化NC Action列表中...") {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                GoToNCRequestMappingAction.init(project);
            }
        };
        backgroundable.setCancelText("放弃吧,没有卵用的按钮");
        backgroundable.setCancelTooltipText("这是一个没有卵用的按钮");
        ProgressManager.getInstance().run(backgroundable);
    }

    //这种会 显示模态 加载框
    private void init1(Project project) {
        ProgressManager.getInstance().run((Task) new Task.Modal(project, "初始化NC Action列表中...", false) {
            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                GoToNCRequestMappingAction.init(project);
            }
        });
    }
}