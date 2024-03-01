package com.air.nc5dev.util.jdbc;

import com.air.nc5dev.util.CollUtil;
import com.air.nc5dev.util.ExceptionUtil;
import com.air.nc5dev.util.IoUtil;
import com.air.nc5dev.util.ProjectNCConfigUtil;
import com.air.nc5dev.util.ReflectUtil;
import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.util.V;
import com.air.nc5dev.util.idea.LogUtil;
import com.air.nc5dev.util.ncutils.AESEncode;
import com.air.nc5dev.vo.ExportContentVO;
import com.air.nc5dev.vo.ItemsItemVO;
import com.air.nc5dev.vo.NCDataSourceVO;
import com.air.nc5dev.vo.SubTableVO;
import com.alibaba.fastjson.JSON;
import com.intellij.openapi.project.Project;
import nc.uap.studio.pub.db.ScriptHelper;
import nc.uap.studio.pub.db.SqlUtil;
import nc.uap.studio.pub.db.model.ITable;
import nc.uap.studio.pub.db.query.SqlQueryResultSet;
import nc.uap.studio.pub.db.script.export.SqlQueryInserts;
import org.apache.commons.lang3.StringUtils;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import java.awt.Component;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 数据库 连接工具类 <br/>
 * <br/>
 * <br/>
 *
 * @author 唐粟 Email:209308343@qq.com
 * @date 2020/11/9 13:59
 * @project
 * @Version
 */
public class ConnectionUtil {
    static List<SubTableVO> subs = null;

    /**
     * 根据数据源 获取连接
     *
     * @param ds
     * @return
     */
    public static Connection getConn(NCDataSourceVO ds) throws SQLException, ClassNotFoundException {
        if (StringUtils.isNotBlank(ds.getDriverClassName())) {
            Class.forName(ds.getDriverClassName());
        }

        try {
            return DriverManager.getConnection(ds.getDatabaseUrl().trim(), ds.getUser(), ds.getPassword());
        } catch (Throwable e) {
            //NCC 新的加密方式？
            return DriverManager.getConnection(ds.getDatabaseUrl().trim(), ds.getUser(),
                    AESEncode.decrypt(ds.getPasswordOrgin()));
        }
    }

    public static void initDataSourceClass(NCDataSourceVO ds, Project project, Component c) {
        if (StringUtils.isBlank(ds.getDriverClassName())) {
            return;
        }

        try {
            Class.forName(ds.getDriverClassName());
            return;
        } catch (Throwable e) {
            LogUtil.error("读取class失败：" + e.toString(), e);
            try {
                loadDriverClass(ds, project, c);
            } catch (Throwable ex) {
                LogUtil.error("根据jar载入读取class失败：" + ex.toString(), ex);
            }
        }

        Connection conn = null;
        try {
            conn = getConn(ds);
        } catch (Throwable e) {
            LogUtil.error("尝试连接数据库失败：" + e.toString(), e);

            if (e.getMessage().toLowerCase().contains("no suitable driver")) {
                try {
                    loadDriverClass(ds, project, c);
                } catch (Throwable ex) {
                    LogUtil.error("根据jar载入读取class失败：" + ex.toString(), ex);
                    LogUtil.error(ex.toString(), ex);
                }
            }
            return;
        } finally {
            IoUtil.close(conn);
        }
    }

    public static void loadDriverClass(NCDataSourceVO ds, Project project, Component c) throws SQLException,
            ClassNotFoundException {
        String dir = null;
        try {
            dir = new File(ProjectNCConfigUtil.getNCHome(project), "driver").getPath();
        } catch (Throwable e) {
        }
        JFileChooser fileChooser = new JFileChooser(dir);
        fileChooser.setDialogTitle(String.format("请选择数据库驱动的jar文件(%s %s):"
                , ds.getDatabaseType()
                , ds.getDriverClassName()));
//        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".jar");
            }

