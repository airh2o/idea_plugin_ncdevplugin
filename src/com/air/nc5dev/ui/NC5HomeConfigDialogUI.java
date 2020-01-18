package com.air.nc5dev.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class NC5HomeConfigDialogUI extends DialogWrapper {
    private NC5HomeConfigDiaLogPanel centerPanel;
    public NC5HomeConfigDialogUI(@Nullable Project project) {
        super(project);
        super.init();
        setTitle("配置NC5x HOME和数据源");
        super.getPeer().setSize(centerPanel.getWidth() + 10, centerPanel.getHeight() + 10);
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        centerPanel = new NC5HomeConfigDiaLogPanel();
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
