package com.air.nc5dev.util.docgenerate;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.javadoc.PsiDocComment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author beeant
 */
public abstract class AbstractJavaDocGenerator<T extends PsiElement> implements JavaDocGenerator<T> {

    public PsiElementFactory psiElementFactory;

    public AbstractJavaDocGenerator(Project project) {
        super();
        psiElementFactory = PsiElementFactory.getInstance(project);
    }

    @Nullable
    @Override
    public abstract PsiDocComment generate(PsiClass psiClass, @NotNull T element);
}
