package com.air.nc5dev.ui.patcherselectfile;

import com.air.nc5dev.ui.PatcherDialog;
import com.air.nc5dev.ui.SearchTableFieldDialog;
import com.air.nc5dev.ui.actionurlsearch.ActionResultListTable;
import com.air.nc5dev.util.CollUtil;
import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.vo.PatcherSelectFileVO;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PatcherSelectFilesTableMouseListenerImpl implements MouseListener {
    PathcerSelectFilesDialog mainPanel;

    public PatcherSelectFilesTableMouseListenerImpl(PathcerSelectFilesDialog mainPanel) {
        this.mainPanel = mainPanel;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int clickCount = e.getClickCount();

        if (clickCount > 1) {
            List<PatcherSelectFileVO> vs = PatcherDialog.getForceAddSelectFiles(mainPanel.getProject());
            if (CollUtil.isEmpty(vs)) {
                return;
            }

            if (vs.size() > mainPanel.getTable().getSelectedRow()) {
                PatcherSelectFileVO v = vs.get(mainPanel.getTable().getSelectedRow());
                ActionResultListTable.openFile(mainPanel.getProject(), v.getFile(), 0, 0);
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

        JMenuItem memo = new JMenuItem("编辑备注");
        popup.add(memo);
        memo.addActionListener(event -> {
            List<PatcherSelectFileVO> vs = PatcherDialog.getForceAddSelectFiles(mainPanel.getProject());
            if (CollUtil.isEmpty(vs)) {
                return;
            }

            String txt = JOptionPane.showInputDialog("请输入备注内容(取消视为清空内容)", StringUtil.get(vs.get(0).getMemo()));

            int[] rs = mainPanel.getTable().getSelectedRows();
            ArrayList<PatcherSelectFileVO> vv = new ArrayList<>();
            for (int r : rs) {
                vs.get(r).setMemo(txt);
                vv.add(vs.get(r));
            }

            PatcherDialog.setSelectFiles(mainPanel.getProject(), vs);

            try {
                mainPanel.loadDatas();
            } catch (Throwable ex) {
            }
        });

        JMenuItem sort = new JMenuItem("根据备注排序");
        popup.add(sort);
        sort.addActionListener(event -> {
            List<PatcherSelectFileVO> vs = PatcherDialog.getForceAddSelectFiles(mainPanel.getProject());
            if (CollUtil.isEmpty(vs)) {
                return;
            }

            for (PatcherSelectFileVO v : vs) {
                if (v.getMemo() == null) {
                    v.setMemo("");
                }
            }

            vs = vs.stream().sorted((a,b) -> a.getMemo().compareTo(b.getMemo())).collect(Collectors.toList());

            PatcherDialog.setSelectFiles(mainPanel.getProject(), vs);

            try {
                mainPanel.loadDatas();
            } catch (Throwable ex) {
            }
        });

        JMenuItem remove = new JMenuItem("删除选中行");
        popup.add(remove);
        remove.addActionListener(event -> {
            List<PatcherSelectFileVO> vs = PatcherDialog.getForceAddSelectFiles(mainPanel.getProject());
            if (CollUtil.isEmpty(vs)) {
                return;
            }

            int[] rs = mainPanel.getTable().getSelectedRows();
            ArrayList<PatcherSelectFileVO> vv = new ArrayList<>();
            for (int r : rs) {
                vv.add(vs.get(r));
            }

            for (PatcherSelectFileVO v : vv) {
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
