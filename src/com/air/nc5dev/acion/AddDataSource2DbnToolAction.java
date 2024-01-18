package com.air.nc5dev.acion;

import com.air.nc5dev.acion.base.AbstractIdeaAction;
import com.air.nc5dev.service.ui.IMeassgeConsole;
import com.air.nc5dev.util.ExceptionUtil;
import com.air.nc5dev.util.NCPropXmlUtil;
import com.air.nc5dev.util.ProjectNCConfigUtil;
import com.air.nc5dev.util.idea.LogUtil;
import com.air.nc5dev.util.idea.ProjectUtil;
import com.air.nc5dev.util.jdbc.DatasourceConfigUtil;
import com.air.nc5dev.vo.NCDataSourceVO;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;

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
        int updateClassPath = Messages.showYesNoDialog(
                "操作前一定要在IDEA数据源database里面手工随便新增几个数据源!乱写都可以，才可以点击此按钮!是否继续?"
                , "询问", Messages.getQuestionIcon());
        if (updateClassPath != Messages.OK) {
            return;
        }

        String basePath = e.getProject().getBasePath();
        NCPropXmlUtil.loadConfFromFile(ProjectNCConfigUtil.getNCHomePath(e.getProject()));
        List<NCDataSourceVO> dataSourceVOS = NCPropXmlUtil.getDataSourceVOS(e.getProject());
  /*      ProjectUtil.addDatabaseToolLinks(basePath, dataSourceVOS);

        e.getProject().getBaseDir().refresh(false, true);
*/
        try {
            DatasourceConfigUtil.addDatabaseToolLinks(e.getProject(), basePath, dataSourceVOS);

            e.getProject().getBaseDir().refresh(false, true);

            LogUtil.infoAndHide("数据源添加完成!如果错误请删除.idea文件夹里dataSources开头文件后重启IDEA后再试!");
        } catch (Throwable e1) {
            //不要弹框报错！
            try {
                IMeassgeConsole service = ProjectUtil.getService(IMeassgeConsole.class, e.getProject());
                service.getConsoleView().print(ExceptionUtil.getExcptionDetall(e1) + "\n",
                        ConsoleViewContentType.ERROR_OUTPUT);
            } catch (Throwable ex) {
            }
        }
    }
}
