package com.air.nc5dev.util.docgenerate;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.javadoc.PsiDocComment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FieldJavaDocGenerator extends AbstractJavaDocGenerator<PsiField> {
    public FieldJavaDocGenerator(Project project) {
        super(project);
    }

    @Nullable
    @Override
    public PsiDocComment generate(PsiClass psiClass, @NotNull PsiField element) {
        JavaDoc javaDoc = new JavaDoc();
        String name = element.getName();
        String description = PsiUtil.getDescription(element);
        if (null == description) {
            description = WebTranslateApi.translate(name);//JavaBeansUtil.humpToSpace(
        }

        // doc for description
        javaDoc.addDescription(description);

        return psiElementFactory.createDocCommentFromText(javaDoc.toString());
    }
}
