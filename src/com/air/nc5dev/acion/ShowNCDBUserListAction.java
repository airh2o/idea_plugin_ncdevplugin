package com.air.nc5dev.acion;

import cn.hutool.core.collection.CollUtil;
import com.air.nc5dev.acion.base.AbstractIdeaAction;
import com.air.nc5dev.util.NCPropXmlUtil;
import com.air.nc5dev.util.ProjectNCConfigUtil;
import com.air.nc5dev.util.idea.LogUtil;
import com.air.nc5dev.vo.NCDataSourceVO;
import com.google.common.base.Joiner;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 显示home里配置的NC的用户和密码列表
 *
 * @Author 唐粟 Email: 209308343@qq.com
 * @Description
 * @Date 2020/11/9 10:56
 **/
public class ShowNCDBUserListAction extends AbstractIdeaAction {
    @Override
    protected void doHandler(AnActionEvent e) throws IOException {
        NCPropXmlUtil.loadConfFromFile(ProjectNCConfigUtil.getNCHomePath(e.getProject()));
        List<NCDataSourceVO> ds = NCPropXmlUtil.getDataSourceVOS(e.getProject());
        if (CollUtil.isEmpty(ds)) {
            LogUtil.info("没有找到数据源列表,请配置NCHOME和NC的数据源!");
            return;
        }

        final ArrayList<String> list = new ArrayList<>(100);
        ds.forEach(d -> {
            String line = String.format("数据源: %s,数据库: %s, 用户: %s, 密码: %s"
                    , d.getDataSourceName(), d.getDatabaseType(), d.getUser()
                    , d.getPassword()
            );
            list.add(line);
        });

        final StringBuilder stringBuilder = new StringBuilder(10000);
        list.forEach(pid -> {
            stringBuilder.append(pid).append("\n");
        });

        LogUtil.debug(Joiner.on("\n").skipNulls().join(list));
        Messages.showInfoMessage(stringBuilder.toString(), "数据源列表:");
    }
}
