package com.air.nc5dev.util.jdbc;

import com.air.nc5dev.util.CollUtil;
import com.air.nc5dev.util.ProjectNCConfigUtil;
import com.air.nc5dev.util.ReflectUtil;
import com.air.nc5dev.util.idea.LogUtil;
import com.air.nc5dev.util.ncutils.AESEncode;
import com.air.nc5dev.vo.ExportContentVO;
import com.air.nc5dev.vo.ItemsItemVO;
import com.air.nc5dev.vo.NCDataSourceVO;
import com.air.nc5dev.vo.SubTableVO;
import nc.uap.studio.pub.db.ScriptHelper;
import nc.uap.studio.pub.db.SqlUtil;
import nc.uap.studio.pub.db.model.ITable;
import nc.uap.studio.pub.db.query.SqlQueryResultSet;
import nc.uap.studio.pub.db.script.export.SqlQueryInserts;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

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
            return DriverManager.getConnection(ds.getDatabaseUrl(), ds.getUser(), ds.getPassword());
        } catch (Throwable e) {
            //NCC 新的加密方式？
            return DriverManager.getConnection(ds.getDatabaseUrl(), ds.getUser(), AESEncode.decrypt(ds.getPasswordOrgin()));
        }
    }

    public static void toInserts(Connection con, ItemsItemVO itemVO, StringBuilder txt, Set exsitSqlSet, ExportContentVO contentVO) {
        try {
            ITable iTable = SqlUtil.retrieveTable(itemVO.getItemKey(), null, con);
            SqlQueryResultSet rs = SqlUtil.queryResults(iTable, itemVO.getFixedWhere(), con);
            if (rs == null) {
                return;
            }

            if (!"false".equalsIgnoreCase(ProjectNCConfigUtil.getConfigValue("enableSubResultSet"))) {
                //处理默认的子表
                List<SqlQueryResultSet> subResultSets = new ArrayList<>();
                ReflectUtil.setFieldValue(rs, "subResultSets", subResultSets);
                subs(iTable, subResultSets);
            }

            SqlQueryInserts sqls = convert2InsertSQLs(rs, "true".equalsIgnoreCase(ProjectNCConfigUtil.getConfigValue("includeDeletes")));

            apend(con, itemVO, txt, exsitSqlSet, contentVO, sqls);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.error("导出表:" + itemVO.getItemKey() + " 出错!" + e.toString() + " ," + itemVO.getFixedWhere(), e);
            throw new RuntimeException("导出表:" + itemVO.getItemKey() + " 出错!" + e.toString() + " ," + itemVO.getFixedWhere(), e);
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

    public static SqlQueryInserts convert2InsertSQLs(SqlQueryResultSet rs, boolean includeDeletes) {
        if (rs == null) {
            return null;
        } else {
            SqlQueryInserts inserts = getInserts(rs, ";", includeDeletes);
            if (rs.getSubResultSets() != null) {
                Iterator it = rs.getSubResultSets().iterator();

                while (it.hasNext()) {
                    SqlQueryResultSet resultSet = (SqlQueryResultSet) it.next();
                    fillSqls(inserts, resultSet, ";", includeDeletes);
                }
            }

            return inserts;
        }
    }

    public static SqlQueryInserts getInserts(SqlQueryResultSet resultSet, String separator, boolean includeDeletes) {
        ITable table = resultSet.getTable();
        SqlQueryInserts inserts = new SqlQueryInserts(table);
        inserts.setResults(new ArrayList());
        Iterator var7 = resultSet.getResults().iterator();

        while (var7.hasNext()) {
            Map<String, Object> result = (Map) var7.next();
            String sql;
            if (includeDeletes) {
                sql = ScriptHelper.convert2DeleteSql(table, result, separator);
                inserts.getResults().add(sql);
            }

            sql = ScriptHelper.convert2InsertSqls(table, result, separator, null);
            inserts.getResults().add(sql);
        }

        inserts.setResultSet(resultSet);
        return inserts;
    }

    public static void fillSqls(SqlQueryInserts parentInserts, SqlQueryResultSet rs, String separator, boolean includeDeletes) {
        SqlQueryInserts inserts = getInserts(rs, separator, includeDeletes);
        parentInserts.getSubInserts().add(inserts);
        if (rs.getSubResultSets() != null) {
            Iterator it = rs.getSubResultSets().iterator();

            while (it.hasNext()) {
                SqlQueryResultSet resultSet = (SqlQueryResultSet) it.next();
                fillSqls(inserts, resultSet, separator, includeDeletes);
            }
        }

    }
}
