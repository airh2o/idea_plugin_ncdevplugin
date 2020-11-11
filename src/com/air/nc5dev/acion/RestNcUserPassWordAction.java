package com.air.nc5dev.acion;

import com.air.nc5dev.acion.base.AbstractIdeaAction;
import com.air.nc5dev.ui.PatcherDialog;
import com.air.nc5dev.ui.RestNcUserPassWordDialog;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * 重置操作员密码
 *
 * @Author 唐粟 Email: 209308343@qq.com
 * @Description
 * @Date 2020/11/9 11:24
 **/
public class RestNcUserPassWordAction extends AbstractIdeaAction {
    @Override
    protected void doHandler(AnActionEvent e) {
        RestNcUserPassWordDialog dialog = new RestNcUserPassWordDialog(e);
        dialog.show();
    }
}
