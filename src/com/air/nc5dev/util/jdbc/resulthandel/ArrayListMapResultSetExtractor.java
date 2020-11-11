package com.air.nc5dev.util.jdbc.resulthandel;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

/**
 * 把查询结果 转换成  Arraylist 里是map的 顺序集合 <br/>
 * <br/>
 * <br/>
 *
 * @author 唐粟 Email:209308343@qq.com
 * @date 2020/9/18 9:37
 * @project
 * @Version
 */
public class ArrayListMapResultSetExtractor
        implements ResultSetExtractor<ArrayList<Map<String, Object>>> {

    @Override
    public ArrayList<Map<String, Object>> extractData(ResultSet rs) throws SQLException {
        ArrayList<Map<String, Object>> rows = Lists.newArrayListWithCapacity(200);

        Map<String, Object> row;
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        while (rs.next()) {
            row = Maps.newHashMapWithExpectedSize(columnCount);

            for (int i = 1; i <= columnCount; i++) {
                row.put(metaData.getColumnLabel(i), rs.getObject(i));
            }

            rows.add(row);
        }

        return rows;
    }

}
