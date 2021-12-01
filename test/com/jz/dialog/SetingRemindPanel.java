package com.jz.dialog;

import javax.swing.*;

public class SetingRemindPanel extends JPanel {
	public JTextArea textArea_title;
	public JComboBox comboBox_type;
	public JLabel lblNewLabel;
	public JLabel lblNewLabel_1;
	public JComboBox comboBox_timeUnit;
	public JLabel lblNewLabel_2;
	public JTextField textField_time;
	public JLabel lblNewLabel_3;
	public JTextArea textArea_msg;
	public JLabel lblNewLabel_4;

	/**
	 * Create the panel.
	 */
	public SetingRemindPanel() {

		initComponents();
	}
	private void initComponents() {
		setLayout(null);
		
		this.textArea_title = new JTextArea();
		this.textArea_title.setText("请输入提醒标题");
		this.textArea_title.setBounds(10, 81, 463, 130);
		add(this.textArea_title);
		
		this.comboBox_type = new JComboBox();
		this.comboBox_type.setModel(new DefaultComboBoxModel(new String[] {"每隔多少时间", "指定某个时间"}));
		this.comboBox_type.setSelectedIndex(0);
		this.comboBox_type.setBounds(74, 10, 122, 23);
		add(this.comboBox_type);
		
		this.lblNewLabel = new JLabel("提醒类型:");
		this.lblNewLabel.setBounds(10, 14, 54, 15);
		add(this.lblNewLabel);
		
		this.lblNewLabel_1 = new JLabel("循环时间单位:");
		this.lblNewLabel_1.setBounds(217, 14, 78, 15);
		add(this.lblNewLabel_1);
		
		this.comboBox_timeUnit = new JComboBox();
		this.comboBox_timeUnit.setModel(new DefaultComboBoxModel(new String[] {"秒", "分钟", "小时", "天", "周", "月", "季度", "年"}));
		this.comboBox_timeUnit.setSelectedIndex(1);
		this.comboBox_timeUnit.setBounds(305, 10, 54, 23);
		add(this.comboBox_timeUnit);
		
		this.lblNewLabel_2 = new JLabel("循环时间长度:");
		this.lblNewLabel_2.setBounds(375, 14, 78, 15);
		add(this.lblNewLabel_2);
		
		this.textField_time = new JTextField();
		this.textField_time.setText("30");
		this.textField_time.setBounds(463, 11, 160, 21);
		add(this.textField_time);
		this.textField_time.setColumns(10);
		
		this.lblNewLabel_3 = new JLabel("提醒标题:");
		this.lblNewLabel_3.setBounds(10, 56, 54, 15);
		add(this.lblNewLabel_3);
		
		this.textArea_msg = new JTextArea();
		this.textArea_msg.setText("请输入提醒内容");
		this.textArea_msg.setBounds(10, 246, 463, 130);
		add(this.textArea_msg);
		
		this.lblNewLabel_4 = new JLabel("提醒内容:");
		this.lblNewLabel_4.setBounds(10, 221, 54, 15);
		add(this.lblNewLabel_4);
	}
}
