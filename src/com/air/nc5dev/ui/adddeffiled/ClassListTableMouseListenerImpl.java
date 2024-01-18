package com.air.nc5dev.ui.adddeffiled;

import com.air.nc5dev.ui.SearchTableFieldDialog;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

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
