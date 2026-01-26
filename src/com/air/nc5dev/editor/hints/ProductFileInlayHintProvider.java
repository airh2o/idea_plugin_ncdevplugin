package com.air.nc5dev.editor.hints;

import cn.hutool.core.util.StrUtil;
import com.air.nc5dev.acion.ExportChangeCodeFilesAction;
import com.air.nc5dev.ui.actionurlsearch.ActionResultListTable;
import com.air.nc5dev.util.CollUtil;
import com.intellij.codeInsight.hints.*;
import com.intellij.codeInsight.hints.presentation.InlayPresentation;
import com.intellij.codeInsight.hints.presentation.MouseButton;
import com.intellij.codeInsight.hints.presentation.PresentationFactory;
import com.intellij.lang.Language;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.util.Collection;
import java.util.Map;

/**
 * 标品类  编辑器 hints超链接提示
 */
public class ProductFileInlayHintProvider implements InlayHintsProvider<NoSettings> {
    public InlayHintsCollector getCollectorFor(@NotNull PsiFile psiFile
            , @NotNull Editor editor
            , @NotNull NoSettings settings
            , @NotNull InlayHintsSink inlayHintsSink) {
        return createCollector(psiFile, editor, settings);
    }

    public InlayHintsCollector createCollector(@NotNull PsiFile psiFile
            , @NotNull Editor editor
            , @NotNull NoSettings settings) {
        return new FactoryInlayHintsCollector(editor) {
            @Override
            public boolean collect(@NotNull PsiElement element, @NotNull Editor editor, @NotNull InlayHintsSink sink) {
                ExportChangeCodeFilesAction.init(element.getProject());
                VirtualFile virtualFile = null;
                String modelName = null;
                ExportChangeCodeFilesAction.FileInfoDTO v = null;
                PsiElement identifier = null;
                String className = null;
                if (element instanceof PsiClass) {
                    PsiClass pc = (PsiClass) element;
                    virtualFile = pc.getContainingFile().getVirtualFile();
                    identifier = pc.getNameIdentifier();
                    modelName = "";
                    className = pc.getName();
                }
                if (virtualFile != null) {
                    ExportChangeCodeFilesAction.loadFile(editor.getProject()
                            , null
                            , new File(virtualFile.getPath())
                            , null
                            , modelName);

                    String path = StrUtil.replaceChars(virtualFile.getPath(), new char[]{'\\', '/', File.separatorChar}, File.separator);
                    Map<String, ExportChangeCodeFilesAction.FileInfoDTO> m = ExportChangeCodeFilesAction.infoMap.get(editor.getProject().getBasePath());
                    v = m == null ? null : m.get(path);
                }

                if (v != null && identifier != null) {
                    Collection<VirtualFile> fs = v.getOrgins();
                    PresentationFactory factory = getFactory();
                    String classFullName = className;
                    // 创建文本
                    InlayPresentation presentation = factory.text(" *标品类");
                    // 绑定点击：MouseButton.Left 是 2021.2 引入的通用枚举
                    InlayPresentation onClick = factory.onClick(presentation, MouseButton.Left, (event, payload) -> {
                        // 执行你的点击逻辑
                        System.out.println("点击了类: " + classFullName);
                        if (CollUtil.isNotEmpty(fs)) {
                            for (VirtualFile f : fs) {
                                ActionResultListTable.openFile(editor.getProject(), f, 1, 1);
                            }
                        }
                        return null;
                    });
                    // 插入到类名后面
                    // 参数：offset, relatesToPrecedingText, presentation
                    sink.addInlineElement(
                            identifier.getTextRange().getEndOffset(),
                            true,
                            onClick
                    );
                }

                return true;
            }
        };
    }

    // --- 以下是 2021.2+ 必须实现的样板方法 ---

    @NotNull
    @Override
    public NoSettings createSettings() {
        return new NoSettings();
    }

    @NotNull
    @Override
    public String getName() {
        return "NC标品类提示";
    }

    @NotNull
    @Override
    public SettingsKey<NoSettings> getKey() {
        return new SettingsKey<>(this.getClass().getName());
    }

    @Nullable
    @Override
    public String getPreviewText() {
        return "class Example {}";
    }

    @NotNull
    @Override
    public ImmediateConfigurable createConfigurable(@NotNull NoSettings settings) {
        return changeListener -> new JPanel();
    }

    @Override
    public boolean isLanguageSupported(@NotNull com.intellij.lang.Language language) {
        // 这种写法兼容性最强，能适配所有 Java 变体
        return language.getID().equals("JAVA") ||
                language.getDisplayName().equalsIgnoreCase("Java");
    }

    @Override
    public boolean isVisibleInSettings() {
        return false;
    }
}