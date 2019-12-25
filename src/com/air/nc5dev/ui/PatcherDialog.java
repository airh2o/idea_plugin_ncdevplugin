package com.air.nc5dev.ui;

import com.air.nc5dev.util.ExportPatcherUtil;
import com.air.nc5dev.util.idea.ProjectUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/***
 *   导出NC 补丁包的 弹框UI        </br>
 *           </br>
 *           </br>
 *           </br>
 * @author air Email: 209308343@qq.com
 * @date 2019/12/25 0025 15:29
 * @Param
 * @return
 */
public class PatcherDialog
        extends DialogWrapper {
    private JPanel contentPane;
    private AnActionEvent event;
    private JTextField textField_saveName;
    private JTextField textField_savePath;

    public PatcherDialog(AnActionEvent event) {
        super(event.getProject());
        this.event = event;
        init();
        setTitle("导出NC补丁包...");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        if (contentPane == null) {
            contentPane = new JPanel();
            contentPane.setLayout(null);

            JLabel label = new JLabel("补丁名称:");
            label.setBounds(21, 16, 82, 31);
            contentPane.add(label);

            textField_saveName = new JTextField();
            textField_saveName.setBounds(117, 14, 462, 36);
            contentPane.add(textField_saveName);
            textField_saveName.setColumns(10);

            JLabel label_1 = new JLabel("导出位置:");
            label_1.setBounds(21, 63, 82, 31);
            contentPane.add(label_1);

            textField_savePath = new JTextField();
            textField_savePath.setColumns(10);
            textField_savePath.setBounds(117, 61, 462, 36);
            contentPane.add(textField_savePath);

            JButton button_selectSavePath = new JButton("选择路径");
            button_selectSavePath.setBounds(583, 57, 113, 42);
            button_selectSavePath.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String userDir = System.getProperty("user.home");
                    JFileChooser fileChooser = new JFileChooser(userDir);
                    fileChooser.setFileSelectionMode(1);
                    int flag = fileChooser.showOpenDialog(null);
                    if (flag == 0) {
                        textField_savePath.setText(fileChooser.getSelectedFile().getAbsolutePath());
                    }
                }
            });
            contentPane.add(button_selectSavePath);
        }

        return this.contentPane;
    }
    /***
      *  TODO  点击了确认，开始导出补丁包        </br>
      *     导出功能还没实现      </br>
      *           </br>
      *           </br>
      * @author air Email: 209308343@qq.com
      * @date 2019/12/25 0025 16:17
      * @Param []
      * @return void
     */
    public void onOK() {
        if ((null == this.textField_saveName.getText()) || ("".equals(this.textField_saveName.getText()))) {
            Messages.showErrorDialog(event.getProject(), "请输入补丁包名字!", "Error");
            return;
        }
        if ((null == this.textField_savePath.getText()) || ("".equals(this.textField_savePath.getText()))) {
            Messages.showErrorDialog(event.getProject(), "请选择补丁要导出到的路径!", "Error");
            return;
        }

        File srcFile = new File(ProjectUtil.getDefaultProject().getBasePath()
                + File.separatorChar + "src");
        if (!srcFile.exists() || !srcFile.isDirectory()) {
            Messages.showErrorDialog(event.getProject(), "项目 src 文件夹不存在!", "Error");
            return;
        }
        final VirtualFile srcDir = LocalFileSystem.getInstance().findFileByIoFile(srcFile);
        String exportPath = this.textField_savePath.getText();
        ExportPatcherUtil util = new ExportPatcherUtil(this.textField_saveName.getText(), exportPath, this.event);
        try {
            util.exportPatcher(new VirtualFile[]{srcDir});
            String zipName = util.getZipName();
            if (StringUtils.isBlank(zipName)) {
                zipName = "没有导出任何文件 , 请重新 build 项目 , 或者刷新项目后重试 !";
            } else {
                zipName = "输出文件 : " + zipName;
            }
            Messages.showInfoMessage("导出成功!\n" + zipName, "Tips");
        } catch (Exception e) {
            Messages.showErrorDialog(e.getMessage(), "错误");
        } finally {
            util.delete(new File(util.getExportPath()));
        }
        dispose();
    }

    private void onCancel() {
        dispose();
    }
}
