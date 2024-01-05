package com.air.nc5dev.util.docgenerate;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiParameterList;
import com.intellij.psi.PsiPrimitiveType;
import com.intellij.psi.PsiReferenceList;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.javadoc.PsiDocComment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MethodJavaDocGenerator extends AbstractJavaDocGenerator<PsiMethod> {
    public MethodJavaDocGenerator(Project project) {
        super(project);
    }

    @Nullable
    @Override
    public PsiDocComment generate(PsiClass psiClass, @NotNull PsiMethod element) {
        JavaDoc javaDoc = new JavaDoc();
        String name = element.getName();
        // doc for description
        String description = PsiUtil.getDescription(element);

        if (null == description) {
            description = WebTranslateApi.translate(JavaBeansUtil.humpToSpace(name));
        }
        javaDoc.addDescription(description);
        // doc for parameter
        PsiParameterList parameterList = element.getParameterList();
        PsiParameter[] parameters = parameterList.getParameters();
        for (PsiParameter parameter : parameters) {
            String className = null;
            if (parameter instanceof PsiClassReferenceType) {
                className = ((PsiClassReferenceType) parameter.getType()).getClassName();
            }
            javaDoc.addParam(parameter.getName(), className);
        }
        // doc for return
        PsiType resultType = element.getReturnType();
        if (resultType instanceof PsiClassReferenceType) {
            PsiClassReferenceType psiClassReferenceType = (PsiClassReferenceType) resultType;
            String returnClass = psiClassReferenceType.getClassName();
            javaDoc.setRturn(returnClass);

            javaDoc.addSee(psiClassReferenceType.getClassName());

            // doc for see
            PsiType[] resultTypeParameters = psiClassReferenceType.getParameters();
            for (PsiType resultTypeParameter : resultTypeParameters) {
                javaDoc.addSee(((PsiClassReferenceType) resultTypeParameter).getClassName());
            }
        } else {
            PsiPrimitiveType psiPrimitiveType = (PsiPrimitiveType) resultType;
            if(!"void".equals(psiPrimitiveType.getName())) {
                javaDoc.setRturn(psiPrimitiveType.getName());
            }
        }

        PsiReferenceList throwsList = element.getThrowsList();
        PsiJavaCodeReferenceElement[] referenceElements = throwsList.getReferenceElements();
        for (PsiJavaCodeReferenceElement referenceElement : referenceElements) {
            javaDoc.addThrowz(referenceElement.getQualifiedName());
        }


        return psiElementFactory.createDocCommentFromText(javaDoc.toString());
    }
}
