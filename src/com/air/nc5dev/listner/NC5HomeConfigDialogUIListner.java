package com.air.nc5dev.listner;

import com.air.nc5dev.acion.NC5HomePathConfigAction;
import com.air.nc5dev.bean.NCDataSourceVO;
import com.air.nc5dev.ui.NC5HomeConfigDiaLogPanel;
import com.air.nc5dev.util.NCPropXmlUtil;
import com.air.nc5dev.util.ProjectNCConfigUtil;
import com.air.nc5dev.util.idea.ProjectUtil;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import nc.vo.framework.rsa.Encode;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

/**
 * NC5HomeConfigDialogUI 的监听器
 */
public class NC5HomeConfigDialogUIListner {
    private NC5HomeConfigDiaLogPanel ui;
    /**
     * 当点击了  测试数据库链接
     *
     * @param e
     */
    private void OnTestDb(ActionEvent e) {
        if (NCPropXmlUtil.isDataSourceEmpty()) {
            return;
        }
        int selectedIndex = ui.comboBox_datasource.getSelectedIndex();
        testConnectDb(NCPropXmlUtil.get(selectedIndex));
    }

    /**
     * 测试数据源链接
     *
     * @param ds
     */
    private void testConnectDb(NCDataSourceVO ds) {
        //final String ora11 = "ORACLE11G", ora10 = "ORACLE10G", sqlserver = "SQLSERVER2008", db297 = "DB297";
        Connection con;
        String user = ui.textField_user.getText();
        String password = ui.textField_pass.getText();
        String className = ds.getDriverClassName();
        String url = ui.textField_ip.getText();
        try {
            Class.forName(className);
            con = DriverManager.getConnection(url, user, password);
            con.close();
        } catch (Exception e) {
            Messages.showErrorDialog(ProjectUtil.getDefaultProject(), e.toString(), "连接失败");
            return;
        }

        Messages.showMessageDialog("连接成功", "提示", null);
    }

    /**
     * 当点击了  选择HOME文件夹
     *
     * @param e
     */
    private void OnChoseHomeDir(ActionEvent e) {
        VirtualFile virtualFile = FileChooser.chooseFile(new FileChooserDescriptor(false, true
                        , false, false, false, false), null
                , null);
        if (null == virtualFile) {
            return;
        }
        String path = virtualFile.getPath();
        File home = new File(path);
        if (home.exists() && home.isDirectory()) {
            ui.textField_home.setText(home.getPath());
            readNCConfig();
        } else {
            Messages.showMessageDialog("请选择正确的文件夹！", "错误", null);
        }
    }

    /**
     * 读取 NC的各种基本配置信息
     */
    private void readNCConfig() {
        ProjectNCConfigUtil.setNCHomePath(ui.textField_home.getText());
        NCPropXmlUtil.loadConfFromFile(ProjectNCConfigUtil.getNCHomePath());
        loadConf();
    }

    /**
     * 当点击了  新增开发数据源
     *
     * @param e
     */
    private void OnAddDesgin(ActionEvent e) {
        if (NCPropXmlUtil.isDataSourceEmpty()) {
            return;
        }
        final String designName = "design";
        boolean hasDesign = NCPropXmlUtil.stream().anyMatch(ncDataSourceVO -> designName.equals(ncDataSourceVO.getDataSourceName()));
        if (hasDesign) {
            return;
        }

        try {
            NCDataSourceVO design = NCPropXmlUtil.get(ui.comboBox_datasource
                    .getSelectedIndex()).clone();
            design.setDataSourceName(designName);
            design.setIsBase("false");
            NCPropXmlUtil.add(design);
            putDataSourceVOs2UI(NCPropXmlUtil.getDataSourceVOS());

        } catch (CloneNotSupportedException e1) {
            e1.printStackTrace();
            Messages.showErrorDialog(ProjectUtil.getDefaultProject(), e.toString(), "新增design数据源错误");
        }

    }

    /**
     * 当点击了  保存确认
     *
     * @param e
     */
    public void OnSave(@Nullable ActionEvent e) {
        ProjectNCConfigUtil.setNCHomePath(ui.textField_home.getText());
        ProjectNCConfigUtil.setNCClientIP(ui.textField_clientip.getText());
        ProjectNCConfigUtil.setNCClientPort(ui.textField_cientport.getText());

        ProjectNCConfigUtil.saveConfig2File();
        //NCPropXmlUtil.saveDataSources();
    }

