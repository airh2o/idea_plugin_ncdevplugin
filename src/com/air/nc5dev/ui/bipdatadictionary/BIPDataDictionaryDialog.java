package com.air.nc5dev.ui.bipdatadictionary;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.air.nc5dev.util.CollUtil;
import com.air.nc5dev.util.ExceptionUtil;
import com.air.nc5dev.util.NCPropXmlUtil;
import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.util.idea.LogUtil;
import com.air.nc5dev.util.idea.ProjectUtil;
import com.air.nc5dev.util.jdbc.ConnectionUtil;
import com.air.nc5dev.vo.DataDictionaryAggVO;
import com.air.nc5dev.vo.NCDataSourceVO;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.*;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.HashSet;
import java.util.List;

/***
 *     弹框UI        <br>
 *           <br>
 *           <br>
 *           <br>
 * @author air Email: 209308343@qq.com
 * @date 2019/12/25 0025 15:29
 * @Param
 * @return
 */
@Data
public class BIPDataDictionaryDialog extends DialogWrapper {
    JBTabbedPane contentPane;
    JBPanel panel_main;
    JBTextField url;
    JBTextField user;
    JBTextField pass;
    JBTextArea textFieldSerach;
    JButton buttonSearch;
    JBLabel labelInfo;
    int height = 200;
    int width = 300;
    Project project;
    long start;
    JButton buttonClose;
    JButton buttonCacheClear;
    JButton buttonTestDb;

