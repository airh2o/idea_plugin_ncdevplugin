package com.air.nc5dev.util.exportpatcher.searchs;

import com.air.nc5dev.util.IoUtil;
import com.air.nc5dev.util.idea.LogUtil;
import com.air.nc5dev.vo.ExportConfigVO;
import com.air.nc5dev.vo.ExportContentVO;
import com.air.nc5dev.vo.FileContentVO;
import com.air.nc5dev.vo.ModuleWarpVO;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.CompilerModuleExtension;
import lombok.Data;

import java.io.File;
import java.util.Iterator;
import java.util.Map;

/**
 * </br>
 * </br>
 * </br>
 *
 * @author 唐粟 Email:209308343@qq.com
 * @date 2024/11/26 0026 19:28
 * @project
 * @Version
 */
@Data
public class NccloudHotwebsResourcesFileContentSearchImpl extends AbstractContentSearchImpl {
    @Override
    public void search(ExportContentVO contentVO) {
        File dist = new File(new File(contentVO.getHotwebsResourcePath()), "dist");
        if (!dist.isDirectory()) {
            return;
        }

        if (dist.listFiles() == null || dist.listFiles().length < 1) {
            return;
        }

        if (!contentVO.isExportResources()
                || contentVO.indicator.isCanceled()) {
            return;
        }

        File p_hotwebs = new File(new File(contentVO.getOutPath()).getParentFile(), "hotwebs");
        File nccloud = new File(p_hotwebs, "nccloud");
        File resources = new File(nccloud, "resources");

        contentVO.indicator.setText("导出模块resources:" + resources.getPath());
        contentVO.addOutFiles(new FileContentVO()
                .setModule(null)
                .setFile(dist.getPath())
                .setFileTo(resources.getPath())
                .setHasChildDir(true)
                .setName(FileContentVO.NAME_MODULE_RESOURCES)
        );
    }
}
