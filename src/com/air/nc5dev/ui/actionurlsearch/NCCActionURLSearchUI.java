package com.air.nc5dev.ui.actionurlsearch;

import com.air.nc5dev.nccrequstsearch.RequestMappingItemProvider;
import com.air.nc5dev.util.CollUtil;
import com.air.nc5dev.util.ReflectUtil;
import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.vo.NCCActionInfoVO;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.TextFieldWithAutoCompletion;
import com.intellij.ui.components.*;
import com.intellij.util.ui.UIUtil;
import lombok.Getter;
import lombok.Setter;
import org.jdesktop.swingx.JXComboBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tangsu.mstsc.service.MstscEntitServiceImpl;
import org.tangsu.mstsc.vo.WindowSizeVO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter
@Setter
public class NCCActionURLSearchUI extends DialogWrapper {
    public static String[] TYPE_DESC = new String[]{"全部", "Action", "UPM的Servlet"
            , "非UPM的Servlet", "SpringMVC注解接口", "Jaxrs(javax.ws.rs.Path)"};
    public static String[] tableNames = new String[]{
            "序号"
            , "来源(1=工程)"
            // , "模块"
            , "URL"
            , "标签"
            , "类名"
            , "xml路径"
            // , "项目路径"
    };
    public static String[] tableAttrs = new String[]{
            "order1"
            , "from"
            //   , "appcode"
            , "name"
            , "label"
            , "clazz"
            , "xmlPath"
            //   , "project"
    };

    public static int pk_index = 7;
    Project project;
    JBTabbedPane contentPane;
    JBPanel panel_main;
    JBTextField textField_dbfile;
    ActionResultListTable table;
    JTextField textField_search;
    TextFieldWithAutoCompletion textField_key;
    JTextField textField_name;
    JTextField textField_ip;
    JTextField textField_port;
    JTextField textField_user;
    JTextField textField_pass;
    JTextField textField_order;
    JBTextArea textArea_detail;
    JTextField textField_width;
    JTextField textField_higth;
    JTextField textField_id;
    DefaultComboBoxModel<WindowSizeVO> comboBoxModel_type;
    JComboBox comboBox_type;
    JScrollPane panel_table;
    JCheckBox checkBox_onlyProject;
    JCheckBox checkBox_AutoColumnSize;
    JCheckBox checkBox_jar2last;
    JButton btnNewButton_link;
    JButton btnNewButton_search;
    JButton btnNewButton_searchProject;
    JButton btnNewButton_save;
    JButton btnNewButton_reload;
    DefaultTableModel tableModel;
    JCheckBox checkBox_wapper;
    MstscEntitServiceImpl mstscEntitService;
    List<ActionResultDTO> results;
    AtomicBoolean update = new AtomicBoolean(false);
    JButton btnNewButton_export;
    JButton btnNewButton_import;
    int height = 800;
    int width = 1700;
    JBPanel panel_info;
    JBScrollPane ui;
    JBLabel label_error;

    public NCCActionURLSearchUI(Project project) {
        super(project);
        this.project = project;
        init();
        setTitle("NCC(BIP) Action和UPM URL搜索(双击行 打开Class,或对表格行点击右键)");
    }

    @Override
    protected @Nullable
    JComponent createCenterPanel() {
        return createUI();
    }

    public Container getContentPane() {
        return createCenterPanel();
    }

    public JBScrollPane createUI() {
        if (ui != null) {
            return ui;
        }

        initUI();

        ui = new JBScrollPane(contentPane);

        return ui;
    }

