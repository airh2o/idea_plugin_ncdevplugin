package com.air.nc5dev.ui;

import com.air.nc5dev.ui.actionurlsearch.NCCActionURLSearchView;
import com.air.nc5dev.util.idea.LogUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;

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
public class SearchNCRequestMappingDialog extends DialogWrapper {
    int height = 400;
    int width = 580;
    NCCActionURLSearchView contentPane;
    Project project;

    public SearchNCRequestMappingDialog(Project project) {
        super(project);
        this.project = project;
        createCenterPanel();
        init();
        setTitle("NCC&BIP URL&ACTION 搜一下");
    }

    @Override
    protected void doOKAction() {
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
        contentPane = new NCCActionURLSearchView(getProject());
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
