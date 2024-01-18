//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.jz.dialog;

import com.air.nc5dev.util.subscribe.itf.ISubscriber;
import com.jz.base.TreeData;
import com.jz.helper.NodeScriptDiffHelper;
import com.jz.tools.Util;
import com.jz.tree.CheckBoxTreeTool;
import com.jz.xmlopt.XmlCtrl;
import nc.ui.trade.pub.IVOTreeDataByCode;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Iterator;
import java.util.List;

public class ExportDlg extends JDialog {
    private static final long serialVersionUID = 1L;
    private final JPanel contentPanel = new JPanel();
    CheckBoxTreeTool treeTool = null;
    private JButton cancelButton;
    private JButton okButton;
    private ISubscriber<ExportDlg> onOk;
    private ISubscriber<ExportDlg> onCancel;

    public static void main(String[] args) {
        try {
            ExportDlg dialog = new ExportDlg();
            dialog.setDefaultCloseOperation(2);
            dialog.setVisible(true);
        } catch (Exception var2) {
            var2.printStackTrace();
        }

    }

    public ExportDlg() {
        this.setTitle("导出节点相关SQL文");
        this.setBounds(100, 100, 589, 435);
        this.getContentPane().setLayout(new BorderLayout());
        this.contentPanel.setLayout(new FlowLayout());
        this.contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        this.getContentPane().add(this.contentPanel, "West");
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(2));
        this.getContentPane().add(panel, "South");
        okButton = new JButton("确定");
        okButton.setActionCommand("OK");
        okButton.addActionListener(new ExportDlg.BtnListener(41));
        panel.add(okButton);
        this.getRootPane().setDefaultButton(cancelButton);
        cancelButton = new JButton("取消");
        cancelButton.setActionCommand("Cancel");
        cancelButton.addActionListener(new ExportDlg.BtnListener(42));
        panel.add(cancelButton);
        panel = new JPanel();
        this.getContentPane().add(panel, "East");
        JScrollPane scrollPane = new JScrollPane();
        this.getContentPane().add(scrollPane, "Center");
        IVOTreeDataByCode data = new TreeData();
        CheckBoxTreeTool cbTree = this.getCheckBoxTreeTool(data);
        cbTree.setCheckBoxTreeModal();
        scrollPane.setViewportView(cbTree.getCheckTree());
    }

    public CheckBoxTreeTool getCheckBoxTreeTool(IVOTreeDataByCode data) {
        if (this.treeTool == null) {
            this.treeTool = new CheckBoxTreeTool(data, "产品节点");
        }

        return this.treeTool;
    }

    class BtnListener implements ActionListener {
        int key = -1;
        JDialog dlg = null;

        public BtnListener() {
        }

        public BtnListener(int btnKey) {
            this.key = btnKey;
        }

        public void actionPerformed(ActionEvent arg0) {
            switch (this.key) {
                case 41:
                    this.onOkPress();
                    break;
                case 42: {
                    if (onCancel != null) {
                        onCancel.accept(ExportDlg.this);
                    }
                    ExportDlg.this.dispose();
                }
            }

        }

        private void onOkPress() {
            Util util = new Util();
            List nodeLst = ExportDlg.this.treeTool.getCheckedLeafNode();

            try {
                if (nodeLst == null || nodeLst.size() == 0) {
                    System.out.println("请选择要导出的节点!");
                    return;
                }

                this.exportNodeSQL(nodeLst, util);
                System.out.println("导出成功!");
            } catch (Exception var4) {
                var4.printStackTrace();
                System.out.println("导出异常!");
            }

            if (onOk != null) {
                onOk.accept(ExportDlg.this);
            }
        }

        public void exportNodeSQL(List<String> nodeLst, Util util) throws Exception {
            String fPath = util.getFilePath() + File.separator + "sqldiffProp.xml";
            File f = new File(fPath);
            if (f.canRead()) {
                XmlCtrl xmlCtrl = new XmlCtrl();
                xmlCtrl.parserXml();
                Iterator var7 = nodeLst.iterator();

                while (var7.hasNext()) {
                    String node = (String) var7.next();
                    (new NodeScriptDiffHelper()).exportScript(xmlCtrl.getDefPropInfo(), node);
                }
            } else {
                System.out.println("请配置插件环境!");
            }

        }
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public JPanel getContentPanel() {
        return contentPanel;
    }

    public CheckBoxTreeTool getTreeTool() {
        return treeTool;
    }

    public void setTreeTool(CheckBoxTreeTool treeTool) {
        this.treeTool = treeTool;
    }

    public JButton getCancelButton() {
        return cancelButton;
    }

    public void setCancelButton(JButton cancelButton) {
        this.cancelButton = cancelButton;
    }

    public JButton getOkButton() {
        return okButton;
    }

    public void setOkButton(JButton okButton) {
        this.okButton = okButton;
    }

    public ISubscriber<ExportDlg> getOnOk() {
        return onOk;
    }

    public void setOnOk(ISubscriber<ExportDlg> onOk) {
        this.onOk = onOk;
    }

    public ISubscriber<ExportDlg> getOnCancel() {
        return onCancel;
    }

    public void setOnCancel(ISubscriber<ExportDlg> onCancel) {
        this.onCancel = onCancel;
    }
}
