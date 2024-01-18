package com.air.nc5dev.ui.adddeffiled;

import cn.hutool.core.io.FileUtil;
import com.air.nc5dev.ui.exportbmf.ExportbmfDialog;
import com.air.nc5dev.util.CollUtil;
import com.air.nc5dev.util.ReflectUtil;
import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.util.V;
import com.air.nc5dev.util.XmlUtil;
import com.air.nc5dev.util.idea.LogUtil;
import com.air.nc5dev.util.meta.consts.PropertyDataTypeEnum;
import com.air.nc5dev.util.meta.xml.MeatBaseInfoReadUtil;
import com.air.nc5dev.vo.meta.ClassDTO;
import com.air.nc5dev.vo.meta.ComponentDTO;
import com.air.nc5dev.vo.meta.PropertyDTO;
import com.google.common.collect.Maps;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTabbedPane;
import lombok.Getter;
import lombok.Setter;
import nc.vo.pub.VOStatus;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.jetbrains.annotations.Nullable;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.stream.Collectors;

/***
 *     弹框UI        </br>
 *           </br>
 *           </br>
 *           </br>
 * @author air Email: 209308343@qq.com
 * @date 2019/12/25 0025 15:29
 * @Param
 * @return
 */
@Getter
@Setter
public class AddDefField2BmfDialog extends DialogWrapper {
    public static String[] tableNames = new String[]{
            "序号", "组件名", "组件显示名", "模块", "名称空间"
            , "表名", "类名", "主实体"
            , "ID", "文件版本号", "文件名", "组件类型", "文件路径"
    };
    public static String[] tableAttrs = new String[]{
            "order1", "name", "displayName", "ownModule", "namespace"
            , "defaultTableName", "fullClassName", "isPrimary"
            , "id", "fileVersion", "fileName", "metaType", "filePath"
    };


    public static String[] tablePropNames = new String[]{
            "序号", "名称", "显示名称", "类型样式"
            , "类型", "字段名称", "字段类型", "参照名称"
    };
    public static String[] tablePropAttrs = new String[]{
            "attrsequence", "name", "displayName", "dataTypeStyleName"
            , "typeName", "fieldName", "fieldType", "refModelName"
    };

    VirtualFile bmf;
    JBTabbedPane contentPane;
    JBPanel panel_main;

    DefaultComboBoxModel<ClassComboxDTO> comboBoxModelClass;
    ComboBox comboBoxClass;

    JBLabel labelInfo;

    DefaultTableModel tableModelPropertis;
    ProperteListTable tablePropertis;
    JScrollPane panel_tablePropertis;

    DefaultTableModel tableModelClass;
    ClassEntityListTable tableClass;
    JScrollPane panel_tableClass;

    int height = 800;
    int width = 1200;
    Project project;

    /**
     * 列表
     */
    volatile List<PropertyDTO> result;
    /**
     * key=calssid 实体id, value =属性列表
     */
    Map<String, List<PropertyDTO>> propertyMap = new HashMap<>();

    String oKButtonTextStr = "保存到文件";

    public AddDefField2BmfDialog(Project project, VirtualFile bmf) {
        super(project);
        this.project = project;
        this.bmf = bmf;
        createCenterPanel();
        init();
        setOKButtonText(oKButtonTextStr);
        setCancelButtonText("关闭窗口");
        setTitle("快速新增自定义项字段或复制字段到元数据文件(表格点击右键操作)");
    }

    private void createCenterPanel0() throws Exception {
        JBTabbedPane jtab = new JBTabbedPane();
        contentPane = jtab;
        contentPane.setBounds(0, 0, getWidth(), getHeight());
        {
            int x = 1;
            int y = 1;
            int w = 60;
            int h = 40;

            panel_main = new JBPanel();
            panel_main.setLayout(null);
            panel_main.setBounds(0, 0, 1200, 800);
            jtab.addTab(bmf.getPath(), panel_main);

            comboBoxModelClass = new DefaultComboBoxModel<ClassComboxDTO>(readClassList());
            JBLabel label = new JBLabel("选择实体:");
            label.setBounds(x, y, w, 60);
            panel_main.add(label);
            comboBoxClass = new ComboBox(comboBoxModelClass);
            comboBoxClass.setBounds(x + label.getWidth(), y, 600, h);
            panel_main.add(comboBoxClass);

            labelInfo = new JBLabel();
            labelInfo.setBounds(x = 1, y += comboBoxClass.getHeight() + 3, getWidth() - 50, 30);
            panel_main.add(labelInfo);

            tableModelClass = new DefaultTableModel(null, tableNames);
            tableClass = new ClassEntityListTable(tableModelClass, this);
            panel_tableClass = new JBScrollPane(tableClass);
            panel_tableClass.setAutoscrolls(true);
            panel_tableClass.setBounds(x = 1, y += labelInfo.getHeight() + 2, getWidth() - 50, 100);
            panel_main.add(panel_tableClass);

            tableModelPropertis = new DefaultTableModel(null, tablePropNames);
            tablePropertis = new ProperteListTable(tableModelPropertis, this);
            panel_tablePropertis = new JBScrollPane(tablePropertis);
            panel_tablePropertis.setAutoscrolls(true);
            panel_tablePropertis.setBounds(x = 1, y += panel_tableClass.getHeight() + 5, getWidth() - 50, 400);
            panel_main.add(panel_tablePropertis);

            initListeners();
        }

        //设置点默认值
        initDefualtValues();
    }

