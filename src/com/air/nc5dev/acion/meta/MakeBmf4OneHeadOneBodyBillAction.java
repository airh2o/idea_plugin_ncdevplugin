package com.air.nc5dev.acion.meta;

import cn.hutool.core.io.FileUtil;
import com.air.nc5dev.acion.base.AbstractIdeaAction;
import com.air.nc5dev.ui.MakeBmfDialog;
import com.air.nc5dev.ui.MstscDialog;
import com.air.nc5dev.util.IdeaProjectGenerateUtil;
import com.air.nc5dev.util.ProjectNCConfigUtil;
import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.util.idea.ApplicationLibraryUtil;
import com.air.nc5dev.util.idea.LogUtil;
import com.air.nc5dev.util.idea.ProjectUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

import java.io.File;

/**
 * NC主子表单据
 */
public class MakeBmf4OneHeadOneBodyBillAction extends AbstractMakeBmfAction {
    @Override
    public String getBmfTemplateStr() {
      return getResourceTemplates("demo.bmf");
    }

    @Override
    public int getType() {
        return 0;
    }
}
