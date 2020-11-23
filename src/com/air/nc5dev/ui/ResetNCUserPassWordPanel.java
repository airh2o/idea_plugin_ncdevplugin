package com.air.nc5dev.ui;

import com.air.nc5dev.ui.listener.*;
import com.air.nc5dev.util.ProjectNCConfigUtil;
import com.air.nc5dev.util.idea.LogUtil;
import com.air.nc5dev.util.ncutils.nc5x.NC5xEncode;
import nc.bcmanage.vo.SuperAdminVO;
import nc.hb.account.AccountXMLUtil;
import nc.vo.sm.config.Account;
import nc.vo.sm.config.AccountAdm;
import nc.vo.sm.config.ConfigParameter;
import nc.vo.sm.config.SysAdm;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

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
    public JTabbedPane tabbedPane;
    public JPanel panel_smUser;
    public JComboBox comboBox_dataSource;
    public JTextArea textArea_queryUserSql;
    public JButton btn_loadUserList;
    public JPanel panel_root;
    public JList list_selectUsers;
    public DefaultListModel listModel_selectUsers = new DefaultListModel();
    public JLabel label_2;
    public JTextField textInput_newPass;
    public JButton btn_executeOk;
    public JButton btn_copyExecuteSqls;
    public JLabel label_3;
    public JTextPane txtpnSql;
    public JTabbedPane tabbedPane_rootUser;
    public JPanel jPanel_rootUser_nc6;
    public JPanel jPanel_rootUser_ncc;
    public JPanel jPanel_rootUser_nc5x;
    public JLabel lblactio;
    public JTextField textField_nc5x_xmlpath;
    public JButton btn_nc5x_selectRootXmlPath;
    public JTable table_nc5x_rootuser;
    public DefaultTableModel tableModel_nc5x_rootuser;
    public DefaultTableModel tableModel_nc6x_rootuser;
    public JLabel lblactio_1;
    public JComboBox comboBox_nc5x_rootuserSelectzt;
    public JButton btn_nc5x_rootuserSave;
    public JTextArea textArea_nc5x_rootuserZtInfo;
    public JButton btn_nc5x_rootuserAddLine;
    public JButton btn_nc5x_rootuserDelLine;
    public JLabel lblsuperadminxml;
    public JTextField textField_nc6x_rootuserXmlPath;
    public JButton btn_nc6x_selectRootXmlPath;
    public JButton btn_nc6x_rootuserSave;
    public JTable table_nc6x_rootuser;
    public ConfigParameter config5x;
    public ArrayList<SuperAdminVO> nc6xAdminVOList;


    public ResetNCUserPassWordPanel() {
        createSmUserPanel();

        createRootUserPanel();

        initLayout();

        addRootUserElementListeners();

        initUIValues();
    }

    private void initUIValues() {
        File xml = new File(ProjectNCConfigUtil.getNCHome(), "ierp"
                + File.separatorChar + "bin" + File.separatorChar + "account.xml");
        if (xml.isFile()) {
            textField_nc5x_xmlpath.setText(xml.getAbsolutePath());
        }

        xml = new File(ProjectNCConfigUtil.getNCHome(), "ierp"
                + File.separatorChar + "sf" + File.separatorChar + "superadmin.xml");
        if (xml.isFile()) {
            textField_nc6x_rootuserXmlPath.setText(xml.getAbsolutePath());
        }

        reloadRootXml();
    }

    /**
     * 重载 xml配置信息
     */
    public void reloadRootXml() {
        reloadNc5RootXml();
        reloadNc6RootXml();
    }

    public void reloadNc5RootXml() {
        try {
            File xml = new File(textField_nc5x_xmlpath.getText());
            if (!xml.isFile()) {
                return;
            }

            while (tableModel_nc5x_rootuser.getRowCount() > 0) {
                tableModel_nc5x_rootuser.removeRow(0);
            }
            textArea_nc5x_rootuserZtInfo.setText("");
            comboBox_nc5x_rootuserSelectzt.removeAllItems();

            config5x = AccountXMLUtil.getConfigParameter(xml.getAbsolutePath());
            SysAdm[] adms = config5x.getArySysAdms();

            for (int i = 0; i < adms.length; i++) {
                tableModel_nc5x_rootuser.addRow(new Object[]{adms[i].getSysAdmCode()
                        , adms[i].getPassword(), Boolean.valueOf(adms[i]
                        .isLocked())});
            }

            Account[] accounts = config5x.getAryAccounts();
            int selectedIndex = 0;
            for (int i = 0; i < accounts.length; i++) {
                if (!"0000".equals(accounts[i].getAccountCode())) {
                    comboBox_nc5x_rootuserSelectzt.addItem(accounts[i].getAccountName());
                    selectedIndex = i;
                }
            }
            if (comboBox_nc5x_rootuserSelectzt.getItemCount() >= 1) {
                comboBox_nc5x_rootuserSelectzt.setSelectedItem(accounts[selectedIndex].getAccountName());
            }

            textArea_nc5x_rootuserZtInfo.setText(getAccountInfo(comboBox_nc5x_rootuserSelectzt.getSelectedIndex()));
        } catch (Throwable e) {
            LogUtil.error(e.toString(), e);
        }
    }

    public void reloadNc6RootXml() {
        try {
            File xml = new File(textField_nc6x_rootuserXmlPath.getText());
            if (!xml.isFile()) {
                return;
            }

            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            String xmlContent = getXMLContentFromFile(xml.getAbsolutePath());
            File tempFile = generateTempFile(xmlContent);

            Document doc = builder.parse(tempFile);
            XPath xpath = XPathFactory.newInstance().newXPath();
            NodeList nodeList = (NodeList) xpath.evaluate("/admins/admin", doc, XPathConstants.NODESET);
            nc6xAdminVOList = new ArrayList<>(nodeList.getLength());
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                String admincode = xpath.evaluate("code", node);
                String adminname = xpath.evaluate("name", node);
                String password = xpath.evaluate("password", node);
                String pwdinuse = xpath.evaluate("pwdinuse", node);
                String pwdlvl = xpath.evaluate("pwdlvl", node);
                String identify = xpath.evaluate("identify", node);
                String isLocked = xpath.evaluate("isLocked", node);
                String oldpwd1 = xpath.evaluate("oldpwd1", node);
                String oldpwd2 = xpath.evaluate("oldpwd2", node);
                String oldpwd3 = xpath.evaluate("oldpwd3", node);

                SuperAdminVO admin = new SuperAdminVO();
                admin.setAdmCode(admincode == null ? "" : admincode);
                admin.setAdmName(adminname == null ? "" : adminname);
                admin.setPassword(password == null ? "" : password);
                admin.setPwdinuse(pwdinuse);
                admin.setPwdlvl(pwdlvl);
                admin.setIdentify(identify);
                admin.setIsLocked(isLocked == null ? false : isLocked.equalsIgnoreCase("Y"));
                admin.setOldpwd1(oldpwd1);
                admin.setOldpwd2(oldpwd2);
                admin.setOldpwd3(oldpwd3);
                nc6xAdminVOList.add(admin);
            }


            while (tableModel_nc6x_rootuser.getRowCount() > 0) {
                tableModel_nc6x_rootuser.removeRow(0);
            }

            int size = nc6xAdminVOList.size();
            SuperAdminVO[] vos = new SuperAdminVO[size];
            for (int i = 0; i < size; i++) {
                vos[i] = nc6xAdminVOList.get(i).clone();
                tableModel_nc6x_rootuser.addRow(new Object[]{vos[i].getAdmCode()
                        , ""
                        , Boolean.valueOf(vos[i].getIsLocked())});
            }
        } catch (Throwable e) {
            LogUtil.error(e.toString(), e);
        }
    }

    private void createRootUserPanel() {
        this.tableModel_nc5x_rootuser = new DefaultTableModel();
        this.tableModel_nc5x_rootuser.addColumn("用户编码");
        this.tableModel_nc5x_rootuser.addColumn("登录密码");
        this.tableModel_nc5x_rootuser.addColumn("是否被锁定");

        this.tableModel_nc6x_rootuser = new DefaultTableModel();
        this.tableModel_nc6x_rootuser.addColumn("用户编码");
        this.tableModel_nc6x_rootuser.addColumn("登录密码");
        this.tableModel_nc6x_rootuser.addColumn("是否被锁定");

        panel_root = new JPanel();
        tabbedPane.addTab("Root系列用户", null, panel_root, null);
        this.panel_root.setLayout(null);

        this.tabbedPane_rootUser = new JTabbedPane(JTabbedPane.TOP);
        this.tabbedPane_rootUser.setBounds(0, 0, 945, 531);
        this.tabbedPane_rootUser.setFont(new Font("宋体", Font.PLAIN, 18));
        this.panel_root.add(this.tabbedPane_rootUser);

        this.jPanel_rootUser_nc6 = new JPanel();
        this.tabbedPane_rootUser.addTab("NC6x系列", null, this.jPanel_rootUser_nc6, null);
        this.jPanel_rootUser_nc6.setLayout(null);

        this.lblsuperadminxml = new JLabel("\u9009\u62E9superadmin.xml\u6587\u4EF6:");
        this.lblsuperadminxml.setFont(new Font("宋体", Font.PLAIN, 14));
        this.lblsuperadminxml.setBounds(10, 10, 178, 38);
        this.jPanel_rootUser_nc6.add(this.lblsuperadminxml);

        this.textField_nc6x_rootuserXmlPath = new JTextField();
        this.textField_nc6x_rootuserXmlPath.setFont(new Font("宋体", Font.PLAIN, 14));
        this.textField_nc6x_rootuserXmlPath.setColumns(10);
        this.textField_nc6x_rootuserXmlPath.setBounds(184, 10, 643, 38);
        this.jPanel_rootUser_nc6.add(this.textField_nc6x_rootuserXmlPath);

        this.btn_nc6x_selectRootXmlPath = new JButton("\u6D4F\u89C8");
        this.btn_nc6x_selectRootXmlPath.setFont(new Font("宋体", Font.PLAIN, 14));
        this.btn_nc6x_selectRootXmlPath.setBounds(837, 10, 78, 38);
        this.jPanel_rootUser_nc6.add(this.btn_nc6x_selectRootXmlPath);

        this.table_nc6x_rootuser = new JTable(this.tableModel_nc6x_rootuser);
        JScrollPane jScrollPane_16 = new JScrollPane(table_nc6x_rootuser);
        jScrollPane_16.setBounds(10, 58, 908, 369);
        this.jPanel_rootUser_nc6.add(jScrollPane_16);

        this.btn_nc6x_rootuserSave = new JButton("\u4FDD\u5B58");
        this.btn_nc6x_rootuserSave.setForeground(Color.RED);
        this.btn_nc6x_rootuserSave.setFont(new Font("宋体", Font.BOLD, 16));
        this.btn_nc6x_rootuserSave.setBounds(824, 448, 106, 38);
        this.jPanel_rootUser_nc6.add(this.btn_nc6x_rootuserSave);

        this.jPanel_rootUser_ncc = new JPanel();
        this.tabbedPane_rootUser.addTab("NCC系列", null, this.jPanel_rootUser_ncc, null);
        this.jPanel_rootUser_ncc.setLayout(null);

        this.jPanel_rootUser_nc5x = new JPanel();
        this.tabbedPane_rootUser.addTab("NC5x系列", null, this.jPanel_rootUser_nc5x, null);
        this.jPanel_rootUser_nc5x.setLayout(null);

        this.lblactio = new JLabel("\u9009\u62E9account.xml\u6587\u4EF6:");
        this.lblactio.setFont(new Font("宋体", Font.PLAIN, 14));
        this.lblactio.setBounds(10, 10, 145, 38);
        this.jPanel_rootUser_nc5x.add(this.lblactio);

        this.textField_nc5x_xmlpath = new JTextField();
        this.textField_nc5x_xmlpath.setFont(new Font("宋体", Font.PLAIN, 14));
        this.textField_nc5x_xmlpath.setBounds(165, 10, 643, 38);
        this.jPanel_rootUser_nc5x.add(this.textField_nc5x_xmlpath);
        this.textField_nc5x_xmlpath.setColumns(10);

        this.btn_nc5x_selectRootXmlPath = new JButton("\u6D4F\u89C8");
        this.btn_nc5x_selectRootXmlPath.setFont(new Font("宋体", Font.PLAIN, 14));
        this.btn_nc5x_selectRootXmlPath.setBounds(824, 10, 106, 38);
        this.jPanel_rootUser_nc5x.add(this.btn_nc5x_selectRootXmlPath);
        this.table_nc5x_rootuser = new JTable(this.tableModel_nc5x_rootuser);
        JScrollPane jScrollPane_15 = new JScrollPane(table_nc5x_rootuser);
        jScrollPane_15.setBounds(10, 58, 920, 292);
        this.jPanel_rootUser_nc5x.add(jScrollPane_15);

        this.lblactio_1 = new JLabel("\u4E1A\u52A1\u8D26\u5957:");
        this.lblactio_1.setFont(new Font("宋体", Font.PLAIN, 14));
        this.lblactio_1.setBounds(10, 362, 72, 38);
        this.jPanel_rootUser_nc5x.add(this.lblactio_1);

        this.comboBox_nc5x_rootuserSelectzt = new JComboBox();
        this.comboBox_nc5x_rootuserSelectzt.setBounds(82, 364, 275, 34);
        this.jPanel_rootUser_nc5x.add(this.comboBox_nc5x_rootuserSelectzt);

        this.btn_nc5x_rootuserSave = new JButton("\u4FDD\u5B58");
        this.btn_nc5x_rootuserSave.setForeground(Color.RED);
        this.btn_nc5x_rootuserSave.setFont(new Font("宋体", Font.BOLD, 16));
        this.btn_nc5x_rootuserSave.setBounds(824, 448, 106, 38);
        this.jPanel_rootUser_nc5x.add(this.btn_nc5x_rootuserSave);

        this.textArea_nc5x_rootuserZtInfo = new JTextArea();
        JScrollPane jScrollPane_17 = new JScrollPane(this.textArea_nc5x_rootuserZtInfo);
        jScrollPane_17.setBounds(10, 404, 800, 82);
        this.jPanel_rootUser_nc5x.add(jScrollPane_17);

        this.btn_nc5x_rootuserAddLine = new JButton("\u589E\u884C");
        this.btn_nc5x_rootuserAddLine.setFont(new Font("宋体", Font.PLAIN, 15));
        this.btn_nc5x_rootuserAddLine.setBounds(660, 360, 113, 34);
        this.jPanel_rootUser_nc5x.add(this.btn_nc5x_rootuserAddLine);

        this.btn_nc5x_rootuserDelLine = new JButton("\u5220\u9664\u9009\u4E2D\u884C");
        this.btn_nc5x_rootuserDelLine.setFont(new Font("宋体", Font.PLAIN, 15));
        this.btn_nc5x_rootuserDelLine.setBounds(783, 360, 113, 34);
        this.jPanel_rootUser_nc5x.add(this.btn_nc5x_rootuserDelLine);

        TableColumnModel tcm = table_nc5x_rootuser.getColumnModel();
        tcm.getColumn(2).setCellEditor(new DefaultCellEditor(new JCheckBox()));
        tcm.getColumn(2).setCellRenderer(new TestTableCellRenderer());
        tcm.getColumn(2).setPreferredWidth(80);
        tcm.getColumn(2).setWidth(80);
        tcm.getColumn(2).setMaxWidth(80);

        tcm = table_nc6x_rootuser.getColumnModel();
        tcm.getColumn(2).setCellEditor(new DefaultCellEditor(new JCheckBox()));
        tcm.getColumn(2).setCellRenderer(new TestTableCellRenderer());
        tcm.getColumn(2).setPreferredWidth(80);
        tcm.getColumn(2).setWidth(80);
        tcm.getColumn(2).setMaxWidth(80);
    }

    /**
     * 创建SM User表 panel
     */
    private void createSmUserPanel() {
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        this.tabbedPane.setFont(new Font("宋体", Font.PLAIN, 18));

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
    }

    private void initLayout() {
        GroupLayout groupLayout = new GroupLayout(this);
        groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(tabbedPane,
                Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 773, Short.MAX_VALUE));
        groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addComponent(tabbedPane,
                Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 398, Short.MAX_VALUE));
        setLayout(groupLayout);
    }

    private void addRootUserElementListeners() {
        btn_nc5x_selectRootXmlPath.addActionListener(new Nc5xSelectRootXmlPathListener(this));
        btn_nc5x_rootuserAddLine.addActionListener(new Nc5xRootUserAddLineListener(this));
        btn_nc5x_rootuserDelLine.addActionListener(new Nc5xRootUserDelLineListener(this));
        btn_nc5x_rootuserSave.addActionListener(new Nc5xRootUserSaveListener(this));
        comboBox_nc5x_rootuserSelectzt.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                textArea_nc5x_rootuserZtInfo.setText(getAccountInfo(comboBox_nc5x_rootuserSelectzt.getSelectedIndex()));
            }
        });

        btn_nc6x_selectRootXmlPath.addActionListener(new Nc6xSelectRootXmlPathListener(this));
        btn_nc6x_rootuserSave.addActionListener(new Nc6xRootUserSaveListener(this));
    }

    private String getAccountInfo(int index) {
        if (config5x == null) {
            return "请选择xml";
        }

        Account[] accounts = config5x.getAryAccounts();

        if (index < 0 || index > accounts.length) {
            return index + " 索引不处在 账套列表个数中: " + accounts.length;
        }

        StringBuffer infotemp = new StringBuffer();
        infotemp.append("账套编码：");
        infotemp.append(accounts[index].getAccountCode() + "\n");
        infotemp.append("账套名称：");
        infotemp.append(accounts[index].getAccountName() + "\n");
        infotemp.append("数据源：");
        infotemp.append(accounts[index].getDataSourceName() + "\n");

        infotemp.append("生效日期：");
        infotemp.append(accounts[index].getEffectDate() + "    ");
        infotemp.append("失效日期：");
        infotemp.append(accounts[index].getExpireDate() + "\n");

        AccountAdm[] adms = accounts[index].getAryAccountAdms();

        if ((adms != null) && (adms.length > 0)) {
            for (int i = 0; i < adms.length; i++) {
                infotemp.append("管理员编码：");
                infotemp.append(adms[i].getAccountAdmCode() + "    ");
                infotemp.append("管理员名称：");
                infotemp.append(adms[i].getAccountAdmName() + "    ");
                infotemp.append("登录密码：");
                infotemp.append(adms[i].getPassword() + "    ");
                infotemp.append("是否被锁定：");
                infotemp.append(adms[i].isLocked() ? "是" : "否");
                infotemp.append("\n");
            }
        }

        return infotemp.toString();
    }

    private String getXMLContentFromFile(String path) throws Exception {
        StringBuffer sb = new StringBuffer();
        File configFile = new File(path);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(configFile);
            byte[] b = new byte[1024];
            for (; ; ) {
                int i = fis.read(b);
                if (i == -1)
                    break;
                sb.append(new String(b, 0, i));
            }
        } catch (Exception e) {
            throw new Exception(e);
        } finally {
            if (fis != null)
                fis.close();
        }
        String content = sb.toString();

        if (content.startsWith("<?xml")) {
            return content;
        }
        return new NC5xEncode().decode(content);
    }

    private File generateTempFile(String xmlContent) throws Exception {
        File directory = new File(System.getProperty("java.io.tmpdir"), "ideancplugin" + File.separatorChar + "temp");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File tempFile = File.createTempFile("superadm", "file", directory);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(tempFile);
            fos.write(xmlContent.getBytes());
        } catch (Exception e) {
            throw new Exception(e);
        } finally {
            fos.close();
        }
        return tempFile;
    }

    /**
     * @author Administrator
     */
    class TestTableCellRenderer extends JCheckBox implements TableCellRenderer {
        private static final long serialVersionUID = 1L;

        TestTableCellRenderer() {
        }

        public Component getTableCellRendererComponent(JTable table, Object value
                , boolean isSelected, boolean hasFocus, int row, int column) {
            setHorizontalAlignment(0);
            if (value != null) {
                Boolean b = (Boolean) value;
                setSelected(b.booleanValue());
            }

            return this;
        }
    }
}
