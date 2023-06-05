package org.tangsu.mstsc.ui;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.air.nc5dev.util.CollUtil;
import com.air.nc5dev.util.IdeaProjectGenerateUtil;
import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.util.idea.LogUtil;
import com.alibaba.fastjson.JSON;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBPanel;
import lombok.Data;
import org.tangsu.mstsc.entity.MstscEntity;
import org.tangsu.mstsc.rdp.RDPCompile;
import org.tangsu.mstsc.service.MstscEntitServiceImpl;
import org.tangsu.mstsc.ui.listeners.SearchInputMouseListenerImpl;
import org.tangsu.mstsc.ui.listeners.TableMouseListenerImpl;
import org.tangsu.mstsc.vo.WindowSizeVO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

@Data
public class MainPanel extends JBPanel {
    MstscListTable table;
    JTextField textField_search;
    JTextField textField_code;
    JTextField textField_name;
    JTextField textField_ip;
    JTextField textField_port;
    JTextField textField_user;
    JTextField textField_pass;
    JTextField textField_order;
    JTextField textField_memo;
    JTextField textField_width;
    JTextField textField_higth;
    JTextField textField_id;
    DefaultComboBoxModel<WindowSizeVO> comboBoxSizeModel;
    JScrollPane pane_config;
    JComboBox comboBox_size;
    JScrollPane pane_table;
    JCheckBox checkBox_full;
    JCheckBox checkBox_disk;
    JCheckBox checkBox_relink;
    JPanel panel_btn;
    JButton btnNewButton_link;
    JButton btnNewButton_add;
    JButton btnNewButton_del;
    JButton btnNewButton_save;
    JButton btnNewButton_reload;
    DefaultTableModel tableModel;
    JCheckBox checkBox_wapper;
    MstscEntitServiceImpl mstscEntitService;
    List<MstscEntity> es;
    AtomicBoolean update = new AtomicBoolean(false);

    public static String[] tableNames = new String[]{
            "序号", "编码", "标题", "IP", "端口"
            , "用户名", "备注", "ID"
    };
    public static int pk_index = 7;
    public static String[] tableAttrs = new String[]{
            "order1", "code", "title", "ip", "port"
            , "user", "memo", "pk"
    };
    private JButton btnNewButton_export;
    private JButton btnNewButton_import;

    /**
     * Create the panel.
     */
    public MainPanel(MstscEntitServiceImpl mstscEntitService) {
        this.mstscEntitService = mstscEntitService;
        setLayout(null);
        setBounds(0, 0, 1010, 720);

        tablePanel();

        configPanel();

        btnPanel();

        initListeners();
    }

    private void btnPanel() {
        int y = pane_config.getY() + pane_config.getHeight() + 1;
        panel_btn = new JPanel();
        panel_btn.setBounds(1, y, getWidth() - 1, 57);
        add(panel_btn);
        panel_btn.setLayout(null);

        y = 10;
        int hight = 35;
        int width = 64;
        btnNewButton_add = new JButton("新增");
        btnNewButton_add.setBounds(587, y, width, hight);
        panel_btn.add(btnNewButton_add);

        btnNewButton_del = new JButton("删除");
        // btnNewButton_del.setForeground(new Color(0, 0, 0));
        // btnNewButton_del.setBackground(new Color(255, 0, 0));
        btnNewButton_del.setBounds(520, y, width, hight);
        panel_btn.add(btnNewButton_del);

        textField_search = new JTextField();
        textField_search.setBounds(77, y, 433, hight);
        panel_btn.add(textField_search);
        textField_search.setColumns(10);

        JLabel lblNewLabel = new JLabel("过滤:");
        lblNewLabel.setBounds(10, y, 54, hight);
        panel_btn.add(lblNewLabel);

        btnNewButton_save = new JButton("保存");
        btnNewButton_save.setBounds(655, y, width, hight);
        panel_btn.add(btnNewButton_save);

        btnNewButton_reload = new JButton("刷新");
        btnNewButton_reload.setBounds(725, y, width, hight);
        panel_btn.add(btnNewButton_reload);
        btnNewButton_link = new JButton("连接");

        // btnNewButton_link.setForeground(new Color(0, 255, 0));
        btnNewButton_link.setFont(UIManager.getFont("Button.font"));
        btnNewButton_link.setBounds(797, y, 60, hight);
        panel_btn.add(btnNewButton_link);

        btnNewButton_export = new JButton("导出");
        btnNewButton_export.setBounds(
                btnNewButton_link.getX() + btnNewButton_link.getWidth() + 3
                , y
                , 54
                , hight);
        panel_btn.add(btnNewButton_export);

        btnNewButton_import = new JButton("导入");
        btnNewButton_import.setBounds(
                btnNewButton_export.getX() + btnNewButton_export.getWidth() + 3
                , y
                , 54
                , hight);
        panel_btn.add(btnNewButton_import);

        //  btnNewButton_del.setUI(new BEButtonUI().setNormalColor(BEButtonUI.NormalColor.red));
        //  btnNewButton_add.setUI(new BEButtonUI().setNormalColor(BEButtonUI.NormalColor.green));
        //  btnNewButton_save.setUI(new BEButtonUI().setNormalColor(BEButtonUI.NormalColor.lightBlue));
        //  btnNewButton_link.setUI(new BEButtonUI().setNormalColor(BEButtonUI.NormalColor.blue));
    }