    private void initUI() {
        JBTabbedPane jtab = new JBTabbedPane();
        contentPane = jtab;
        contentPane.setBounds(0, 0, getWidth(), getHeight());
        JBLabel label;

        {
            int x = 1;
            int y = 1;

            panel_main = new JBPanel();
            panel_main.setLayout(null);
            panel_main.setBounds(0, 0, 1200, 800);
            jtab.addTab("URL搜索", panel_main);

            x = 1;
            y = 1;
            int w = 80;
            int h = 30;
            label = new JBLabel("URL或关键词:");
            label.setBounds(x, y, w, h);
            panel_main.add(label);

            x += w + 2;
            w = 460;
            textField_key = new TextFieldWithAutoCompletion(project
                    , new NCCActionCompletionProvider(30, this)
                    , true, "");
            textField_key.setBounds(x, y, w, h);
            panel_main.add(textField_key);

            btnNewButton_search = new JButton("搜索");
            btnNewButton_search.setBounds(x += w + 5, y, w = 80, h);
            panel_main.add(btnNewButton_search);

/*            btnNewButton_searchProject = new JButton("搜索本项目");
            btnNewButton_searchProject.setBounds(x += w + 5, y, w, h);
            panel_main.add(btnNewButton_searchProject);*/

            checkBox_AutoColumnSize = new JBCheckBox("自动适应列宽");
            checkBox_AutoColumnSize.setBounds(x += w + 5, y, w = 100, h);
            panel_main.add(checkBox_AutoColumnSize);

            checkBox_onlyProject = new JBCheckBox("只搜索本工程");
            checkBox_onlyProject.setBounds(x += w + 5, y, w = 100, h);
            panel_main.add(checkBox_onlyProject);

            checkBox_jar2last = new JBCheckBox("排序优先非Jar文件");
            checkBox_jar2last.setBounds(x += w + 5, y, w = 140, h);
            panel_main.add(checkBox_jar2last);

            label = new JBLabel("搜索类型:");
            label.setBounds(x += w + 10, y, w = 60, h);
            panel_main.add(label);
            comboBoxModel_type = new DefaultComboBoxModel(TYPE_DESC);
            comboBox_type = new JXComboBox(comboBoxModel_type) {
                @Override
                public String getToolTipText() {
                    switch (getSelectedIndex()) {
                        case 0:
                            return "搜索全部类型";
                        case 1:
                            return "只搜索配置的前端Action(实现 nccloud.framework.web.action.itf.ICommonAction )";
                        case 2:
                            return "只搜索UPM文件配置的实现了nc.bs.framework.adaptor.IHttpServletAdaptor的接口";
                        case 3:
                            return "只搜索未配置UPM但实现nc.bs.framework.adaptor" +
                                    ".IHttpServletAdaptor的通过http://IP:端口/servlet/~模块名/完整类名 访问的接口";
                        case 4:
                            return "只搜索 SpringMVC注解的接口";
                        case 5:
                            return "只搜索 Java RS标准注解(javax.ws.rs.Path)的接口";
                    }

                    return "";
                }
            };
            comboBox_type.setBounds(x += w + 1, y, w = 150, h);
            panel_main.add(comboBox_type);

            label_error = new JBLabel("请输入搜索关键词!nccloud/aim/acceptance/querylist.do 或 aim/acceptance/querylist 或 " +
                    "acceptance.querylist 或 采购订单 查询 等");
            label_error.setBounds(x = 1, y += h + 5, 900, h);
            label_error.setFontColor(UIUtil.FontColor.BRIGHTER);
            panel_main.add(label_error);

            x = 1;
            y += h + 10;
            tableModel = new DefaultTableModel(null, tableNames);
            table = new ActionResultListTable(tableModel, this);
            panel_table = new JBScrollPane(table);
            panel_table.setAutoscrolls(true);
            panel_table.setBounds(x, y, getWidth() - 100, 300);
            panel_main.add(panel_table);

            TableColumn firsetColumn = table.getColumnModel().getColumn(0);
            firsetColumn.setPreferredWidth(30);
            firsetColumn.setMaxWidth(30);
            firsetColumn = table.getColumnModel().getColumn(1);
            firsetColumn.setPreferredWidth(50);
            firsetColumn.setMaxWidth(50);

            textArea_detail = new JBTextArea();
            textArea_detail.setEditable(true);
            textArea_detail.setAutoscrolls(true);
            // textArea_detail.setLineWrap(true);
            JBScrollPane sc = new JBScrollPane(textArea_detail);
            sc.setAutoscrolls(true);
            sc.setBounds(x += panel_table.getWidth() + 5, y, 200, 200);
            // panel_main.add(sc);

            initListeners();
        }

    }

    private void initListeners() {
        btnNewButton_search.addActionListener(e -> search(checkBox_onlyProject.isSelected() ? 0 : 1));
        if (btnNewButton_searchProject != null) {
            btnNewButton_searchProject.addActionListener(e -> search(0));
        }
    }

