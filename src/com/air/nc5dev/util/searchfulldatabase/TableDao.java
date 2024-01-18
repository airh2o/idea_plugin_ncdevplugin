package com.air.nc5dev.util.searchfulldatabase;

import com.air.nc5dev.util.ExceptionUtil;
import com.air.nc5dev.util.IoUtil;
import com.air.nc5dev.util.StringUtil;
import com.intellij.openapi.progress.ProgressIndicator;
import lombok.Data;

import javax.swing.JProgressBar;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 表的数据库dao
 *
 * @author Air
 * @date 2018年11月21日15:03:39
 * @since v1
 */
@Data
public class TableDao {
    /**
     * 是否快速模式-精确到表，不精确到列
     */
    private boolean isFast;
    public static Map<String, ArrayList<String>> tableColumsMap = new ConcurrentHashMap<>();
    SearchFullDatabaseConfigVO config;

    /**
     * 搜索所有的表,查询包含指定文本的表的字段
     *
     * @param text
     * @return
     */
    public List<SearchResultVO> queryTableAllColumn(String text) {
        if (StringUtil.isBlank(text) || config.indicator.isCanceled()) {
            return new ArrayList<>(0);
        }

        List<SearchResultVO> re = new ArrayList<>(tabs.size());
        Connection conn = new DbConnectionFactory()
                .getConnection(taskFather.getConfig().getDataSource(), taskFather.getConfig().getIndicator());
        Statement st = null;
        ResultSet rs = null;
        ResultSet rsContent = null;
        String sql = null;
        try {
            st = conn.createStatement();
            SearchResultVO searchResult;
            ResultSetMetaData metaData;
            String sql_QueryTableColum = "select * from ";
            String sql_QueryContent = " from ";
            String whereNoResult = "1=2";
            String whereContent = "  like '%" + text + "%'";
            // 0 like '%内容%' , 1 like '内容%' , 2 like '%内容', 3 ='内容'
            switch (taskFather.getConfig().getLikeType()) {
                case 0:
                    whereContent = "  like '%" + text + "%'";
                    break;
                case 1:
                    whereContent = "  like '" + text + "%'";
                    break;
                case 2:
                    whereContent = "  like '%" + text + "'";
                    break;
                case 3:
                    whereContent = "  = '" + text + "'";
                    break;
                default:
                    whereContent = "  like '%" + text + "%'";
                    break;
            }

            whereContent = text.startsWith("where:") ? text.replace("where:", "") : whereContent;
            String where = " where ";
            ArrayList<String> columNames = new ArrayList<>(500);
            ArrayList<String> allColumNames;
            final ProgressIndicator indicator = taskFather.getConfig().getIndicator();
            final JProgressBar threadProgressBar = taskFather.getConfig().getDialog().getThreadProgressBar();
            for (String tab : tabs) {
                if (config.indicator.isCanceled()) {
                    return re;
                }

                if (taskFather.isStop()) {
                    break;
                }

                allColumNames = tableColumsMap.get(taskFather.getConfig().getDataSource().getDatabaseUrl()
                        + ':' + taskFather.getConfig().getDataSource().getUser() + ':' + tab);
                try {
                    if (allColumNames == null) {
                        allColumNames = new ArrayList<>(500);
                        tableColumsMap.put(taskFather.getConfig().getDataSource().getDatabaseUrl()
                                + ':' + taskFather.getConfig().getDataSource().getUser() + ':' + tab, allColumNames);
                        try {
                            rs = st.executeQuery(String.format("select * from %s where 1=2 ", tab));
                            metaData = rs.getMetaData();
                            int columCount = metaData.getColumnCount();
                            columNames = new ArrayList<>(columCount);
                            for (int columIndex = 1; columIndex <= columCount; columIndex++) {
                                if (config.indicator.isCanceled()) {
                                    return re;
                                }

                                if (!metaData.isSearchable(columIndex)
                                    //    ||
                                ) {
                                    //跳过 不能对比字符串的类型列
                                    continue;
                                }
                                allColumNames.add(metaData.getColumnName(columIndex));
                            }
                        } finally {
                            IoUtil.close(rs);
                        }
                    }

                    columNames.clear();
                    for (String columnName : allColumNames) {
                        if (notHas(taskFather.getConfig().getSkipColumns(), columnName)) {
                            columNames.add(columnName);
                        }
                    }

                    if (isFast()) {
//                    System.out.println("快速模式搜索表: " + tab);
                        if (text.startsWith("{}:")) {
                            sql = text
                                    .replace("{table}", tab)
                                    .replace("{where}", inSql(whereContent, columNames))
                            ;
                        } else {
                            sql = sql_QueryContent + tab + where + " " + inSql(whereContent, columNames);
                        }

                        rsContent = st.executeQuery(appendLimitSql(sql));
                        if (rsContent.next()) {
                            searchResult = new SearchResultVO();
                            searchResult.setField("");
                            searchResult.setTable(tab);
                            searchResult.setSql("select * " + sql);
                            searchResult.setRow(re.size() + 1);
                            re.add(searchResult);
                            taskFather.addSearchResult(searchResult);
                        }
                        rsContent.close();
                    } else {
                        for (String columName : columNames) {
//                        System.out.println("搜索表: " + tab + " 中列: " + columName);
                            if (text.startsWith("{}:")) {
                                sql = text
                                        .replace("{table}", tab)
                                        .replace("{colum}", columName)
                                ;
                            } else {
                                sql = sql_QueryContent + tab + where + columName + whereContent;
                            }

                            rsContent = st.executeQuery(appendLimitSql(sql));
                            if (rsContent.next()) {
                                searchResult = new SearchResultVO();
                                searchResult.setField(columName);
                                searchResult.setTable(tab);
                                searchResult.setSql("select * " + sql);
                                searchResult.setRow(re.size() + 1);
                                re.add(searchResult);
                                taskFather.addSearchResult(searchResult);
                            }
                            rsContent.close();
                        }
                    }
                } catch (Throwable e) {
                    searchResult = new SearchResultVO();
                    searchResult.setField("执行错误");
                    searchResult.setTable(tab);
                    searchResult.setSql(sql + ExceptionUtil.stacktraceToString(e, 25));
                    searchResult.setRow(re.size() + 1);
                    re.add(searchResult);
                    taskFather.addSearchResult(searchResult);
                }finally {
                    IoUtil.close(rs);
                }
            }
        } catch (Throwable e) {
            taskFather.finalshError("发生错误终止搜索: " + ExceptionUtil.stacktraceToString(e, 25));
            e.printStackTrace();
            return re;
        } finally {
            try {
                if (null != rs && !rs.isClosed()) {
                    rs.close();
                }
                if (null != st && !st.isClosed()) {
                    st.close();
                }
                if (null != rsContent && !rsContent.isClosed()) {
                    rsContent.close();
                }
            } catch (Throwable e) {
                taskFather.finalshError("发生错误终止搜索: " + ExceptionUtil.stacktraceToString(e, 25));
                e.printStackTrace();
            }
        }
        return re;
    }

