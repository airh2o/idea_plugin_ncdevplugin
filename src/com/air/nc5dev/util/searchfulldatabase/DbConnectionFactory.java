package com.air.nc5dev.util.searchfulldatabase;

import com.air.nc5dev.util.idea.LogUtil;
import com.air.nc5dev.vo.NCDataSourceVO;
import com.intellij.openapi.progress.ProgressIndicator;
import lombok.Data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据库链接 工具,管理数据库连接的
 *
 * @author air
 * @date 2018年11月21日12:54:59
 * @since v1
 */
@Data
public class DbConnectionFactory {
    ProgressIndicator indicator;
    static Map<String, List<Connection>> dbConnectionPool = new ConcurrentHashMap();
    int max = 50;
    int now = 0;

    /**
     * 获取一个数据库链接
     *
     * @param config
     * @param indicator
     * @return 如果已经超出了 最大连接数 就从第0个获得连接开始获取 连接 不停循环
     */
    public Connection getConnection(NCDataSourceVO ds, ProgressIndicator indicator) {
        String driver = ds.getDriverClassName();
        String url = ds.getDatabaseUrl();
        String user = ds.getUser();
        String pass = ds.getPassword();
        this.indicator = indicator;
        return getConnection(url, user, pass, driver);
    }

    public Connection getConnection(String url, String user, String pass, String driver) {
        synchronized (DbConnectionFactory.class) {
            List<Connection> cs = dbConnectionPool.get(url + user);
            if (cs == null) {
                cs = new ArrayList<>();
                dbConnectionPool.put(url + user, cs);
            }
            LogUtil.tryInfo(String.format("数据库连接数量: 类别数量:%s - 类别:%s - 池大小:%s "
                    , dbConnectionPool.size()
                    , url + user
                    , cs.size()));
            if (cs.size() >= max) {
                return cs.get(0);
            }

            Connection con = null;
            try {
                Class.forName(driver);
                con = DriverManager.getConnection(url, user, pass);
                cs.add(con);
                return con;
            } catch (Exception e) {
                LogUtil.error("连接数据库出错：" + e);
                if (indicator != null) {
                    indicator.setText("连接数据库出错：" + e);
                }
                e.printStackTrace();
                return null;
            }
        }
    }

    /**
     * 关闭所有连接
     */
    public void closeDBAll() {
        for (String s : dbConnectionPool.keySet()) {
            dbConnectionPool.get(s).stream().forEach(e -> {
                try {
                    e.close();
                } catch (Throwable e1) {
                    LogUtil.error("断开数据库出错：" + e);
                    if (indicator != null) {
                        indicator.setText("断开数据库出错：" + e);
                    }
                }
            });
        }
        dbConnectionPool.clear();
        now = 0;
    }
}
