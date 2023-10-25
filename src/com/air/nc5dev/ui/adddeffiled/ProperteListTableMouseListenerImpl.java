package com.air.nc5dev.ui.adddeffiled;

import cn.hutool.core.util.StrUtil;
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

public class ProperteListTableMouseListenerImpl implements MouseListener {
    AddDefField2BmfDialog mainPanel;

    public ProperteListTableMouseListenerImpl(AddDefField2BmfDialog mainPanel) {
        this.mainPanel = mainPanel;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int clickCount = e.getClickCount();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() != MouseEvent.BUTTON3) {
            return;
        }

        final JPopupMenu popup = new JPopupMenu();

        JMenuItem item = new JMenuItem("增加自定义项");
        popup.add(item);
        item.addActionListener(event -> {
            mainPanel.initFiles(1);
        });

        item = new JMenuItem("增加自由项");
        popup.add(item);
        item.addActionListener(event -> {
            mainPanel.initFiles(1);
        });

        item = new JMenuItem("复制选中字段");
        popup.add(item);
        item.addActionListener(event -> {
            mainPanel.initFiles(1);
        });

        item = new JMenuItem("粘贴到头部");
        popup.add(item);
        item.addActionListener(event -> {
            mainPanel.initFiles(1);
        });

        item = new JMenuItem("粘贴到末尾");
        popup.add(item);
        item.addActionListener(event -> {
            mainPanel.initFiles(1);
        });

        item = new JMenuItem("粘贴到当前行后");
        popup.add(item);
        item.addActionListener(event -> {
            mainPanel.initFiles(1);
        });

        item = new JMenuItem("全选/全消");
        popup.add(item);
        item.addActionListener(event -> {
            mainPanel.initFiles(1);
        });

        item = new JMenuItem("反选");
        popup.add(item);
        item.addActionListener(event -> {
            mainPanel.initFiles(1);
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
