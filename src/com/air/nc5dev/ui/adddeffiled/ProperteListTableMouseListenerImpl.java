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

        JMenuItem item = new JMenuItem("查找");
        popup.add(item);
        item.addActionListener(event -> {
            new SearchTableFieldDialog(mainPanel.getProject(), mainPanel.getTablePropertis()).show();
        });

        item = new JMenuItem("增加自定义项");
        popup.add(item);
        item.addActionListener(event -> {
            mainPanel.addDef(PropertyDataTypeEnum.BS000010000100001056);
        });

        item = new JMenuItem("增加自由项");
        popup.add(item);
        item.addActionListener(event -> {
            mainPanel.addDef(PropertyDataTypeEnum.BS000010000100001059);
        });

        item = new JMenuItem("复制选中行");
        popup.add(item);
        item.addActionListener(event -> {
            mainPanel.setCopyedPropertis(mainPanel.getSelects());
        });
        item.setEnabled(CollUtil.isNotEmpty(mainPanel.getSelects()));

        item = new JMenuItem("粘贴到头部");
        popup.add(item);
        item.addActionListener(event -> {
            mainPanel.past2top();
        });
        item.setEnabled(CollUtil.isNotEmpty(mainPanel.getCopyedPropertis()));

        item = new JMenuItem("粘贴到末尾");
        popup.add(item);
        item.addActionListener(event -> {
            mainPanel.past2tail();
        });
        item.setEnabled(CollUtil.isNotEmpty(mainPanel.getCopyedPropertis()));

        item = new JMenuItem("粘贴到当前行后");
        popup.add(item);
        item.addActionListener(event -> {
            mainPanel.past2LineAfter(mainPanel.getCopyedPropertis(), mainPanel.getTablePropertis().getSelectedRow() + 1);
        });
        item.setEnabled(CollUtil.isNotEmpty(mainPanel.getCopyedPropertis()));

        item = new JMenuItem("删除/撤销删除 选中行");
        popup.add(item);
        item.addActionListener(event -> {
            List<PropertyDTO> ps = mainPanel.getSelects();
            for (PropertyDTO p : ps) {
                if (p.getState() == VOStatus.DELETED) {
                    p.setState(VOStatus.UPDATED);
                } else {
                    p.setState(VOStatus.DELETED);
                }
            }
        });
        item.setEnabled(CollUtil.isNotEmpty(mainPanel.getSelects()));

        item = new JMenuItem("全选");
        popup.add(item);
        item.addActionListener(event -> {
            mainPanel.getTablePropertis().selectAll();
        });

        item = new JMenuItem("全消");
        popup.add(item);
        item.addActionListener(event -> {
            mainPanel.getTablePropertis().clearSelection();
        });
        item.setEnabled(CollUtil.isNotEmpty(mainPanel.getResult()));

        item = new JMenuItem("反选");
        popup.add(item);
        item.addActionListener(event -> {
            int[] srs = mainPanel.getTablePropertis().getSelectedRows();
            mainPanel.getTablePropertis().selectAll();
            if (srs == null || srs.length < 1) {
                return;
            } else if (srs.length == mainPanel.getTablePropertis().getRowCount()) {
                mainPanel.getTablePropertis().clearSelection();
                return;
            }

            for (int sr : srs) {
                mainPanel.getTablePropertis().getSelectionModel().removeSelectionInterval(sr, sr);
            }
        });
        item.setEnabled(CollUtil.isNotEmpty(mainPanel.getSelects()));

//        item = new JMenuItem("强制刷新缓存的BMF文件信息");
//        popup.add(item);
//        item.addActionListener(event -> {
//            mainPanel.initFiles(1);
//        });

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
