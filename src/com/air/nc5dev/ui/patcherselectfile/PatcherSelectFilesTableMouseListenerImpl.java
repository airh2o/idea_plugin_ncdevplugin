package com.air.nc5dev.ui.patcherselectfile;

import com.air.nc5dev.ui.MstscDialog;
import com.air.nc5dev.ui.PatcherDialog;
import com.air.nc5dev.ui.SearchTableFieldDialog;
import com.air.nc5dev.ui.actionurlsearch.ActionResultListTable;
import com.air.nc5dev.util.CollUtil;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PatcherSelectFilesTableMouseListenerImpl implements MouseListener {
    PathcerSelectFilesDialog mainPanel;

    public PatcherSelectFilesTableMouseListenerImpl(PathcerSelectFilesDialog mainPanel) {
        this.mainPanel = mainPanel;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int clickCount = e.getClickCount();

        if (clickCount > 1) {
            List<VirtualFile> vs = PatcherDialog.getForceAddSelectFiles(mainPanel.getProject());
            if (CollUtil.isEmpty(vs)) {
                return;
            }

            if (vs.size() > mainPanel.getTable().getSelectedRow()) {
                VirtualFile v = vs.get(mainPanel.getTable().getSelectedRow());
                ActionResultListTable.openFile(mainPanel.getProject(), v, 0, 0);
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

        JMenuItem remove = new JMenuItem("删除选中行");
        popup.add(remove);
        remove.addActionListener(event -> {
            List<VirtualFile> vs = PatcherDialog.getForceAddSelectFiles(mainPanel.getProject());
            if (CollUtil.isEmpty(vs)) {
                return;
            }

            int[] rs = mainPanel.getTable().getSelectedRows();
            ArrayList<VirtualFile> vv = new ArrayList<>();
            for (int r : rs) {
                vv.add(vs.get(r));
            }

            for (VirtualFile v : vv) {
                PatcherDialog.removeForceAddSelectFile(mainPanel.getProject(), v);
            }

            try {
                mainPanel.loadDatas();
            } catch (Throwable ex) {
            }
        });
        JMenuItem clear = new JMenuItem("清空所有");
        popup.add(clear);
        clear.addActionListener(event -> {
            PatcherDialog.clearForceAddSelectFile(mainPanel.getProject());
            mainPanel.loadDatas(null);
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
