package com.air.nc5dev.ui.nccactionsearch;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;


public class RequestPath {
    private String path;

    private String method;

    private PsiMethod psiMethod;

    private String moduleName;

    public RequestPath(String controllerPath, String methodPath, String method, PsiMethod psiMethod, String moduleName) {
        String prePath = controllerPath;
        if (controllerPath.endsWith("/")) {
            prePath = controllerPath.substring(0, controllerPath.length() - 1);
        }

        String path = methodPath;
        if (!methodPath.startsWith("/") && !"".equals(path)) {
            path = "/" + methodPath;
        }

        this.path = prePath + path;
        this.method = method;
        this.psiMethod = psiMethod;
        this.moduleName = moduleName;
    }

    public String getPath() {
        if (null == path) {
            return "";
        }
        return path;
    }

    public String getMethod() {
        int length = 10;
        if (null != method) {
            StringBuilder sb = new StringBuilder();
            int spaceLength = Math.min(method.length(), length);
            int i = 0;
            sb.append(method);
            while (i < spaceLength) {
                sb.append(" ");
                i += 1;
            }
            return sb.toString();
        }
        return "          ";
    }

    public PsiElement getPsiMethod() {
        return psiMethod;
    }

    public String getModuleName() {
        return moduleName;
    }
}