    private void configPanel() {
        int x = 1;
        int y = pane_table.getY() + pane_table.getHeight() + 1;
        int w = pane_table.getWidth();
        int h = 240;

        JPanel panel = new JPanel();
        pane_config = new JScrollPane(panel);
        pane_config.setAutoscrolls(true);
        pane_config.setBounds(x, y, w, h);
        panel.setBounds(2, 2, w - 10, h + 100);
        add(pane_config);
        //pane_config.setLayout(null);
        panel.setLayout(null);

        y = 1;
        x = 1;
        w = 40;
        h = 30;
        JLabel lblNewLabel_1 = new JLabel("编码:");
        lblNewLabel_1.setBounds(x, y, w, h);
        panel.add(lblNewLabel_1);

        x += w + 2;
        w = 100;
        textField_code = new JTextField();
        textField_code.setBounds(x, y, w, h);
        panel.add(textField_code);
        textField_code.setColumns(10);

        x += w + 2;
        w = 40;
        JLabel lblNewLabel_1_1 = new JLabel("名称:");
        lblNewLabel_1_1.setBounds(x, y, w, h);
        panel.add(lblNewLabel_1_1);

        x += w + 2;
        w = 200;
        textField_name = new JTextField();
        textField_name.setColumns(10);
        textField_name.setBounds(x, y, w, h);
        panel.add(textField_name);

        x += w + 2;
        w = 40;
        JLabel lblNewLabel_1_1_1 = new JLabel("IP:");
        lblNewLabel_1_1_1.setBounds(x, y, w, h);
        panel.add(lblNewLabel_1_1_1);

        x += w + 2;
        w = 200;
        textField_ip = new JTextField();
        textField_ip.setColumns(10);
        textField_ip.setBounds(x, y, w, h);
        panel.add(textField_ip);

        x += w + 2;
        w = 40;
        JLabel lblNewLabel_1_1_1_1 = new JLabel("端口:");
        lblNewLabel_1_1_1_1.setBounds(x, y, w, h);
        panel.add(lblNewLabel_1_1_1_1);

        x += w + 2;
        w = 70;
        textField_port = new JTextField();
        textField_port.setColumns(10);
        textField_port.setText("3389");
        textField_port.setBounds(x, y, w, h);
        panel.add(textField_port);

        x += w + 2;
        w = 65;
        JLabel lblNewLabel_1_2 = new JLabel("用户名:");
        lblNewLabel_1_2.setBounds(x, y, w, h);
        panel.add(lblNewLabel_1_2);

        x += w + 2;
        w = 120;
        textField_user = new JTextField();
        textField_user.setColumns(10);
        textField_user.setText("Administrator");
        textField_user.setBounds(x, y, w, h);
        panel.add(textField_user);

        y += h + 3;
        x = 1;

        w = 40;
        JLabel lblNewLabel_1_1_2 = new JLabel("密码:");
        lblNewLabel_1_1_2.setBounds(x, y, w, h);
        panel.add(lblNewLabel_1_1_2);

        x += w + 2;
        w = 300;
        textField_pass = new JTextField();
        textField_pass.setColumns(10);
        textField_pass.setBounds(x, y, w, h);
        panel.add(textField_pass);

        x += w + 1;
        w = 40;
        JLabel lblNewLabel_1_1_1_1_1 = new JLabel("排序:");
        lblNewLabel_1_1_1_1_1.setBounds(x, y, w, h);
        panel.add(lblNewLabel_1_1_1_1_1);

        x += w + 2;
        w = 70;
        textField_order = new JTextField();
        textField_order.setColumns(10);
        textField_order.setBounds(x, y, w, h);
        panel.add(textField_order);

        x += w + 2;
        w = 40;
        JLabel lblNewLabel_1_1_3 = new JLabel("备注:");
        lblNewLabel_1_1_3.setBounds(x, y, w, h);
        panel.add(lblNewLabel_1_1_3);

        x += w + 2;
        w = 420;
        textField_memo = new JTextField();
        textField_memo.setColumns(10);
        textField_memo.setBounds(x, y, w, h);
        panel.add(textField_memo);

        y += h + 3;
        x = 1;

        w = 65;
        JLabel lblNewLabel_1_1_1_1_2 = new JLabel("分辨率:");
        lblNewLabel_1_1_1_1_2.setBounds(x, y, w, h);
        panel.add(lblNewLabel_1_1_1_1_2);

        x += w + 2;
        w = 120;
        initSizeCombobox();
        comboBox_size.setBounds(x, y, w, h);
        panel.add(comboBox_size);

        x += w + 2;
        w = 60;
        textField_width = new JTextField();
        textField_width.setColumns(10);
        textField_width.setBounds(x, y, w, h);
        panel.add(textField_width);

        x += w + 2;
        w = 10;
        JLabel lblNewLabel_1_2_1 = new JLabel("*");
        lblNewLabel_1_2_1.setBounds(x, y, w, h);
        panel.add(lblNewLabel_1_2_1);

        x += w + 2;
        w = 60;
        textField_higth = new JTextField();
        textField_higth.setColumns(10);
        textField_higth.setBounds(x, y, w, h);
        panel.add(textField_higth);

        x += w + 2;
        w = 100;
        checkBox_full = new JCheckBox("是否全屏");
        checkBox_full.setSelected(true);
        checkBox_full.setBounds(x, y, w, h);
        panel.add(checkBox_full);

        x += w + 2;
        w = 150;
        checkBox_disk = new JCheckBox("映射本地硬盘");
        checkBox_disk.setBounds(x, y, w, h);
        panel.add(checkBox_disk);

        x += w + 2;
        w = 150;
        checkBox_relink = new JCheckBox("断开自动重连");
        checkBox_relink.setBounds(x, y, w, h);
        panel.add(checkBox_relink);

        x += w + 2;
        w = 150;
        checkBox_wapper = new JCheckBox("是否真实桌面");
        checkBox_wapper.setBounds(x, y, w, h);
        panel.add(checkBox_wapper);

        y += h + 3;
        x = 1;

        w = 40;
        JLabel lblNewLabel_1_1_4 = new JLabel("行ID:");
        lblNewLabel_1_1_4.setBounds(x, y, w, h);
        panel.add(lblNewLabel_1_1_4);

        x += w + 2;
        w = 300;
        textField_id = new JTextField();
        // textField_id.setEditable(false);
        textField_id.setColumns(10);
        textField_id.setBounds(x, y, w, h);
        panel.add(textField_id);
    }

