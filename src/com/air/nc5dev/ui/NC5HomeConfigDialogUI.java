package com.air.nc5dev.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class NC5HomeConfigDialogUI extends DialogWrapper {
    private NC5HomeConfigDiaLogPanel centerPanel;
    Project project;
    public NC5HomeConfigDialogUI(@Nullable Project project) {
        super(project);
        super.init();
        setTitle("配置NC HOME(只需要保证HOME位置对和客户端IP端口配置正确)");
        super.getPeer().setSize(centerPanel.getWidth() + 10, centerPanel.getHeight() + 10);
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        centerPanel = new NC5HomeConfigDiaLogPanel(project);
        centerPanel.setPreferredSize(new Dimension(769, 606));
        return centerPanel;
    }

    @Nullable
    @Override
    protected String getDimensionServiceKey() {
        return "NC5HomeConfigDialogUI";
    }

    public NC5HomeConfigDiaLogPanel getCenterPanel() {
        return centerPanel;
    }
}
