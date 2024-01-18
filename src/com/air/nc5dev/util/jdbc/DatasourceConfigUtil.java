package com.air.nc5dev.util.jdbc;

import com.air.nc5dev.util.idea.ProjectUtil;
import com.air.nc5dev.vo.NCDataSourceVO;
import com.intellij.openapi.project.Project;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * </br>
 * </br>
 * </br>
 *
 * @author 唐粟 Email:209308343@qq.com
 * @date 2022/8/3 0003 10:09
 * @project
 * @Version
 */
public class DatasourceConfigUtil {
    /**
     * 往IDEA的数据库database管理工具 新增数据连接
     *
     * @param project
     * @param basePath      项目ROOT文件夹路径
     * @param dataSourceVOS 数据源们
     * @throws IOException
     */
    public static void addDatabaseToolLinks(Project project, String basePath, List<NCDataSourceVO> dataSourceVOS)
            throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        if (dataSourceVOS == null) {
            return;
        }

        for (NCDataSourceVO ds : dataSourceVOS) {
           /* Class<?> clz = Class.forName("com.intellij.database.dataSource.LocalDataSource");
            Constructor<?> constructor = clz.getConstructor(String.class, String.class, String.class, String.class, String.class);
            Object d = constructor.newInstance(ds.getDataSourceName()
                    , ds.getDriverClassName()
                    , ds.getDatabaseUrl()
                    , ds.getUser()
                    , ds.getPassword());
           *//* com.intellij.database.dataSource.LocalDataSource d = new com.intellij.database.dataSource.LocalDataSource(ds.getDataSourceName()
                    , ds.getDriverClassName()
                    , ds.getDatabaseUrl()
                    , ds.getUser()
                    , ds.getPassword());*//*
        *//*    d.setReadOnly(false);
            d.setSingleConnection(false);
            d.setCheckOutdated(true);
            d.setGlobal(false);
            d.setComment(JSON.toJSONString(ds));
            d.setAuthRequired(true);
            d.setPasswordStorage(com.intellij.database.dataSource.LocalDataSource.Storage.PERSIST);

                    new com.intellij.database.dataSource.LocalDataSourceManager(project).addDataSource(d);
            *//*

            clz.getMethod("setComment", String.class).invoke(d, JSON.toJSONString(ds));
            clz.getMethod("setPasswordStorage", String.class).invoke(d, JSON.toJSONString(ds));

            Class<?> LocalDataSourceManagerClz = Class.forName("com.intellij.database.dataSource.LocalDataSourceManager");
            Object LocalDataSourceManagerObj = LocalDataSourceManagerClz.getMethod("getInstance", Project.class).invoke(null, project);

            LocalDataSourceManagerClz.getMethod("addDataSource", clz).invoke(d);*/

            ProjectUtil.addDatabaseToolLinks(basePath, dataSourceVOS);
        }

    }
}
