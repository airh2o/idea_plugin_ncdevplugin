package com.air.ideaplugin.ui.test;

import com.air.ideaplugin.ui.ResetNCUserPassWordPanel;

public class ResetNCUserPassWordPanelTest {
	public static void main(String[] args) {
		JWindow w = new JWindow();
		w.setBounds(400, 500, 1000, 600);
		ResetNCUserPassWordPanel m = new ResetNCUserPassWordPanel();
		w.add(m);
		m.setVisible(true);
		w.setVisible(true);
	}
}
