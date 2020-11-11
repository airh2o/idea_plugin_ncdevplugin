package com.air.nc5dev.util.jdbc;

import com.air.nc5dev.util.NCPassWordUtil;
import com.air.nc5dev.util.ncutils.nc5x.NC5xEncode;
import com.air.nc5dev.vo.NCDataSourceVO;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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
        return DriverManager.getConnection(ds.getDatabaseUrl(), ds.getUser(), ds.getPassword());
    }
}
