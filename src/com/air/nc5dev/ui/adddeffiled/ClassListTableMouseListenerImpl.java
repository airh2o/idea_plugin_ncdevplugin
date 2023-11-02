package com.air.nc5dev.ui.adddeffiled;

import cn.hutool.core.util.StrUtil;
import com.air.nc5dev.ui.SearchTableFieldDialog;
import com.air.nc5dev.ui.actionurlsearch.ActionResultListTable;
import com.air.nc5dev.util.CollUtil;
import com.air.nc5dev.util.IoUtil;
import com.air.nc5dev.util.meta.consts.PropertyDataTypeEnum;
import com.air.nc5dev.vo.meta.PropertyDTO;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import nc.vo.pub.VOStatus;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.List;

public class ClassListTableMouseListenerImpl implements MouseListener {
    AddDefField2BmfDialog mainPanel;

    public ClassListTableMouseListenerImpl(AddDefField2BmfDialog mainPanel) {
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

        JMenuItem item = new JMenuItem("查找");
        popup.add(item);
        item.addActionListener(event -> {
            new SearchTableFieldDialog(mainPanel.getProject(), mainPanel.getTableClass()).show();
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
}
