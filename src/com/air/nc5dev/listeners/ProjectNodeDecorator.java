package com.air.nc5dev.listeners;

import cn.hutool.core.util.StrUtil;
import com.air.nc5dev.acion.ExportChangeCodeFilesAction;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.ProjectViewNodeDecorator;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.packageDependencies.ui.PackageDependenciesNode;
import com.intellij.psi.PsiClass;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.psi.PsiJavaFile;
import com.intellij.openapi.util.IconLoader;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * </br>
 * </br>
 * </br>
 *
 * @author 唐粟 Email:209308343@qq.com
 * @date 2026/1/22 0022 18:03
 * @project
 * @Version
 */
public class ProjectNodeDecorator implements ProjectViewNodeDecorator {
    public static SimpleTextAttributes tagStyle = SimpleTextAttributes.GRAYED_ATTRIBUTES;

    static {
        try {
            tagStyle = new SimpleTextAttributes(
                    SimpleTextAttributes.STYLE_SMALLER, // 让字体略小一点，更有层次感
                    new com.intellij.ui.JBColor(new Color(0, 120, 215), new Color(100, 150, 255)) // 适配明亮/黑暗模式
            );
        } catch (Throwable e) {
        }
    }

    @Override
    public void decorate(ProjectViewNode<?> node, PresentationData data) {
        // 1. 获取节点背后的对象
        Object value = node.getValue();

        VirtualFile virtualFile = null;
        String modelName = null;
        if (value instanceof PsiClass) {
            PsiClass pc = (PsiClass) value;
            virtualFile = pc.getContainingFile().getVirtualFile();
            modelName = "";
        } else if (value instanceof PsiJavaFile) {
            PsiJavaFile javaFile = (PsiJavaFile) value;
            virtualFile = javaFile.getVirtualFile();
            modelName = javaFile.getModuleDeclaration().getName();
        }

        if (virtualFile != null) {
            ExportChangeCodeFilesAction.loadFile(node.getProject()
                    , null
                    , new File(virtualFile.getPath())
                    , null
                    , modelName);

            String path = StrUtil.replaceChars(virtualFile.getPath(), new char[]{'\\', '/', File.separatorChar}, File.separator);
            Map<String, ExportChangeCodeFilesAction.FileInfoDTO> m = ExportChangeCodeFilesAction.infoMap.get(node.getProject().getBasePath());
            ExportChangeCodeFilesAction.FileInfoDTO v = m.get(path);
            // 3. 这里编写你的自定义逻辑：决定显示什么文本
            // 例如：如果是接口，显示 "Interface"
            if (v != null) {
                // 定义一个淡蓝色的样式（仿标签感）
                // 4. 将文本添加到右侧
                // 2. 清除当前可能存在的错误状态（可选，防止重复）
                // data.clearText(); // 如果发现文字重复，取消此行注释

                // 3. 【核心】重新添加原始文件名，使用常规样式
                data.addText(data.getPresentableText(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
                // SimpleTextAttributes.GRAYED_ATTRIBUTES 会让文本显示为灰色，不干扰主文件名
                // new SimpleTextAttributes(SimpleTextAttributes.STYLE_ITALIC, JBColor.BLUE): 蓝色斜体。
                // SimpleTextAttributes.ERROR_ATTRIBUTES: 红色文本。
                data.addText(" *标品类", tagStyle);
                data.setTooltip(
                        StrUtil.blankToDefault(data.getTooltip(), "")
                        + v.getOrgins().stream()
                                .map(VirtualFile::getPath)
                                .collect(Collectors.joining("\n"))
                );
            }
        }
    }

    @Override
    public void decorate(PackageDependenciesNode packageDependenciesNode
            , ColoredTreeCellRenderer coloredTreeCellRenderer) {

    }

    public void decorate(PackageDependenciesNode node, PresentationData data) {
        // 通常用于依赖视图，不需要可以留空
    }
}
