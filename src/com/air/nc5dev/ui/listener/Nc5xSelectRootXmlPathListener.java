package com.air.nc5dev.ui.listener;

import com.air.nc5dev.ui.ResetNCUserPassWordPanel;
import com.air.nc5dev.util.ProjectNCConfigUtil;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * <br/>
 * <br/>
 * <br/>
 *
 * @author 唐粟 Email:209308343@qq.com
 * @date 2020/11/23 16:01
 * @project
 * @Version
 */
public class Nc5xSelectRootXmlPathListener implements ActionListener {
    private ResetNCUserPassWordPanel panel;

    public Nc5xSelectRootXmlPathListener(ResetNCUserPassWordPanel panel) {
        this.panel = panel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        File file = null;
        JFileChooser fileChooser = new JFileChooser(ProjectNCConfigUtil.getNCHomePath() + File.separatorChar + "ierp");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().trim().endsWith(".xml");
            }

            @Override
            public String getDescription() {
                return "*.xml";
            }
        });
        fileChooser.setDialogTitle("请选择xml文件:");
        int flag = fileChooser.showOpenDialog(null);
        if (flag == JFileChooser.APPROVE_OPTION
                && fileChooser.getSelectedFile() != null) {
            file = fileChooser.getSelectedFile();
        }

        if (file == null || !file.isFile()) {
            return;
        }

        panel.textField_nc5x_xmlpath.setText(file.getAbsolutePath());

        panel.reloadNc5RootXml();
    }
}
