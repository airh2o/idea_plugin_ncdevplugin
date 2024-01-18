package com.air.nc5dev.ui;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import com.air.nc5dev.enums.NcVersionEnum;
import com.air.nc5dev.util.CollUtil;
import com.air.nc5dev.util.ConvertUtil;
import com.air.nc5dev.util.ExceptionUtil;
import com.air.nc5dev.util.ExecUtil;
import com.air.nc5dev.util.ExportNCPatcherUtil;
import com.air.nc5dev.util.IoUtil;
import com.air.nc5dev.util.NCPropXmlUtil;
import com.air.nc5dev.util.ProjectNCConfigUtil;
import com.air.nc5dev.util.ReflectUtil;
import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.util.V;
import com.air.nc5dev.util.idea.LogUtil;
import com.air.nc5dev.util.idea.ProjectUtil;
import com.air.nc5dev.util.jdbc.ConnectionUtil;
import com.air.nc5dev.vo.ExportConfigVO;
import com.air.nc5dev.vo.ExportContentVO;
import com.air.nc5dev.vo.NCDataSourceVO;
import com.air.nc5dev.vo.PatcherSelectFileVO;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.table.JBTable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.compress.utils.Sets;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;
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
@Getter
@Setter
public class PatcherDialog extends DialogWrapper {
    //全局标识， 是否有导出任务未处理完
    private static boolean isRuning = false;
    private JComponent contentPane;
    private AnActionEvent event;
    private JTextField textField_saveName;
    private JTextField textField_hotwebsProject;
    private JTextField textField_savePath;
    private JTextField textField_npmRun;
    JBCheckBox filtersql;
    JBCheckBox rebuild;
    JComboBox dataSourceIndex;
    JComboBox ncVersion;
    JBCheckBox reNpmBuild;
    JBCheckBox format4Ygj;
    JBCheckBox exportResources;
    JBCheckBox exportHotwebsClass;
    JBCheckBox exportModules;
    JBCheckBox exportSql;
    JBCheckBox onleyFullSql;
    JBCheckBox deleteDir;
    JBCheckBox reWriteSourceFile;
    JBCheckBox saveConfig;
    JBCheckBox no2Jar;
    JBCheckBox zip;
    JBCheckBox selectExport;
    JBCheckBox selectExport2;

    JBCheckBox exportModuleResources;
    JBCheckBox exportModuleLib;
    JBCheckBox exportModuleMeteinfo;
    JBCheckBox exportModuleMetadata;

    JBTable selectTable;
    DefaultTableModel defaultTableModel;

    JBTable selectModuleTable;
    DefaultTableModel selectModuleTableTableModel;

    JTextField textField_ygj_version;
    JTextField textField_ygj_no;
    JTextField textField_ygj_id;
    JTextField textField_ygj_applyVersion;
    JTextField textField_ygj_department;
    JTextField textField_ygj_provider;
    JBCheckBox checkBox_ygj_deploy;
    JBCheckBox checkBox_ygj_appleyjar;

    public PatcherDialog(AnActionEvent event) {
        super(event.getProject());
        this.event = event;
        init();
        setTitle("导出NC补丁包..."
                       /* + (
                        ExportContentVO.EVENT_POPUP_CLICK.equals(event.getPlace())
                                ? "(项目树点击的导出:只导出选中的模块和文件。如果不想这样 可以点击 Tools菜单选中导出)"
                                : "(Tools菜单点击的导出:导出全部项目。如果不想这样 可以点击 左侧项目树 选中要导出的模块或具体文件)"
                )*/
        );
        setOKButtonText("执行导出");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        if (contentPane == null) {
            contentPaneInit();
        }

        return this.contentPane;
    }

