package com.air.nc5dev.acion;

import com.air.nc5dev.acion.base.AbstractIdeaAction;
import com.air.nc5dev.component.BuildManagerListenerImpl;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * @author xiaobiao
 * @version 2020/9/26
 */
public class NCCodeCheckAction extends AbstractIdeaAction {

    @Override
    public void doHandler(AnActionEvent e) {
        BuildManagerListenerImpl.checkNCCode(e.getProject());
    }
}
