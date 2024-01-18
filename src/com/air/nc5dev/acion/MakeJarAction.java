package com.air.nc5dev.acion;

import com.air.nc5dev.acion.base.AbstractIdeaAction;
import com.air.nc5dev.util.ExportNCPatcherUtil;
import com.air.nc5dev.util.IoUtil;
import com.air.nc5dev.util.idea.LogUtil;
import com.air.nc5dev.util.idea.ProjectUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;

import javax.swing.JFileChooser;
import java.io.File;
import java.util.jar.Manifest;

/**
 * 生成jar包         </br>
 * </br>
 * </br>
 * </br>
 *
 * @author air Email: 209308343@qq.com
 * @date 2020/4/1 0001 22:03
 * @Param
 * @return
 */
public class MakeJarAction extends AbstractIdeaAction {

    @Override
    protected void doHandler(AnActionEvent e) {
        //选择路径
        JFileChooser fileChooser = new JFileChooser(ProjectUtil.getDefaultProject().getBasePath()
                + File.separatorChar + "patchers");
        fileChooser.setDialogTitle("选择要转换jar的文件夹位置:");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int flag = fileChooser.showOpenDialog(null);
        if (flag != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File dir = fileChooser.getSelectedFile();

        //输入jar名字
        String jarName = dir.getName() + ".jar";

        //选择jar保存位置
        File outJarFile = new File(dir.getParentFile(), jarName);
        fileChooser = new JFileChooser();
        fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        fileChooser.setCurrentDirectory(dir.getParentFile());
        fileChooser.setSelectedFile(outJarFile);
        fileChooser.setDialogTitle("选择jar保存名字:");
        fileChooser.showDialog(null, null);
        if (flag != JFileChooser.APPROVE_OPTION
                && fileChooser.getSelectedFile() != null) {
            outJarFile = fileChooser.getSelectedFile();
            if (!outJarFile.getName().endsWith(".jar")) {
                outJarFile = new File(outJarFile.getParentFile(), outJarFile.getName() + ".jar");
            }
        }

        //选择jar 的 manifest 文件 位置
        File manifest = null;
        fileChooser = new JFileChooser(dir);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle("选择jar 的 manifest 文件，可以不选择 就是用默认格式:");
        fileChooser.showOpenDialog(null);
        if (flag != JFileChooser.APPROVE_OPTION
                && fileChooser.getSelectedFile() != null) {
            manifest = fileChooser.getSelectedFile();
        }

        Manifest maniFest = ExportNCPatcherUtil.getManiFest(manifest, dir.getName());

        IoUtil.makeJar(dir, outJarFile, maniFest, null);

        LogUtil.info("生成成功： " + outJarFile.getPath());

        try {
            IoUtil.tryOpenFileExpolor(outJarFile);
        } catch (Throwable iae) {
            LogUtil.error("自动打开路径失败: " + outJarFile.getParentFile().getPath(), iae);
        }
    }
}