    private void contentPaneInit() {
        JBTabbedPane jtab = new JBTabbedPane();
        contentPane = jtab;
        //  contentPane = new JBScrollPane(jtab);
        //   contentPane.setAutoscrolls(true);
        JBLabel label_2;
        JBPanel panel2;
        JBPanel panel5;
        JLabel label_6;
        {
            int x = 21;
            int y = 16;
            int height = 30;
            int width = 600;

            JBPanel jbxxp = new JBPanel();
            jbxxp.setLayout(null);
            JBScrollPane jbxx = new JBScrollPane(jbxxp);
            jbxx.setAutoscrolls(true);
            jtab.addTab("基本信息", jbxx);

            JLabel label = new JBLabel("补丁名称:");
            label.setBounds(x, y, 82, height);
            jbxxp.add(label);
            textField_saveName = new JBTextField();
            textField_saveName.setBounds(117, 14, 462, height);
            jbxxp.add(textField_saveName);
            textField_saveName.setColumns(10);

            JLabel label_1 = new JBLabel("前端工程路径:");
            label_1.setBounds(x, y = y + height + 5, 82, height);
            jbxxp.add(label_1);
            textField_hotwebsProject = new JBTextField();
            textField_hotwebsProject.setColumns(10);
            textField_hotwebsProject.setBounds(117, y, 500, height);
            jbxxp.add(textField_hotwebsProject);
            JButton button_selectSavePath = new JButton("选择路径");
            button_selectSavePath.setBounds(620, y, 113, height);
            button_selectSavePath.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    File d = new File(event.getProject().getBasePath() + File.separatorChar + "hotwebs");
                    JFileChooser fileChooser = new JFileChooser(d.exists() ? d.getPath() : d.getParent());
                    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    int flag = fileChooser.showOpenDialog(null);
                    if (flag == JFileChooser.APPROVE_OPTION) {
                        textField_hotwebsProject.setText(fileChooser.getSelectedFile().getAbsolutePath());
                    }
                }
            });
            jbxxp.add(button_selectSavePath);

            label_1 = new JBLabel("导出位置:");
            label_1.setBounds(x, y = y + height + 5, 82, height);
            jbxxp.add(label_1);
            textField_savePath = new JBTextField();
            textField_savePath.setColumns(10);
            textField_savePath.setBounds(117, y = y, 500, height);
            jbxxp.add(textField_savePath);
            button_selectSavePath = new JButton("选择路径");
            button_selectSavePath.setBounds(620, y, 113, height);
            button_selectSavePath.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    // String userDir = System.getProperty("user.home");
                    File d = new File(event.getProject().getBasePath() + File.separatorChar + "patchers");
                    JFileChooser fileChooser = new JFileChooser(d.exists() ? d.getPath() : d.getParent());
                    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    int flag = fileChooser.showOpenDialog(null);
                    if (flag == JFileChooser.APPROVE_OPTION) {
                        textField_savePath.setText(fileChooser.getSelectedFile().getAbsolutePath());
                    }
                }
            });
            jbxxp.add(button_selectSavePath);

            label_2 = new JBLabel("是否导出SQL:");
            exportSql = new JBCheckBox();
            exportSql.setSelected("true".equalsIgnoreCase(ProjectNCConfigUtil.getConfigValue("exportSql", "true")));
            panel2 = new JBPanel();
            //panel2.setBorder(LineBorder.createGrayLineBorder());
            panel2.setLayout(new BoxLayout(panel2, BoxLayout.X_AXIS));
            panel2.setBounds(x, y = y + height + 5, width, height);
            panel2.add(label_2);
            panel2.add(exportSql);
            jbxxp.add(panel2);

            JLabel label_5 = new JBLabel("强制指定导出使用的NC版本:");
            ncVersion = new JComboBox<NcVersionEnum>(NcVersionEnum.values());
            ncVersion.setSelectedItem(ProjectNCConfigUtil.getNCVersion());
            JBPanel panel4 = new JBPanel();
            //  panel4.setBorder(LineBorder.createGrayLineBorder());
            panel4.setLayout(new BoxLayout(panel4, BoxLayout.X_AXIS));
            panel4.setBounds(x, y = y + height + 5, width, height);
            panel4.add(label_5);
            panel4.add(ncVersion);
            jbxxp.add(panel4);

            label_6 = new JBLabel("是否导出前端资源(resources):");
            exportResources = new JBCheckBox();
            exportResources.setSelected("true".equalsIgnoreCase(ProjectNCConfigUtil.getConfigValue("exportResources",
                    "true")));
            panel5 = new JBPanel();
            //  panel5.setBorder(LineBorder.createGrayLineBorder());
            panel5.setLayout(new BoxLayout(panel5, BoxLayout.X_AXIS));
            panel5.setBounds(x, y = y + height + 5, width, height);
            panel5.add(label_6);
            panel5.add(exportResources);
            jbxxp.add(panel5);

            label_6 = new JBLabel("是否导出client:");
            exportHotwebsClass = new JBCheckBox();
            exportHotwebsClass.setSelected(true);
            panel5 = new JBPanel();
            //  panel5.setBorder(LineBorder.createGrayLineBorder());
            panel5.setLayout(new BoxLayout(panel5, BoxLayout.X_AXIS));
            panel5.setBounds(x, y = y + height + 5, width, height);
            panel5.add(label_6);
            panel5.add(exportHotwebsClass);
            jbxxp.add(panel5);

            label_6 = new JBLabel("是否导出public和private:");
            exportModules = new JBCheckBox();
            exportModules.setSelected("true".equalsIgnoreCase(ProjectNCConfigUtil.getConfigValue("exportModules",
                    "true")));
            panel5 = new JBPanel();
            //  panel5.setBorder(LineBorder.createGrayLineBorder());
            panel5.setLayout(new BoxLayout(panel5, BoxLayout.X_AXIS));
            panel5.setBounds(x, y = y + height + 5, width, height);
            panel5.add(label_6);
            panel5.add(exportModules);
            jbxxp.add(panel5);

            JLabel label_10 = new JBLabel("混淆覆写导出的源码文件内容(java文件和js源码:__SOURCE__CODE__):");
            reWriteSourceFile = new JBCheckBox();
            reWriteSourceFile.setSelected("true".equalsIgnoreCase(ProjectNCConfigUtil.getConfigValue(
                    "reWriteSourceFile", "false")));
            JBPanel panel7 = new JBPanel();
            //  panel7.setBorder(LineBorder.createGrayLineBorder());
            panel7.setLayout(new BoxLayout(panel7, BoxLayout.X_AXIS));
            panel7.setBounds(x, y = y + height + 5, width, height);
            panel7.add(label_10);
            panel7.add(reWriteSourceFile);
            jbxxp.add(panel7);

            label_10 = new JBLabel("是否保留本次导出配置:");
            saveConfig = new JBCheckBox();
            panel7 = new JBPanel();
            //  panel7.setBorder(LineBorder.createGrayLineBorder());
            panel7.setLayout(new BoxLayout(panel7, BoxLayout.X_AXIS));
            panel7.setBounds(x, y = y + height + 5, width, height);
            panel7.add(label_10);
            panel7.add(saveConfig);
            jbxxp.add(panel7);

            label_10 = new JBLabel("是否强制不压缩类文件成为jar文件:");
            no2Jar = new JBCheckBox();
            panel7 = new JBPanel();
            //  panel7.setBorder(LineBorder.createGrayLineBorder());
            panel7.setLayout(new BoxLayout(panel7, BoxLayout.X_AXIS));
            panel7.setBounds(x, y = y + height + 5, width, height);
            panel7.add(label_10);
            panel7.add(no2Jar);
            jbxxp.add(panel7);

            label_10 = new JBLabel("是否导出模块下的META-INF文件夹:");
            exportModuleMeteinfo = new JBCheckBox();
            panel7 = new JBPanel();
            //  panel7.setBorder(LineBorder.createGrayLineBorder());
            panel7.setLayout(new BoxLayout(panel7, BoxLayout.X_AXIS));
            panel7.setBounds(x, y = y + height + 5, width, height);
            panel7.add(label_10);
            panel7.add(exportModuleMeteinfo);
            jbxxp.add(panel7);

            label_10 = new JBLabel("是否导出模块下的METADATA文件夹:");
            exportModuleMetadata = new JBCheckBox();
            panel7 = new JBPanel();
            //  panel7.setBorder(LineBorder.createGrayLineBorder());
            panel7.setLayout(new BoxLayout(panel7, BoxLayout.X_AXIS));
            panel7.setBounds(x, y = y + height + 5, width, height);
            panel7.add(label_10);
            panel7.add(exportModuleMetadata);
            jbxxp.add(panel7);

            label_10 = new JBLabel("是否导出模块下的resources文件夹:");
            exportModuleResources = new JBCheckBox();
            panel7 = new JBPanel();
            //  panel7.setBorder(LineBorder.createGrayLineBorder());
            panel7.setLayout(new BoxLayout(panel7, BoxLayout.X_AXIS));
            panel7.setBounds(x, y = y + height + 5, width, height);
            panel7.add(label_10);
            panel7.add(exportModuleResources);
            jbxxp.add(panel7);

            label_10 = new JBLabel("是否导出模块下的lib文件夹:");
            exportModuleLib = new JBCheckBox();
            panel7 = new JBPanel();
            //  panel7.setBorder(LineBorder.createGrayLineBorder());
            panel7.setLayout(new BoxLayout(panel7, BoxLayout.X_AXIS));
            panel7.setBounds(x, y = y + height + 5, width, height);
            panel7.add(label_10);
            panel7.add(exportModuleLib);
            jbxxp.add(panel7);
        }

        {
            int x = 21;
            int y = 16;
            int height = 30;
            int width = 600;
            JBPanel qdp = new JBPanel();
            qdp.setLayout(null);
            JBScrollPane qd = new JBScrollPane(qdp);
            qd.setAutoscrolls(true);
            jtab.addTab("前端和SQL脚本", qd);

            JLabel label_3 = new JBLabel("汇总SQL文件时 过滤重复SQL:");
            qdp.add(label_3);
            filtersql = new JBCheckBox();
            filtersql.setSelected("true".equalsIgnoreCase(ProjectNCConfigUtil.getConfigValue("filtersql", "true")));
            JBPanel panel1 = new JBPanel();
            //  panel1.setBorder(LineBorder.createGrayLineBorder());
            panel1.setLayout(new BoxLayout(panel1, BoxLayout.X_AXIS));
            panel1.setBounds(x, y, width, height);
            panel1.add(label_3);
            panel1.add(filtersql);
            qdp.add(panel1);

            label_2 = new JBLabel("是否只保留全量sql单个文件:");
            onleyFullSql = new JBCheckBox();
            onleyFullSql.setSelected("true".equalsIgnoreCase(ProjectNCConfigUtil.getConfigValue("onleyFullSql", "true"
            )));
            panel2 = new JBPanel();
            //panel2.setBorder(LineBorder.createGrayLineBorder());
            panel2.setLayout(new BoxLayout(panel2, BoxLayout.X_AXIS));
            panel2.setBounds(x, y = y + height + 5, width, height);
            panel2.add(label_2);
            panel2.add(onleyFullSql);
            qdp.add(panel2);

            label_2 = new JBLabel("强制IDEA连接数据库导出SQL:");
            rebuild = new JBCheckBox();
            rebuild.setSelected("true".equalsIgnoreCase(ProjectNCConfigUtil.getConfigValue("rebuildsql", "false")));
            panel2 = new JBPanel();
            //panel2.setBorder(LineBorder.createGrayLineBorder());
            panel2.setLayout(new BoxLayout(panel2, BoxLayout.X_AXIS));
            panel2.setBounds(x, y = y + height + 5, width, height);
            panel2.add(label_2);
            panel2.add(rebuild);
            qdp.add(panel2);

            JLabel label_4 = new JBLabel("强制IDEA连接数据库导出SQL使用NC配置的数据源第几个(0开始):");
            try {
                List<NCDataSourceVO> dataSourceVOS = NCPropXmlUtil.getDataSourceVOS(event.getProject());
                dataSourceIndex = new JComboBox(new Vector(
                        dataSourceVOS.stream()
                                .map(v -> v.getDataSourceName() + '/' + v.getUser())
                                .collect(Collectors.toList())
                ));
                dataSourceIndex.setSelectedIndex(ConvertUtil.toInt(ProjectNCConfigUtil.getConfigValue(
                        "data_source_index"), 0));
            } catch (Exception e) {
                e.printStackTrace();
                LogUtil.error(e.getMessage(), e);
            }
            qdp.add(dataSourceIndex);
            JButton button_TestDb = new JButton("测试连接");
            button_TestDb.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Integer i = dataSourceIndex.getSelectedIndex();
                    NCDataSourceVO ds = NCPropXmlUtil.get(i);
                    if (ds == null) {
                        Messages.showErrorDialog(event.getProject(), "数据源索引不存在: " + i + " ,数据源数量: "
                                        + (NCPropXmlUtil.getDataSourceVOS(event.getProject()) == null ? 0 :
                                        NCPropXmlUtil.getDataSourceVOS(event.getProject()).size())
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
            JBPanel panel3 = new JBPanel();
            //  panel3.setBorder(LineBorder.createGrayLineBorder());
            panel3.setLayout(new BoxLayout(panel3, BoxLayout.X_AXIS));
            panel3.setBounds(x, y = y + height + 5, width, height);
            panel3.add(label_4);
            panel3.add(dataSourceIndex);
            panel3.add(button_TestDb);
            qdp.add(panel3);

            label_6 = new JBLabel("自动删除hotwebs的dist后执行:");
            reNpmBuild = new JBCheckBox();
            reNpmBuild.setSelected("true".equalsIgnoreCase(ProjectNCConfigUtil.getConfigValue("reNpmBuild", "true")));
            textField_npmRun = new JTextField("npm run build");
            panel5 = new JBPanel();
            //  panel5.setBorder(LineBorder.createGrayLineBorder());
            panel5.setLayout(new BoxLayout(panel5, BoxLayout.X_AXIS));
            panel5.setBounds(x, y = y + height + 5, width, height);
            panel5.add(label_6);
            panel5.add(reNpmBuild);
            panel5.add(textField_npmRun);
            qdp.add(panel5);
        }

        {
            int x = 21;
            int y = 16;
            int height = 30;
            int width = 600;
            JBPanel jgp = new JBPanel();
            jgp.setLayout(null);
            JBScrollPane jg = new JBScrollPane(jgp);
            jg.setAutoscrolls(true);
            jtab.addTab("补丁结构", jg);

            JLabel label_7 = new JBLabel("导出云管家格式:");
            format4Ygj = new JBCheckBox();
            format4Ygj.setSelected("true".equalsIgnoreCase(ProjectNCConfigUtil.getConfigValue("format4Ygj", "true")));
            JBPanel panel6 = new JBPanel();
            //  panel6.setBorder(LineBorder.createGrayLineBorder());
            panel6.setLayout(new BoxLayout(panel6, BoxLayout.X_AXIS));
            panel6.setBounds(x, y, width, height);
            panel6.add(label_7);
            panel6.add(format4Ygj);
            jgp.add(panel6);

            JLabel label_13 = new JBLabel("生成zip压缩文件:");
            zip = new JBCheckBox();
            zip.setSelected("true".equalsIgnoreCase(ProjectNCConfigUtil.getConfigValue("zip", "true")));
            JBPanel panel13 = new JBPanel();
            //  panel6.setBorder(LineBorder.createGrayLineBorder());
            panel13.setLayout(new BoxLayout(panel13, BoxLayout.X_AXIS));
            panel13.setBounds(x, y = y + height + 5, width, height);
            panel13.add(label_13);
            panel13.add(zip);
            jgp.add(panel13);

            JLabel label_12 = new JBLabel("是否只保留zip文件(删除补丁文件夹):");
            deleteDir = new JBCheckBox();
            deleteDir.setSelected("true".equalsIgnoreCase(ProjectNCConfigUtil.getConfigValue("deleteDir", "true")));
            deleteDir.addActionListener(ev -> {
                if (deleteDir.isSelected()) {
                    zip.setSelected(true);
                }
            });
            JBPanel panel12 = new JBPanel();
            //  panel6.setBorder(LineBorder.createGrayLineBorder());
            panel12.setLayout(new BoxLayout(panel12, BoxLayout.X_AXIS));
            panel12.setBounds(x, y = y + height + 5, width, height);
            panel12.add(label_12);
            panel12.add(deleteDir);
            jgp.add(panel12);

            JLabel label_8 = new JBLabel("只导出选中的模块或文件:");
            selectExport = new JBCheckBox();
            this.selectExport.setSelected(ExportContentVO.EVENT_POPUP_CLICK.equals(event.getPlace()));
            this.selectExport.addItemListener((e) -> updateSelectFileTable());
            JBPanel panel10 = new JBPanel();
            panel10.setLayout(new BoxLayout(panel10, BoxLayout.X_AXIS));
            panel10.setBounds(x, y = y + height + 5, 220, height);
            panel10.add(label_8);
            panel10.add(selectExport);
            jgp.add(panel10);

            label_8 = new JBLabel("只导出右键选择的文件:");
            selectExport2 = new JBCheckBox();
            selectExport2.setSelected(ExportContentVO.EVENT_POPUP_CLICK.equals(event.getPlace()));
            selectExport2.addItemListener((e) -> updateSelectFileTable2());
            panel10 = new JBPanel();
            panel10.setLayout(new BoxLayout(panel10, BoxLayout.X_AXIS));
            panel10.setBounds(x + 230, y, 220, height);
            panel10.add(label_8);
            panel10.add(selectExport2);
            jgp.add(panel10);

            JLabel label_9 = new JBLabel("当前选中的要导出的内容:");
            label_9.setBounds(x, y = y + height + 5, 280, height);
            jgp.add(label_9);
            Vector heads = new Vector();
            heads.add("选择");
            heads.add("路径");
            heads.add("备注");
            defaultTableModel = new DefaultTableModel(heads, 0);
            updateSelectFileTable();
            selectTable = new JBTable(defaultTableModel);
            selectTable.setBorder(LineBorder.createBlackLineBorder());
            selectTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            selectTable.addMouseListener(new MySelectFileTableMouseAdpaterImpl(this, selectTable));
            //  JScrollPane jScrollPane = new JBScrollPane(selectTable);
            // jScrollPane.setAutoscrolls(true);
            JBScrollPane selectTableScrollPane = new JBScrollPane(selectTable);
            selectTableScrollPane.setBounds(x, y = y + height + 5, 770, 200);
            // 对每一列设置单元格渲染器
            MyTableRenderer cellRenderer = new MyTableRenderer();
            cellRenderer.getSelectColorIndex().put(0, new Color(170, 200, 140));
            cellRenderer.getUnSelectColorIndex().put(0, new Color(150, 50, 50));
            cellRenderer.getColumn2ToolTipTextMap().put(0, "只导出这个表格中的文件，且必须这行勾选了的");
            for (int i = 0; i < selectTable.getColumnCount(); i++) {
                selectTable.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
            }
            selectTable.getColumnModel().getColumn(0).setCellEditor(new MyTableCellEditor(this, new JBCheckBox()));
            jgp.add(selectTableScrollPane);

            label_9 = new JBLabel("要导出的模块:");
            label_9.setBounds(x, y = y + selectTableScrollPane.getHeight() + 5, 280, height);
            jgp.add(label_9);
            heads = new Vector();
            heads.add("导出");
            heads.add("模块名");
            heads.add("指定导出的模块名");
            heads.add("生成jar文件");
            heads.add("路径");
            selectModuleTableTableModel = new DefaultTableModel(heads, 0);
            selectModuleTable = new JBTable(selectModuleTableTableModel);
            selectModuleTable.setBorder(LineBorder.createBlackLineBorder());
            selectModuleTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            selectModuleTable.addMouseListener(new MySelectFileTableMouseAdpaterImpl(this, selectModuleTable));
            selectTableScrollPane = new JBScrollPane(selectModuleTable);
            selectTableScrollPane.setBounds(x, y = y + label_9.getHeight() + 5, 770, 200);
            // 对每一列设置单元格渲染器
            cellRenderer = new MyTableRenderer();
            cellRenderer.getSelectColorIndex().put(0, new Color(170, 200, 140));
            cellRenderer.getUnSelectColorIndex().put(0, new Color(150, 50, 50));
            cellRenderer.getColumn2ToolTipTextMap().put(0, "勾选:导出模块|不勾选:不导出这个模块啦");

            cellRenderer.getJCheckBoxIndexs().add(3);
            cellRenderer.getSelectColorIndex().put(3, new Color(170, 200, 140));
            cellRenderer.getUnSelectColorIndex().put(3, new Color(150, 50, 50));
            cellRenderer.getColumn2ToolTipTextMap().put(3, "勾选:class文件打包成jar|不勾选:不打包啦");
            for (int i = 0; i < selectModuleTable.getColumnCount(); i++) {
                selectModuleTable.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
            }
            selectModuleTable.getColumnModel().getColumn(0).setCellEditor(new MyTableCellEditor(this,
                    new JBCheckBox()));
            selectModuleTable.getColumnModel().getColumn(3).setCellEditor(new MyTableCellEditor(this,
                    new JBCheckBox()));
            jgp.add(selectTableScrollPane);
        }

        {
            int x = 21;
            int y = 16;
            int height = 30;
            int width = 600;

            JBPanel tab = new JBPanel();
            tab.setLayout(null);
            JBScrollPane jbxx = new JBScrollPane(tab);
            jbxx.setAutoscrolls(true);
            jtab.addTab("云管家补丁信息", jbxx);

            JLabel label = new JBLabel("补丁版本:");
            label.setBounds(x, y, 82, height);
            tab.add(label);
            textField_ygj_version = new JBTextField();
            textField_ygj_version.setBounds(label.getX() + label.getWidth() + 2, 14, 462, height);
            tab.add(textField_ygj_version);
            textField_ygj_version.setColumns(10);

            label = new JBLabel("补丁编号:");
            label.setBounds(x, y = y + height + 5, 82, height);
            tab.add(label);
            textField_ygj_no = new JBTextField();
            textField_ygj_no.setColumns(10);
            textField_ygj_no.setBounds(label.getX() + label.getWidth() + 2, y, 500, height);
            tab.add(textField_ygj_no);

            label = new JBLabel("补丁ID:");
            label.setBounds(x, y = y + height + 5, 82, height);
            tab.add(label);
            textField_ygj_id = new JBTextField();
            textField_ygj_id.setColumns(10);
            textField_ygj_id.setBounds(label.getX() + label.getWidth() + 2, y, 500, height);
            tab.add(textField_ygj_id);

            label = new JBLabel("补丁适用版本:");
            label.setBounds(x, y = y + height + 5, 82, height);
            tab.add(label);
            textField_ygj_applyVersion = new JBTextField();
            textField_ygj_applyVersion.setColumns(10);
            textField_ygj_applyVersion.setBounds(label.getX() + label.getWidth() + 2, y, 500, height);
            tab.add(textField_ygj_applyVersion);

            label = new JBLabel("部门:");
            label.setBounds(x, y = y + height + 5, 82, height);
            tab.add(label);
            textField_ygj_department = new JBTextField();
            textField_ygj_department.setColumns(10);
            textField_ygj_department.setBounds(label.getX() + label.getWidth() + 2, y, 500, height);
            tab.add(textField_ygj_department);

            label = new JBLabel("提供人:");
            label.setBounds(x, y = y + height + 5, 82, height);
            tab.add(label);
            textField_ygj_provider = new JBTextField();
            textField_ygj_provider.setColumns(10);
            textField_ygj_provider.setBounds(label.getX() + label.getWidth() + 2, y, 500, height);
            tab.add(textField_ygj_provider);

            label = new JBLabel("是否需要部署:");
            label.setBounds(x, y = y + height + 5, 80, height);
            tab.add(label);
            checkBox_ygj_deploy = new JBCheckBox();
            checkBox_ygj_deploy.setBounds(label.getX() + label.getWidth() + 2, y, 50, height);
            tab.add(checkBox_ygj_deploy);

            label = new JBLabel("是否需要重新生成客户端Applet Jar包:");
            label.setBounds(x, y = y + height + 5, 220, height);
            tab.add(label);
            checkBox_ygj_appleyjar = new JBCheckBox();
            checkBox_ygj_appleyjar.setBounds(label.getX() + label.getWidth() + 2, y, 50, height);
            tab.add(checkBox_ygj_appleyjar);
        }


        //设置点默认值
        initDefualtValues();

        // jp.setPreferredSize(new Dimension(width + 40, y + 10));
    }

    public void initDefualtValues() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss");
        this.textField_saveName.setText("patcher-" + event.getProject().getName() + "-" + LocalDateTime.now().format(formatter));
        this.textField_savePath.setText(ProjectUtil.getDefaultProject().getBasePath()
                + File.separatorChar + "patchers"
                + File.separatorChar + "patcher-" + event.getProject().getName() + "-" + LocalDateTime.now().format(formatter)
        );

        File hotwebs = new File(event.getProject().getBasePath(), "hotwebs");
        if (hotwebs.isDirectory()) {
            textField_hotwebsProject.setText(hotwebs.getPath());
        }

        ExportContentVO c = ExportNCPatcherUtil.readConfig(event.getProject());
        if (c == null) {
            return;
        }

        Module[] modules = ModuleManager.getInstance(event.getProject()).getModules();
        while (selectModuleTableTableModel.getRowCount() > 0) {
            selectModuleTableTableModel.removeRow(0);
        }
        for (Module module : modules) {
            c.getModuleDirPath2ModuleMap().put(module.getName(), module);
            Vector v = new Vector();
            ExportConfigVO exportConfigVO =
                    ExportNCPatcherUtil.loadExportConfig(module.getModuleFile().getParent().getPath(), module);
            if (exportConfigVO != null) {
                v.add(!exportConfigVO.ignoreModule);
            } else {
                v.add(true);
            }

            v.add(module.getName());

            if (!c.getModuleName2ExportModuleNameMap().containsKey(module.getName())) {
                c.getModuleName2ExportModuleNameMap().put(module.getName(), module.getName());
            }
            v.add(c.getModuleName2ExportModuleNameMap().get(module.getName()));

            if (exportConfigVO != null) {
                v.add(exportConfigVO.toJar);
            } else {
                v.add(false);
            }

            v.add(module.getModuleFile().getParent().getPath());

            selectModuleTableTableModel.addRow(v);
        }
        fitTableColumns(selectModuleTable);

        set2UI(c);

        textField_ygj_version.setText("1");
        textField_ygj_id.setText(UUID.randomUUID().toString());
        textField_ygj_no.setText(UUID.randomUUID().toString());
        textField_ygj_applyVersion.setText("5.0,5.01,5.011,5.02,5.3,5.5,5.6,5.7,5.75,6.0,6.1,6.3");
        textField_ygj_department.setText("air QQ 209308343, 微信: yongyourj");
        textField_ygj_provider.setText("QQ 209308343, 微信: yongyourj");
        checkBox_ygj_deploy.setSelected(true);
    }

    public void set2UI(ExportContentVO c) {
        textField_saveName.setText(c.name);
        if (StringUtil.isNotBlank(c.outPath)) {
            textField_savePath.setText(new File(c.outPath).getParent());
        }
        if (StringUtil.isNotBlank(c.hotwebsResourcePath)) {
            textField_hotwebsProject.setText(c.hotwebsResourcePath);
        }
        filtersql.setSelected(c.filtersql);
        rebuild.setSelected(c.rebuildsql);
        reWriteSourceFile.setSelected(c.reWriteSourceFile);
        reNpmBuild.setSelected(c.reNpmBuild);
        deleteDir.setSelected(c.deleteDir);
        format4Ygj.setSelected(c.format4Ygj);
        exportResources.setSelected(c.exportResources);
        exportHotwebsClass.setSelected(c.exportHotwebsClass);
        exportModules.setSelected(c.exportModules);
        exportSql.setSelected(c.exportSql);
        onleyFullSql.setSelected(c.onleyFullSql);
        zip.setSelected(c.zip);
        saveConfig.setSelected(c.saveConfig);
        no2Jar.setSelected(c.no2Jar);
        dataSourceIndex.setSelectedIndex(c.data_source_index);
        exportModuleResources.setSelected(c.exportModuleResources);
        exportModuleLib.setSelected(c.exportModuleLib);
        exportModuleMeteinfo.setSelected(c.exportModuleMeteinfo);
        exportModuleMetadata.setSelected(c.exportModuleMetadata);
        if (c.ncVersion != null) {
            NcVersionEnum[] vs = NcVersionEnum.values();
            for (int i = 0; i < vs.length; i++) {
                if (vs[i].equals(c.getNcVersion())) {
                    ncVersion.setSelectedIndex(i);
                    break;
                }
            }
        }

        if (StrUtil.isNotBlank(c.getV())) {
            textField_ygj_version.setText((V.getInt(c.getV()) + 1) + "");
        }
        if (StrUtil.isNotBlank(c.getNo())) {
            textField_ygj_no.setText(c.getNo());
        }
        if (StrUtil.isNotBlank(c.getNo())) {
            textField_ygj_id.setText(c.getNo());
        }
        if (StrUtil.isNotBlank(c.getApplyVersion())) {
            textField_ygj_applyVersion.setText(c.getApplyVersion());
        }
        if (StrUtil.isNotBlank(c.getDepartment())) {
            textField_ygj_department.setText(c.getDepartment());
        }
        if (StrUtil.isNotBlank(c.getProvider())) {
            textField_ygj_provider.setText(c.getProvider());
        }
        checkBox_ygj_deploy.setSelected(c.isDeploy());
        checkBox_ygj_appleyjar.setSelected(c.isAppleyjar());

    }

    public ExportContentVO getFromUI() {
        /* if ((null == this.textField_saveName.getText()) || ("".equals(this.textField_saveName.getText()))) {
            LogUtil.error("请输入补丁包名字!");
            return;
        }*/
        if ((null == this.textField_savePath.getText()) || ("".equals(this.textField_savePath.getText()))) {
            LogUtil.error("请选择补丁要导出到的路径!");
            return null;
        }

        String dirName = "modules"; //this.textField_saveName.getText();
        dirName = null == dirName || dirName.trim().isEmpty() ? "export" : dirName;
        String exportPath = this.textField_savePath.getText() + File.separatorChar + dirName;

        ExportContentVO contentVO = new ExportContentVO();
        contentVO.name = textField_saveName.getText();
        contentVO.hotwebsResourcePath = textField_hotwebsProject.getText();
        contentVO.outPath = exportPath;
        contentVO.project = event.getProject();
        contentVO.event = event;
        contentVO.filtersql = filtersql.isSelected();
        contentVO.rebuildsql = rebuild.isSelected();
        contentVO.reWriteSourceFile = reWriteSourceFile.isSelected();
        contentVO.reNpmBuild = reNpmBuild.isSelected();
        contentVO.deleteDir = deleteDir.isSelected();
        contentVO.format4Ygj = format4Ygj.isSelected();
        contentVO.exportResources = exportResources.isSelected();
        contentVO.exportHotwebsClass = exportHotwebsClass.isSelected();
        contentVO.exportModules = exportModules.isSelected();
        contentVO.exportSql = exportSql.isSelected();
        contentVO.onleyFullSql = onleyFullSql.isSelected();
        contentVO.zip = zip.isSelected();
        contentVO.no2Jar = no2Jar.isSelected();
        contentVO.saveConfig = saveConfig.isSelected();
        contentVO.data_source_index = dataSourceIndex.getSelectedIndex();
        contentVO.ncVersion = (NcVersionEnum) ncVersion.getSelectedItem();
        contentVO.setNpmRunCommand(textField_npmRun.getText());

        //要导出的文件们
        contentVO.init();
        contentVO.selectExport = selectExport.isSelected() || selectExport2.isSelected();
        if (contentVO.selectExport) {
            Vector<Vector> rows = defaultTableModel.getDataVector();
            contentVO.setSelectFiles(Lists.newArrayList());
            for (Vector row : rows) {
                if (("true".equals(row.get(0))
                        || Boolean.TRUE.equals(row.get(0)))
                        && StringUtil.isNotBlank((String) row.get(1))
                ) {
                    String path = StringUtil.replaceChars((String) row.get(1), "/", File.separator);
                    path = StringUtil.replaceChars(path, "\\", File.separator);
                    contentVO.getSelectFiles().add(path);
                }
            }
            contentVO.initSelectModules();
        }

        //要导出的模块
        contentVO.initSelectModules();
        Vector<Vector> rows = selectModuleTableTableModel.getDataVector();
        HashSet<String> skipModuleNames = Sets.newHashSet();
        for (Vector row : rows) {
            if (!"true".equals(row.get(0))
                    && !Boolean.TRUE.equals(row.get(0))) {
                skipModuleNames.add((String) row.get(1));
            }

            ExportConfigVO exportConfigVO =
                    contentVO.getModule2ExportConfigVoMap().get(contentVO.getSelectExportModules().stream()
                            .filter(m -> m.getName().equals(row.get(1)))
                            .findAny()
                            .orElse(null));
            if (exportConfigVO != null) {
                exportConfigVO.setToJar("true".equals(row.get(3)) || Boolean.TRUE.equals(row.get(3)));
            }

            contentVO.getModuleName2ExportModuleNameMap().put((String) row.get(1), (String) row.get(2));
        }
        contentVO.setSelectExportModules(contentVO.getSelectExportModules().stream()
                .filter(m -> !skipModuleNames.contains(m.getName()))
                .collect(Collectors.toList()));

        contentVO.setV(textField_ygj_version.getText());
        contentVO.setNo(textField_ygj_no.getText());
        contentVO.setApplyVersion(textField_ygj_applyVersion.getText());
        contentVO.setDepartment(textField_ygj_department.getText());
        contentVO.setProvider(textField_ygj_provider.getText());
        contentVO.setDeploy(checkBox_ygj_deploy.isSelected());
        contentVO.setAppleyjar(checkBox_ygj_appleyjar.isSelected());

        return contentVO;
    }

    @Data
    @AllArgsConstructor
    public static class MySelectFileTableMouseAdpaterImpl extends MouseAdapter {
        PatcherDialog patcherDialog;
        JTable table;

        @Override
        public void mousePressed(MouseEvent me) {
            if (SwingUtilities.isRightMouseButton(me)) {
                final int row = table.rowAtPoint(me.getPoint());
                if (row < 0) {
                    return;
                }

                final int column = table.columnAtPoint(me.getPoint());
                boolean isCheckbox = false;
                Component tableCellRendererComponent =
                        table.getColumnModel().getColumn(column).getCellRenderer().getTableCellRendererComponent(table
                                , table.getValueAt(row, column)
                                , true
                                , true
                                , row
                                , column);
                if (tableCellRendererComponent != null) {
                    if (JCheckBox.class.isAssignableFrom(tableCellRendererComponent.getClass())
                            || Checkbox.class.isAssignableFrom(tableCellRendererComponent.getClass())) {
                        isCheckbox = true;
                    }
                }

                final JPopupMenu popup = new JPopupMenu();
                JMenuItem item = new JMenuItem("查找");
                popup.add(item);
                item.addActionListener(event -> {
                    new SearchTableFieldDialog(getPatcherDialog().getFromUI().getProject(), table).show();
                });

                JMenuItem selectAllOrNot = new JMenuItem("反选");
                final boolean isCheckboxFinal = isCheckbox;
                selectAllOrNot.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        //高亮选择指定的行
                        if (table == patcherDialog.getSelectTable()) {
                            List<SelectDTO> ds = patcherDialog.getSelectDtos();
                            if (ds != null) {
                                for (SelectDTO d : ds) {
                                    d.setSelect(!d.isSelect());
                                }
                                patcherDialog.setSelectTableDatas(ds);
                            }
                        } else {
                            DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
                            for (int i = 0; i < tableModel.getRowCount(); i++) {
                                if ("true".equals(tableModel.getDataVector().get(i).get(column))
                                        || Boolean.TRUE.equals(tableModel.getDataVector().get(i).get(column))) {
                                    table.setValueAt(false, i, isCheckboxFinal ? column : 0);
                                } else {
                                    table.setValueAt(true, i, isCheckboxFinal ? column : 0);
                                }
                            }

                            patcherDialog.fitTableColumns(table);
                        }
                        // table.setRowSelectionInterval(row, row);
                    }
                });
                popup.add(selectAllOrNot);

                selectAllOrNot = new JMenuItem("全选/全消");
                selectAllOrNot.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        //高亮选择指定的行
                        if (table == patcherDialog.getSelectTable()) {
                            List<SelectDTO> ds = patcherDialog.getSelectDtos();
                            if (ds != null) {
                                boolean is = ds.get(0).isSelect();
                                for (SelectDTO d : ds) {
                                    d.setSelect(!is);
                                }
                                patcherDialog.setSelectTableDatas(ds);
                            }
                        } else {
                            DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
                            boolean is = "true".equals(tableModel.getDataVector().get(0).get(column))
                                    || Boolean.TRUE.equals(tableModel.getDataVector().get(0).get(column));
                            for (int i = 0; i < tableModel.getRowCount(); i++) {
                                table.setValueAt(!is, i, isCheckboxFinal ? column : 0);
                            }

                            patcherDialog.fitTableColumns(table);
                        }
                        // table.setRowSelectionInterval(row, row);
                    }
                });
                popup.add(selectAllOrNot);
                popup.add(new JSeparator());

                JMenuItem edit = new JMenuItem("编辑");
                popup.add(edit);
                edit.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        table.clearSelection(); //清除高亮选择状态
                        table.editCellAt(row, column); //设置某列为可编辑
                    }
                });

                JMenuItem calcel = new JMenuItem("删行");
                calcel.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (table == patcherDialog.getSelectTable()) {
                            List<SelectDTO> ds = patcherDialog.getSelectDtos();
                            if (ds != null && ds.size() > row) {
                                ds.remove(row);
                                patcherDialog.setSelectTableDatas(ds);
                            }
                        } else {
                            ((DefaultTableModel) table.getModel()).removeRow(row);
                        }
                    }
                });
                popup.add(new JSeparator());
                popup.add(calcel);
                popup.show(me.getComponent(), me.getX(), me.getY());
            }
        }
    }

    public void fitTableColumns(JTable myTable) {
        if (myTable == null) {
            return;
        }
        JTableHeader header = myTable.getTableHeader();
        int rowCount = myTable.getRowCount();
        Enumeration columns = myTable.getColumnModel().getColumns();
        while (columns.hasMoreElements()) {
            TableColumn column = (TableColumn) columns.nextElement();
            int col = header.getColumnModel().getColumnIndex(column.getIdentifier());
            int width = (int) myTable.getTableHeader().getDefaultRenderer()
                    .getTableCellRendererComponent(myTable, column.getIdentifier()
                            , false, false, -1, col).getPreferredSize().getWidth();
            for (int row = 0; row < rowCount; row++) {
                int preferedWidth = (int) myTable.getCellRenderer(row, col).getTableCellRendererComponent(myTable,
                        myTable.getValueAt(row, col), false, false, row, col).getPreferredSize().getWidth();
                width = Math.max(width, preferedWidth);
            }
            header.setResizingColumn(column); // 此行很重要
            column.setWidth(width + myTable.getIntercellSpacing().width + 5);
        }
    }

    public static List<PatcherSelectFileVO> getForceAddSelectFiles(Project project) {
        List<PatcherSelectFileVO> vs = loadForceAddSelectFiles(project);

        return vs;
    }

    public static boolean addForceAddSelectFile(Project project, PatcherSelectFileVO f) {
        List<PatcherSelectFileVO> vs = getForceAddSelectFiles(project);
        try {
            if (vs.contains(f)) {
                return false;
            }

            return vs.add(f);
        } finally {
            flushForceAddSelectFiles(project, vs);
        }
    }

    public static boolean containsForceAddSelectFile(Project project, PatcherSelectFileVO f) {
        return getForceAddSelectFiles(project).contains(f);
    }

    public static boolean removeForceAddSelectFile(Project project, VirtualFile f) {
        List<PatcherSelectFileVO> vs = getForceAddSelectFiles(project);
        try {
            return vs.remove(f);
        } finally {
            flushForceAddSelectFiles(project, vs);
        }
    }

    public static boolean removeForceAddSelectFile(Project project, PatcherSelectFileVO f) {
        List<PatcherSelectFileVO> vs = getForceAddSelectFiles(project);
        try {
            return vs.remove(f);
        } finally {
            flushForceAddSelectFiles(project, vs);
        }
    }

    public static void setSelectFiles(Project project, List<PatcherSelectFileVO> vs) {
        flushForceAddSelectFiles(project, vs);
    }

    private static void flushForceAddSelectFiles(Project project, List<PatcherSelectFileVO> vs) {
        FileUtil.writeUtf8String(JSON.toJSONString(vs)
                , new File(new File(project.getBasePath(), ".idea")
                        , "ForceAddSelectFiles.json"));
    }

    public static List<PatcherSelectFileVO> loadForceAddSelectFiles(Project project) {
        List<PatcherSelectFileVO> pts = null;
        try {
            String json = FileUtil.readUtf8String(new File(new File(project.getBasePath(), ".idea"),
                    "ForceAddSelectFiles.json"));
            pts = JSON.parseArray(json, PatcherSelectFileVO.class);

            if (pts != null) {
                for (PatcherSelectFileVO pt : pts) {
                    if (pt.getSort() != null) {
                        pt.setSort(0);
                    }
                }
                pts = pts.stream().sorted((a, b) -> b.getSort().compareTo(a.getSort())).collect(Collectors.toList());
            }
        } catch (Throwable e) {
        }

        List<PatcherSelectFileVO> vs = new CopyOnWriteArrayList<>();
        VirtualFileManager vm = VirtualFileManager.getInstance();
        if (CollUtil.isNotEmpty(pts)) {
            for (PatcherSelectFileVO pt : pts) {
                VirtualFile f = vm.findFileByNioPath(Path.of(pt.getPath()));
                if (f == null) {
                    continue;
                }
                pt.setFile(f);
                vs.add(pt);
            }
        }

        return vs;
    }

    public static void clearForceAddSelectFile(Project project) {
        List<PatcherSelectFileVO> vs = getForceAddSelectFiles(project);
        try {
            vs.clear();
        } finally {
            flushForceAddSelectFiles(project, vs);
        }
    }

    private void updateSelectFileTable() {
        ArrayList<SelectDTO> rows = Lists.newArrayList();
        selectExport2.setSelected(false);
        if (selectExport.isSelected()) {
            VirtualFile[] selects = V.get(LangDataKeys.VIRTUAL_FILE_ARRAY.getData(event.getDataContext()),
                    new VirtualFile[0]);
            List<PatcherSelectFileVO> vs = getForceAddSelectFiles(event.getProject());
            for (VirtualFile select : selects) {
                if (StringUtil.isBlank(select.getPath())) {
                    continue;
                }

                SelectDTO s = new SelectDTO();
                s.setFile(select);
                s.setPath(select.getPath());
                rows.add(s);
            }
            for (PatcherSelectFileVO select : vs) {
                if (StringUtil.isBlank(select.getPath())) {
                    continue;
                }

                rows.add(ReflectUtil.copy2VO(select, SelectDTO.class));
            }
        } else {
            rows.add(new SelectDTO().path("全部"));
        }

        setSelectTableDatas(rows);
    }

    private void updateSelectFileTable2() {
        ArrayList<SelectDTO> rows = Lists.newArrayList();
        selectExport.setSelected(false);
        if (selectExport2.isSelected()) {
            List<PatcherSelectFileVO> vs = getForceAddSelectFiles(event.getProject());
            for (PatcherSelectFileVO select : vs) {
                if (StringUtil.isBlank(select.getPath())) {
                    continue;
                }

                rows.add(ReflectUtil.copy2VO(select, SelectDTO.class));
            }
        } else {
            rows.add(new SelectDTO().path("全部"));
        }

        setSelectTableDatas(rows);
    }

    @lombok.Data
    @Accessors(chain = true)
    public static class SelectDTO extends PatcherSelectFileVO {
        boolean select = true;

        public SelectDTO path(String path) {
            setPath(path);
            return this;
        }

        public SelectDTO file(VirtualFile f) {
            setFile(f);
            return this;
        }

        public SelectDTO setSelect(boolean select) {
            this.select = select;
            return this;
        }
    }

    List<SelectDTO> selectDtos;

    public void setSelectTableDatas(List<SelectDTO> rows) {
        setSelectDtos(rows);

        while (defaultTableModel.getRowCount() > 0) {
            defaultTableModel.removeRow(0);
        }

        for (SelectDTO row : rows) {
            Vector v = new Vector();
            v.add(row.isSelect());
            v.add(row.getPath());
            v.add(row.getMemo());
            defaultTableModel.addRow(v);
        }

        fitTableColumns(selectTable);
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

        try {
            ExportContentVO contentVO = getFromUI();
            if (contentVO == null) {
                return;
            }

            if (contentVO.saveConfig) {
                ExportNCPatcherUtil.saveConfig(event.getProject(), contentVO);
            }

            Task.Backgroundable backgroundable = new Task.Backgroundable(event.getProject(), "导出中...请等待...") {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    isRuning = true;
                    indicator.setText("正在玩命导出NC补丁包中...");
                    indicator.setText2("导出成功后会自动打开文件夹： " + contentVO.outPath);
                    indicator.setIndeterminate(true);
                    long s = System.currentTimeMillis();
                    contentVO.indicator = indicator;
                    File opendir = null;
                    try {
                        if (contentVO.exportResources && contentVO.reNpmBuild && !contentVO.indicator.isCanceled()) {
                            //你懂得！
                            reBuildNpmPatcther(contentVO, indicator);
                        }

                        if (!contentVO.indicator.isCanceled()) {
                            ExportNCPatcherUtil.export(contentVO);
                        }

                        long e = System.currentTimeMillis();
                        LogUtil.infoAndHide("导出成功,耗时:" + ((e - s) / 1000.0d) + " (秒s)!硬盘路径： " + contentVO.getOutPath());

                        File dirToOpen = new File(contentVO.getOutPath());

                        LogUtil.infoAndHide("导出补丁原始目录： " + dirToOpen.getPath());

                        try {
                            if (contentVO.zip && !contentVO.indicator.isCanceled()) {
                                opendir = zipPathcers(contentVO, dirToOpen);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                        if (contentVO.isFormat4Ygj() && !contentVO.indicator.isCanceled()) {
                            if (contentVO.isDeleteDir()) {
                                try {
                                    FileUtil.del(dirToOpen);
                                } catch (Throwable ex) {
                                }
                            }
                        } else {
                            if (contentVO.isDeleteDir() && !contentVO.indicator.isCanceled()) {
                                try {
                                    FileUtil.del(dirToOpen.getParentFile());
                                } catch (Throwable ex) {
                                }
                            }
                        }

                        if (opendir != null) {
                            try {
                                if (!opendir.exists()) {
                                    opendir = opendir.getParentFile();
                                    if (!opendir.exists()) {
                                        opendir = opendir.getParentFile();
                                    }
                                }

                                IoUtil.tryOpenFileExpolor(opendir);
                            } catch (Throwable ioException) {
                                ioException.printStackTrace();
                                LogUtil.infoAndHide("导出成功！路径: " + opendir.getPath());
                            }
                        }

                        if (contentVO.indicator.isCanceled()) {
                            LogUtil.infoAndHide("手工取消导出补丁成功.");
                        }
                    } catch (Throwable iae) {
                        LogUtil.error("自动打开路径失败: " + ExceptionUtil.getExcptionDetall(iae)
                                + " ,路径: " + (opendir == null ? null : opendir.getPath()));
                    } finally {
                        isRuning = false;
                    }
                    isRuning = false;
                }
            };
            backgroundable.setCancelText("停止导出");
            backgroundable.setCancelTooltipText("立即停止导出");
            ProgressManager.getInstance().run(backgroundable);

            dispose();
        } catch (Throwable ex) {
            ex.printStackTrace();
            LogUtil.error(ex.toString(), ex);
            isRuning = false;
        }
    }

    private void reBuildNpmPatcther(ExportContentVO contentVO, @NotNull ProgressIndicator indicator) throws IOException {
        File dir = new File(contentVO.hotwebsResourcePath);
        if (dir.isDirectory()) {
            indicator.setText("强制删除前端hotwebs的dist后执行" + contentVO.getNpmRunCommand() + "中...");
            IoUtil.cleanUpDirFiles(new File(dir, "dist"));
            String cm = ExecUtil.npmBuild(dir.getPath()
                    , (line) -> {
                        indicator.setText("npm building:" + StrUtil.removeAllLineBreaks(line));
                    }
                    , contentVO.getNpmRunCommand()
            );
            LogUtil.infoAndHide("前端 " + contentVO.getNpmRunCommand() + ": " + cm);
        }
    }

    private File zipPathcers(ExportContentVO contentVO, File dirToOpen) {
        File zip = null;
        if (contentVO.isFormat4Ygj()) {
            zip = new File(dirToOpen.getParentFile(), dirToOpen.getName() + ".zip");
            contentVO.indicator.setText("自动打包成zip压缩包中..." + zip.getPath());
            zip = ZipUtil.zip(dirToOpen);
        } else {
            zip = new File(dirToOpen.getParentFile().getParentFile(), dirToOpen.getParentFile().getName() + ".zip");
            contentVO.indicator.setText("自动打包成zip压缩包中..." + zip.getPath());
            zip = ZipUtil.zip(dirToOpen.getParentFile());
        }

        LogUtil.infoAndHide("自动打包成补丁zip压缩包的硬盘路径： " + zip.getPath() + " ,文件夹: " + dirToOpen.getPath());

        return zip;
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
