package com.air.nc5dev.ui;

import com.air.nc5dev.enums.NcVersionEnum;
import com.air.nc5dev.ui.listener.NC5HomeConfigDialogUIListener;
import com.air.nc5dev.util.ProjectNCConfigUtil;
import com.intellij.openapi.project.Project;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.Font;

@Data
@Accessors(chain = true)
public class NC5HomeConfigDiaLogPanel extends JScrollPane {
    public JComboBox comboBox_libScope;
    public JComboBox comboBox_ncversion;
    public JTextField textField_home;
    public JTextField textField_ip;
    public JTextField textField_port;
    public JTextField textField_oidmark;
    public JTextField textField_sid;
    public JTextField textField_user;
    public JTextField textField_pass;
    public JTextField textField_clientip;
    public JTextField textField_cientport;
    public JButton button_testdb;
    public JButton button_tempInputPass;
    public JButton button_choseDir;
    public JButton button_adddesign;
    public JButton button_yes;
    public JButton button_canel;
    public JLabel label_3;
    public JComboBox comboBox_datasource;
    public JComboBox comboBox_dbtype;
    public JTextField textField_minConCout;
    public JTextField textField_maxConCount;
    public NC5HomeConfigDialogUIListener nc5HomeConfigDialogUIListner;
    Project project;

