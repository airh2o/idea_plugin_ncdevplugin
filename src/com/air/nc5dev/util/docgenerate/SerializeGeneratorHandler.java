package com.air.nc5dev.util.docgenerate;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.JavaTokenType;
import com.intellij.psi.PsiAnonymousClass;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLiteral;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiPrefixExpression;
import com.intellij.psi.PsiTypeParameter;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.siyeh.ig.fixes.SerialVersionUIDBuilder;
import com.siyeh.ig.psiutils.SerializationUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.SwingUtilities;
import java.text.MessageFormat;

/**
 * @author Beeant
 * @version 2020/6/6
 */
public class SerializeGeneratorHandler implements CodeInsightActionHandler {

    private static final boolean m_ignoreSerializableDueToInheritance = false;
    private static boolean _showing = false;

    @Nullable
    public static PsiClass getPsiClass(@Nullable VirtualFile virtualFile,
                                       @NotNull PsiManager manager,
                                       @NotNull Editor editor) {
        final PsiFile psiFile = (virtualFile == null) ? null : manager.findFile(virtualFile);

        if (psiFile == null) {
            return null;
        }
        final PsiElement elementAtCaret = psiFile.findElementAt(editor.getCaretModel().getOffset());

        return findPsiClass(elementAtCaret);
    }

    public static PsiClass findPsiClass(@Nullable PsiElement element) {
        while (true) {
            final PsiClass psiClass = (element instanceof PsiClass) ? (PsiClass) element
                    : PsiTreeUtil.getParentOfType(element, PsiClass.class);

            if (psiClass == null || !(psiClass.getContainingClass() instanceof PsiAnonymousClass)) {
                return psiClass;
            }
            element = psiClass.getParent();
        }
    }

    private static void displayMessage(@NotNull final String message) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    if (!SerializeGeneratorHandler._showing) {
                        SerializeGeneratorHandler._showing = true;
                        Messages.showErrorDialog(message, "Error");
                    }
                } finally {
                    SerializeGeneratorHandler._showing = false;
                }
            }
        });
    }

    public static boolean needsUIDField(@Nullable PsiClass aClass) {
        if (aClass == null) {
            return false;
        }
        if (aClass.isInterface() || aClass.isAnnotationType() || aClass.isEnum()) {
            return false;
        }
        if (aClass instanceof PsiTypeParameter || aClass instanceof PsiAnonymousClass) {
            return false;
        }

        if (m_ignoreSerializableDueToInheritance) {
            if (!SerializationUtils.isDirectlySerializable(aClass)) {
                return false;
            }
        }
        return SerializationUtils.isSerializable(aClass);
    }

    @Nullable
    public static PsiField getUIDField(@Nullable PsiClass psiClass) {
        if (psiClass != null) {
            for (final PsiField field : psiClass.getFields()) {
                if (isUIDField(field)) {
                    return field;
                }
            }
        }
        return null;
    }

    public static boolean isUIDField(@Nullable PsiField field) {
        return (field != null && "serialVersionUID".equals(field.getName()));
    }

    public static boolean hasUIDField(@Nullable PsiClass psiClass, long serialVersionUIDValue) {
        final PsiField field = getUIDField(psiClass);

        if (field != null) {
            PsiExpression initializer = field.getInitializer();
            int sign = 1;

            if (initializer instanceof PsiPrefixExpression) {
                final PsiPrefixExpression prefixExpression = (PsiPrefixExpression) initializer;

                if (prefixExpression.getOperationSign().getTokenType() == JavaTokenType.MINUS) {
                    sign = -1;
                }
                initializer = prefixExpression.getOperand();
            }

            final Object literalValue = (initializer instanceof PsiLiteral) ? ((PsiLiteral) initializer).getValue() : null;

            return (literalValue instanceof Long && (((Long) literalValue) * sign) == serialVersionUIDValue);
        }
        return false;
    }

    @Nullable
    public static String getFullDeclaration(String sourceFileExtension, long serial) {
        for (Language language : Language.values()) {
            if (language.fileExtension.equals(sourceFileExtension)) {
                return language.format.format(new String[]{Long.toString(serial)});
            }
        }
        return null;
    }

    private static void insertSerialVersionUID(Project project, String extension, PsiClass psiClass, long serial) {
        final PsiElementFactory psiElementFactory = JavaPsiFacade.getInstance(project).getElementFactory();
        final CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(project);

        if (psiElementFactory != null && codeStyleManager != null) {
            try {
                final String fullDeclaration = getFullDeclaration(extension, serial);
                final PsiField psiField = psiElementFactory.createFieldFromText(fullDeclaration, null);

                if (psiField != null) {
                    final PsiField oldPsiField = getUIDField(psiClass);

                    codeStyleManager.reformat(psiField);
                    if (oldPsiField != null) {
                        oldPsiField.replace(psiField);
                    } else {
                        psiClass.add(psiField);
                    }
                }
            } catch (IncorrectOperationException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void invoke(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile psiFile) {
        final PsiManager manager = PsiManager.getInstance(project);
        // GenerateMembersUtil.
        final PsiClass psiClass = getPsiClass(psiFile.getVirtualFile(), manager, editor);

        if (psiClass == null) {
            displayMessage("Not a Java class file.");
            return;
        }

        final long serialVersionUIDValue = SerialVersionUIDBuilder.computeDefaultSUID(psiClass);

        if (needsUIDField(psiClass) && !hasUIDField(psiClass, serialVersionUIDValue)) {
            insertSerialVersionUID(project, psiFile.getVirtualFile().getExtension(), psiClass, serialVersionUIDValue);
        }
    }

    @Override
    public boolean startInWriteAction() {
        return true;
    }

    @SuppressWarnings("UnusedDeclaration")
    public enum Language {
        JAVA("java", "private static final long serialVersionUID = {0}L;"),
        GROOVY("groovy", "private static final serialVersionUID = {0}L"),
        SCALA("scala", "private val serialVersionUID = {0}L");

        private String fileExtension;
        private MessageFormat format;

        private Language(@NotNull String fileExtension, @NotNull String format) {
            this.fileExtension = fileExtension;
            this.format = new MessageFormat(format);
        }

        @Nullable
        public static Language getFromExtension(String fileExtension) {
            for (Language language : Language.values()) {
                if (language.fileExtension.equals(fileExtension)) {
                    return language;
                }
            }
            return null;
        }

        public String getFileExtension() {
            return this.fileExtension;
        }
    }
}
