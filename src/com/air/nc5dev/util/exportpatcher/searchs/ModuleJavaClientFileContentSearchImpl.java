package com.air.nc5dev.util.exportpatcher.searchs;

import com.air.nc5dev.enums.NcVersionEnum;
import com.air.nc5dev.util.IoUtil;
import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.util.idea.ProjectUtil;
import com.air.nc5dev.vo.ExportConfigVO;
import com.air.nc5dev.vo.ExportContentVO;
import com.air.nc5dev.vo.FileContentVO;
import com.air.nc5dev.vo.ModuleWarpVO;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
public class ModuleJavaClientFileContentSearchImpl extends ModuleJavaFileContentSearchImpl {
    @Override
    public void export(@NotNull String ncType
            , @NotNull String exportDir
            , @NotNull ModuleWarpVO module
            , @NotNull String sourceRoot
            , @NotNull String classDir
            , @Nullable String testClassDir
            , @NotNull ExportContentVO contentVO) {
        if (!NC_TYPE_CLIENT.equals(ncType)) {
            return;
        }

        super.export(ncType, exportDir, module, sourceRoot, classDir, testClassDir, contentVO);
    }
}