    public void save2Files() {
//        String bmfXml = FileUtil.readUtf8String(bmf.toNioPath().toFile());
//        if (bmfXml.startsWith("\uFEFF")) {
//            bmfXml = StringUtil.removeStart(bmfXml, "\uFEFF");
//        }
        org.dom4j.Document doc = null;
        org.dom4j.Element root = null;
        try {
            doc = new SAXReader().read(bmf.toNioPath().toFile());
            root = doc.getRootElement();
        } catch (Throwable e) {
            //出错了
            e.printStackTrace();
            LogUtil.error("解析xml文件失败:" + e.getMessage(), e);
            return;
        }

        org.dom4j.Element celllist = null;
        List<Element> ets = null;
        if (root.elements("celllist").size() < 1) {
            celllist = root.addElement("celllist");
        } else {
            celllist = root.element("celllist");
        }
        ets = celllist.elements("entity");

        if (ets.size() < 0) {
            ets = new ArrayList<>();
        }

        HashMap<String, org.dom4j.Element> entityId2EleMap = Maps.newHashMap();
        for (Element et : ets) {
            entityId2EleMap.put(et.attributeValue("id"), et);
        }

        HashMap<String, String> attributeNameMaping = new HashMap();
        attributeNameMaping.put("hided", "isHide");
        attributeNameMaping.put("nullable", "isNullable");
        attributeNameMaping.put("readonly", "isReadOnly");
        attributeNameMaping.put("accessorclassname", "accessStrategy");
        attributeNameMaping.put("attrmaxvalue", "maxValue");
        attributeNameMaping.put("attrminvalue", "minValue");
        attributeNameMaping.put("attrlength", "length");
        attributeNameMaping.put("attrsequence", "sequence");
        attributeNameMaping.put("dynamicattr", "dynamic");

        org.dom4j.Element e = null;
        for (String entityId : propertyMap.keySet()) {
            e = entityId2EleMap.get(entityId);
            if (e == null) {
                continue;
            }

            List<PropertyDTO> vs = propertyMap.get(entityId);
            int i = 1;
            for (PropertyDTO v : vs) {
                v.setAttrsequence(i++);
            }

            Map<String, Element> attrId2EleMap = new HashMap<>();
            Element attributelist = e.element("attributelist");
            if (attributelist != null) {
                attrId2EleMap = attributelist.elements("attribute")
                        .stream().collect(Collectors.toMap(v -> v.attributeValue("id"), v -> v));
            } else {
                attributelist = e.addElement("attributelist");
            }

            Map<Integer, List<PropertyDTO>> state2ProsMap =
                    vs.stream().collect(Collectors.groupingBy(v -> v.getState()));
            List<PropertyDTO> ps = state2ProsMap.get(VOStatus.DELETED);
            if (CollUtil.isNotEmpty(ps)) {
                for (PropertyDTO p : ps) {
                    Element el = attrId2EleMap.get(p.getId());
                    if (el != null) {
                        attributelist.remove(el);
                    }
                }
            }

            ps = state2ProsMap.get(VOStatus.NEW);
            if (CollUtil.isNotEmpty(ps)) {
                for (PropertyDTO p : ps) {
                    Element el = attributelist.addElement("attribute");
                    XmlUtil.addAttr(doc, el, p, attributeNameMaping);
                    el.addAttribute("dataTypeStyle", p.getDataTypeStyleName());
                    el.addAttribute("visibility", p.getVisibilityName());
                }
            }

            ps = state2ProsMap.get(VOStatus.UPDATED);
            if (CollUtil.isNotEmpty(ps)) {
                for (PropertyDTO p : ps) {
                    Element el = attrId2EleMap.get(p.getId());
                    if (el == null) {
                        el = attributelist.addElement("attribute");
                    }
                    XmlUtil.addAttr(doc, el, p, attributeNameMaping);
                    el.addAttribute("dataTypeStyle", p.getDataTypeStyleName());
                    el.addAttribute("visibility", p.getVisibilityName());
                }
            }

            ps = state2ProsMap.get(VOStatus.UNCHANGED);
            if (CollUtil.isNotEmpty(ps)) {
                for (PropertyDTO p : ps) {
                    Element el = attrId2EleMap.get(p.getId());
                    if (el != null) {
                        el.addAttribute("sequence", String.valueOf(p.getAttrsequence()));
                    }
                }
            }
        }

        FileUtil.writeUtf8String(doc.asXML(), bmf.toNioPath().toFile());
        LogUtil.infoAndHide("保存文件成功!");

        readClassList();
        loadPerpotes((ClassComboxDTO) comboBoxClass.getSelectedItem());
    }

