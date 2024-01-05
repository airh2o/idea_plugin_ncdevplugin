package com.air.nc5dev.acion;

import com.air.nc5dev.acion.base.AbstractBaseIntentionAction;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDeclarationStatement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLocalVariable;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.impl.source.PsiJavaCodeReferenceElementImpl;
import com.intellij.psi.impl.source.tree.java.PsiLocalVariableImpl;
import com.intellij.psi.impl.source.tree.java.PsiReferenceExpressionImpl;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiTypesUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import java.util.Hashtable;
import java.util.regex.Pattern;

/**
 * 一键生成set方法，感谢原作者 hjp22222@163.com！
 * <p>
 * Created by IntelliJ IDEA.
 * User: donnie
 * email:hjp22222@163.com
 * blog: www.okhjp.com
 * Date: 2013-6-1 Time: 上午11:21
 * <p>
 * desc ：
 */

public class GenerateAllSetterAction extends AbstractBaseIntentionAction {

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) throws
            IncorrectOperationException {
        final int offset = editor.getCaretModel().getOffset();
        final Document document = editor.getDocument();
        int lineNum = document.getLineNumber(offset);
        int startOffset = document.getLineStartOffset(lineNum);

        CharSequence editorText = document.getCharsSequence();
        int wordStartOffset = getWordStartOffset(editorText, offset);
        final int distance = wordStartOffset - startOffset;

        //PsiIdentifierImpl psiIdentifier = (PsiIdentifierImpl) psiElement;
        PsiClass psiClass = getLocalVarialbeContainingClass(psiElement);
        String name = psiClass.getName();
        String firstChar = psiClass.getName().substring(0, 1).toLowerCase();
        final String className = firstChar + name.substring(1, name.length());

        String visibleName = className;
        if (psiElement.getParent() instanceof PsiReferenceExpressionImpl) {
            //表达式调用
        } else if (psiElement.getParent() instanceof PsiJavaCodeReferenceElementImpl) {
            //类名调用
        }
        if (psiElement.getParent() instanceof PsiLocalVariableImpl) {
            //变量调用
            visibleName = psiElement.getText();
        }
        final String invokeVisibleName = visibleName;


        // 定义正则表达式，从方法中过滤出 setter 函数.
        String ss = "set(\\w+)";
        Pattern setM = Pattern.compile(ss);
        // 把方法中的"set" 或者 "get" 去掉
        String rapl = "$1";
        String param;

        // 存放set方法
        final Hashtable<String, String> setMethods = new Hashtable<String, String>();
        PsiMethod[] methods = psiClass.getMethods();
        for (PsiMethod method : methods) {
            String methodName = method.getName();
            if (Pattern.matches(ss, methodName)) {
                param = setM.matcher(methodName).replaceAll(rapl).toLowerCase();
                setMethods.put(param, methodName);
                //methods[7].getParameterList().getParameters()[0].getType().getCanonicalText()
            } else {
            }
        }


        Application application = ApplicationManager.getApplication();
        application.runWriteAction(new Runnable() {
            public void run() {
                String blankSpace = "";
                for (int i = 0; i < distance; i++) {
                    blankSpace = blankSpace + " ";
                }

                int lineNumber = document.getLineNumber(offset) + 1;
                for (String arg : setMethods.keySet()) {
                    int lineStartOffset = document.getLineStartOffset(lineNumber++);
                    document.insertString(lineStartOffset, blankSpace + invokeVisibleName + "." + setMethods.get(arg)
                            + "" +
                            "(     );" +
                            "\n");
                    editor.getCaretModel().moveToOffset(lineStartOffset + 2);
                    editor.getScrollingModel().scrollToCaret(ScrollType.MAKE_VISIBLE);
                }

                document.insertString(document.getLineStartOffset(lineNumber++), "\n");
            }
        });
    }

    @NotNull
    @Override
    public String getText() {
        return "生成所有set方法调用";
    }

    private int getWordStartOffset(CharSequence editorText, int cursorOffset) {
        if (editorText.length() == 0) return 0;
        if (cursorOffset > 0 && !Character.isJavaIdentifierPart(editorText.charAt(cursorOffset))
                && Character.isJavaIdentifierPart(editorText.charAt(cursorOffset - 1))) {
            cursorOffset--;
        }

        if (Character.isJavaIdentifierPart(editorText.charAt(cursorOffset))) {
            int start = cursorOffset;
            int end = cursorOffset;

            //定位开始位置
            while (start > 0 && Character.isJavaIdentifierPart(editorText.charAt(start - 1))) {
                start--;
            }
            return start;

        }

        return 0;

    }

    public static PsiClass getLocalVarialbeContainingClass(PsiElement element) {
        PsiElement psiParent = PsiTreeUtil.getParentOfType(element,
                PsiLocalVariable.class);
        if (psiParent == null) {
            return null;
        }
        PsiLocalVariable psiLocal = (PsiLocalVariable) psiParent;
        if (!(psiLocal.getParent() instanceof PsiDeclarationStatement)) {
            return null;
        }
        PsiClass psiClass = PsiTypesUtil.getPsiClass(psiLocal.getType());
        return psiClass;
    }
}
