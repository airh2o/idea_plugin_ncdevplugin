package com.air.nc5dev.ui.exportbmf;

import cn.hutool.core.util.StrUtil;
import com.air.nc5dev.ui.PatcherDialog;
import com.air.nc5dev.ui.actionurlsearch.ActionResultDTO;
import com.air.nc5dev.ui.actionurlsearch.ActionResultListTable;
import com.air.nc5dev.util.CollUtil;
import com.air.nc5dev.util.IoUtil;
import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.vo.meta.SearchComponentVO;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ExportbmfTableMouseListenerImpl implements MouseListener {
    ExportbmfDialog mainPanel;

    public ExportbmfTableMouseListenerImpl(ExportbmfDialog mainPanel) {
        this.mainPanel = mainPanel;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int clickCount = e.getClickCount();

        if (clickCount > 1) {
            SearchComponentVO v = CollUtil.get(mainPanel.getResult(), mainPanel.getTable().getSelectedRow());
            if (v != null && StringUtil.isNotBlank(v.getFilePath())) {
                openXml(mainPanel.getProject(), v.getFilePath());
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() != MouseEvent.BUTTON3) {
            return;
        }

        final JPopupMenu popup = new JPopupMenu();

        JMenuItem export = new JMenuItem("导出当前行BMF文件");
        popup.add(export);
        export.addActionListener(event -> {
            SearchComponentVO v = CollUtil.get(mainPanel.getResult(), mainPanel.getTable().getSelectedRow());
            if (v != null && StringUtil.isNotBlank(v.getFilePath())) {
                mainPanel.export2Files(CollUtil.toList(v));
            }
        });

        JMenuItem remove = new JMenuItem("打开BMF文件");
        popup.add(remove);
        remove.addActionListener(event -> {
            SearchComponentVO v = CollUtil.get(mainPanel.getResult(), mainPanel.getTable().getSelectedRow());
            if (v != null && StringUtil.isNotBlank(v.getFilePath())) {
                openXml(mainPanel.getProject(), v.getFilePath());
            }
        });

        JMenuItem clear = new JMenuItem("打开BMF文件位置");
        popup.add(clear);
        clear.addActionListener(event -> {
            SearchComponentVO v = CollUtil.get(mainPanel.getResult(), mainPanel.getTable().getSelectedRow());
            if (v != null && StringUtil.isNotBlank(v.getFilePath())) {
                IoUtil.tryOpenFileExpolor(new File(v.getFilePath()));
            }
        });

        JMenuItem forceRefash = new JMenuItem("强制刷新缓存的BMF文件信息");
        popup.add(forceRefash);
        forceRefash.addActionListener(event -> {
            mainPanel.initFiles(1);
        });

        popup.show(e.getComponent(), e.getX(), e.getY());
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    public static void openXml(Project project, String xml) {
        // 直接 打开 文件 编辑
        final FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);

        VirtualFile virtualFile = null;

        String jar = "jar://" + StrUtil.replace(StrUtil.replace(xml, "\\", "/"), ".jar/", ".jar!/");
        if (jar.toLowerCase().contains(".jar!/")) {
            virtualFile = VirtualFileManager.getInstance().findFileByUrl(jar);
        } else {
            virtualFile = VirtualFileManager.getInstance().findFileByNioPath(new File(xml).toPath());
        }

        if (virtualFile == null) {
            IoUtil.tryOpenFileExpolor(new File(xml));
            return;
        }

        ActionResultListTable.openFile(project, virtualFile, 1, 1);
    }
}
