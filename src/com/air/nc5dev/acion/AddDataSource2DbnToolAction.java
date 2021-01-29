package com.air.nc5dev.acion;

import com.air.nc5dev.acion.base.AbstractIdeaAction;
import com.air.nc5dev.util.NCPropXmlUtil;
import com.air.nc5dev.util.ProjectNCConfigUtil;
import com.air.nc5dev.util.idea.ProjectUtil;
import com.air.nc5dev.vo.NCDataSourceVO;
import com.intellij.openapi.actionSystem.AnActionEvent;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 一键添加 数据库连接到 database插件里
 */
public class AddDataSource2DbnToolAction extends AbstractIdeaAction {
    @Override
    protected void doHandler(AnActionEvent e) throws IOException {
        File ncHome = ProjectNCConfigUtil.getNCHome();
        if (!ncHome.isDirectory()) {
            ProjectUtil.warnNotification("未配置NC HOME 无法执行此操作！ ", e.getProject());
            return;
        }

        String basePath = e.getProject().getBasePath();

        List<NCDataSourceVO> dataSourceVOS = NCPropXmlUtil.getDataSourceVOS();
        ProjectUtil.addDatabaseToolLinks(basePath, dataSourceVOS);

        e.getProject().getBaseDir().refresh(false,true);
    }
}
