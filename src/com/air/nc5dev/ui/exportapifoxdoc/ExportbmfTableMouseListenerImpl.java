package com.air.nc5dev.ui.exportapifoxdoc;

import cn.hutool.core.util.StrUtil;
import com.air.nc5dev.ui.SearchTableFieldDialog;
import com.air.nc5dev.ui.actionurlsearch.ActionResultListTable;
import com.air.nc5dev.util.CollUtil;
import com.air.nc5dev.util.IoUtil;
import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.vo.meta.SearchComponentVO;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;

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

        JMenuItem item = new JMenuItem("查找");
        popup.add(item);
        item.addActionListener(event -> {
            new SearchTableFieldDialog(mainPanel.getProject(), mainPanel.getTable()).show();
        });

        JMenuItem export = new JMenuItem("导出当前行Apifox文档JSON配置文件");
        popup.add(export);
        export.addActionListener(event -> {
            SearchComponentVO v = CollUtil.get(mainPanel.getResult(), mainPanel.getTable().getSelectedRow());
            if (v != null) {
                mainPanel.export2Files(CollUtil.toList(v));
            }
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
