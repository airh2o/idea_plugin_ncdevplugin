package com.air.nc5dev.acion;

import com.air.nc5dev.acion.base.AbstractIdeaAction;
import com.air.nc5dev.util.IoUtil;
import com.air.nc5dev.util.idea.LogUtil;
import com.air.nc5dev.util.idea.ProjectUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import java.awt.Desktop;
import java.io.File;

/**
 * 把一个NC元数据文件复制成新的文件且重置里面的各种id等        </br>
 * </br>
 * </br>
 * </br>
 *
 * @author air Email: 209308343@qq.com
 * @date 2020/4/1 0001 22:03
 * @Param
 * @return
 */
public class CopyMateFile2NewFileAction extends AbstractIdeaAction {

    @Override
    protected void doHandler(AnActionEvent e) {
        //选择路径
        JFileChooser fileChooser = new JFileChooser(ProjectUtil.getDefaultProject().getBasePath()
                + File.separatorChar + "patchers");
        fileChooser.setDialogTitle("选择要复制的元数据文件:");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".bmf");
            }

            @Override
            public String getDescription() {
                return "只能选择.bmf文件";
            }
        });
        int flag = fileChooser.showOpenDialog(null);
        if (flag != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File old = fileChooser.getSelectedFile();

        //选择 保存位置
        File newFile = new File(old.getParentFile(), old.getName().substring(0
                , old.getName().length() - 4) + "_new_" + System.currentTimeMillis() + ".bmf");
        fileChooser = new JFileChooser();
        fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        fileChooser.setCurrentDirectory(newFile.getParentFile());
        fileChooser.setSelectedFile(newFile);
        fileChooser.setDialogTitle("选择保存位置:");
        fileChooser.showDialog(null, null);
        if (flag != JFileChooser.APPROVE_OPTION
                && fileChooser.getSelectedFile() != null) {
            newFile = fileChooser.getSelectedFile();
            if (!newFile.getName().endsWith(".bmf")) {
                newFile = new File(newFile.getParentFile(), newFile.getName() + ".bmf");
            }
        }

        try {
            IoUtil.copyNCMateFile(old, newFile);

            LogUtil.info("生成成功： " + newFile.getPath());

            Desktop desktop = Desktop.getDesktop();
            IoUtil.tryOpenFileExpolor(newFile);
        } catch (Throwable iae) {
            iae.printStackTrace();
            LogUtil.error(iae.toString() + newFile.getParentFile().getPath(), iae);
        }
    }
}
