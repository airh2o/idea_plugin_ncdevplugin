package com.air.nc5dev.util.jdbc.resulthandel;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 返回null， 没有结果的查询 <br/>
 * <br/>
 * <br/>
 *
 * @author 唐粟 Email:209308343@qq.com
 * @date 2020/9/18 9:37
 * @project
 * @Version
 */
public class NoValueResultSetExtractor
        implements ResultSetExtractor {

    @Override
    public Object extractData(ResultSet rs) throws SQLException {
        return null;
    }

}