    AtomicBoolean runing = new AtomicBoolean(false);

    public void search(int type) {
        if (runing.get()) {
            label_error.setText("请等待上一次搜索完成...再试");
            return;
        }

        runing.set(true);
        Task.Backgroundable backgroundable = new Task.Backgroundable(project, "正在搜索匹配的NC Action中...") {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {
                    if (1 == type) {
                        searchAll();
                    } else if (0 == type) {
                        searchProject();
                    }
                } finally {
                    runing.set(false);
                }
            }
        };
        backgroundable.setCancelText("放弃吧,没有卵用的按钮");
        backgroundable.setCancelTooltipText("这是一个没有卵用的按钮");
        ProgressManager.getInstance().run(backgroundable);
    }

    public void searchAll() {
        String key = getKey();
        if (key == null) {
            return;
        }

        label_error.setText("搜索中...请稍后...");

        List<NCCActionInfoVO> res = fileter(RequestMappingItemProvider.getMe().search(project, key, true));
        setResult(res);

        label_error.setText("搜索完成!匹配数: " + CollUtil.size(res));
    }

    private List<NCCActionInfoVO> fileter(List<NCCActionInfoVO> res) {
        List<NCCActionInfoVO> vs = filter0(res);

        if (checkBox_jar2last.isSelected()) {
            for (NCCActionInfoVO v : vs) {
                if (!v.getXmlPath().toLowerCase().contains(".jar")) {
                    v.setScore(v.getScore() + 100);
                }
            }
            vs.sort((a, b) -> b.getScore() - a.getScore());
        }

        return vs;
    }

    @Nullable
    private List<NCCActionInfoVO> filter0(List<NCCActionInfoVO> res) {
        if (comboBox_type.getSelectedIndex() < 1 || CollUtil.isEmpty(res)) {
            return res;
        }

        ArrayList<NCCActionInfoVO> vs = new ArrayList();
        for (NCCActionInfoVO r : res) {
            if (comboBox_type.getSelectedIndex() == 1
                    && r.getType() == NCCActionInfoVO.TYPE_ACTION) {
                vs.add(r);
            } else if (comboBox_type.getSelectedIndex() == 2
                    && r.getType() == NCCActionInfoVO.TYPE_UPM) {
                vs.add(r);
            }else if (comboBox_type.getSelectedIndex() == 3
                    && r.getType() == NCCActionInfoVO.TYPE_SERVLET) {
                vs.add(r);
            }else if (comboBox_type.getSelectedIndex() == 4
                    && r.getType() == NCCActionInfoVO.TYPE_SPRINTMVC) {
                vs.add(r);
            }else if (comboBox_type.getSelectedIndex() == 5
                    && r.getType() == NCCActionInfoVO.TYPE_JAXRS) {
                vs.add(r);
            }
        }

        return vs;
    }

    private void setResult(List<NCCActionInfoVO> res) {
        SwingUtilities.invokeLater(() -> {
            if (CollUtil.isEmpty(res)) {
                label_error.setText("未搜索到任何内容");
                textArea_detail.setText("");
                table.removeAll();
                return;
            }

            ArrayList<ActionResultDTO> vos = new ArrayList(res.size() << 1);
            int i = 1;
            for (NCCActionInfoVO re : res) {
                ActionResultDTO v = ReflectUtil.copy2VO(re, ActionResultDTO.class);
                v.setOrder1(i++);
                vos.add(v);
            }

            textArea_detail.setText(vos.get(0).displayText());

            getTable().setRows(vos);
        });
    }

    public String getKey() {
        String key = StringUtil.trim(textField_key.getText());
        if (StringUtil.isBlank(key)) {
            label_error.setText("请输入搜索关键词!nccloud/aim/acceptance/querylist.do 或 aim/acceptance/querylist 或 acceptance" +
                    ".querylist 或 采购订单 查询 等");
            return null;
        }

        return key;
    }

    public void searchProject() {
        String key = getKey();
        if (key == null) {
            return;
        }
        label_error.setText("搜索中...请稍后...");

        List<NCCActionInfoVO> res = fileter(RequestMappingItemProvider.getMe().search(project, key, false));

        setResult(res);

        label_error.setText("搜索完成!匹配数: " + CollUtil.size(res));
    }
}
