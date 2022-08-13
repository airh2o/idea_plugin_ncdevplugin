package com.air.nc5dev.codecheck;

import com.intellij.codeInspection.ex.GlobalInspectionContextImpl;
import com.intellij.codeInspection.ex.InspectionProfileImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.ui.content.ContentManager;
import org.jetbrains.annotations.NotNull;

/**
 * </br>
 * </br>
 * </br>
 *
 * @author 唐粟 Email:209308343@qq.com
 * @date 2022/8/12 0012 15:39
 * @project
 * @Version
 */
public class NCInspectionContextImpl extends GlobalInspectionContextImpl {
    public NCInspectionContextImpl(@NotNull Project project,
                                   @NotNull NotNullLazyValue<? extends ContentManager> contentManager) {
        super(project, contentManager);
    }

}
