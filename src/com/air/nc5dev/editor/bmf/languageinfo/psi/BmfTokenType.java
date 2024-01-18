package com.air.nc5dev.editor.bmf.languageinfo.psi;

import com.air.nc5dev.editor.bmf.languageinfo.BmfMDPLanguage;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * </br>
 * </br>
 * </br>
 * </br>
 *
 * @author air Email: 209308343@qq.com
 * @version NC505, JDK1.5+, V1.0
 * @date 2020/1/17 0017 19:33
 * @project
 */
public class BmfTokenType  extends IElementType {
    public BmfTokenType(@NotNull @NonNls String debugName) {
        super(debugName, BmfMDPLanguage.INSTANCE);
    }

    @Override
    public String toString() {
        return "bmfTokenType." + super.toString();
    }
}