    /**
     * Create the dialog.
     */
    public NC5HomeConfigDiaLogPanel(Project project) {
        this.project = project;
        setLayout(null);
        JPanel contentPanel = new JPanel();
        contentPanel.setBounds(14, 14, 738, 543);
        add(contentPanel);
        contentPanel.setLayout(null);

        JLabel lblNc = new JLabel("NC 文件夹: ");
        lblNc.setBounds(14, 29, 107, 19);
        contentPanel.add(lblNc);

        textField_home = new JTextField();
        textField_home.setForeground(new Color(255, 0, 0));
        textField_home.setToolTipText("可编辑输入框");
        textField_home.setFont(new Font("宋体", Font.PLAIN, 16));
        textField_home.setBounds(114, 26, 489, 32);
        contentPanel.add(textField_home);
        textField_home.setColumns(10);

        button_choseDir = new JButton("选择");
        button_choseDir.setBounds(617, 9, 101, 59);
        contentPanel.add(button_choseDir);

        JLabel label = new JLabel("数据库类型: ");
        label.setBounds(14, 142, 107, 19);
        contentPanel.add(label);

        comboBox_dbtype = new JComboBox(ProjectNCConfigUtil.dsTypes);
        comboBox_dbtype.setBounds(114, 139, 170, 32);
        contentPanel.add(comboBox_dbtype);

        button_testdb = new JButton("测试链接");
        button_testdb.setBounds(300, 140, 120, 42);
        contentPanel.add(button_testdb);
        button_tempInputPass = new JButton("手工临时录入这个数据源的密码(不会保存!针对无法解密密码情况)");
        button_tempInputPass.setBounds(300, 70, 400, 35);
        contentPanel.add(button_tempInputPass);

        JLabel l34 = new JLabel("指定NC版本:");
        l34.setBounds(450, 100, 80, 42);
        contentPanel.add(l34);
        comboBox_ncversion = new JComboBox(NcVersionEnum.values());
        comboBox_ncversion.setBounds(570, 100, 120, 42);
        contentPanel.add(comboBox_ncversion);

        l34 = new JLabel("添加依赖的Scope:");
        l34.setBounds(450, 140, 110, 70);
        contentPanel.add(l34);
        comboBox_libScope = new JComboBox(ProjectNCConfigUtil.LIB_SCOPES);
        comboBox_libScope.setBounds(570, 140, 120, 42);
        contentPanel.add(comboBox_libScope);

        button_adddesign = new JButton("新增开发源");
        button_adddesign.setBounds(517, 112, 120, 42);
        contentPanel.add(button_adddesign);

        JLabel lblIp = new JLabel("IP:");
        lblIp.setBounds(14, 191, 46, 19);
        contentPanel.add(lblIp);

        textField_ip = new JTextField();
        textField_ip.setForeground(new Color(255, 0, 0));
        textField_ip.setToolTipText("可编辑输入框");
        textField_ip.setFont(new Font("宋体", Font.PLAIN, 16));
        textField_ip.setColumns(10);
        textField_ip.setBounds(114, 188, 354, 32);
        contentPanel.add(textField_ip);

        JLabel lblOid = new JLabel("Port:");
        lblOid.setBounds(14, 227, 80, 19);
        contentPanel.add(lblOid);

        textField_port = new JTextField();
        textField_port.setForeground(new Color(255, 0, 0));
        textField_port.setToolTipText("可编辑输入框");
        textField_port.setFont(new Font("宋体", Font.PLAIN, 16));
        textField_port.setColumns(10);
        textField_port.setBounds(114, 224, 113, 32);
        contentPanel.add(textField_port);

        JLabel lblOidmark = new JLabel("OIDMark:");
        lblOidmark.setBounds(246, 230, 80, 19);
        contentPanel.add(lblOidmark);

        textField_oidmark = new JTextField();
        textField_oidmark.setForeground(new Color(255, 0, 0));
        textField_oidmark.setToolTipText("可编辑输入框");
        textField_oidmark.setFont(new Font("宋体", Font.PLAIN, 16));
        textField_oidmark.setColumns(10);
        textField_oidmark.setBounds(340, 224, 128, 32);
        contentPanel.add(textField_oidmark);

        JLabel lblDbnamesid = new JLabel("DBNAME(SID):");
        lblDbnamesid.setBounds(14, 263, 137, 19);
        contentPanel.add(lblDbnamesid);

        textField_sid = new JTextField();
        textField_sid.setForeground(new Color(255, 0, 0));
        textField_sid.setToolTipText("可编辑输入框");
        textField_sid.setFont(new Font("宋体", Font.PLAIN, 16));
        textField_sid.setColumns(10);
        textField_sid.setBounds(172, 260, 295, 32);
        contentPanel.add(textField_sid);

        JLabel lblUsername = new JLabel("用户名:");
        lblUsername.setBounds(14, 306, 96, 25);
        contentPanel.add(lblUsername);

        textField_user = new JTextField();
        textField_user.setForeground(new Color(255, 0, 0));
        textField_user.setToolTipText("可编辑输入框");
        textField_user.setFont(new Font("宋体", Font.PLAIN, 16));
        textField_user.setColumns(10);
        textField_user.setBounds(114, 306, 582, 32);
        contentPanel.add(textField_user);

        JLabel lblPassword = new JLabel("密  码:");
        lblPassword.setBounds(14, 361, 96, 25);
        contentPanel.add(lblPassword);

        textField_pass = new JTextField();
        textField_pass.setForeground(new Color(255, 0, 0));
        textField_pass.setToolTipText("可编辑输入框");
        textField_pass.setFont(new Font("宋体", Font.PLAIN, 16));
        textField_pass.setColumns(10);
        textField_pass.setBounds(114, 358, 572, 32);
        contentPanel.add(textField_pass);

        JLabel label_1 = new JLabel("客户端地址:");
        label_1.setBounds(8, 451, 96, 25);
        contentPanel.add(label_1);

        textField_clientip = new JTextField();
        textField_clientip.setForeground(new Color(255, 0, 0));
        textField_clientip.setToolTipText("可编辑输入框");
        textField_clientip.setFont(new Font("宋体", Font.PLAIN, 16));
        textField_clientip.setBounds(120, 444, 582, 32);
        contentPanel.add(textField_clientip);
        textField_clientip.setColumns(10);

        JLabel label_2 = new JLabel("客户端端口:");
        label_2.setBounds(14, 505, 96, 25);
        contentPanel.add(label_2);

        textField_cientport = new JTextField();
        textField_cientport.setForeground(new Color(255, 0, 0));
        textField_cientport.setToolTipText("可编辑输入框");
        textField_cientport.setFont(new Font("宋体", Font.PLAIN, 16));
        textField_cientport.setBounds(120, 498, 582, 32);
        contentPanel.add(textField_cientport);
        textField_cientport.setColumns(10);

        label_3 = new JLabel("数据源: ");
        label_3.setBounds(14, 82, 107, 19);
        contentPanel.add(label_3);

        comboBox_datasource = new JComboBox();
        comboBox_datasource.setBounds(114, 79, 170, 32);
        contentPanel.add(comboBox_datasource);

        JLabel label_4 = new JLabel("连接数最小:");
        label_4.setBounds(482, 206, 101, 26);
        contentPanel.add(label_4);

        textField_minConCout = new JTextField();
        textField_minConCout.setForeground(new Color(255, 0, 0));
        textField_minConCout.setToolTipText("可编辑输入框");
        textField_minConCout.setFont(new Font("宋体", Font.PLAIN, 16));
        textField_minConCout.setColumns(10);
        textField_minConCout.setBounds(589, 204, 113, 32);
        contentPanel.add(textField_minConCout);

        JLabel label_5 = new JLabel("连接数最大:");
        label_5.setBounds(487, 256, 96, 26);
        contentPanel.add(label_5);

        textField_maxConCount = new JTextField();
        textField_maxConCount.setForeground(new Color(255, 0, 0));
        textField_maxConCount.setToolTipText("可编辑输入框");
        textField_maxConCount.setFont(new Font("宋体", Font.PLAIN, 16));
        textField_maxConCount.setColumns(10);
        textField_maxConCount.setBounds(589, 250, 119, 32);
        contentPanel.add(textField_maxConCount);

        nc5HomeConfigDialogUIListner = NC5HomeConfigDialogUIListener.build(this);

        //不允许编辑 数据源信息
        textField_maxConCount.setEditable(false);
        textField_ip.setEditable(false);
        textField_minConCout.setEditable(false);
        textField_user.setEditable(false);
       // textField_pass.setEditable(false);
        textField_oidmark.setEditable(false);
        textField_sid.setEditable(false);
        button_adddesign.setVisible(false);
    }
}
