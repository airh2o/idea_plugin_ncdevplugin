package org.tangsu.mstsc.ui;

import lombok.Data;
import org.tangsu.mstsc.service.MstscEntitServiceImpl;

import javax.swing.*;
import java.sql.SQLException;

@Data
public class MainWindow extends JFrame {
    MainPanel mainPanel;
    volatile MstscEntitServiceImpl mstscEntitService;

    public MainWindow() throws Exception {
        init();
    }

    public void init() throws Exception {
        mstscEntitService = new MstscEntitServiceImpl(null);

        mainPanel = new MainPanel(mstscEntitService);
        setSize(mainPanel.getWidth() + 10, mainPanel.getHeight() + 10);
        setTitle("MSTSC远程连接工具 V1");
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setContentPane(mainPanel);
        setVisible(true);

    }

    public void loadDatas() throws SQLException, InstantiationException, IllegalAccessException {
        mainPanel.loadDatas();
    }

}
