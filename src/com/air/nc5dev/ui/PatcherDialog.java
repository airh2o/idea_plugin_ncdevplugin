package com.air.nc5dev.ui;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.net.Ipv4Util;
import cn.hutool.core.util.NumberUtil;
import com.air.nc5dev.enums.NcVersionEnum;
import com.air.nc5dev.util.*;
import com.air.nc5dev.util.idea.LogUtil;
import com.air.nc5dev.util.idea.ProjectUtil;
import com.air.nc5dev.util.jdbc.ConnectionUtil;
import com.air.nc5dev.vo.ExportContentVO;
import com.air.nc5dev.vo.NCDataSourceVO;
import com.alibaba.fastjson.JSON;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

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
    //全局标识， 是否有导出任务未处理完
    private static boolean isRuning = false;
    private JBScrollPane contentPane;
    private AnActionEvent event;
    private JTextField textField_saveName;
    private JTextField textField_savePath;
    JBCheckBox filtersql;
    JBCheckBox rebuild;
    JComboBox dataSourceIndex;
    JComboBox ncVersion;

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
            contentPane = new JBScrollPane();
            contentPane.setLayout(null);
            contentPane.setAutoscrolls(true);

            JLabel label = new JBLabel("补丁名称:");
            label.setBounds(21, 16, 82, 31);
            contentPane.add(label);
            textField_saveName = new JBTextField();
            textField_saveName.setBounds(117, 14, 462, 36);
            contentPane.add(textField_saveName);
            textField_saveName.setColumns(10);

            JLabel label_1 = new JBLabel("导出位置:");
            label_1.setBounds(21, 50, 82, 31);
            contentPane.add(label_1);
            textField_savePath = new JBTextField();
            textField_savePath.setColumns(10);
            textField_savePath.setBounds(117, 50, 462, 36);
            contentPane.add(textField_savePath);
            JButton button_selectSavePath = new JButton("选择路径");
            button_selectSavePath.setBounds(583, 50, 113, 36);
            button_selectSavePath.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String userDir = System.getProperty("user.home");
                    JFileChooser fileChooser = new JFileChooser(userDir);
                    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    int flag = fileChooser.showOpenDialog(null);
                    if (flag == JFileChooser.APPROVE_OPTION) {
                        textField_savePath.setText(fileChooser.getSelectedFile().getAbsolutePath());
                    }
                }
            });
            contentPane.add(button_selectSavePath);

            JLabel label_3 = new JBLabel("汇总SQL文件时 过滤重复SQL:");
            label_3.setBounds(21, 90, 200, 31);
            contentPane.add(label_3);
            filtersql = new JBCheckBox();
            filtersql.setSelected("true".equalsIgnoreCase(ProjectNCConfigUtil.getConfigValue("filtersql", "true")));
            filtersql.setBounds(210, 90, 60, 36);
            contentPane.add(filtersql);

            JLabel label_2 = new JBLabel("强制IDEA连接数据库导出SQL:");
            label_2.setBounds(21, 130, 200, 31);
            contentPane.add(label_2);
            rebuild = new JBCheckBox();
            rebuild.setSelected("true".equalsIgnoreCase(ProjectNCConfigUtil.getConfigValue("rebuildsql", "false")));
            rebuild.setBounds(210, 130, 60, 36);
            contentPane.add(rebuild);

            JLabel label_4 = new JBLabel("强制IDEA连接数据库导出SQL使用NC配置的数据源第几个(0开始):");
            label_4.setBounds(21, 165, 400, 31);
            contentPane.add(label_4);
            List<NCDataSourceVO> dataSourceVOS = NCPropXmlUtil.getDataSourceVOS();
            dataSourceIndex = new JComboBox(new Vector(
                    dataSourceVOS.stream()
                            .map(v -> v.getDataSourceName() + '/' + v.getUser())
                            .collect(Collectors.toList())
            ));
            dataSourceIndex.setSelectedIndex(ConvertUtil.toInt(ProjectNCConfigUtil.getConfigValue("data_source_index"), 0));
            dataSourceIndex.setBounds(410, 165, 100, 35);
            contentPane.add(dataSourceIndex);
            JButton button_TestDb = new JButton("测试连接");
            button_TestDb.setBounds(520, 165, 113, 42);
            button_TestDb.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Integer i = dataSourceIndex.getSelectedIndex();
                    NCDataSourceVO ds = NCPropXmlUtil.get(i);
                    if (ds == null) {
                        Messages.showErrorDialog(event.getProject(), "数据源索引不存在: " + i + " ,数据源数量: "
                                        + (NCPropXmlUtil.getDataSourceVOS() == null ? 0 : NCPropXmlUtil.getDataSourceVOS().size())
                                , "错误:");
                        return;
                    }
                    Connection conn = null;
                    try {
                        conn = ConnectionUtil.getConn(ds);
                        Messages.showInfoMessage(event.getProject(), JSON.toJSONString(ds, true), "恭喜!测试成功:");
                    } catch (Throwable ex) {
                        ex.printStackTrace();
                        Messages.showErrorDialog(event.getProject(), "连接失败: " + JSON.toJSONString(ds, true)
                                + " !错误原因:\n" + ExceptionUtil.toStringLines(ex, 5), "错误");
                    } finally {
                        IoUtil.close(conn);
                    }
                }
            });
            contentPane.add(button_TestDb);

            JLabel label_5 = new JBLabel("强制指定导出使用的NC版本:");
            label_5.setBounds(21, 200, 180, 30);
            contentPane.add(label_5);
            ncVersion = new JComboBox<NcVersionEnum>(NcVersionEnum.values());
            ncVersion.setSelectedItem(ProjectNCConfigUtil.getNCVerSIon());
            ncVersion.setBounds(190, 200, 100, 30);
            contentPane.add(ncVersion);

            //设置点默认值
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss");
            this.textField_saveName.setText("modules");
            this.textField_savePath.setText(ProjectUtil.getDefaultProject().getBasePath() + File.separatorChar +
                    "patchers" + File.separatorChar + "exportpatcher-" + LocalDateTime.now().format(formatter));
        }

        contentPane.setPreferredSize(new Dimension(780, 260));
        return this.contentPane;
    }

    /***
     *   点击了确认，开始导出补丁包        </br>
     *           </br>
     *           </br>
     * @author air Email: 209308343@qq.com
     * @date 2019/12/25 0025 16:17
     * @Param []
     * @return void
     */
    public void onOK() {
        if (isRuning) {
            LogUtil.error("上一次导出任务还未处理完，请稍后再试!");
            return;
        }

        if ((null == this.textField_saveName.getText()) || ("".equals(this.textField_saveName.getText()))) {
            LogUtil.error("请输入补丁包名字!");
            return;
        }
        if ((null == this.textField_savePath.getText()) || ("".equals(this.textField_savePath.getText()))) {
            LogUtil.error("请选择补丁要导出到的路径!");
            return;
        }

        String dirName = this.textField_saveName.getText();
        dirName = null == dirName || dirName.trim().isEmpty() ? "export" : dirName;
        String exportPath = this.textField_savePath.getText() + File.separatorChar + dirName;
        Task.Backgroundable backgroundable = new Task.Backgroundable(event.getProject(), "导出中...请等待...") {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                isRuning = true;
                indicator.setText("正在玩命导出NC补丁包中...");
                indicator.setText2("导出成功后会自动打开文件夹： " + exportPath);
                indicator.setIndeterminate(true);

                try {
                    long s = System.currentTimeMillis();
                    ExportContentVO contentVO = new ExportContentVO();
                    contentVO.outPath = exportPath;
                    contentVO.project = event.getProject();
                    contentVO.event = event;
                    contentVO.indicator = indicator;
                    contentVO.filtersql = filtersql.isSelected();
                    contentVO.rebuildsql = rebuild.isSelected();
                    contentVO.data_source_index = dataSourceIndex.getSelectedIndex();
                    contentVO.ncVersion = (NcVersionEnum) ncVersion.getSelectedItem();
                    ExportNCPatcherUtil.export(contentVO);
                    long e = System.currentTimeMillis();
                    LogUtil.info("导出成功,耗时:" + ((e - s) / 1000.0d) + " (秒s)!硬盘路径： " + exportPath);

                    Desktop desktop = Desktop.getDesktop();
                    File dirToOpen = new File(exportPath);
                    desktop.open(dirToOpen);
                } catch (Throwable iae) {
                    LogUtil.error("自动打开路径失败: " + ExceptionUtil.getExcptionDetall(iae));
                } finally {
                    isRuning = false;
                }
            }
        };
        backgroundable.setCancelText("放弃吧,没有卵用的按钮");
        backgroundable.setCancelTooltipText("这是一个没有卵用的按钮");
        ProgressManager.getInstance().run(backgroundable);

        dispose();
    }

    @Nullable
    @Override
    protected String getDimensionServiceKey() {
        return "PatcherDialog";
    }

    private void onCancel() {
        dispose();
    }
}