    public BIPDataDictionaryDialog(Project project) {
        super(project);
        this.project = project;
        createCenterPanel();
        init();
        setOKButtonText("查看字典(生成临时文件 浏览器直接打开)");
        setCancelButtonText("导出离线文件");
        setTitle("生成BIP旗舰版系列数据字典");

        loadValues();
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

            JBLabel label = new JBLabel("JDBC URL:");
            label.setBounds(1, y, 100, 60);
            panel_main.add(label);

            url = new JBTextField();
            url.setBounds(label.getX() + label.getWidth() + 3, y, 500, h);
            panel_main.add(url);

            label = new JBLabel("数据库用户:");
            label.setBounds(1, y = url.getY() + url.getHeight() + 5, 100, 60);
            panel_main.add(label);

            user = new JBTextField();
            user.setBounds(label.getX() + label.getWidth() + 3, y, 500, h);
            panel_main.add(user);

            label = new JBLabel("数据库密码:");
            label.setBounds(1, y = user.getY() + user.getHeight() + 5, 100, 60);
            panel_main.add(label);

            pass = new JBTextField();
            pass.setBounds(label.getX() + label.getWidth() + 3, y, 500, h);
            panel_main.add(pass);

            labelInfo = new JBLabel();
            labelInfo.setBounds(1, y += pass.getHeight() + 3, getWidth(), h);
            panel_main.add(labelInfo);

            textFieldSerach = new JBTextArea();
            textFieldSerach.setEditable(true);
            textFieldSerach.setLineWrap(true);
            JBScrollPane jbScrollPane = new JBScrollPane(textFieldSerach);
            jbScrollPane.setBounds(x = 1, y = labelInfo.getY() + labelInfo.getHeight() + 5, 500, h = 250);
            panel_main.add(jbScrollPane);

            w = getWidth() - 10;
            buttonSearch = new JButton("搜索");
            buttonSearch.setBounds(x += jbScrollPane.getWidth() + 5, y, w = 60, h = 40);
            panel_main.add(buttonSearch);

            buttonTestDb = new JButton("测试数据库");
            buttonTestDb.setBounds(x += w + 5, y, w = 100, h = 40);
            panel_main.add(buttonTestDb);
            buttonTestDb.addActionListener(this::testDbConnection);

            buttonClose = new JButton("关闭窗口");
            buttonClose.setBounds(x += w + 5, y, w = 100, h = 40);
            panel_main.add(buttonClose);
            buttonClose.addActionListener(e -> close(0));

            buttonCacheClear = new JButton("清空缓存(不清空就会缓存到关闭idea为止)");
            buttonCacheClear.setBounds(x += w + 5, y, w = 200, h = 40);
            panel_main.add(buttonCacheClear);
            buttonCacheClear.addActionListener(e -> BIPLoadDataDictionaryAggVOUtil.cache.cleanUp());
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
            ConnectionUtil.initDataSourceClass(getDataSource(), project, contentPane);
            start = System.currentTimeMillis();
            Task.Backgroundable backgroundable = new Task.Backgroundable(project, "正在生成...耗时会比较长...完成后会自动打开...请耐心等待") {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    try {
                        getButtonSearch().setEnabled(false);
                        getOKAction().setEnabled(false);
                        getCancelAction().setEnabled(false);

                        export2Files0(
                                new File(System.getProperty("java.io.tmpdir"),
                                        "bip_data_dictionary_" + System.currentTimeMillis() + ".html")
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
        NCDataSourceVO dataSource = getDataSource();
        if (dataSource == null) {
            LogUtil.infoAndHide("没有配置数据库连接哦!");
            return;
        }

        ConnectionUtil.initDataSourceClass(dataSource, project, contentPane);
        //选择保存位置
        File outDir = new File(getProject().getBasePath(), "bip_data_dictionary_" + System.currentTimeMillis());
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
            File f = new File(outDir, "bip_data_dictionary");
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
            NCDataSourceVO ds = getDataSource();
            ConnectionUtil.initDataSourceClass(ds, project, contentPane);

            BIPLoadDataDictionaryAggVOUtil util = new BIPLoadDataDictionaryAggVOUtil(getProject(), ds);
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
                FileUtil.writeUtf8String(str, new File("e:/temp/bip_data_dictionary.json"));// TODO FIXME 测试用，正式注释这行
            } catch (Throwable e) {
            }*/

            String html = ProjectUtil.getResourceTemplatesUtf8Txt("nc_data_dictionary/index.html");
            html = html.replace("{{DataDictionaryAggVOJsonString}}", str);
            html = html.replace("{{version}}", agg.getNcVersion());
            html = html.replace("<title>NC 数据字典", "<title>" + agg.getNcVersion() + " 数据字典");

            String name = StringUtil.replaceAll(agg.getNcVersion(), ":", ".");
            name = StringUtil.replaceAll(name, "&", ".");
            name = StringUtil.replaceAll(name, "$", ".");
            name = StringUtil.replaceAll(name, "\\", ".");
            name = StringUtil.replaceAll(name, "/", ".");
            name = StringUtil.replaceAll(name, "*", ".");
            name = StringUtil.replaceAll(name, "?", ".");
            name = StringUtil.replaceAll(name, "\"", ".");
            name = StringUtil.replaceAll(name, "'", ".");
            name = StringUtil.replaceAll(name, "<", ".");
            name = StringUtil.replaceAll(name, ">", ".");
            name = StringUtil.replaceAll(name, "(", ".");
            name = StringUtil.replaceAll(name, ")", ".");
            name = StringUtil.replaceAll(name, "=", ".");
            name = StringUtil.replaceAll(name, "|", ".");
            File index = null;
            try {
                index = new File(f, name + "_离线数据字典.html");
            } catch (Throwable e) {
                index = new File(f, StrUtil.replaceChars(agg.getNcVersion(), new char[]{'/', '\\', ':'}, "") + "_" +
                        "离线数据字典.html");
            }
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
                if (s.equals("index.js")) {
                    // 采购订单(po_order) (实体*) | 表: po_order | VO类: nc.vo.pu.m21.entity.OrderHeaderVO
                    // | Agg类: nc.vo.pu.m21.entity.OrderVO | 单据编码: 21 | 单据名称: 采购订单 | 节点编码: 40040400
                    // | 轻量端页码编码: 400400800_card | 轻量端页码地址: /nccloud/resources/pu/pu/poorder/main/index.html#/card
                    String ss = ProjectUtil.getResourceTemplatesUtf8Txt("nc_data_dictionary/" + s);
                    ss = StringUtil.replaceAll(ss, "轻量端页码编码", "页面编码cbillno");  // pagecode
                    ss = StringUtil.replaceAll(ss, "轻量端页码地址", "页面类型");  // pageurl
                    ss = StringUtil.replaceAll(ss, "节点编码", "页面名称");  // nodecode
                    ss = StringUtil.replaceAll(ss, "Agg类", "Schema"); // aggFullClassName
                    ss = StringUtil.replaceAll(ss, "VO类", "页面名称");  // fullClassName
                    ss = StringUtil.replaceAll(ss, "重量端XML配置", "其他信息");  // paramvalue
                    ss = StringUtil.replaceAll(ss, "重量端节点名", "完整URI");  // fun_name
                    FileUtil.writeUtf8String(ss, new File(f, s));
                    continue;
                }

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

    public void testDbConnection(ActionEvent actionEvent) {
        NCDataSourceVO ds = getDataSource(false);
        ConnectionUtil.initDataSourceClass(ds, getProject(), getContentPane(), re -> {
            if (re instanceof Boolean) {
                if (((Boolean) re).booleanValue()) {
                    Messages.showInfoMessage("数据库连接成功!", "恭喜");
                } else {
                    String msg = "未知原因";
                    if (re instanceof Throwable) {
                        msg = ExceptionUtil.toString(ExceptionUtil.getTopCase((Throwable) re));
                    }
                    Messages.showErrorDialog("数据库连接失败:" + msg, "哦豁");
                }
            }
        });
    }

    public NCDataSourceVO getDataSource() {
        return getDataSource(true);
    }

    public NCDataSourceVO getDataSource(boolean save) {
        NCDataSourceVO ds = new NCDataSourceVO();
        ds.setDatabaseUrl(getUrl().getText());
        ds.setUser(getUser().getText());
        ds.setPassword(getPass().getText());
        ds.setPasswordOrgin(ds.getPassword());

        if (save) {
            FileUtil.writeUtf8String(JSON.toJSONString(ds)
                    , new File(new File(getProject().getBasePath(), ".idea"), this.getClass().getSimpleName() + ".json")
            );
        }

        return ds;
    }

    public void loadValues() {
        try {
            String str = FileUtil.readUtf8String(
                    new File(new File(getProject().getBasePath(), ".idea"), this.getClass().getSimpleName() + ".json")
            );
            if (StrUtil.isBlank(str)) {
                return;
            }

            NCDataSourceVO ds = JSON.parseObject(str, NCDataSourceVO.class);
            getUrl().setText(ds.getDatabaseUrl());
            getUser().setText(ds.getUser());
            getPass().setText(ds.getPassword());
        } catch (Throwable e) {
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
            textFieldSerach.setText("select c.uri as id \n " +
                    "     ,c.legacy_id \n " +
                    "     ,c.domain as namespace \n " +
                    "     ,c.uri \n " +
                    "     ,c.display_name as displayName \n " +
                    "     ,c.own_module as ownModule \n " +
                    "     ,c.name \n " +
                    "     ,c.description \n " +
                    //   "     ,c.create_time as createTime \n " +
                    "     ,c.version \n " +
                    "     ,c.micro_service_code as filePath \n " +
                    "from iuap_metadata_base.md_meta_component c \n " +
                    "where c.ytenant_id='0' \n " +
                    "order by c.pubts desc \n ");
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