    private void initSizeCombobox() {
        comboBoxSizeModel = new DefaultComboBoxModel();
        comboBoxSizeModel.addElement(WindowSizeVO.builder().width(1920).hight(1080).build());
        comboBoxSizeModel.addElement(WindowSizeVO.builder().width(1600).hight(900).build());
        comboBoxSizeModel.addElement(WindowSizeVO.builder().width(1680).hight(1050).build());
        comboBoxSizeModel.addElement(WindowSizeVO.builder().width(1280).hight(720).build());
        comboBoxSizeModel.addElement(WindowSizeVO.builder().width(800).hight(600).build());
        comboBoxSizeModel.addElement(WindowSizeVO.builder().width(1024).hight(576).build());
        comboBoxSizeModel.addElement(WindowSizeVO.builder().width(1024).hight(768).build());
        comboBoxSizeModel.addElement(WindowSizeVO.builder().width(1280).hight(800).build());
        comboBoxSizeModel.addElement(WindowSizeVO.builder().width(1280).hight(1024).build());
        comboBoxSizeModel.addElement(WindowSizeVO.builder().width(1366).hight(768).build());
        comboBoxSizeModel.addElement(WindowSizeVO.builder().width(1440).hight(900).build());
        comboBoxSizeModel.addElement(WindowSizeVO.builder().width(2560).hight(1440).build());
        comboBoxSizeModel.addElement(WindowSizeVO.builder().width(3840).hight(2160).build());
        comboBox_size = new JComboBox(comboBoxSizeModel);
    }

    private void tablePanel() {
        tableModel = new DefaultTableModel(null, tableNames);
        table = new MstscListTable(tableModel);
        pane_table = new JScrollPane(table);
        pane_table.setAutoscrolls(true);
        pane_table.setBounds(1, 1, getWidth() - 50, 330);
        table.setBounds(0, 0, pane_table.getWidth() - 10, pane_table.getHeight() - 10);
        //pane_table.setLayout(null);
        add(pane_table);
    }

