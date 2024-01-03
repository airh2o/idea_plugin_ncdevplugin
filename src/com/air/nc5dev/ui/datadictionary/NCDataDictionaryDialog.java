package com.air.nc5dev.ui.datadictionary;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.http.HttpUtil;
import com.air.nc5dev.enums.NcVersionEnum;
import com.air.nc5dev.ui.exportbmf.BmfListTable;
import com.air.nc5dev.ui.exportbmf.ExportbmfTableMouseListenerImpl;
import com.air.nc5dev.util.*;
import com.air.nc5dev.util.idea.LogUtil;
import com.air.nc5dev.util.idea.ProjectUtil;
import com.air.nc5dev.util.jdbc.ConnectionUtil;
import com.air.nc5dev.util.jdbc.resulthandel.VOArrayListResultSetExtractor;
import com.air.nc5dev.util.meta.QueryCompomentVOUtil;
import com.air.nc5dev.util.meta.xml.MeatBaseInfoReadUtil;
import com.air.nc5dev.util.meta.xml.MetaXmlBuilder4NC6;
import com.air.nc5dev.vo.DataDictionaryAggVO;
import com.air.nc5dev.vo.NCDataSourceVO;
import com.air.nc5dev.vo.meta.ComponentAggVO;
import com.air.nc5dev.vo.meta.ComponentDTO;
import com.air.nc5dev.vo.meta.SearchComponentVO;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.*;
import com.intellij.ui.jcef.JBCefBrowser;
import com.intellij.ui.jcef.JBCefJSQuery;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/***
 *     弹框UI        </br>
 *           </br>
 *           </br>
 *           </br>
 * @author air Email: 209308343@qq.com
 * @date 2019/12/25 0025 15:29
 * @Param
 * @return
 */
@Data
public class NCDataDictionaryDialog extends DialogWrapper {
    JBTabbedPane contentPane;
    JBPanel panel_main;
    DefaultComboBoxModel<NCDataSourceVO> comboBoxModelDb;
    ComboBox comboBoxDb;
    JBTextArea textFieldSerach;
    JButton buttonSearch;
    JBLabel labelInfo;
    int height = 200;
    int width = 300;
    Project project;
    long start;

    public NCDataDictionaryDialog(Project project) {
        super(project);
        this.project = project;
        createCenterPanel();
        init();
        setOKButtonText("查看字典(生成临时文件 浏览器直接打开)");
        setCancelButtonText("导出离线文件");
        setTitle("生成NC数据字典");
    }

    private void createCenterPanel0() throws Exception {
        JBTabbedPane jtab = new JBTabbedPane();
        contentPane = jtab;
        contentPane.setBounds(0, 0, getWidth(), getHeight());
        {
            int x = 1;
            int y = 1;
            int w = 60;
            int h = 40;

            panel_main = new JBPanel();
            panel_main.setLayout(null);
            panel_main.setBounds(0, 0, height, width);
            jtab.addTab("选项", panel_main);

            final List<NCDataSourceVO> dataSourceVOS = NCPropXmlUtil.getDataSourceVOS(getProject());
            comboBoxModelDb = new DefaultComboBoxModel<>(new Vector(
                    dataSourceVOS.stream()
                            .map(v -> v.getDataSourceName() + '/' + v.getUser())
                            .collect(Collectors.toList())
            ));

            JBLabel label = new JBLabel("数据源:");
            label.setBounds(1, y, w, 60);
            panel_main.add(label);
            comboBoxDb = new ComboBox(comboBoxModelDb);
            comboBoxDb.setBounds(x += w, y, 150, h);
            panel_main.add(comboBoxDb);

            labelInfo = new JBLabel();
            labelInfo.setBounds(1, y += comboBoxDb.getHeight() + 3, getWidth(), h);
            panel_main.add(labelInfo);

            textFieldSerach = new JBTextArea();
            textFieldSerach.setEditable(true);
            textFieldSerach.setLineWrap(true);
            textFieldSerach.setBounds(x = 1, y += h + 5, w = getWidth() - 10, h = 150);
            panel_main.add(textFieldSerach);

            buttonSearch = new JButton("搜索");
            buttonSearch.setBounds(x += w + 5, y, w = 60, h = 40);
            panel_main.add(buttonSearch);
        }

        //设置点默认值
        initDefualtValues();
    }

