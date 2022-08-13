package com.air.nc5dev.util.docgenerate;

import com.intellij.codeInsight.generation.PsiFieldMember;
import com.intellij.ide.util.MemberChooser;
import com.intellij.ide.util.MemberChooserBuilder;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.javadoc.PsiDocTokenImpl;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.ui.CollectionListModel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;


public class PsiUtil {
    private PsiUtil() {

    }

    public static String getDescription(PsiJavaDocumentedElement element) {
        PsiDocComment docComment = element.getDocComment();

        if (null == docComment) {
            return null;
        }
        StringBuilder sb = new StringBuilder();

        PsiElement[] descriptionElements = docComment.getDescriptionElements();
        for (PsiElement descriptionElement : descriptionElements) {
            if (descriptionElement instanceof PsiDocTokenImpl) {
                sb.append(((PsiDocTokenImpl) descriptionElement).getText());
                sb.append(" ");
            }
        }

        return sb.toString();
    }

    /**
     * Gets the java element.
     *
     * @param element the Element
     * @return the Java element
     */
    @NotNull
    public static PsiElement getJavaElement(@NotNull PsiElement element) {
        PsiElement result = element;
        PsiField field = PsiTreeUtil.getParentOfType(element, PsiField.class);
        PsiMethod method = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
        PsiClass clazz = PsiTreeUtil.getParentOfType(element, PsiClass.class);
        PsiParameter parameter = PsiTreeUtil.getParentOfType(element, PsiParameter.class);
        if (field != null) {
            result = field;
        } else if (parameter != null) {
            return parameter;
        } else if (method != null) {
            result = method;
        } else if (clazz != null) {
            result = clazz;
        }
        return result;
    }

    public static PsiElement getPsiElement(PsiFile file, int startPosition) {
        return PsiUtilCore.getElementAtOffset(file, startPosition);
    }

    public static boolean isAllowedElementType(@NotNull PsiElement element) {
        boolean result = false;
        if (element instanceof PsiClass ||
                element instanceof PsiField ||
                element instanceof PsiMethod) {
            result = true;
        }
        return result;
    }

    public static boolean isElementInSelection(@NotNull PsiElement element, int startPosition, int endPosition) {
        boolean result = false;
        int elementTextOffset = element.getTextRange().getStartOffset();
        if (elementTextOffset >= startPosition &&
                elementTextOffset <= endPosition) {
            result = true;
        }
        return result;
    }

    public static void showDialog(AnActionEvent e, EnumMethodType methodType) {
        Template template = TemplatePersistentConfiguration.getInstance().getTemplate();
        PsiClass psiClass = getPsiMethodFromContext(e);
        if (null == psiClass) {
            return;
        }
        final PsiFile file = psiClass.getContainingFile();
        Project project = psiClass.getProject();

        // show fields selector
        final MemberChooserBuilder<PsiFieldMember> builder = new MemberChooserBuilder<>(project);
        builder.setTitle("Select Fields to Generate Getters and Setters");
        builder.setHeaderPanel(createFieldsPanel());

        final PsiFieldMember[] psiMethodMembers = PsiUtil.fields(psiClass);
        SwingUtilities.invokeLater(() -> {
            if (project.isDisposed()) {
                return;
            }
            MemberChooser<PsiFieldMember> dialog = builder.createBuilder(psiMethodMembers);
            dialog.selectElements(psiMethodMembers);
            dialog.show();
            if (MemberChooser.OK_EXIT_CODE == dialog.getExitCode()) {
                List<PsiField> selectedFields = PsiUtil.toPsiFields(dialog.getSelectedElements());
                WriteCommandAction.writeCommandAction(project, file)
                        .withGlobalUndo()
                        .run(() -> createGetSet(psiClass, selectedFields,
                                !EnumMethodType.SET.equals(methodType),
                                !EnumMethodType.GET.equals(methodType),
                                template));
            }
        });
    }

    public static PsiClass getPsiMethodFromContext(AnActionEvent e) {
        PsiElement elementAt = getPsiElement(e);
        return (elementAt == null) ? null : PsiTreeUtil.getParentOfType(elementAt, PsiClass.class);
    }

    public static JComponent createFieldsPanel() {
        FlowLayout layout = new FlowLayout(FlowLayout.LEFT);
        return new JPanel(layout);
    }

    public static PsiFieldMember[] fields(PsiClass psiClass) {
        List<PsiField> fields = new CollectionListModel<>(psiClass.getFields()).getItems();
        return toMembers(fields);
    }

    private static List<PsiField> toPsiFields(List<PsiFieldMember> members) {
        List<PsiField> psiFields = new ArrayList<>();
        for (PsiFieldMember psiFieldMember : members) {
            PsiElement psiElement = psiFieldMember.getPsiElement();
            psiFields.add((PsiField) psiElement);
        }
        return psiFields;
    }

