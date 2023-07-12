package com.air.nc5dev.acion.meta;

import cn.hutool.core.io.FileUtil;
import com.air.nc5dev.util.idea.ProjectUtil;

import java.io.File;

/**
 *
 */
public class MakeBmf4skBillAction extends AbstractMakeBmfAction {
    @Override
    public String getBmfTemplateStr() {
        File resourceTemplates = ProjectUtil.getResourceTemplates("treeCard.bmf");
        return FileUtil.readUtf8String(resourceTemplates);
    }

    @Override
    public int getType() {
        return 1;
    }
}
