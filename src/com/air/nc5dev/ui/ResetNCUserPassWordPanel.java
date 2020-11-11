package com.air.nc5dev.ui;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import java.awt.*;

/**
 * 重置NC操作员密码的 panel <br/>
 * <br/>
 * <br/>
 *
 * @author 唐粟 Email:209308343@qq.com
 * @date 2020/11/9 11:14
 * @project
 * @Version
 */
public class ResetNCUserPassWordPanel extends JPanel {
    protected JTabbedPane tabbedPane;
    protected JPanel panel_smUser;
    protected JComboBox comboBox_dataSource;
    protected JTextArea textArea_queryUserSql;
    protected JButton btn_loadUserList;
    protected JPanel panel_root;
    protected JList list_selectUsers;
    protected DefaultListModel listModel_selectUsers = new DefaultListModel();
    protected JLabel label_2;
    protected JTextField textInput_newPass;
    protected JButton btn_executeOk;
    protected JButton btn_copyExecuteSqls;
    protected JLabel label_3;
    protected JTextPane txtpnSql;

    public ResetNCUserPassWordPanel() {
        createSmUserPanel();
    }

    /**
     * 创建SM User表 panel
     */
    private void createSmUserPanel() {
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        this.tabbedPane.setFont(new Font("宋体", Font.PLAIN, 18));
        GroupLayout groupLayout = new GroupLayout(this);
        groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(tabbedPane,
                Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 773, Short.MAX_VALUE));
        groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(tabbedPane,
                Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 398, Short.MAX_VALUE));

        panel_smUser = new JPanel();
        tabbedPane.addTab("操作员sm_user密码", null, panel_smUser, null);
        panel_smUser.setLayout(null);

        comboBox_dataSource = new JComboBox();
        comboBox_dataSource.setBounds(100, 10, 325, 34);
        panel_smUser.add(comboBox_dataSource);

        JLabel label_1 = new JLabel("选择数据源:");
        label_1.setFont(new Font("Dialog", Font.PLAIN, 15));
        label_1.setBounds(10, 10, 92, 34);
        panel_smUser.add(label_1);

        JLabel lblsql = new JLabel("用户查询SQL:");
        lblsql.setFont(new Font("Dialog", Font.PLAIN, 15));
        lblsql.setBounds(10, 54, 104, 34);
        panel_smUser.add(lblsql);

        textArea_queryUserSql = new JTextArea();
        textArea_queryUserSql.setBounds(124, 54, 811, 195);
        panel_smUser.add(textArea_queryUserSql);

        btn_loadUserList = new JButton("加载用户列表");
        this.btn_loadUserList.setFont(new Font("宋体", Font.PLAIN, 16));
        btn_loadUserList.setBounds(469, 8, 158, 39);
        panel_smUser.add(btn_loadUserList);

        JLabel label = new JLabel("选择用户:");
        label.setFont(new Font("Dialog", Font.PLAIN, 15));
        label.setBounds(10, 275, 92, 34);
        panel_smUser.add(label);

        list_selectUsers = new JList(listModel_selectUsers);
        list_selectUsers.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        list_selectUsers.setVisibleRowCount(5);
        list_selectUsers.setToolTipText("双击复制选中行文本");
        JScrollPane jScrollPane_11 = new JScrollPane();
        jScrollPane_11.setBounds(100, 268, 581, 120);
        jScrollPane_11.setViewportView(list_selectUsers);
        panel_smUser.add(jScrollPane_11);

        this.label_2 = new JLabel("输入新密码:");
        this.label_2.setFont(new Font("Dialog", Font.PLAIN, 15));
        this.label_2.setBounds(10, 424, 92, 34);
        this.panel_smUser.add(this.label_2);

        this.textInput_newPass = new JTextField();
        this.textInput_newPass.setForeground(Color.RED);
        this.textInput_newPass.setFont(new Font("宋体", Font.BOLD, 28));
        this.textInput_newPass.setText("123");
        this.textInput_newPass.setBounds(100, 407, 835, 51);
        this.panel_smUser.add(this.textInput_newPass);
        this.textInput_newPass.setColumns(10);

        this.btn_executeOk = new JButton("保存到数据库");
        this.btn_executeOk.setToolTipText("点击后 直接执行update语句写入数据库!");
        this.btn_executeOk.setBackground(Color.WHITE);
        this.btn_executeOk.setFont(new Font("宋体", Font.BOLD, 18));
        this.btn_executeOk.setForeground(Color.RED);
        this.btn_executeOk.setBounds(717, 486, 164, 51);
        this.panel_smUser.add(this.btn_executeOk);

        this.btn_copyExecuteSqls = new JButton("复制SQL语句");
        this.btn_copyExecuteSqls.setToolTipText("点击后 复制update语句到剪切板!");
        this.btn_copyExecuteSqls.setForeground(Color.BLUE);
        this.btn_copyExecuteSqls.setFont(new Font("宋体", Font.BOLD, 18));
        this.btn_copyExecuteSqls.setBackground(Color.WHITE);
        this.btn_copyExecuteSqls.setBounds(482, 486, 164, 51);
        this.panel_smUser.add(this.btn_copyExecuteSqls);

        this.label_3 = new JLabel("或者");
        this.label_3.setFont(new Font("宋体", Font.BOLD, 16));
        this.label_3.setBounds(656, 486, 40, 51);
        this.panel_smUser.add(this.label_3);

        this.txtpnSql = new JTextPane();
        this.txtpnSql.setFont(new Font("宋体", Font.PLAIN, 13));
        this.txtpnSql.setEditable(false);
        this.txtpnSql.setText("注意:\r\n" + "字段必须有别名列:\r\n" + "用户id as id，密码 as pass，用户编码 as code，用户名称 as name");
        this.txtpnSql.setBounds(10, 81, 80, 181);
        this.panel_smUser.add(this.txtpnSql);

        panel_root = new JPanel();
        tabbedPane.addTab("Root系列用户", null, panel_root, null);
        panel_root.setLayout(null);
        setLayout(groupLayout);
    }
}
