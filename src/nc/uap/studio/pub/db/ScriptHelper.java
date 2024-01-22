//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package nc.uap.studio.pub.db;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.vo.ItemsItemVO;
import nc.uap.studio.pub.db.model.IColumn;
import nc.uap.studio.pub.db.model.ITable;
import nc.uap.studio.pub.db.script.export.IScriptExportStratege;
import nc.uap.studio.pub.db.script.export.InitDataExportStratege2;

public class ScriptHelper {
    public ScriptHelper() {
    }

    public static String getSelectTable(String sql) {
        String regex = "";
        if (Pattern.matches("^.*\\swhere\\s.*$", sql)) {
            regex = "(from)(.+?)(where)";
        } else {
            regex = "(from)(.+)$";
        }

        Pattern pattern = Pattern.compile(regex, 2);
        Matcher matcher = pattern.matcher(sql);
        return matcher.find() ? matcher.group(2) : null;
    }

    public static String convert2DeleteSql(ITable table
            , Map<String, Object> result
            , String separator
            , ItemsItemVO itemVO) {
        List<IColumn> pkColumns = table.getPkConstraint().getColumns();
        boolean bHasPK = pkColumns != null && pkColumns.size() > 0;
        StringBuilder sql = new StringBuilder();
        if (bHasPK) {
            sql.append("delete from ");
            if (StringUtil.isNotBlank(itemVO.getSchema())) {
                sql.append(itemVO.getSchema());
            }
            sql.append(table.getName());
            sql.append(" where ");

            for (int i = 0; i < pkColumns.size(); ++i) {
                IColumn column = (IColumn) pkColumns.get(i);
                String txtValue = SqlUtil.formatSql(result.get(column.getName()), column.getDataType());
                sql.append(column.getName());
                sql.append("=");
                sql.append(txtValue);
                if (i == pkColumns.size() - 1) {
                    sql.append(separator);
                } else {
                    sql.append(" and ");
                }
            }
        }

        return sql.toString();
    }

    public static String convert2InsertSqls(ITable table
            , Map<String, Object> result
            , String separator
            , IScriptExportStratege stratege
            , ItemsItemVO itemVO) {
        List<IColumn> pkColumns = table.getPkConstraint().getColumns();
        boolean bHasPK = pkColumns != null && pkColumns.size() > 0;
        List<String> lstPKColumnName = null;
        StringBuilder sbPKColumns = new StringBuilder();
        if (bHasPK) {
            lstPKColumnName = new ArrayList(pkColumns.size());
            Iterator iterator = pkColumns.iterator();

            while (iterator.hasNext()) {
                Object element = iterator.next();
                IColumn column = (IColumn) element;
                if (column != null) {
                    String columnName = column.getName();
                    lstPKColumnName.add(columnName);
                    sbPKColumns.append(columnName).append(",");
                }
            }
        }

        StringBuilder cols = new StringBuilder();
        StringBuilder values = new StringBuilder();
        StringBuilder singleSql = new StringBuilder("insert into ");

        if (StringUtil.isNotBlank(itemVO.getSchema())) {
            singleSql.append(itemVO.getSchema());
        }
        singleSql.append(table.getName()).append("(");

        StringBuilder sbPKValues = new StringBuilder();
        if (bHasPK) {
            Iterator iterator = lstPKColumnName.iterator();

            while (iterator.hasNext()) {
                Object element = iterator.next();
                String pkColumnName = (String) element;
                IColumn col = table.getColumnByName(pkColumnName);
                sbPKValues.append(SqlUtil.formatSql(result.get(pkColumnName), col.getDataType())).append(",");
            }
        }

        List<String> multColumnList = new LinkedList();
        Iterator iterator = result.entrySet().iterator();

        while (true) {
            while (true) {
                Entry entry;
                String columnName;
                do {
                    if (!iterator.hasNext()) {
                        cols.deleteCharAt(cols.length() - 1);
                        values.deleteCharAt(values.length() - 1);
                        if (bHasPK) {
                            singleSql.append(sbPKColumns);
                        }

                        singleSql.append(cols.toString()).append(") values").append("(");
                        if (bHasPK) {
                            singleSql.append(sbPKValues.toString());
                        }

                        singleSql.append(values.toString()).append(")").append(separator);
                        return singleSql.toString();
                    }

                    entry = (Entry) iterator.next();
                    columnName = (String) entry.getKey();
                } while (bHasPK && lstPKColumnName.contains(columnName));

                IColumn col = table.getColumnByName(columnName);
                if (isBlobColumn(col)) {
                    cols.append(columnName).append(",");
                    values.append("null,");
                } else if (!isMultColumn(col, table, stratege) && !multColumnList.contains(columnName)) {
                    cols.append(columnName).append(",");
                    values.append(SqlUtil.formatSql(entry.getValue(), col.getDataType())).append(",");
                } else {
                    char endChar = columnName.charAt(columnName.length() - 1);
                    if (!Character.isDigit(endChar)) {
                        cols.append(columnName).append(",");
                        values.append("'~'").append(",");
                        multColumnList.clear();
                        multColumnList.add(columnName + "2");
                        multColumnList.add(columnName + "3");
                        multColumnList.add(columnName + "4");
                        multColumnList.add(columnName + "5");
                        multColumnList.add(columnName + "6");
                    }
                }
            }
        }
    }

    public static boolean isBlobColumn(IColumn col) {
        return col.getTypeName().equalsIgnoreCase("image")
                || col.getTypeName().equalsIgnoreCase("blob")
                || col.getTypeName().equalsIgnoreCase("blob(128m)");
    }

    public static boolean isMultColumn(IColumn col, ITable table, IScriptExportStratege stratege) {
        if (stratege instanceof InitDataExportStratege2) {
            Map<String, List<String>> mlTableInfo = ((InitDataExportStratege2) stratege).getMlTableInfo();
            List<String> list = (List) mlTableInfo.get(table.getName());
            if (list != null) {
                return list.contains(col.getName().toLowerCase());
            }
        }

        return false;
    }
}