    public void initListeners() {
        btnNewButton_save.addActionListener(this::save);
        btnNewButton_reload.addActionListener(this::reload);
        btnNewButton_del.addActionListener(this::delete);
        btnNewButton_link.addActionListener(this::link);
        btnNewButton_add.addActionListener(e -> clearConfig());
        btnNewButton_export.addActionListener(e -> export());
        btnNewButton_import.addActionListener(e -> imports());

        comboBox_size.addItemListener(e -> {
            WindowSizeVO s = (WindowSizeVO) e.getItem();
            textField_width.setText(s.getWidth() + "");
            textField_higth.setText(s.getHight() + "");
        });

        table.addMouseListener(new TableMouseListenerImpl(this));
        textField_search.addMouseListener(new SearchInputMouseListenerImpl(this));

        Field[] fs = MainPanel.class.getDeclaredFields();
        for (Field f : fs) {
            if (f.getType().isAssignableFrom(JTextField.class)) {
                JTextField v = (JTextField) ReflectUtil.getFieldValue(this, f);
                if (v != null) {
                    v.addKeyListener(new KeyAdapter() {
                        @Override
                        public void keyReleased(KeyEvent e) {
                            update.set(true);
                        }
                    });
                }
            } else if (f.getType().isAssignableFrom(JCheckBox.class)) {
                JCheckBox v = (JCheckBox) ReflectUtil.getFieldValue(this, f);
                if (v != null) {
                    v.addActionListener(e -> update.set(true));
                }
            } else if (f.getType().isAssignableFrom(JComboBox.class)) {
                JComboBox v = (JComboBox) ReflectUtil.getFieldValue(this, f);
                if (v != null) {
                    v.addItemListener(e -> update.set(true));
                }
            }
        }
    }

    public void imports() {
        try {
            String json = getFromClipboard();
            if (StrUtil.isBlank(json)) {
                return;
            }

            List<MstscEntity> all = JSON.parseArray(json, MstscEntity.class);
            for (MstscEntity a : all) {
                if (StrUtil.isBlank(a.getPk())) {
                    a.setPk(UUID.fastUUID().toString(true));
                }
                mstscEntitService.insert(a);
            }
            loadDatas();
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.error(
                    "导入出错!请检查粘贴板里面的json文本格式:" + e.getMessage()
            );
        }
    }

    public void export() {
        setIntoClipboard(JSON.toJSONString(es));
    }

