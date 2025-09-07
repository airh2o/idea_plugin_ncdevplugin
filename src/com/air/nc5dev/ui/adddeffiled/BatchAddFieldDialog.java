package com.air.nc5dev.ui.adddeffiled;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.air.nc5dev.util.CollUtil;
import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.util.V;
import com.air.nc5dev.util.XmlUtil;
import com.air.nc5dev.util.idea.LogUtil;
import com.air.nc5dev.util.meta.consts.PropertyDataTypeEnum;
import com.air.nc5dev.util.meta.xml.MetaAggVOConvertToXmlUtil;
import com.air.nc5dev.vo.meta.PropertyDTO;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.JBIntSpinner;
import com.intellij.ui.components.*;
import lombok.Data;
import nc.vo.pub.VOStatus;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/***
 *     弹框UI        <br>
 *           <br>
 *           <br>
 *           <br>
 * @author air Email: 209308343@qq.com
 * @date 2019/12/25 0025 15:29
 * @Param
 * @return
 */
@Data
public class BatchAddFieldDialog extends DialogWrapper {
    int height = 600;
    int width = 800;
    JBPanel contentPane;
    JBTextArea textArea;
    AddDefField2BmfDialog mainPanel;
    ProperteListTable tablePropertis;
    int rowIndex;

    public BatchAddFieldDialog(Project project, ProperteListTable tablePropertis, AddDefField2BmfDialog mainPane, int rowIndex) {
        super(project);
        this.tablePropertis = tablePropertis;
        this.mainPanel = mainPane;
        this.rowIndex = rowIndex;
        createCenterPanel();
        init();
        setTitle("批量新增字段");
    }

    private void createCenterPanel0() throws Exception {
        contentPane = new JBPanel();
        contentPane.setBounds(0, 0, getWidth(), getHeight());
        contentPane.setLayout(null);
        JBPanel panel_main = contentPane;
        {
            int x = 1;
            int y = 1;

            JBLabel label = new JBLabel("请输入内容： 字段编码 字段名称 字段类型 长度 回车换行， 用,，、空格 tab 均可分割!");
            label.setBounds(x, y, 500, 40);
            panel_main.add(label);

            textArea = new JBTextArea();
            JBScrollPane sp = new JBScrollPane(textArea);
            textArea.setEditable(true);
            textArea.setAutoscrolls(true);
            sp.setBounds(x, 60, getWidth() - 10, 500);
            panel_main.add(sp);

            initListeners();
        }

        //设置点默认值
        initDefualtValues();
    }