    /**
     * 导出
     */
    @Override
    protected void doOKAction() {
        try {
            start = System.currentTimeMillis();
            Task.Backgroundable backgroundable = new Task.Backgroundable(project, "正在生成...耗时会比较长...完成后会自动打开...请耐心等待") {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    try {
                        getButtonSearch().setEnabled(false);
                        getOKAction().setEnabled(false);
                        getCancelAction().setEnabled(false);

                        export2Files0(
                                new File(System.getProperty("java.io.tmpdir"), "nc_data_dictionary_" + System.currentTimeMillis() + ".html")
                                , false
                                , indicator
                        );
                    } catch (Throwable e) {
                        LogUtil.error(e.getMessage(), e);
                    } finally {
                        getButtonSearch().setEnabled(true);
                        getOKAction().setEnabled(true);
                        getCancelAction().setEnabled(true);
                    }
                }
            };
            backgroundable.setCancelText("停止任务");
            backgroundable.setCancelTooltipText("停止这个任务");
            ProgressManager.getInstance().run(backgroundable);
        } finally {
            close(1);
        }
    }

    public void export2Files() {
        //选择保存位置
        File outDir = new File(getProject().getBasePath(), "nc_data_dictionary_" + System.currentTimeMillis());
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        fileChooser.setCurrentDirectory(outDir);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle("选择保存路径:");
        int flag = fileChooser.showSaveDialog(null);
        if (flag == JFileChooser.APPROVE_OPTION
                && fileChooser.getSelectedFile() != null) {
            outDir = fileChooser.getSelectedFile();
        } else {
            outDir = null;
        }

        if (outDir == null) {
            this.close(1);
            return;
        }

        if (!outDir.exists()) {
            outDir.mkdirs();
        }

        try {
            File f = new File(outDir, "nc_data_dictionary");
            Task.Backgroundable backgroundable = new Task.Backgroundable(project, "正在生成...耗时会比较长...完成后会自动打开...请耐心等待") {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    try {
                        getButtonSearch().setEnabled(false);
                        getOKAction().setEnabled(false);
                        getCancelAction().setEnabled(false);

                        export2Files0(
                                f
                                , true
                                , indicator
                        );
                    } catch (Throwable e) {
                        LogUtil.error(e.getMessage(), e);
                    } finally {
                        getButtonSearch().setEnabled(true);
                        getOKAction().setEnabled(true);
                        getCancelAction().setEnabled(true);
                    }
                }
            };
            backgroundable.setCancelText("停止任务");
            backgroundable.setCancelTooltipText("停止这个任务");
            ProgressManager.getInstance().run(backgroundable);
        } finally {
            close(1);
        }
    }

    public void export2Files0(File f, boolean openDir, ProgressIndicator indicator) {
        if (f == null) {
            return;
        }

        try {
            NCDataSourceVO ds = NCPropXmlUtil.getDataSourceVOS(getProject()).get(comboBoxDb.getSelectedIndex());
            LoadDataDictionaryAggVOUtil util = new LoadDataDictionaryAggVOUtil(getProject(), ds);
            util.setIndicator(indicator);
            util.setCompomentSql(textFieldSerach.getText());
            DataDictionaryAggVO agg = util.read();

            agg.setClassId2EnumValuesMap(null);
            agg.setId2ModuleMap(null);
            agg.setAllModules(null);

            if (indicator.isCanceled()) {
                return;
            }

            String str = JSON.toJSONString(agg, SerializerFeature.DisableCircularReferenceDetect);

            /*try {
                FileUtil.writeUtf8String(str, new File("e:/temp/nc_data_dictionary.json"));// TODO FIXME 测试用，正式注释这行
            } catch (Throwable e) {
            }*/

            String html = ProjectUtil.getResourceTemplatesUtf8Txt("nc_data_dictionary/index.html");
            html = html.replace("{{DataDictionaryAggVOJsonString}}", str);

            File index = new File(f, "index.html");
            FileUtil.writeUtf8String(html, index);

            HashSet<String> fs = CollUtil.newHashSet("index.js"
                    , "manifest.js"
                    , "vendor.js"

                    , "674f50d287a8c48dc19ba404d20fe713.eot"
                    , "912ec66d7572ff821749319396470bde.svg"
                    , "535877f50039c0cb49a6196a5b7517cd.woff"
                    , "732389ded34cb9c52dd88271f1345af9.ttf"
                    , "af7ae505a9eed503f8b8e6982036873e.woff2"
                    , "b06871f281fee6b241d60582ae9369b9.ttf"
                    , "fee66e712a8a08eef5805a46892932ad.woff"
            );
            for (String s : fs) {
                byte[] bts = ProjectUtil.getResourceByte("nc_data_dictionary/" + s);
                if (bts == null) {
                    File strf = ProjectUtil.getResourceTemplates("nc_data_dictionary/" + s);
                    if (strf != null && strf.isFile()) {
                        bts = FileUtil.readBytes(strf);
                    }
                }
                if (bts == null) {
                    continue;
                }
                FileUtil.writeBytes(bts, new File(f, s));
            }

            LogUtil.infoAndHide("耗时: " + ((System.currentTimeMillis() - start) / 1000) + "秒 ,生成数据字典文件成功: " + f.getPath());

            if (openDir) {//保存离线数据字典html文件
                Runtime.getRuntime().exec("explorer /select, " + index.getPath());
            } else {//直接浏览器打开临时文件即可
                try {
                    Desktop desktop = Desktop.getDesktop();
                    desktop.open(index);
                } catch (Throwable ioException) {
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
            LogUtil.error(e.getMessage(), e);
        } finally {
        }
    }

    @Override
    public void doCancelAction() {
        start = System.currentTimeMillis();
        export2Files();
    }

    @Override
    protected JComponent createCenterPanel() {
        if (contentPane == null) {
            try {
                createCenterPanel0();
            } catch (Exception e) {
                e.printStackTrace();
                LogUtil.error(e.getMessage(), e);
            }
        }

        return this.contentPane;
    }

    public void initDefualtValues() {
        try {
            textFieldSerach.setText("select id,name,namespace,displayName,ownModule,version " +
                    "\nfrom md_component where 1=1 " +
                    "\norder by ts desc ");
            labelInfo.setText("保存文件弹框 直接点击取消 不选择文件 就是关闭窗口！");
        } catch (Throwable e) {
            e.printStackTrace();
            LogUtil.error(e.getMessage(), e);
        }
    }

    @Nullable
    @Override
    protected String getDimensionServiceKey() {
        return this.getClass().getName();
    }

    private void onCancel() {
        dispose();
    }
}
