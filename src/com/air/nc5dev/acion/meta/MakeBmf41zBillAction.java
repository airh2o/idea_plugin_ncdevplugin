package com.air.nc5dev.acion.meta;

import cn.hutool.core.io.FileUtil;
import com.air.nc5dev.util.idea.ProjectUtil;

import java.io.File;

/**
 *
 */
public class MakeBmf41zBillAction extends AbstractMakeBmfAction {
    @Override
    public String getBmfTemplateName() {
        return "singleHdemo.bmf";
    }

    @Override
    public int getType() {
        return 4;
    }
}
