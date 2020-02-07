package com.air.nc5dev.acion;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;

public class AboutMeAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("作者： air Email 209308343@qq.com,QQ 209308343 \n");
        stringBuilder.append("使用提示: ");
        stringBuilder.append("第一次新建项目-必须步骤：\n" );
        stringBuilder.append("1. Tools -> 配置NC HOME   进行NC HOME配置！ \n" );
        stringBuilder.append("2. 第一步保存后，如果没有选更新依赖，请在 Tools -> 更新NC 库依赖 执行依赖更新 \n" );
        stringBuilder.append("3. 第2步后，请在 Tools -> 生成默认NC运行配置 执行Idea的运行配置,注意 执行后请运行时候根据提示手工修改里面的modelu项目名 \n");
        stringBuilder.append("4. 第4步后，会生成项目默认的几个文件夹和xml，请手工在 项目结构修改几个文件夹的IDEA属性为正确值！\n");


        stringBuilder.append("常见问题：\n " );
        Messages.showInfoMessage(stringBuilder.toString(), "关于我");
    }
}
