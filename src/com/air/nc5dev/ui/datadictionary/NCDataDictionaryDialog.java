package com.air.nc5dev.ui.datadictionary;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.http.HttpUtil;
import com.air.nc5dev.util.*;
import com.air.nc5dev.vo.NCDataSourceVO;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.*;
import com.intellij.ui.jcef.JBCefBrowser;
import com.intellij.ui.jcef.JBCefJSQuery;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.util.List;
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
@Data
public class NCDataDictionaryDialog extends DialogWrapper {
    JComponent contentPane;
    Project project;
    ComboBox<NCDataSourceVO> ncDataSourceVOComboBox;

    public NCDataDictionaryDialog(Project project) {
        super(project);
        this.project = project;
        init();
        setTitle("NC数据字典");
    }

    public JComponent getContentPane() {
        return createCenterPanel();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        if (contentPane == null) {
            contentPaneInit();
        }

        return this.contentPane;
    }

    private void contentPaneInit() {
        JScrollPane sp = new JScrollPane();
        contentPane = sp;

        List<NCDataSourceVO> dataSourceVOS = NCPropXmlUtil.getDataSourceVOS(getProject());
        ComboBoxModel comboBoxModel = new DefaultComboBoxModel(new Vector(
                dataSourceVOS.stream()
                        .map(v -> v.getDataSourceName() + '/' + v.getUser())
                        .collect(Collectors.toList())
        ));
        ncDataSourceVOComboBox = new ComboBox<>();
        ncDataSourceVOComboBox.addItemListener((e) -> onNCDataSourceSelectChange(e));

        sp.setLayout(null);
        sp.setBounds(1, 1, 1000, 800);

        ncDataSourceVOComboBox.setBounds(1, 1, 70, 400);
        sp.add(ncDataSourceVOComboBox);

        JBCefBrowser browser = new JBCefBrowser();
        browser.loadHTML(FileUtil.readUtf8String("F:\\temp\\1.html"));
        browser.getComponent().setBounds(1, ncDataSourceVOComboBox.getHeight() + 5, 990, 780);

      /*  // Create a JS query instance
        JBCefJSQuery openInBrowserJsQuery =
                JBCefJSQuery.create(browser);

        // Add a query handler
        openInBrowserJsQuery.addHandler((link) -> {
            // handle link here
            return null; // can respond back to JS with JBCefJSQuery.Response
        });

        // Inject the query callback into JS
        browser.executeJavaScript(
                "window.JavaPanelBridge = {" +
                        "openInExternalBrowser : function(link) {" +
                        openInBrowserJsQuery.inject("link") +
                        "}" +
                        "};",
                browser.getURL(), 0);*/

        contentPane = browser.getComponent();
        sp.add(browser.getComponent());

/*

        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem menuItem = new JMenuItem("切换数据源");
        menuItem.addActionListener(event -> {

        });
        contentPane.setComponentPopupMenu(popupMenu);
*/

        //设置点默认值
        initDefualtValues();
    }


    public void initDefualtValues() {
    }

    public void onNCDataSourceSelectChange(ItemEvent e) {

    }

    @Nullable
    @Override
    protected String getDimensionServiceKey() {
        return this.getClass().getName();
    }

    private void onCancel() {
        dispose();
    }
}
