package com.air.ideaplugin.ui;

import javax.swing.GroupLayout.Alignment;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

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
	protected JTabbedPane tabbedPane_rootUser;
	protected JPanel jPanel_rootUser_nc6;
	protected JPanel jPanel_rootUser_ncc;
	protected JPanel jPanel_rootUser_nc5x;
	protected JLabel lblactio;
	protected JTextField textField_nc5x_xmlpath;
	protected JButton btn_nc5x_selectRootXmlPath;
	protected JTable table_nc5x_rootuser;
	protected DefaultTableModel tableModel_nc5x_rootuser;
	protected DefaultTableModel tableModel_nc6x_rootuser;
	protected JLabel lblactio_1;
	protected JComboBox comboBox_nc5x_rootuserSelectzt;
	protected JButton btn_nc5x_rootuserSave;
	protected JTextArea textArea_nc5x_rootuserZtInfo;
	protected JButton btn_nc5x_rootuserAddLine;
	protected JButton btn_nc5x_rootuserDelLine;
	protected JLabel lblsuperadminxml;
	protected JTextField textField_nc6x_rootuserXmlPath;
	protected JButton btn_nc6x_selectRootXmlPath;
	protected JButton btn_nc6x_rootuserSave;
	protected JTable table_nc6x_rootuser;

	/**
	 * Create the panel.
	 */
	public ResetNCUserPassWordPanel() {

		createSmUserPanel();
		createRootUserPanel();

		initLayout();

		tableModel_nc5x_rootuser.addRow(new Object[] { "root", "1", true });
		tableModel_nc6x_rootuser.addRow(new Object[] { "root", "1", true });
	}

	private void createRootUserPanel() {
		this.tableModel_nc5x_rootuser = new DefaultTableModel();
		this.tableModel_nc5x_rootuser.addColumn("�û�����");
		this.tableModel_nc5x_rootuser.addColumn("��¼����");
		this.tableModel_nc5x_rootuser.addColumn("�Ƿ�����");

		this.tableModel_nc6x_rootuser = new DefaultTableModel();
		this.tableModel_nc6x_rootuser.addColumn("�û�����");
		this.tableModel_nc6x_rootuser.addColumn("��¼����");
		this.tableModel_nc6x_rootuser.addColumn("�Ƿ�����");

		panel_root = new JPanel();
		tabbedPane.addTab("Rootϵ���û�", null, panel_root, null);
		this.panel_root.setLayout(null);

		this.tabbedPane_rootUser = new JTabbedPane(JTabbedPane.TOP);
		this.tabbedPane_rootUser.setBounds(0, 0, 945, 531);
		this.tabbedPane_rootUser.setFont(new Font("����", Font.PLAIN, 18));
		this.panel_root.add(this.tabbedPane_rootUser);

		this.jPanel_rootUser_nc6 = new JPanel();
		this.tabbedPane_rootUser.addTab("NC6xϵ��", null, this.jPanel_rootUser_nc6, null);
		this.jPanel_rootUser_nc6.setLayout(null);

		this.lblsuperadminxml = new JLabel("\u9009\u62E9superadmin.xml\u6587\u4EF6:");
		this.lblsuperadminxml.setFont(new Font("����", Font.PLAIN, 14));
		this.lblsuperadminxml.setBounds(10, 10, 178, 38);
		this.jPanel_rootUser_nc6.add(this.lblsuperadminxml);

		this.textField_nc6x_rootuserXmlPath = new JTextField();
		this.textField_nc6x_rootuserXmlPath.setFont(new Font("����", Font.PLAIN, 14));
		this.textField_nc6x_rootuserXmlPath.setColumns(10);
		this.textField_nc6x_rootuserXmlPath.setBounds(184, 10, 643, 38);
		this.jPanel_rootUser_nc6.add(this.textField_nc6x_rootuserXmlPath);

		this.btn_nc6x_selectRootXmlPath = new JButton("\u6D4F\u89C8");
		this.btn_nc6x_selectRootXmlPath.setFont(new Font("����", Font.PLAIN, 14));
		this.btn_nc6x_selectRootXmlPath.setBounds(837, 10, 78, 38);
		this.jPanel_rootUser_nc6.add(this.btn_nc6x_selectRootXmlPath);

		this.table_nc6x_rootuser = new JTable(this.tableModel_nc6x_rootuser);
		JScrollPane jScrollPane_16 = new JScrollPane(table_nc6x_rootuser);
		jScrollPane_16.setBounds(10, 58, 908, 369);
		this.jPanel_rootUser_nc6.add(jScrollPane_16);

		this.btn_nc6x_rootuserSave = new JButton("\u4FDD\u5B58");
		this.btn_nc6x_rootuserSave.setForeground(Color.RED);
		this.btn_nc6x_rootuserSave.setFont(new Font("����", Font.BOLD, 16));
		this.btn_nc6x_rootuserSave.setBounds(824, 448, 106, 38);
		this.jPanel_rootUser_nc6.add(this.btn_nc6x_rootuserSave);

		this.jPanel_rootUser_ncc = new JPanel();
		this.tabbedPane_rootUser.addTab("NCCϵ��", null, this.jPanel_rootUser_ncc, null);
		this.jPanel_rootUser_ncc.setLayout(null);

		this.jPanel_rootUser_nc5x = new JPanel();
		this.tabbedPane_rootUser.addTab("NC5xϵ��", null, this.jPanel_rootUser_nc5x, null);
		this.jPanel_rootUser_nc5x.setLayout(null);

		this.lblactio = new JLabel("\u9009\u62E9account.xml\u6587\u4EF6:");
		this.lblactio.setFont(new Font("����", Font.PLAIN, 14));
		this.lblactio.setBounds(10, 10, 145, 38);
		this.jPanel_rootUser_nc5x.add(this.lblactio);

		this.textField_nc5x_xmlpath = new JTextField();
		this.textField_nc5x_xmlpath.setFont(new Font("����", Font.PLAIN, 14));
		this.textField_nc5x_xmlpath.setBounds(165, 10, 643, 38);
		this.jPanel_rootUser_nc5x.add(this.textField_nc5x_xmlpath);
		this.textField_nc5x_xmlpath.setColumns(10);

		this.btn_nc5x_selectRootXmlPath = new JButton("\u6D4F\u89C8");
		this.btn_nc5x_selectRootXmlPath.setFont(new Font("����", Font.PLAIN, 14));
		this.btn_nc5x_selectRootXmlPath.setBounds(824, 10, 106, 38);
		this.jPanel_rootUser_nc5x.add(this.btn_nc5x_selectRootXmlPath);
		this.table_nc5x_rootuser = new JTable(this.tableModel_nc5x_rootuser);
		JScrollPane jScrollPane_15 = new JScrollPane(table_nc5x_rootuser);
		jScrollPane_15.setBounds(10, 58, 920, 292);
		this.jPanel_rootUser_nc5x.add(jScrollPane_15);

		this.lblactio_1 = new JLabel("\u4E1A\u52A1\u8D26\u5957:");
		this.lblactio_1.setFont(new Font("����", Font.PLAIN, 14));
		this.lblactio_1.setBounds(10, 362, 72, 38);
		this.jPanel_rootUser_nc5x.add(this.lblactio_1);

		this.comboBox_nc5x_rootuserSelectzt = new JComboBox();
		this.comboBox_nc5x_rootuserSelectzt.setBounds(82, 364, 275, 34);
		this.jPanel_rootUser_nc5x.add(this.comboBox_nc5x_rootuserSelectzt);

		this.btn_nc5x_rootuserSave = new JButton("\u4FDD\u5B58");
		this.btn_nc5x_rootuserSave.setForeground(Color.RED);
		this.btn_nc5x_rootuserSave.setFont(new Font("����", Font.BOLD, 16));
		this.btn_nc5x_rootuserSave.setBounds(824, 448, 106, 38);
		this.jPanel_rootUser_nc5x.add(this.btn_nc5x_rootuserSave);

		this.textArea_nc5x_rootuserZtInfo = new JTextArea();
		this.textArea_nc5x_rootuserZtInfo.setBounds(10, 404, 800, 82);
		this.jPanel_rootUser_nc5x.add(this.textArea_nc5x_rootuserZtInfo);

		this.btn_nc5x_rootuserAddLine = new JButton("\u589E\u884C");
		this.btn_nc5x_rootuserAddLine.setFont(new Font("����", Font.PLAIN, 15));
		this.btn_nc5x_rootuserAddLine.setBounds(660, 360, 113, 34);
		this.jPanel_rootUser_nc5x.add(this.btn_nc5x_rootuserAddLine);

		this.btn_nc5x_rootuserDelLine = new JButton("\u5220\u9664\u9009\u4E2D\u884C");
		this.btn_nc5x_rootuserDelLine.setFont(new Font("����", Font.PLAIN, 15));
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

	private void createSmUserPanel() {
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		this.tabbedPane.setFont(new Font("����", Font.PLAIN, 18));

		panel_smUser = new JPanel();
		tabbedPane.addTab("����Աsm_user����", null, panel_smUser, null);
		panel_smUser.setLayout(null);

		comboBox_dataSource = new JComboBox();
		comboBox_dataSource.setBounds(100, 10, 325, 34);
		panel_smUser.add(comboBox_dataSource);

		JLabel label_1 = new JLabel("ѡ������Դ:");
		label_1.setFont(new Font("Dialog", Font.PLAIN, 15));
		label_1.setBounds(10, 10, 92, 34);
		panel_smUser.add(label_1);

		JLabel lblsql = new JLabel("�û���ѯSQL:");
		lblsql.setFont(new Font("Dialog", Font.PLAIN, 15));
		lblsql.setBounds(10, 54, 104, 34);
		panel_smUser.add(lblsql);

		textArea_queryUserSql = new JTextArea();
		textArea_queryUserSql.setBounds(124, 54, 811, 195);
		panel_smUser.add(textArea_queryUserSql);

		btn_loadUserList = new JButton("�����û��б�");
		this.btn_loadUserList.setFont(new Font("����", Font.PLAIN, 16));
		btn_loadUserList.setBounds(469, 8, 158, 39);
		panel_smUser.add(btn_loadUserList);

		JLabel label = new JLabel("ѡ���û�:");
		label.setFont(new Font("Dialog", Font.PLAIN, 15));
		label.setBounds(10, 275, 92, 34);
		panel_smUser.add(label);

		list_selectUsers = new JList(listModel_selectUsers);
		list_selectUsers.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		list_selectUsers.setVisibleRowCount(5);
		list_selectUsers.setToolTipText("˫������ѡ�����ı�");
		JScrollPane jScrollPane_11 = new JScrollPane();
		jScrollPane_11.setBounds(100, 268, 581, 120);
		jScrollPane_11.setViewportView(list_selectUsers);
		panel_smUser.add(jScrollPane_11);

		this.label_2 = new JLabel("����������:");
		this.label_2.setFont(new Font("Dialog", Font.PLAIN, 15));
		this.label_2.setBounds(10, 424, 92, 34);
		this.panel_smUser.add(this.label_2);

		this.textInput_newPass = new JTextField();
		this.textInput_newPass.setForeground(Color.RED);
		this.textInput_newPass.setFont(new Font("����", Font.BOLD, 28));
		this.textInput_newPass.setText("123");
		this.textInput_newPass.setBounds(100, 407, 835, 51);
		this.panel_smUser.add(this.textInput_newPass);
		this.textInput_newPass.setColumns(10);

		this.btn_executeOk = new JButton("���浽���ݿ�");
		this.btn_executeOk.setToolTipText("����� ֱ��ִ��update���д�����ݿ�!");
		this.btn_executeOk.setBackground(Color.WHITE);
		this.btn_executeOk.setFont(new Font("����", Font.BOLD, 18));
		this.btn_executeOk.setForeground(Color.RED);
		this.btn_executeOk.setBounds(717, 486, 164, 51);
		this.panel_smUser.add(this.btn_executeOk);

		this.btn_copyExecuteSqls = new JButton("����SQL���");
		this.btn_copyExecuteSqls.setToolTipText("����� ����update��䵽���а�!");
		this.btn_copyExecuteSqls.setForeground(Color.BLUE);
		this.btn_copyExecuteSqls.setFont(new Font("����", Font.BOLD, 18));
		this.btn_copyExecuteSqls.setBackground(Color.WHITE);
		this.btn_copyExecuteSqls.setBounds(482, 486, 164, 51);
		this.panel_smUser.add(this.btn_copyExecuteSqls);

		this.label_3 = new JLabel("����");
		this.label_3.setFont(new Font("����", Font.BOLD, 16));
		this.label_3.setBounds(656, 486, 40, 51);
		this.panel_smUser.add(this.label_3);

		this.txtpnSql = new JTextPane();
		this.txtpnSql.setFont(new Font("����", Font.PLAIN, 13));
		this.txtpnSql.setEditable(false);
		this.txtpnSql.setText("ע��:\r\n" + "�ֶα����б�����:\r\n" + "�û�id as id������ as pass���û����� as code���û����� as name");
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

	/**
	 *
	 * @author Administrator
	 *
	 */
	class TestTableCellRenderer extends JCheckBox implements TableCellRenderer {
		private static final long serialVersionUID = 1L;

		TestTableCellRenderer() {
		}

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			setBackground(Color.white);
			setHorizontalAlignment(0);

			if (value != null) {
				Boolean b = (Boolean) value;
				setSelected(b.booleanValue());
			}

			return this;
		}
	}
}
