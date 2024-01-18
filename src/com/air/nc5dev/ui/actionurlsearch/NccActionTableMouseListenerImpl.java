package com.air.nc5dev.ui.actionurlsearch;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class NccActionTableMouseListenerImpl implements MouseListener {
    ActionResultListTable mainPanel;

    public NccActionTableMouseListenerImpl(ActionResultListTable mainPanel) {
        this.mainPanel = mainPanel;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int clickCount = e.getClickCount();

        if (clickCount > 1) {
            int row = mainPanel.getSelectedRow();
            ActionResultDTO vo = mainPanel.getDataOfRow(row);
            mainPanel.openClass(mainPanel, mainPanel.getNccActionURLSearchUI().getProject(),vo);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {

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