    public static void createGetSet(PsiClass psiClass, List<PsiField> fields, boolean getter, boolean setter, Template template) {
        List<PsiMethod> methods = new CollectionListModel<>(psiClass.getMethods()).getItems();
        HashSet<String> methodSet = new HashSet<>();
        for (PsiMethod method : methods) {
            methodSet.add(method.getName());
        }
        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(psiClass.getProject());
        String methodText;
        for (PsiField field : fields) {
            if (!(Objects.requireNonNull(field.getModifierList())).hasModifierProperty(PsiModifier.FINAL)) {
                if (getter) {
                    methodText = buildGet(field, template);
                    addMethod(psiClass, methodSet, elementFactory, methodText);
                }

                if (setter) {
                    methodText = buildSet(field, template);
                    addMethod(psiClass, methodSet, elementFactory, methodText);
                }
            }
        }
    }

    public static PsiElement getPsiElement(AnActionEvent e) {
        PsiFile psiFile = LangDataKeys.PSI_FILE.getData(e.getDataContext());
        Editor editor = PlatformDataKeys.EDITOR.getData(e.getDataContext());
        if (psiFile != null && editor != null) {
            int offset = editor.getCaretModel().getOffset();
            return psiFile.findElementAt(offset);
        }
        e.getPresentation().setEnabled(false);
        return null;
    }

    public static PsiFieldMember[] toMembers(List<PsiField> fields) {
        PsiFieldMember[] dialogMembers = new PsiFieldMember[fields.size()];

        for (int i = 0; i < dialogMembers.length; i++) {
            dialogMembers[i] = new PsiFieldMember(fields.get(i));
        }
        return dialogMembers;
    }

    private static String buildGet(PsiField field, Template template) {
        StringBuilder sb = new StringBuilder();
        String doc = format("get", field, template);
        if (doc != null) {
            sb.append(doc);
        }
        sb.append("public ");
        if ((Objects.requireNonNull(field.getModifierList())).hasModifierProperty("static")) {
            sb.append("static ");
        }
        sb.append(field.getType().getPresentableText()).append(" ");
        if ("boolean".equals(field.getType().getPresentableText())) {
            sb.append("is");
        } else {
            sb.append("get");
        }
        sb.append(JavaBeansUtil.getFirstCharacterUppercase(field.getName()));
        sb.append("(){\n");
        sb.append(" return this.").append(field.getName()).append(";}\n");
        return sb.toString();
    }

    private static void addMethod(PsiClass psiClass, HashSet<String> methodSet, PsiElementFactory elementFactory, String methodText) {
        PsiMethod toMethod = elementFactory.createMethodFromText(methodText, psiClass);
        if (!methodSet.contains(toMethod.getName())) {
            psiClass.add(toMethod);
        }
    }

    private static String buildSet(PsiField field, Template template) {
        StringBuilder sb = new StringBuilder();
        String doc = format("set", field, template);
        if (doc != null) {
            sb.append(doc);
        }
        sb.append("public ");
        if (field.getModifierList().hasModifierProperty("static")) {
            sb.append("static ");
        }
        sb.append("void ");
        sb.append("set").append(JavaBeansUtil.getFirstCharacterUppercase(field.getName()));
        sb.append("(").append(field.getType().getPresentableText()).append(" ").append(field.getName()).append("){\n");
        sb.append("this.").append(field.getName()).append(" = ").append(field.getName()).append(";");
        sb.append("}");
        return sb.toString();
    }

    private static String format(String string, PsiField field, Template template) {
        DateFormat date = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat dateTime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date currentDate = new Date();
        String oldContent;
        if (field.getDocComment() == null) {
            oldContent = field.getText().substring(0, field.getText().lastIndexOf("\n") + 1);
        } else {
            oldContent = field.getDocComment().getText();
            if (null != oldContent) {
                return oldContent;
            }
        }
        if ("get".equals(string)) {
            oldContent = template.getGetter().toLowerCase()
                    .replaceAll("\\$\\{field_comment}", oldContent)
                    .replaceAll("\\$\\{user}", System.getProperties().getProperty("user.name"))
                    .replaceAll("\\$\\{date}", date.format(currentDate))
                    .replaceAll("\\$\\{time}", dateTime.format(currentDate))
                    .replaceAll("\\$\\{field_name}", field.getName());
        } else if ("set".equals(string)) {
            oldContent = template.getSetter().toLowerCase()
                    .replaceAll("\\$\\{field_comment}", oldContent)
                    .replaceAll("\\$\\{user}", System.getProperties().getProperty("user.name"))
                    .replaceAll("\\$\\{date}", date.format(currentDate))
                    .replaceAll("\\$\\{time}", dateTime.format(currentDate))
                    .replaceAll("\\$\\{field_name}", field.getName());
        }
        return oldContent;
    }
}
