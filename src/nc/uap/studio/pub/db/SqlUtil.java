package nc.uap.studio.pub.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nc.uap.studio.pub.db.exception.DatabaseRuntimeException;
import nc.uap.studio.pub.db.model.IColumn;
import nc.uap.studio.pub.db.model.IFkConstraint;
import nc.uap.studio.pub.db.model.IPkConstraint;
import nc.uap.studio.pub.db.model.ITable;
import nc.uap.studio.pub.db.model.impl.Column;
import nc.uap.studio.pub.db.model.impl.FkConstraint;
import nc.uap.studio.pub.db.model.impl.PkConstraint;
import nc.uap.studio.pub.db.model.impl.Table;
import nc.uap.studio.pub.db.query.IQueryInfo;
import nc.uap.studio.pub.db.query.SqlQueryResultSet;
import org.apache.commons.lang.StringUtils;

public class SqlUtil {
    private static final String DB_TYPE_ORACLE = "Oracle";
    private static final String DB_TYPE_SQLSERVER = "Microsoft SQL Server";
    private static final String DB_TYPE_DB2 = "DB2";
    private static final String DB_TYPE_DB20 = "DB2/NT64";

    public SqlUtil() {
    }

    public static ITable retrieveTable(String tableName, List<String> fkColNames, Connection conn) throws DatabaseRuntimeException {
        DatabaseMetaData metaData = null;
        String dbType = null;
        String userName = null;

        try {
            metaData = conn.getMetaData();
            dbType = metaData.getDatabaseProductName();
            userName = metaData.getUserName();
        } catch (SQLException var8) {
            String message = tableName + "  " + var8.getMessage();
            Logger.error(message, var8);
            throw new DatabaseRuntimeException(message);
        }

        return retrieveTable(tableName, fkColNames, metaData, dbType, userName);
    }

    public static ITable retrieveTable(String tableName, List<String> fkColNames, DatabaseMetaData metaData, String dbType, String userName) throws DatabaseRuntimeException {
        Table table = new Table();
        table.setName(tableName);
        String[] pkColNamesBySeq = new String[10];
        int nPos = 0;
        ResultSet pkRs = null;

        try {
            for (pkRs = metaData.getPrimaryKeys(retriveCatelog(dbType), retriveSchema(dbType, userName), formatTableName(tableName, dbType)); pkRs.next(); ++nPos) {
                String colName = pkRs.getString("COLUMN_NAME");
                short seq = pkRs.getShort("KEY_SEQ");
                pkColNamesBySeq[seq - 1] = colName;
            }
        } catch (SQLException var46) {
            Logger.error(tableName + "  " + var46.getMessage(), var46);
            throw new DatabaseRuntimeException(String.format(Messages.SqlUtil_6, tableName));
        } finally {
            if (pkRs != null) {
                try {
                    pkRs.close();
                } catch (SQLException var42) {
                    Logger.error("Close result set error.", var42);
                }
            }

        }

        List<String> pkColNames = new ArrayList(nPos);

        for (int i = 0; i < nPos; ++i) {
            pkColNames.add(pkColNamesBySeq[i]);
        }

        List<IColumn> allCols = table.getAllColumns();
        PkConstraint pkConstraint = new PkConstraint();
        table.setPkConstraint(pkConstraint);
        IColumn[] pkCols = new IColumn[pkColNames.size()];
        List<IFkConstraint> fkConstraints = table.getFkConstraints();
        boolean hasFkCols = false;
        List<String> upperFkColNames = null;
        FkConstraint fkConstraint = null;
        if (fkColNames != null && !fkColNames.isEmpty()) {
            hasFkCols = true;
            upperFkColNames = new ArrayList();
            Iterator var18 = fkColNames.iterator();

            while (var18.hasNext()) {
                String str = (String) var18.next();
                upperFkColNames.add(str.toUpperCase());
            }

            fkConstraint = new FkConstraint();
            fkConstraints.add(fkConstraint);
        }

        ResultSet colRs = null;

        try {
            colRs = metaData.getColumns(retriveCatelog(dbType), retriveSchema(dbType, userName), formatTableName(tableName, dbType), "%");

            while (true) {
                while (colRs.next()) {
                    String colName = colRs.getString("COLUMN_NAME");
                    short dataType = colRs.getShort("DATA_TYPE");
                    String typeName = colRs.getString("TYPE_NAME");
                    Column col = new Column();
                    col.setName(colName);
                    col.setDataType(dataType);
                    col.setTypeName(typeName);
                    allCols.add(col);
                    if (hasFkCols && upperFkColNames.contains(colName.toUpperCase())) {
                        fkConstraint.getColumns().add(col);
                    } else if (pkColNames.contains(colName)) {
                        int pos = pkColNames.indexOf(colName);
                        pkCols[pos] = col;
                    }
                }

                if (!allCols.isEmpty()) {
                    if (hasFkCols && fkConstraint.getColumns().isEmpty()) {
                        throw new DatabaseRuntimeException(String.format(Messages.SqlUtil_7, tableName, StringUtils.join(fkColNames.iterator(), ",")));
                    }

                    pkConstraint.getColumns().addAll(Arrays.asList(pkCols));
                    Table var24 = table;
                    return var24;
                }

                return null;
            }
        } catch (SQLException var44) {
            Logger.error(tableName + "  " + var44.getMessage());
            throw new DatabaseRuntimeException(String.format(Messages.SqlUtil_9, tableName));
        } finally {
            if (colRs != null) {
                try {
                    colRs.close();
                } catch (SQLException var43) {
                    Logger.error("Close result set error.", var43);
                }
            }

        }
    }