    public void setIntoClipboard(String data) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(data), null);
    }

    public String getFromClipboard() {
        Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            try {
                return (String) transferable.getTransferData(DataFlavor.stringFlavor);
            } catch (Exception e) {
                return "";
            }
        }
        return "";
    }

    public void link(ActionEvent actionEvent) {
        MstscEntity e = getConfigVO();
        System.out.println("正在连接..." + e.getTitle());

        RDPCompile.link(e, this);
    }

    public void delete(ActionEvent actionEvent) {
        int re = Messages.showYesNoDialog("你确定删除当前行?"
                , "警告", Messages.getQuestionIcon());
        if (re != Messages.OK) {
            return;
        }

        String id = textField_id.getText();
        if (StrUtil.isBlank(id)) {
            return;
        }

        try {
            mstscEntitService.delete(id);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.error("删除失败:" + e.getMessage());
        }

        try {
            loadDatas();
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.error("刷新失败:" + e.getMessage());
        }

        LogUtil.infoAndHide("删除成功!" + textField_code.getText());

        select(-1);
    }

    public void select(int row) {
        update.set(false);
        if (row < 0 || row >= tableModel.getRowCount()) {
            clearConfig();
            return;
        }

        if (es == null) {
            es = new ArrayList<>();
        }

        String id = StringUtil.getSafeString(tableModel.getValueAt(row, pk_index));
        if (StrUtil.isBlank(id)) {
            clearConfig();
            return;
        }

        MstscEntity m = es.stream()
                .filter(e -> StrUtil.equals(e.getPk(), id))
                .findAny().orElse(null);

        if (m == null) {
            clearConfig();
            return;
        }

        textField_name.setText(m.getTitle());
        textField_id.setText(m.getPk());
        textField_code.setText(m.getCode());
        textField_higth.setText(m.getDesktopheight());
        textField_memo.setText(m.getMemo());
        textField_order.setText(m.getOrder1() + "");
        textField_pass.setText(m.getPass());
        textField_port.setText(m.getPort());
        textField_search.setText(null);
        textField_user.setText(m.getUser());
        textField_width.setText(m.getDesktopwidth());
        checkBox_full.setSelected("1".equals(m.getWinposstr()));
        checkBox_wapper.setSelected(!"0".equals(m.getDisable_wallpaper()));
        checkBox_relink.setSelected("1".equals(m.getAutoreconnection()));
        checkBox_disk.setSelected(true);
        textField_ip.setText(m.getIp());

    }

    public void clearConfig() {
        update.set(false);
        textField_name.setText(null);
        textField_id.setText(null);
        textField_code.setText(String.valueOf(CollUtil.size(es) + 1));
        textField_higth.setText(null);
        textField_memo.setText(null);
        textField_order.setText(String.valueOf(CollUtil.size(es) + 30));
        textField_pass.setText(null);
        textField_port.setText("3389");
        textField_search.setText(null);
        textField_user.setText("Administrator");
        textField_width.setText("1920");
        textField_higth.setText("1080");
    }

    public void reload(ActionEvent actionEvent) {
        int re = Messages.showYesNoDialog("你确定刷新数据?"
                , "警告", Messages.getQuestionIcon());
        if (re != Messages.OK) {
            return;
        }

        update.set(false);
        try {
            loadDatas();
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.error("读取数据库文件错误:" + e.getMessage(), e);
        }
    }

    public void save(ActionEvent actionEvent) {
        update.set(false);
        MstscEntity e = getConfigVO();

        boolean isnew = true;
        if (StrUtil.isBlank(e.getPk())) {
            e.setPk(UUID.fastUUID().toString(true));
        } else {
            isnew = false;
        }

        if (!StrUtil.isAllNotBlank(e.getIp(), e.getUser())) {
            if (actionEvent == null) {
                return;
            }
            LogUtil.error("填写内容不完整!");
            return;
        }

        if (actionEvent == null) {
            int re = Messages.showYesNoDialog("是否保存当前配置里的信息?"
                    , "警告", Messages.getQuestionIcon());
            if (re != Messages.OK) {
                return;
            }
        }

        try {
            if (isnew) {
                e = mstscEntitService.insert(e);
            } else {
                e = mstscEntitService.update(e);
            }

            loadDatas();
        } catch (Exception ex) {
            ex.printStackTrace();
            LogUtil.error("读写数据库文件失败:" + ex.getMessage(), ex);
        }
    }

    public void loadDatas() throws SQLException, InstantiationException, IllegalAccessException {
        es = mstscEntitService.findAll();
        update.set(false);
        DefaultTableModel tm = tableModel;
        removeAll(tm);
        for (MstscEntity e : es) {
            Vector row = new Vector();
            for (String arr : MainPanel.tableAttrs) {
                row.add(ReflectUtil.getFieldValue(e, arr));
            }
            tm.addRow(row);
        }
        table.fitTableColumns();
    }

    public void removeAll(DefaultTableModel tm) {
        update.set(false);
        while (tm.getRowCount() > 0) {
            tm.removeRow(0);
        }
    }


    private MstscEntity getConfigVO() {
        MstscEntity e = new MstscEntity();

        e.setDr(0);
        e.setPk(textField_id.getText());
        e.setCode(textField_code.getText());
        e.setTitle(textField_name.getText());
        e.setIp(textField_ip.getText());
        e.setPort(textField_port.getText());
        e.setUser(textField_user.getText());
        e.setPass(textField_pass.getText());
        e.setOrder1(StrUtil.isBlank(textField_order.getText()) ? 0
                : NumberUtil.parseInt(textField_order.getText()));
        e.setMemo(textField_memo.getText());
        e.setDesktopwidth(textField_width.getText());
        e.setDesktopheight(textField_higth.getText());
        e.setCompression("1");
        e.setDisable_themes(checkBox_wapper.isSelected() ? "0" : "1");
        e.setDisable_wallpaper(checkBox_wapper.isSelected() ? "1" : "0");
        e.setDisplayconnectionbar("1");
        e.setScreen_mode_id(checkBox_full.isSelected() ? "0" : "1");
        e.setSession_bpp("16");
        e.setWinposstr("s:2,3,0,0,800,600");
        e.setRedirectclipboard("1");
        e.setAutoreconnection(checkBox_relink.isSelected() ? "1" : "0");
        return e;
    }

    public void loadDatas(List<MstscEntity> vos) {
        update.set(false);
        DefaultTableModel tm = tableModel;
        removeAll(tm);
        for (MstscEntity e : vos) {
            Vector row = new Vector();
            for (String arr : MainPanel.tableAttrs) {
                row.add(ReflectUtil.getFieldValue(e, arr));
            }
            tm.addRow(row);
        }
        table.fitTableColumns();
    }

}
