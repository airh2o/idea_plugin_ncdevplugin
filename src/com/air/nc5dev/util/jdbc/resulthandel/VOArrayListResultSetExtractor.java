package com.air.nc5dev.util.jdbc.resulthandel;

import com.air.nc5dev.util.CollUtil;
import com.air.nc5dev.util.ReflectUtil;
import com.google.common.collect.Lists;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 把查询结果 转换成 VO Arraylist 顺序集合 <br/>
 * <br/>
 * <br/>
 *
 * @author 唐粟 Email:209308343@qq.com
 * @date 2020/9/18 9:37
 * @project
 * @Version
 */
public class VOArrayListResultSetExtractor<T> implements MapResultSetExtractor<ArrayList<T>>
        , ResultSetExtractor<ArrayList<T>> {
    /**
     * VO的class
     */
    protected Class voClz;

    public VOArrayListResultSetExtractor(Class voClz) {
        this.voClz = voClz;
    }

    public static VOArrayListResultSetExtractor of(Class clz) {
        return new VOArrayListResultSetExtractor(clz);
    }

    @Override
    public ArrayList<T> extractData(List<Map<String, Object>> rows) throws SQLException {
        if (CollUtil.isEmpty(rows)) {
            return Lists.newArrayList();
        }

        ArrayList<T> res = Lists.newArrayListWithCapacity(rows.size());

        for (int i = 0; i < rows.size(); i++) {
            res.add(extractOneRow(rows.get(i)));
        }

        return res;
    }

    @Override
    public ArrayList<T> extractData(ResultSet rs) throws SQLException {
        return extractData(new ArrayListMapResultSetExtractor().extractData(rs));
    }

    /**
     * 解析一行数据
     *
     * @param row
     * @return
     */
    public T extractOneRow(Map<String, Object> row) {
        final T vo = (T) ReflectUtil.newInstance(voClz);

        row.forEach((k, v) -> {
            ReflectUtil.setFieldValueAutoConvertIgnoreNotHasField(vo, k, v);
        });

        return vo;
    }
}
