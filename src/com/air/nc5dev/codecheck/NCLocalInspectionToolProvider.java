package com.air.nc5dev.codecheck;

import com.intellij.codeInspection.InspectionToolProvider;
import com.intellij.codeInspection.LocalInspectionTool;
import org.jetbrains.annotations.NotNull;

/**
 * </br>
 * </br>
 * </br>
 *
 * @author 唐粟 Email:209308343@qq.com
 * @date 2022/8/12 0012 11:59
 * @project
 * @Version
 */
public class NCLocalInspectionToolProvider implements InspectionToolProvider {
    @NotNull
    @Override
    public Class<? extends LocalInspectionTool>[] getInspectionClasses() {
        return new Class[]{NCDefualtLocalInspectionTool.class};
    }

}