    public void initFiles(int type) {
        new ExportbmfDialog(getProject()).initFiles(type);
    }

    @Override
    protected JComponent createCenterPanel() {
        if (contentPane == null) {
            try {
                createCenterPanel0();
            } catch (Exception e) {
                e.printStackTrace();
                LogUtil.error(e.getMessage(), e);
            }
        }

        return this.contentPane;
    }

    public void initDefualtValues() {
        try {
            labelInfo.setText("注意:点击属性表格右键出现功能,背景行粉色 标记删除行,背景行绿色 更新行,背景行 白色 新增行!(都必须点击 "
                    + getOKButtonTextStr() + " 按钮后才会真的保存写入文件)");

            removeAll(tableModelClass);
            if (CollUtil.isNotEmpty(entits)) {
                for (ClassComboxDTO e : entits) {
                    Vector row = new Vector();
                    for (int i = 0; i < tableAttrs.length; i++) {
                        row.add(ReflectUtil.getFieldValue(e, tableAttrs[i]));
                    }
                    tableModelClass.addRow(row);
                }
                tableClass.fitTableColumns();
            }

            loadPerpotes((ClassComboxDTO) comboBoxClass.getSelectedItem());
        } catch (Throwable e) {
            e.printStackTrace();
            LogUtil.error(e.getMessage(), e);
        }
    }

    @Nullable
    @Override
    protected String getDimensionServiceKey() {
        return this.getClass().getName();
    }

    private void onCancel() {
        dispose();
    }

    public void removeAll(DefaultTableModel tm) {
        while (tm.getRowCount() > 0) {
            tm.removeRow(0);
        }
    }

    public List<PropertyDTO> getSelects() {
        int[] rows = tablePropertis.getSelectedRows();
        if (rows == null || rows.length < 1) {
            return null;
        }

        ArrayList<PropertyDTO> ps = new ArrayList();
        for (int row : rows) {
            ps.add(CollUtil.get(getResult(), row));
        }
        return ps;
    }

    public PropertyDTO getSelect() {
        PropertyDTO p = CollUtil.get(getResult(), tablePropertis.getSelectedRow());
        return p;
    }

    public void loadPerpotes(ClassComboxDTO c) {
        List<PropertyDTO> ps = propertyMap.get(c.getId());
        setPerpotiesTableRows(ps);
    }

    Vector<ClassComboxDTO> entits;
    ComponentDTO componentDTO;

    private Vector<ClassComboxDTO> readClassList() {
        String xml = FileUtil.readUtf8String(bmf.toNioPath().toFile());
        componentDTO = MeatBaseInfoReadUtil.readEntityList(xml);
        List<? extends ClassDTO> es = componentDTO.getClassDTOS();

        entits = new Vector(es.stream()
                .map(ClassComboxDTO::ofClassDTO)
                .collect(Collectors.toList()));
        for (ClassComboxDTO entit : entits) {
            entit.setOwnModule(componentDTO.getOwnModule());
        }

        propertyMap.clear();
        for (ClassComboxDTO v : entits) {
            propertyMap.put(v.getId(), V.get(v.getPerperties(), new ArrayList<>()));
        }

        return entits;
    }

    public PropertyDTO getRow(int row) {
        PropertyDTO p = CollUtil.get(getResult(), row);
        return p;
    }

    List<PropertyDTO> copyedPropertis;

    public void setPerpotiesTableRows(List<PropertyDTO> vs) {
        result = vs;

        removeAll(tableModelPropertis);
        if (CollUtil.isNotEmpty(vs)) {
            for (PropertyDTO e : vs) {
                Vector row = new Vector();
                for (int i = 0; i < tablePropAttrs.length; i++) {
                    row.add(ReflectUtil.getFieldValue(e, tablePropAttrs[i]));
                }
                tableModelPropertis.addRow(row);
            }
            tablePropertis.fitTableColumns();
        }
    }

