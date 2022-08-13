package com.air.nc5dev.acion;

import com.air.nc5dev.acion.base.AbstractIdeaAction;
import com.air.nc5dev.util.docgenerate.*;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

public class JavadocGenerateAction extends AbstractIdeaAction {
    @Override
    protected void doHandler(AnActionEvent e) throws IOException, URISyntaxException {
        Editor editor = LangDataKeys.EDITOR.getData(e.getDataContext());
        assert editor != null;
        int startPosition = editor.getSelectionModel().getSelectionStart();
        int endPosition = editor.getSelectionModel().getSelectionEnd();

        PsiClass psiClass = PsiUtil.getPsiMethodFromContext(e);
        assert psiClass != null;
        Project project = psiClass.getProject();
        final PsiFile file = psiClass.getContainingFile();

        List<PsiElement> elements = new LinkedList<>();
        PsiElement element = PsiUtil.getJavaElement(PsiUtil.getPsiElement(file, startPosition));
        do {
            if (PsiUtil.isAllowedElementType(element)) {
                elements.add(element);
            }
            element = element.getNextSibling();
            if (element == null) {
                break;
            }
        } while (PsiUtil.isElementInSelection(element, startPosition, endPosition));
        WriteCommandAction.writeCommandAction(project, file)
                .withGlobalUndo()
                .run(() -> generateJavadoc(psiClass, elements, editor));
    }


    @SuppressWarnings("rawtypes")
    private void generateJavadoc(PsiClass psiClass, List<PsiElement> elements, Editor editor) {
        for (PsiElement element : elements) {
            JavaDocGenerator generator = getGenerator(element);
            if (null != generator) {
                PsiComment docComment = generator.generate(psiClass, element);
                element.addBefore(docComment, element.getFirstChild());
            }
        }

    }


    /**
     * Gets the generator.
     *
     * @param element the Element
     * @return the Generator
     */
    @Nullable
    @SuppressWarnings("rawtypes")
    protected JavaDocGenerator getGenerator(@NotNull PsiElement element) {
        Project project = element.getProject();
        JavaDocGenerator generator = null;
        if (PsiClass.class.isAssignableFrom(element.getClass())) {
            generator = new ClassJavaDocGenerator(project);
        } else if (PsiMethod.class.isAssignableFrom(element.getClass())) {
            generator = new MethodJavaDocGenerator(project);
        } else if (PsiField.class.isAssignableFrom(element.getClass())) {
            generator = new FieldJavaDocGenerator(project);
        }
        return generator;
    }


}