    public static SqlQueryResultSet queryResults(IQueryInfo queryInfo, Connection conn) throws DatabaseRuntimeException {
        return queryResults(queryInfo, queryInfo.getWhereCondition(), conn);
    }

    public static SqlQueryResultSet queryResults(ITable table, String whereCondition, Connection conn) throws DatabaseRuntimeException {
        if (table == null) {
            throw new DatabaseRuntimeException(Messages.SqlUtil_2);
        } else {
            checkTable(table);
            String sql = getSql(table, whereCondition);
            if (Logger.isDebugEnabled()) {
                Logger.debug(String.format("Query: %s", sql));
            }

            Statement stmt = null;
            ResultSet rs = null;

            try {
                stmt = conn.createStatement();
                rs = stmt.executeQuery(sql);
                SqlQueryResultSet sqlQueryResultSet = new SqlQueryResultSet(table);

                while (rs.next()) {
                    Map<String, Object> colNameValueMap = new LinkedHashMap();
                    Iterator var9 = table.getAllColumns().iterator();

                    while (var9.hasNext()) {
                        IColumn col = (IColumn) var9.next();
                        colNameValueMap.put(col.getName(), rs.getObject(col.getName()));
                    }

                    sqlQueryResultSet.getResults().add(colNameValueMap);
                }

                SqlQueryResultSet var11 = sqlQueryResultSet;
                return var11;
            } catch (SQLException var20) {
                Logger.error(Messages.SqlUtil_0, var20);
                throw new DatabaseRuntimeException(Messages.SqlUtil_1 + sql);
            } finally {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (SQLException var19) {
                    }
                }

                if (stmt != null) {
                    try {
                        stmt.close();
                    } catch (SQLException var18) {
                    }
                }

            }
        }
    }

    public static String formatSql(Object obj, int dataType) {
        if (obj != null) {
            String str = obj.toString().trim();
            if (isTypeString(dataType)) {
                str = str.replace("'", "''");
                str = String.format("'%s'", str);
            }

            return str;
        } else {
            return "null";
        }
    }

    public static String geneInClause(String col, Collection<String> values) {
        StringBuilder sql = (new StringBuilder(col)).append(" in(");
        int i = 0;
        int remainedSize = values.size();
        Iterator var6 = values.iterator();

        while (var6.hasNext()) {
            String str = (String) var6.next();
            ++i;
            --remainedSize;
            sql.append(str).append(",");
            if (i >= 100 && remainedSize > 0) {
                sql.deleteCharAt(sql.length() - 1);
                sql.append(")");
                sql.append(" or ").append(col).append(" in(");
                i = 0;
            }
        }

        sql.deleteCharAt(sql.length() - 1);
        sql.append(")");
        return sql.toString();
    }

    private static SqlQueryResultSet queryResults(IQueryInfo queryInfo, String whereCondition, Connection conn) throws DatabaseRuntimeException {
        ITable mainTable = queryInfo.getTable();
        SqlQueryResultSet sqlQueryResultSet = queryResults(mainTable, whereCondition, conn);
        if (sqlQueryResultSet != null && !sqlQueryResultSet.getResults().isEmpty() && queryInfo.getChildren() != null && !queryInfo.getChildren().isEmpty()) {
            IPkConstraint pkConstraint = queryInfo.getTable().getPkConstraint();
            List<IColumn> pkCols = pkConstraint != null ? pkConstraint.getColumns() : null;
            if (pkCols == null || pkCols.isEmpty()) {
                throw new DatabaseRuntimeException(String.format(Messages.SqlUtil_10, mainTable.getName()));
            }

            if (pkCols.size() != 1) {
                throw new DatabaseRuntimeException(String.format(Messages.SqlUtil_11, mainTable.getName()));
            }

            List<Map<String, Object>> results = sqlQueryResultSet.getResults();
            String pkName = ((IColumn) pkCols.get(0)).getName();
            int dataType = ((IColumn) pkCols.get(0)).getDataType();
            Set<String> pks = new HashSet();
            Iterator var12 = results.iterator();

            while (var12.hasNext()) {
                Map<String, Object> colNameValue = (Map) var12.next();
                Object obj = colNameValue.get(pkName);
                pks.add(formatSql(obj, dataType));
            }

            var12 = queryInfo.getChildren().iterator();

            while (var12.hasNext()) {
                IQueryInfo subQryInfo = (IQueryInfo) var12.next();
                ITable subTable = subQryInfo.getTable();
                IFkConstraint fkConstraint = null;
                if (subTable != null) {
                    if (subTable.getFkConstraints() != null) {
                        if (subTable.getFkConstraints().size() == 1) {
                            fkConstraint = (IFkConstraint) subTable.getFkConstraints().get(0);
                        } else {
                            fkConstraint = subTable.getFkConstraintByRefTableName(mainTable.getName());
                        }
                    }

                    if (fkConstraint == null) {
                        throw new DatabaseRuntimeException(String.format(Messages.SqlUtil_12, subTable.getName()));
                    }

                    if (fkConstraint.getColumns().size() != 1) {
                        throw new DatabaseRuntimeException(String.format(Messages.SqlUtil_13, subTable.getName()));
                    }

                    SqlQueryResultSet subResultSet = queryResults(subQryInfo, geneInClause(((IColumn) fkConstraint.getColumns().get(0)).getName(), pks), conn);
                    if (subResultSet != null) {
                        sqlQueryResultSet.getSubResultSets().add(subResultSet);
                    }
                }
            }
        }

        return sqlQueryResultSet;
    }

    private static void checkTable(ITable table) throws DatabaseRuntimeException {
        if (table == null || !StringUtils.isNotBlank(table.getName()) || table.getAllColumns() == null || table.getAllColumns().isEmpty()) {
            throw new DatabaseRuntimeException(Messages.SqlUtil_3);
        }
    }

    private static String getSql(ITable table, String whereCondition) {
        StringBuilder sql = new StringBuilder("select ");
        Iterator var4 = table.getAllColumns().iterator();

        while (var4.hasNext()) {
            IColumn col = (IColumn) var4.next();
            sql.append(col.getName()).append(", ");
        }

        sql.delete(sql.length() - 2, sql.length());
        sql.append(" from ").append(table.getName());
        if (StringUtils.isNotBlank(whereCondition)) {
            sql.append(" where ").append(whereCondition);
        }

        return sql.toString();
    }

    private static boolean isTypeString(int dataType) {
        return 1 == dataType || 12 == dataType || -1 == dataType || -9 == dataType || -15 == dataType;
    }

    private static String retriveCatelog(String dataBaseType) {
        return "Oracle".equalsIgnoreCase(dataBaseType) ? "" : null;
    }

    private static String retriveSchema(String dbType, String schema) {
        if ("Microsoft SQL Server".equalsIgnoreCase(dbType)) {
            return null;
        } else if ("Oracle".equalsIgnoreCase(dbType)) {
            return schema.toUpperCase();
        } else {
            return !"DB2".equalsIgnoreCase(dbType) && !"DB2/NT64".equalsIgnoreCase(dbType) ? null : schema.toUpperCase();
        }
    }

    private static String formatTableName(String tableName, String dbType) {
        if ("Microsoft SQL Server".equalsIgnoreCase(dbType)) {
            return tableName;
        } else if ("Oracle".equalsIgnoreCase(dbType)) {
            return tableName.toUpperCase();
        } else {
            return !"DB2".equalsIgnoreCase(dbType) && !"DB2/NT64".equalsIgnoreCase(dbType) ? tableName : tableName.toUpperCase();
        }
    }
}
