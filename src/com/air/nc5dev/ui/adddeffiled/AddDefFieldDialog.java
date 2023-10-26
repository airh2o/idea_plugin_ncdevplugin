package com.air.nc5dev.ui.adddeffiled;

import cn.hutool.core.io.FileUtil;
import com.air.nc5dev.ui.exportbmf.ExportbmfDialog;
import com.air.nc5dev.util.CollUtil;
import com.air.nc5dev.util.ReflectUtil;
import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.util.V;
import com.air.nc5dev.util.idea.LogUtil;
import com.air.nc5dev.util.meta.consts.PropertyDataTypeEnum;
import com.air.nc5dev.util.meta.xml.MeatBaseInfoReadUtil;
import com.air.nc5dev.vo.meta.ClassDTO;
import com.air.nc5dev.vo.meta.ComponentDTO;
import com.air.nc5dev.vo.meta.PropertyDTO;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBIntSpinner;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.*;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.*;
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
@Data
public class AddDefFieldDialog extends DialogWrapper {
    PropertyDataTypeEnum type;
    AddDefField2BmfDialog addDefField2BmfDialog;
    int height = 100;
    int width = 400;
    JBPanel contentPane;
    JBTextField textFieldFieldCodePrefix;
    JBTextField textFieldFieldNamePrefix;
    JBIntSpinner textFieldNum;
    JBIntSpinner textFieldCount;
    PropertyDTO propertyDTO;

    public AddDefFieldDialog(Project project, AddDefField2BmfDialog addDefField2BmfDialog, PropertyDataTypeEnum type) {
        super(project);
        this.addDefField2BmfDialog = addDefField2BmfDialog;
        this.type = type;
        this.propertyDTO = addDefField2BmfDialog.getSelect();
        createCenterPanel();
        init();
        setTitle("新增字段");
    }

    private void createCenterPanel0() throws Exception {
        contentPane = new JBPanel();
        contentPane.setBounds(0, 0, getWidth(), getHeight());
        JBPanel panel_main = contentPane;
        {
            int x = 1;
            int y = 1;

            JBLabel label = new JBLabel("字段编码前缀:");
            label.setBounds(x, y, 80, 40);
            panel_main.add(label);
            textFieldFieldCodePrefix = new JBTextField();
            textFieldFieldCodePrefix.setBounds(x += label.getWidth() + 2, y, 150, 40);
            panel_main.add(textFieldFieldCodePrefix);

            label = new JBLabel("字段名称前缀:");
            label.setBounds(x, y, 80, 40);
            panel_main.add(label);
            textFieldFieldNamePrefix = new JBTextField();
            textFieldFieldNamePrefix.setBounds(x += label.getWidth() + 2, y, 150, 40);
            panel_main.add(textFieldFieldNamePrefix);

            label = new JBLabel("开始数字:");
            label.setBounds(x += textFieldFieldNamePrefix.getWidth() + 3, y, 80, 40);
            panel_main.add(label);
            textFieldNum = new JBIntSpinner(1, 1, Integer.MAX_VALUE);
            textFieldNum.setBounds(x += label.getWidth() + 2, y, 60, 40);
            panel_main.add(textFieldNum);

            label = new JBLabel("增加字段数量:");
            label.setBounds(x += textFieldNum.getWidth() + 3, y, 80, 40);
            panel_main.add(label);
            textFieldCount = new JBIntSpinner(1, 1, Integer.MAX_VALUE);
            textFieldCount.setBounds(x += label.getWidth() + 2, y, 60, 40);
            panel_main.add(textFieldCount);

            initListeners();
        }

        //设置点默认值
        initDefualtValues();
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
            int i = 1;


            textFieldFieldCodePrefix.setText(removeNumEnd(propertyDTO.getFieldName()));
            textFieldFieldNamePrefix.setText(removeNumEnd(propertyDTO.getDisplayName()));

            for (PropertyDTO p : addDefField2BmfDialog.getResult()) {
                if (type.is(PropertyDataTypeEnum.ofTypeDefualt(p.getDataType()))) {
                    ++i;
                }
            }

            textFieldNum.setNumber(i);
            textFieldCount.setNumber(50);
        } catch (Throwable e) {
            e.printStackTrace();
            LogUtil.error(e.getMessage(), e);
        }
    }

    private String removeNumEnd(String n) {
        char[] cs = n.toCharArray();
        for (int i = cs.length - 1; i > -1; i--) {
            try {
                Integer.parseInt(cs[i] + "");
            } catch (NumberFormatException e) {
                break;
            }
            cs[i] = ' ';
        }

        return new String(cs).trim();
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
