package org.tangsu.mstsc.ui.listeners;

import com.air.nc5dev.ui.MstscDialog;
import org.tangsu.mstsc.ui.MainPanel;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class TableMouseListenerImpl2 implements MouseListener {
    MstscDialog mainPanel;

    public TableMouseListenerImpl2(MstscDialog mainPanel) {
        this.mainPanel = mainPanel;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int clickCount = e.getClickCount();

        if (mainPanel.getUpdate().get()) {
            mainPanel.save(null);
        }

        mainPanel.select(mainPanel.getTable().getSelectedRow());

        if (clickCount > 1) {
            mainPanel.link(null);
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
