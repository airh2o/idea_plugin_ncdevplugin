package com.air.nc5dev.editor.bmf.languageinfo.psi;

import com.air.nc5dev.editor.bmf.languageinfo.BmfMDPLanguage;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * <br>
 * <br>
 * <br>
 * <br>
 *
 * @author air Email: 209308343@qq.com
 * @version NC505, JDK1.5+, V1.0
 * @date 2020/1/17 0017 19:34
 * @project
 */
public class BmfElementType extends IElementType {
    public BmfElementType(@NotNull @NonNls String debugName) {
        super(debugName, BmfMDPLanguage.INSTANCE);
    }
}