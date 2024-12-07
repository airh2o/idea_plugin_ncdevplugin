package com.air.nc5dev.util.exportpatcher.searchs;

import com.air.nc5dev.vo.FileContentVO;
import com.air.nc5dev.vo.ExportConfigVO;
import com.air.nc5dev.vo.ExportContentVO;
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
public class ModuleLibFileContentSearchImpl extends AbstractContentSearchImpl {
    @Override
    public void search(ExportContentVO contentVO) {
        //循环模块，根据编译对象情况，输出补丁
        CompilerModuleExtension compilerModuleExtension;
        Iterator<Map.Entry<String, Module>> moduleIterator = contentVO.moduleHomeDir2ModuleMap.entrySet().iterator();
        Map.Entry<String, Module> entry;
        //模块 自定义输出配置信息
        ExportConfigVO configVO;

        String moduleName;
        ModuleWarpVO module;
        while (moduleIterator.hasNext() && !contentVO.indicator.isCanceled()) {
            //循环所有的模块
            module = new ModuleWarpVO(moduleIterator.next().getValue());
            if (contentVO.getModuleName2ExportModuleNameMap().containsKey(module.getModule().getName())) {
                module.setConfigName(contentVO.getModuleName2ExportModuleNameMap().get(module.getModule().getName()));
            }
            if (!contentVO.getSelectExportModules().contains(module.getModule())) {
                //需要跳过的模块
                continue;
            }

      //      compilerModuleExtension = CompilerModuleExtension.getInstance(module.getModule());

            //读取自定义配置文件
            configVO = contentVO.module2ExportConfigVoMap.get(module.getModule());
            if (configVO.isIgnoreModule()) {
                return ; //忽略模块!
            }

            //复制模块resoureces
            File resourcesDir = new File(new File(module.getModule().getModuleFilePath()).getParentFile(), "lib");
            if (contentVO.isExportModuleLib()
                    && resourcesDir.isDirectory()
                    && !contentVO.indicator.isCanceled()) {
                contentVO.indicator.setText("导出模块lib:" + resourcesDir.getPath());

                contentVO.addOutFiles(new FileContentVO()
                        .setModule(module)
                        .setFile(resourcesDir.getPath())
                        .setFileTo(new File(contentVO.outPath + File.separatorChar + module.getConfigName()
                                + File.separatorChar + resourcesDir.getName()).getPath())
                        .setName(FileContentVO.NAME_MODULE_RESOURCES)
                );
            }

            //模块循环结束
        }
    }
}
