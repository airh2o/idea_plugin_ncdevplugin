package com.air.nc5dev.util.docgenerate;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * @author xiaobiao
 * @version 2020/9/26
 */
public class GenerateGetAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        PsiUtil.showDialog(e, EnumMethodType.GET);
    }
}