    @Override
    protected void doOKAction() {
        String str = getTextArea().getText();
        str = StrUtil.trim(str);
        if (StrUtil.isBlank(str)) {
            return;
        }

        str = StrUtil.replace(str, ";", "\n");
        List<String> lines = StrUtil.splitTrim(str, "\n");
        ArrayList<PropertyDTO> fs = Lists.newArrayList();
        ClassComboxDTO classDTO = (ClassComboxDTO) mainPanel.getComboBoxClass().getSelectedItem();
        for (String line : lines) {
            if (StrUtil.isBlank(line)) {
                continue;
            }

            line = StrUtil.replace(line, ",", " ");
            line = StrUtil.replace(line, "，", " ");
            line = StrUtil.replace(line, "、", " ");
            line = StrUtil.replace(line, "\t", " ");
            line = StrUtil.replace(line, "\r", " ");

            List<String> ks = StrUtil.splitTrim(line, " ");

            PropertyDataTypeEnum type = PropertyDataTypeEnum.agest(CollUtil.get(ks, 2)
                    , PropertyDataTypeEnum.BS000010000100001001);

            PropertyDTO p = new PropertyDTO()
                    .setClassID(classDTO.getId())
                    .setId(StringUtil.uuid())
                    .setFieldName(ks.get(0))
                    .setName(ks.get(0))
                    .setDisplayName(ks.get(1))
                    .setTypeName(type.getTypeName())
                    .setTypeDisplayName(type.getTypeDisplayName())
                    .setFieldType(type.getFieldType())
                    .setDbtype(type.getDbtype())
                    .setDataType(type.getDataType())
                    .setAttrLength(Convert.toInt(CollUtil.get(ks, 2), 100)
                    );
            p.fixDisplays();
            fs.add(p);
        }

        getMainPanel().past2LineAfter(fs, getRowIndex() + 1);

        super.doOKAction();
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
            textArea.setText("例如（长度不输入默认100）:\n"
                    + "其他\tnqt\tufdouble\n"
                    + "name\t名字\tString\n"
                    + "code,编码，string,200\n"
                    + "sex 性别，Integer;"
                    + "times,时间，UFDateTime\n"
                    + "\n \n \n \n 字段格式列表(可以用 BS000010000100001051 也可以 UFID 也可以 char 大小写不敏感 匹配不到默认String): \n \n "
                    + "字段格式列表: \n "
                    + "BS000010000100001001(\"String\", \"String\", \"BS000010000100001001\", \"varchar\", \"varchar\"),\n" +
                    "    BS000010000100001051(\"UFID\", \"UFID\", \"BS000010000100001051\", \"char\", \"char\"),\n" +
                    "    BS000010000100001004(\"Integer\", \"Integer\", \"BS000010000100001004\", \"int\", \"int\"),\n" +
                    "    BS000010000100001031(\"UFDouble\", \"UFDouble\", \"BS000010000100001031\", \"decimal\", \"decimal\"),\n" +
                    "    BS000010000100001032(\"UFBoolean\", \"UFBoolean\", \"BS000010000100001032\", \"char\", \"char\"),\n" +
                    "    BS000010000100001033(\"UFDate\", \"UFDate\", \"BS000010000100001033\", \"char\", \"char\"),\n" +
                    "    BS000010000100001037(\"UFDate_begin\", \"UFDate_begin\", \"BS000010000100001037\", \"char\", \"char\"),\n" +
                    "    BS000010000100001038(\"UFDate_end\", \"UFDate_end\", \"BS000010000100001038\", \"char\", \"char\"),\n" +
                    "    BS000010000100001039(\"UFLiteralDate\", \"UFLiteralDate\", \"BS000010000100001039\", \"char\", \"char\"),\n" +
                    "    BS000010000100001034(\"UFDateTime\", \"UFDateTime\", \"BS000010000100001034\", \"char\", \"char\"),\n" +
                    "    BS000010000100001036(\"UFTime\", \"UFTime\", \"BS000010000100001036\", \"char\", \"char\"),\n" +
                    "    BS000010000100001040(\"BigDecimal\", \"BigDecimal\", \"BS000010000100001040\", \"decimal\", \"decimal\"),\n" +
                    "    BS000010000100001052(\"UFMoney\", \"UFMoney\", \"BS000010000100001052\", \"decimal\", \"decimal\"),\n" +
                    "    BS000010000100001055(\"图片\", \"IMAGE\", \"BS000010000100001055\", \"image\", \"image\"),\n" +
                    "    BS000010000100001053(\"BLOB\", \"BLOB\", \"BS000010000100001053\", \"image\", \"image\"),\n" +
                    "    BS000010000100001059(\"自由项\", \"CUSTOM\", \"BS000010000100001059\", \"varchar\", \"varchar\"),\n" +
                    "    BS000010000100001030(\"备注\", \"MEMO\", \"BS000010000100001030\", \"varchar\", \"varchar\"),\n" +
                    "    BS000010000100001058(\"多语文本\", \"Multilangtext\", \"BS000010000100001058\", \"varchar\", \"varchar\"),\n" +
                    "    BS000010000100001056(\"自定义项\", \"CUSTOM\", \"BS000010000100001056\", \"varchar\", \"varchar\")"
            );
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

    private void initListeners() {
    }

}
