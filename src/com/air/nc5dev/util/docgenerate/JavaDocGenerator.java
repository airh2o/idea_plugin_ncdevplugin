package com.air.nc5dev.util.docgenerate;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface JavaDocGenerator<T extends PsiElement> {

    /**
     * 生成
     *
     * @param psiClass {@link PsiClass}
     * @param element  元素 {@link T}
     * @return {@link PsiComment}
     * @see PsiComment
     */
    @Nullable
    PsiComment generate(PsiClass psiClass, @NotNull T element);

}
