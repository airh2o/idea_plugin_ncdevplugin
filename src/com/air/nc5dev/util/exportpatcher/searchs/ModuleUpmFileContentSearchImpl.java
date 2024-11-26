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
public class ModuleUpmFileContentSearchImpl extends AbstractContentSearchImpl {
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

          //  compilerModuleExtension = CompilerModuleExtension.getInstance(module.getModule());

            //读取自定义配置文件
            configVO = contentVO.module2ExportConfigVoMap.get(module.getModule());
            if (configVO.isIgnoreModule()) {
                return ; //忽略模块!
            }

            File libDir = new File(new File(module.getModule().getModuleFilePath()).getParentFile(), "META-INF");
            if (contentVO.exportModuleMeteinfo && libDir.isDirectory() && !contentVO.indicator.isCanceled()) {
                contentVO.indicator.setText("导出模块META-INF:" + libDir.getPath());
                contentVO.addOutFiles(new FileContentVO()
                        .setModule(module)
                        .setFile(libDir.getPath())
                        .setFileTo(new File(contentVO.outPath + File.separatorChar + module.getConfigName()
                                + File.separatorChar + libDir.getName()).getPath())
                        .setFilter(
                                configVO.notExportModelueXml ? f -> !"module.xml".equalsIgnoreCase(f.getName()) : f -> true
                        )
                        .setName(FileContentVO.NAME_MODULE_LIB)
                );
            }

            //模块循环结束
        }
    }
}