    private void initListeners() {
        tablePropertis.addMouseListener(new ProperteListTableMouseListenerImpl(this));
        tableClass.addMouseListener(new ClassListTableMouseListenerImpl(this));
        comboBoxClass.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    loadPerpotes((ClassComboxDTO) e.getItem());
                }
            }
        });
    }

    @Override
    protected void doOKAction() {
        int updateClassPath = Messages.showYesNoDialog("确定保存修改到文件?"
                , "询问", Messages.getQuestionIcon());
        if (updateClassPath != Messages.OK) {
            return;
        }

        save2Files();
        // super.doOKAction();
    }

    @Override
    public void doCancelAction() {
        int updateClassPath = Messages.showYesNoDialog("你确定不保存文件,直接关闭?"
                , "询问", Messages.getQuestionIcon());
        if (updateClassPath != Messages.OK) {
            return;
        }

        super.doCancelAction();
    }

    public void addDef(PropertyDataTypeEnum type) {
        AddDefFieldDialog d = new AddDefFieldDialog(getProject(), this, type);
        if (d.showAndGet()) {
            String prefix = d.getTextFieldFieldCodePrefix().getText().trim();
            String prefixName = d.getTextFieldFieldNamePrefix().getText().trim();
            int start = d.getTextFieldNum().getNumber();
            int count = d.getTextFieldCount().getNumber();

            PropertyDTO p = d.getPropertyDTO();
            ArrayList<PropertyDTO> ps = new ArrayList<>(count << 1 + 16);
            for (int i = 0; i < count; i++) {
                try {
                    p = p.clone();
                    p.setId(StringUtil.uuid());
                    p.setFieldName(prefix + (start + i));
                    p.setName(prefixName + (start + i));
                    p.setDisplayName(d.getType().getTypeDisplayName() + (start + i));
                    p.fixDisplays();
                    ps.add(p);
                } catch (CloneNotSupportedException e) {
                }
            }

            past2LineAfter(ps, getTablePropertis().getSelectedRow() + 1);
        }
    }

    public void past2LineAfter(List<PropertyDTO> ps, int startRow) {
        if (startRow >= getResult().size()) {
            past2tail();
            return;
        }

        if (CollUtil.isEmpty(ps)) {
            return;
        }

        try {
            int i = 0;
            ArrayList<PropertyDTO> nps = new ArrayList<>();
            for (PropertyDTO p : getResult()) {
                if (i == startRow) {
                    for (PropertyDTO p2 : ps) {
                        PropertyDTO np = p2.clone();
                        np.setId(StringUtil.uuid());
                        np.setAttrsequence(i++);
                        np.setState(VOStatus.NEW);
                        nps.add(np);
                    }
                }

                p.setAttrsequence(i++);
            }

            getResult().addAll(startRow, nps);

            setPerpotiesTableRows(getResult());
        } catch (CloneNotSupportedException ex) {
            ex.printStackTrace();
        }
    }

    public void past2top() {
        List<PropertyDTO> ps = getCopyedPropertis();
        if (CollUtil.isEmpty(ps)) {
            return;
        }

        try {
            int i = 0;
            ArrayList<PropertyDTO> nps = new ArrayList<>();
            for (PropertyDTO p : ps) {
                PropertyDTO np = p.clone();
                np.setId(StringUtil.uuid());
                np.setAttrsequence(i++);
                np.setState(VOStatus.NEW);
                nps.add(np);
            }

            for (PropertyDTO p : getResult()) {
                p.setAttrsequence(i++);
            }

            getResult().addAll(0, nps);
            setPerpotiesTableRows(getResult());
        } catch (CloneNotSupportedException ex) {
            ex.printStackTrace();
        }
    }

    public void past2tail() {
        List<PropertyDTO> ps = getCopyedPropertis();
        if (CollUtil.isEmpty(ps)) {
            return;
        }

        try {
            int i = 0;
            ArrayList<PropertyDTO> nps = new ArrayList<>();
            for (PropertyDTO p : getResult()) {
                p.setAttrsequence(i++);
            }
            for (PropertyDTO p : ps) {
                PropertyDTO np = p.clone();
                np.setId(StringUtil.uuid());
                np.setAttrsequence(i++);
                np.setState(VOStatus.NEW);
                nps.add(np);
            }

            getResult().addAll(nps);
            setPerpotiesTableRows(getResult());
        } catch (CloneNotSupportedException ex) {
            ex.printStackTrace();
        }
    }
}
