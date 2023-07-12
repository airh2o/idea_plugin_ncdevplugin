package com.air.nc5dev.acion.meta;

import cn.hutool.core.io.FileUtil;
import com.air.nc5dev.util.idea.ProjectUtil;

import java.io.File;

/**
 *
 */
public class MakeBmf4Lfw1z1zBillAction extends AbstractMakeBmfAction {
    @Override
    public String getBmfTemplateStr() {
       return getResourceTemplates("lfwdemo.bmf");
    }

    @Override
    public int getType() {
        return 2;
    }
}