            @Override
            public String getDescription() {
                return "请选择数据库驱动jar文件";
            }
        });
        fileChooser.setMultiSelectionEnabled(false);
        int flag = fileChooser.showOpenDialog(c);
        File djar = fileChooser.getSelectedFile();
        ClassLoader pc = V.get(ConnectionUtil.class.getClassLoader()
                , Thread.currentThread().getContextClassLoader());
        LogUtil.infoAndHide("选择jar文件:" + flag + "    " + djar + "   ,父加载器: " + pc + "  ,home:" + dir);
        URLClassLoader urlClassLoader = null;
        try {
            urlClassLoader = new URLClassLoader(new URL[]{djar.toURI().toURL()}, pc);
            Class<?> clz = urlClassLoader.loadClass(ds.getDriverClassName());
            LogUtil.error("读取jar加载数据库class成功：" + clz);
        } catch (Throwable ex) {
            LogUtil.error("读取jar失败：" + ex.toString(), ex);
        }
    }

    public static void toInserts(Connection con, ItemsItemVO itemVO, StringBuilder txt, Set exsitSqlSet,
                                 ExportContentVO contentVO) {
        try {
            ITable iTable = SqlUtil.retrieveTable(itemVO.getItemKey(), null, con);
            SqlQueryResultSet rs = StringUtil.isBlank(itemVO.getSql())
                    ? SqlUtil.queryResults(iTable, itemVO.getFixedWhere(), con)
                    : SqlUtil.queryResultsByFullSql(iTable, itemVO.getSql(), con);
            if (rs == null) {
                return;
            }

            if (!"false".equalsIgnoreCase(ProjectNCConfigUtil.getConfigValue("enableSubResultSet"))) {
                //处理默认的子表
                List<SqlQueryResultSet> subResultSets = new ArrayList<>();
                ReflectUtil.setFieldValue(rs, "subResultSets", subResultSets);
                subs(iTable, subResultSets);
            }

            SqlQueryInserts sqls = convert2InsertSQLs(rs
                    , "true".equalsIgnoreCase(ProjectNCConfigUtil.getConfigValue("includeDeletes"))
                    , itemVO
            );

            apend(con, itemVO, txt, exsitSqlSet, contentVO, sqls);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.error("导出表:" + itemVO.getItemKey() + " 出错!" + e.toString() + " ," + itemVO.getFixedWhere(), e);
            RuntimeException ex = new RuntimeException(
                    "导出表:" + itemVO.getItemKey() + " 出错!"
                            + "sql= " + (StringUtil.isBlank(itemVO.getSql()) ? itemVO.getFixedWhere() : itemVO.getSql())
                            + "，错误栈:" + ExceptionUtil.toString(ExceptionUtil.getTopCase(e))
                            + " ," + JSON.toJSONString(itemVO)
                    , e);
            ex.setStackTrace(e.getStackTrace());
            throw ex;
        }
    }

    private static void subs(ITable iTable, List<SqlQueryResultSet> subResultSets) {

    }

    private static void apend(Connection con, ItemsItemVO itemVO, StringBuilder txt
            , Set exsitSqlSet, ExportContentVO contentVO, SqlQueryInserts sqls) {
        contentVO.indicator.setText("强制生成SQL合并文件-导出表:" + sqls.getTable().getName());
        List<String> rt = sqls.getResults();
        if (CollUtil.isNotEmpty(rt)) {
            for (String sql : rt) {
                if (exsitSqlSet != null) {
                    if (exsitSqlSet.contains(sql)) {
                        continue;
                    }

                    exsitSqlSet.add(sql);
                }

                txt.append(sql).append('\n');
            }
        }

        if (CollUtil.isNotEmpty(sqls.getSubInserts())) {
            for (SqlQueryInserts subInsert : sqls.getSubInserts()) {
                apend(con, itemVO, txt, exsitSqlSet, contentVO, subInsert);
            }
        }
    }

    public static SqlQueryInserts convert2InsertSQLs(SqlQueryResultSet rs
            , boolean includeDeletes
            , ItemsItemVO itemVO) {
        if (rs == null) {
            return null;
        }

        SqlQueryInserts inserts = getInserts(rs, ";", includeDeletes, itemVO);
        if (rs.getSubResultSets() == null) {
            return inserts;
        }

        Iterator it = rs.getSubResultSets().iterator();

        while (it.hasNext()) {
            SqlQueryResultSet resultSet = (SqlQueryResultSet) it.next();
            fillSqls(inserts, resultSet, ";", includeDeletes, itemVO);
        }

        return inserts;
    }

    public static SqlQueryInserts getInserts(SqlQueryResultSet resultSet
            , String separator
            , boolean includeDeletes
            , ItemsItemVO itemVO) {
        ITable table = resultSet.getTable();
        SqlQueryInserts inserts = new SqlQueryInserts(table);
        inserts.setResults(new ArrayList());
        Iterator iterator = resultSet.getResults().iterator();

        String sql;
        while (iterator.hasNext()) {
            Map<String, Object> result = (Map) iterator.next();
            if (includeDeletes) {
                sql = ScriptHelper.convert2DeleteSql(table, result, separator, itemVO);
                inserts.getResults().add(sql);
            }

            sql = ScriptHelper.convert2InsertSqls(table, result, separator, null, itemVO);
            inserts.getResults().add(sql);
        }

        inserts.setResultSet(resultSet);
        return inserts;
    }

    public static void fillSqls(SqlQueryInserts parentInserts
            , SqlQueryResultSet rs
            , String separator
            , boolean includeDeletes
            , ItemsItemVO itemVO) {
        SqlQueryInserts inserts = getInserts(rs, separator, includeDeletes, itemVO);
        parentInserts.getSubInserts().add(inserts);
        if (rs.getSubResultSets() != null) {
            Iterator it = rs.getSubResultSets().iterator();

            while (it.hasNext()) {
                SqlQueryResultSet resultSet = (SqlQueryResultSet) it.next();
                fillSqls(inserts, resultSet, separator, includeDeletes, itemVO);
            }
        }

    }
}
