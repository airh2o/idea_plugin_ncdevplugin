package com.air.nc5dev.codecheck;

import com.intellij.codeInspection.ex.InspectionProfileImpl;
import com.intellij.codeInspection.ex.InspectionToolsSupplier;
import com.intellij.profile.codeInspection.BaseInspectionProfileManager;
import org.jetbrains.annotations.NotNull;

/**
 *
 * </br>
 * </br>
 * </br>
 *
 * @author 唐粟 Email:209308343@qq.com
 * @date 2022/8/12 0012 17:42
 * @project
 * @Version
 */
public class NCGlobalInspectionContextImpl extends InspectionProfileImpl {


    public NCGlobalInspectionContextImpl(@NotNull String profileName
            , @NotNull InspectionToolsSupplier toolSupplier
            , @NotNull BaseInspectionProfileManager profileManager) {
        super(profileName, toolSupplier, profileManager);
    }
}
