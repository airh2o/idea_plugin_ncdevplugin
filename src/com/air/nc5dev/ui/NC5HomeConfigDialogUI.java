package com.air.nc5dev.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;
import javax.swing.*;

public class NC5HomeConfigDialogUI extends DialogWrapper {
    private NC5HomeConfigDiaLogPanel centerPanel;

    public NC5HomeConfigDialogUI(@Nullable Project project) {
        super(project);
        setSize(950, 640);
        init();
        setSize(950, 640);
        setTitle("配置NC5x HOME和数据源");
        setSize(950, 640);
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        centerPanel = new NC5HomeConfigDiaLogPanel();
        centerPanel.setVisible(true);
        return centerPanel;
    }

    public NC5HomeConfigDiaLogPanel getCenterPanel() {
        return centerPanel;
    }
}