    public boolean notHas(Set<String> skipColumns, String columName) {
        if (StringUtil.isBlank(columName) || skipColumns == null) {
            return true;
        }

        columName = columName.toLowerCase();

        for (String sk : skipColumns) {
            if (sk == null) {
                continue;
            }

            if (columName.contains(sk.toLowerCase())) {
                return false;
            }
        }

        return true;
    }

    private String appendLimitSql(String sql) {
        String type = taskFather.getConfig().getDataSource().getDatabaseType().toLowerCase();
        if (type.contains("oracle")) {
            //oracle
            return "select * from ( select rownum,1 " + sql + " ) where rownum =1 ";
        } else if (type.contains("server")) {
            //sqlserver
            return "select top 1 'a' " + sql;
        }
        if (type.contains("mysql")) {
            //mysql
            return "select 1 " + sql + " limit 1 ";
        }

        return sql;
    }

    private String inSql(String whereContent, ArrayList<String> columNames) {
        StringBuilder sb = new StringBuilder(1000);
        boolean first = true;

        for (String columName : columNames) {
            if (!first) {
                sb.append(" or ");
            } else {
                first = false;
            }

            sb.append(columName).append(whereContent);
        }

        return sb.toString();
    }

    /**
     * 是否不能 进行字符串对比
     *
     * @param columnTypeName
     * @return
     */
    private static boolean isSkipColumType(String columnTypeName) {
        if ("BLOB".equals(columnTypeName)) {
            //BLOB 类型要跳过
            return true;
        }
        return false;
    }


    public TableDao(TaskFather taskFather, List<String> tabs) {
        this.taskFather = taskFather;
        this.tabs = tabs;
    }

    private TaskFather taskFather;
    private List<String> tabs;
}
