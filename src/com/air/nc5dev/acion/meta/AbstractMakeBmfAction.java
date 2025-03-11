package com.air.nc5dev.acion.meta;

import com.air.nc5dev.acion.base.AbstractIdeaAction;
import com.air.nc5dev.ui.MakeBmfDialog;
import com.air.nc5dev.util.IdeaProjectGenerateUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;

import java.io.File;

/**
 * NC主子表单据
 */
public abstract class AbstractMakeBmfAction extends AbstractIdeaAction {

    @Override
    protected void doHandler(AnActionEvent e) {
        Project project = e.getProject();
        Module module = LangDataKeys.MODULE.getData(e.getDataContext());

        String moduleName = "";
        String template = getBmfTemplateName();
        File path = null;

        if (module != null) {
            moduleName = module.getName();
            path = new File(IdeaProjectGenerateUtil.getModuleBaseDir(module), "METADATA");
        } else {
            moduleName = project.getName();
            path = new File(project.getBasePath(), "METADATA");
        }

        if (!path.isDirectory()) {
            path.mkdirs();
        }

        MakeBmfDialog dialog = new MakeBmfDialog(
                e.getProject()
                , getType()
                , moduleName
                , template
                , path.toString()
        );
        dialog.setModal(false);
        dialog.show();
    }

    public abstract String getBmfTemplateName();

    public abstract int getType();
}
