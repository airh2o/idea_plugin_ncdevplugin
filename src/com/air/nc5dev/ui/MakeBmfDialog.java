package com.air.nc5dev.ui;

import com.air.nc5dev.enums.NcVersionEnum;
import com.air.nc5dev.util.BmfUtil;
import com.air.nc5dev.util.ExceptionUtil;
import com.air.nc5dev.util.ProjectNCConfigUtil;
import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.util.idea.LogUtil;
import com.air.nc5dev.util.idea.ProjectUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTextField;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComboBox;
import javax.swing.JComponent;

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
@Data
public class MakeBmfDialog extends DialogWrapper {
    String path;
    int type;
    JComponent contentPane;
    int height = 400;
    int width = 580;
    JBPanel panel_info;
    Project project;
    String module;
    String template;
    JBTextField textField_module;
    JBTextField textField_billtype;
    JBTextField textField_billtypename;
    JBTextField textField_langCode;
    JBTextField textField_defnum;
    JBTextField textField_defnumPerfix;
    JBTextField textField_path;
    JComboBox comboBox_version;

    public MakeBmfDialog(Project project, int type, String module, String template, String path) {
        super(project);
        this.type = type;
        this.module = module;
        this.project = project;
        this.template = template;
        this.path = path;
        createCenterPanel();
        init();
        setTitle("生成元数据");
    }

    @Override
    protected void doOKAction() {
        int updateClassPath = Messages.showYesNoDialog(
                "是否继续?"
                , "询问", Messages.getQuestionIcon());
        if (updateClassPath != Messages.OK) {
            return;
        }

        Exception ex = null;

        try {
            String templateStr = null;
            NcVersionEnum ncVersionEnum = (NcVersionEnum) comboBox_version.getSelectedItem();
            if (NcVersionEnum.isNCCOrBIP(ncVersionEnum)) {
                templateStr = ProjectUtil.getResourceTemplatesUtf8Txt("meta/ncc/" + template);
            } else if (NcVersionEnum.NC6.equals(ncVersionEnum)) {
                templateStr = ProjectUtil.getResourceTemplatesUtf8Txt("meta/6/" + template);
            }else if (NcVersionEnum.NC5.equals(ncVersionEnum)) {
                templateStr = ProjectUtil.getResourceTemplatesUtf8Txt("meta/5/" + template);
            }else if (NcVersionEnum.U8Cloud.equals(ncVersionEnum)) {
                templateStr = ProjectUtil.getResourceTemplatesUtf8Txt("meta/5/" + template);
            }

            if (StringUtil.isBlank(templateStr)) {
                throw new RuntimeException("不支持的NC版本(也许你可以试试用NC6的元数据看看是否通用):" + ncVersionEnum);
            }

            BmfUtil.builder()
                    .billname(textField_billtypename.getText().trim())
                    .billtype(textField_billtype.getText().trim())
                    .defName(textField_defnumPerfix.getText().trim())
                    .defnum(Integer.parseInt(textField_defnum.getText().trim()))
                    .langcode(textField_langCode.getText().trim())
                    .modulecode(textField_module.getText().trim())
                    .outfilepath(textField_path.getText().trim())
                    .template(templateStr)
                    .type(type)
                    .versionEnum(ncVersionEnum)
                    .build().newbmf();
        } catch (Exception e) {
            ex = e;
            LogUtil.error(e.toString(), e);
            Messages.showInfoMessage(ExceptionUtil.toStringLines(e, 20), "出错啦:");
        }

        if (ex != null) {
            return;
        }

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

    private void createCenterPanel0() throws Exception {
        JBPanel jtab = new JBPanel();
        contentPane = jtab;
        contentPane.setBounds(0, 0, getWidth(), getHeight());
        JBLabel label;

        {
            int x = 5;
            int y = 1;
            int h = 30;
            int w = 570;

            panel_info = jtab;
            panel_info.setLayout(null);
            panel_info.setBounds(0, 0, width, height);

            label = new JBLabel("NC版本:");
            label.setBounds(x, y += h + 2, w, h);
            panel_info.add(label);
            comboBox_version = new JComboBox<NcVersionEnum>(NcVersionEnum.values());
            comboBox_version.setSelectedItem(ProjectNCConfigUtil.getNCVersion());
            comboBox_version.setBounds(x, y += h + 2, w, h);
            panel_info.add(comboBox_version);

            label = new JBLabel("模块:");
            label.setBounds(x, y += h + 2, w, h);
            panel_info.add(label);
            textField_module = new JBTextField();
            textField_module.setBounds(x, y += h + 2, w, h);
            panel_info.add(textField_module);

            label = new JBLabel("多语模块编码:");
            label.setBounds(x, y += h + 2, w, h);
            panel_info.add(label);
            textField_langCode = new JBTextField();
            textField_langCode.setBounds(x, y += h + 2, w, h);
            panel_info.add(textField_langCode);

            label = new JBLabel("单据编码:");
            label.setBounds(x, y += h + 2, w, h);
            panel_info.add(label);
            textField_billtype = new JBTextField();
            textField_billtype.setBounds(x, y += h + 2, w, h);
            panel_info.add(textField_billtype);

            label = new JBLabel("单据名称:");
            label.setBounds(x, y += h + 2, w, h);
            panel_info.add(label);
            textField_billtypename = new JBTextField();
            textField_billtypename.setBounds(x, y += h + 2, w, h);
            panel_info.add(textField_billtypename);

            label = new JBLabel("自定义项数量:");
            label.setBounds(x, y += h + 2, w, h);
            panel_info.add(label);
            textField_defnum = new JBTextField();
            textField_defnum.setBounds(x, y += h + 2, w, h);
            panel_info.add(textField_defnum);

            label = new JBLabel("自定义项前缀(多个用,隔开.比如 vdef,vbdef 意思是主实体用vdef,其他实体用vbddef. 比如 vdef 就是都用vdef):");
            label.setBounds(x, y += h + 2, w, h);
            panel_info.add(label);
            textField_defnumPerfix = new JBTextField();
            textField_defnumPerfix.setBounds(x, y += h + 2, w, h);
            panel_info.add(textField_defnumPerfix);

            label = new JBLabel("文件输出路径:");
            label.setBounds(x, y += h + 2, w, h);
            panel_info.add(label);
            textField_path = new JBTextField();
            textField_path.setBounds(x, y += h + 2, w, h);
            panel_info.add(textField_path);
        }

        //设置点默认值
        initDefualtValues();
    }

    public void initDefualtValues() {
        textField_module.setText(module);
        textField_langCode.setText(module);
        textField_defnum.setText("50");
        textField_defnumPerfix.setText("vdef,vbdef");
        textField_path.setText(path);
    }

    @Nullable
    @Override
    protected String getDimensionServiceKey() {
        return this.getClass().getName();
    }

    private void onCancel() {
        dispose();
    }

    @Override
    public void doCancelAction() {
        super.doCancelAction();
    }

    public JComponent getContentPane() {
        return createCenterPanel();
    }
}
