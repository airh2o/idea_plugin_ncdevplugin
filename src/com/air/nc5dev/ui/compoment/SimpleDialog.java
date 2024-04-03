package com.air.nc5dev.ui.compoment;

import com.air.nc5dev.ui.PatcherDialog;
import com.air.nc5dev.ui.patcherselectfile.PatcherSelectFilesTableMouseListenerImpl;
import com.air.nc5dev.util.CollUtil;
import com.air.nc5dev.util.idea.LogUtil;
import com.air.nc5dev.vo.PatcherSelectFileVO;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTabbedPane;
import lombok.Data;
import org.jetbrains.annotations.Nullable;
import org.tangsu.mstsc.ui.BaseSimpleListTable;
import org.tangsu.mstsc.vo.WindowSizeVO;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

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
public class SimpleDialog extends DialogWrapper {
    Project project;
    JComponent component;
    JBScrollPane jbScrollPane;

    public SimpleDialog(Project project, String title, JComponent component) {
        super(project);
        this.project = project;
        this.component = component;
        this.jbScrollPane = new JBScrollPane(component);
        createCenterPanel();
        init();
        setTitle(title);
    }

    @Override
    public void doCancelAction() {
        super.doCancelAction();
    }

    public JComponent getContentPane() {
        return createCenterPanel();
    }

    @Override
    protected void doOKAction() {
        super.doOKAction();
    }

    @Override
    protected JComponent createCenterPanel() {
        return jbScrollPane;
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