    /**
     * 数据源选择改变
     */
    private void onDataSourceSelectChange(ItemEvent event) {
        if (event.getStateChange() != ItemEvent.DESELECTED) {
            return;
        }

        if(event.getItem() == null){
            return ;
        }

        //把编辑值 放入到 数据源 配置变量里
        String old = event.getItem().toString();
        NCDataSourceVO oldDs = NCPropXmlUtil.get(old);
        oldDs.setDatabaseUrl(ui.textField_ip.getText());
        oldDs.setOidMark(ui.textField_oidmark.getText());
        oldDs.setUser(ui.textField_user.getText());
        oldDs.setPassword(new Encode().encode(ui.textField_pass.getText()));
        oldDs.setMinCon(ui.textField_minConCout.getText());
        oldDs.setMaxCon(ui.textField_maxConCount.getText());
        oldDs.setDatabaseType(ui.comboBox_dbtype.getSelectedItem().toString());
        oldDs.setDataSourceClassName(ProjectNCConfigUtil.dsTypeClasss[ui.comboBox_dbtype.getSelectedIndex()]);


        //显示最新选择的
        String now = ui.comboBox_datasource.getSelectedItem().toString();
        NCDataSourceVO ds = NCPropXmlUtil.get(now);
        showDataSource2UI(ds);
    }
    /**
      *  数据库类型 下拉框改变         </br>
      *           </br>
      *           </br>
      *           </br>
      * @author air Email: 209308343@qq.com
      * @date 2020/1/17 0017 11:25
      * @Param [event]
      * @return void
     */
    private void onDbTypeSelectChange(ItemEvent event) {
        if (event.getStateChange() != ItemEvent.DESELECTED) {
            return;
        }
        //显示最新选择的
        String dataSourceName = ui.comboBox_datasource.getSelectedItem().toString();
        NCDataSourceVO ds = NCPropXmlUtil.get(dataSourceName);
        String dataBaseType = ui.comboBox_dbtype.getSelectedItem().toString();
        ds.setDriverClassName(getJdbcClassNameByDbType(dataBaseType));
        ds.setDataSourceClassName(ds.getDriverClassName());
    }

    /**
     *   根据下拉框的数据库类型获得jdbc class名字      </br>
     *           </br>
     *           </br>
     *           </br>
     * @author air Email: 209308343@qq.com
     * @date 2019/12/25 0025 11:23
     * @Param [databaseType]
     * @return java.lang.String
     */
    private String getJdbcClassNameByDbType(String databaseType) {
        int i = 0;
        for ( ; i < ProjectNCConfigUtil.dsTypes.length; i++) {
            if(ProjectNCConfigUtil.dsTypes[i].equals(databaseType)){
                break;
            }
        }

        if(i < ProjectNCConfigUtil.dsTypeClasss.length){
            return ProjectNCConfigUtil.dsTypeClasss[i];
        }

        return "";
    }

    /**
     * 显示指定的数据源 到ui界面
     *
     * @param ds
     */
    private void showDataSource2UI(@Nonnull final NCDataSourceVO ds) {
        ui.textField_ip.setText(ds.getDatabaseUrl());
        ui.textField_oidmark.setText(ds.getOidMark());
        ui.textField_user.setText(ds.getUser());
        ui.textField_pass.setText(new Encode().decode(ds.getPassword()));
        ui.textField_minConCout.setText(ds.getMinCon());
        ui.textField_maxConCount.setText(ds.getMaxCon());
        ui.comboBox_dbtype.setSelectedItem(ds.getDatabaseType());
    }

    /**
     * 当点击了  取消关闭
     *
     * @param e
     */
    private void OnCanel(ActionEvent e) {
        NC5HomePathConfigAction.ui.dispose();
    }

    /**
     * 载入NC配置到界面
     */
    private void loadConf() {
        ui.textField_home.setText(ProjectNCConfigUtil.getNCHomePath());
        ui.textField_clientip.setText(ProjectNCConfigUtil.getNCClientIP());
        ui.textField_cientport.setText(ProjectNCConfigUtil.getNCClientPort());

        List<NCDataSourceVO> dataSourceVOS = NCPropXmlUtil.getDataSourceVOS();

        if (null != dataSourceVOS && !dataSourceVOS.isEmpty()) {
            putDataSourceVOs2UI(dataSourceVOS);
        }
    }

    /**
     * 把一堆数据源放入 界面
     *
     * @param dataSourceVOS
     */
    public void putDataSourceVOs2UI(List<NCDataSourceVO> dataSourceVOS) {
        ui.comboBox_datasource.removeAllItems();
        dataSourceVOS.forEach(ds -> ui.comboBox_datasource.addItem(ds.getDataSourceName()));

        final int firstDsIndex = 0;
        ui.comboBox_datasource.setSelectedIndex(firstDsIndex);
        if (dataSourceVOS != null && !dataSourceVOS.isEmpty()) {
            showDataSource2UI(dataSourceVOS.get(firstDsIndex));
        }
    }

    /**
     * 初始化 按钮监听
     */
    private void initEventListeners() {
        ui.button_testdb.addActionListener(this::OnTestDb);
        ui.button_choseDir.addActionListener(this::OnChoseHomeDir);
        ui.button_adddesign.addActionListener(this::OnAddDesgin);
        ui.comboBox_datasource.addItemListener(this::onDataSourceSelectChange);
        ui.comboBox_dbtype.addItemListener(this::onDbTypeSelectChange);
    }

    public static final NC5HomeConfigDialogUIListner build(NC5HomeConfigDiaLogPanel setUI) {
        //每次弹出窗口 自动重新加载一次 插件配置文件
        ProjectNCConfigUtil.initConfigFile();

        NC5HomeConfigDialogUIListner nC5HomeConfigDialogUIListner = new NC5HomeConfigDialogUIListner(setUI);

        nC5HomeConfigDialogUIListner.initEventListeners();
        nC5HomeConfigDialogUIListner.loadConf();

        return nC5HomeConfigDialogUIListner;
    }

    private NC5HomeConfigDialogUIListner(NC5HomeConfigDiaLogPanel setUI) {
        this.ui = setUI;
    }
}
