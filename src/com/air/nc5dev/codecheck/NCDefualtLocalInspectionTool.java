package com.air.nc5dev.codecheck;

import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 * <br>
 * <br>
 * <br>
 *
 * @author air Email:209308343@qq.com
 * @date 2022/8/12 0012 15:24
 * @project
 * @Version
 */
public class NCDefualtLocalInspectionTool extends AbstractBaseJavaLocalInspectionTool {
    public NCDefualtLocalInspectionTool(){

    }

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getGroupDisplayName() {
        return "NCDefualtLocalInspectionToolImpl.getGroupDisplayName";
    }

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getDisplayName() {
        return "NC代码规范检查";
    }

    @NotNull
    @Override
    public String getShortName() {
        return "NCDefualtLocalInspectionToolImpl.getShortName";
    }

    @Nullable
    @Override
    public ProblemDescriptor[] checkMethod(@NotNull PsiMethod method, @NotNull InspectionManager manager,
                                           boolean isOnTheFly) {
        return super.checkMethod(method, manager, isOnTheFly);
    }

    @Nullable
    @Override
    public ProblemDescriptor[] checkClass(@NotNull PsiClass aClass, @NotNull InspectionManager manager,
                                          boolean isOnTheFly) {
        return super.checkClass(aClass, manager, isOnTheFly);
    }

    @Nullable
    @Override
    public ProblemDescriptor[] checkField(@NotNull PsiField field, @NotNull InspectionManager manager, boolean isOnTheFly) {
        return super.checkField(field, manager, isOnTheFly);
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
        return new JavaElementVisitor() {
            public void visitMethod(PsiMethod method) {
                this.addDescriptors(NCDefualtLocalInspectionTool.this.checkMethod(method, holder.getManager(), isOnTheFly));
            }

            public void visitClass(PsiClass aClass) {
                this.addDescriptors(NCDefualtLocalInspectionTool.this.checkClass(aClass, holder.getManager(), isOnTheFly));
            }

            public void visitField(PsiField field) {
                this.addDescriptors(NCDefualtLocalInspectionTool.this.checkField(field, holder.getManager(), isOnTheFly));
            }

            public void visitFile(PsiFile file) {
                this.addDescriptors(NCDefualtLocalInspectionTool.this.checkFile(file, holder.getManager(), isOnTheFly));
            }

            private void addDescriptors(ProblemDescriptor[] descriptors) {
                if (descriptors != null) {
                    ProblemDescriptor[] var2 = descriptors;
                    int var3 = descriptors.length;

                    for(int var4 = 0; var4 < var3; ++var4) {
                        ProblemDescriptor descriptor = var2[var4];
                        holder.registerProblem(descriptor);
                    }
                }

            }
        };
    }
}
