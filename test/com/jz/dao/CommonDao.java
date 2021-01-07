//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.jz.dao;

import com.jz.bean.PropInfo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;

public class CommonDao {
    private Connection conn = null;
    private Statement stmt = null;
    private String db_type = "";

    public static void testConn(PropInfo info) throws Exception {
    }

    public CommonDao(PropInfo info) throws Exception {
        String url = "";
        this.db_type = info.getField("dbtype");
        String ip = info.getField("ip");
        String username = info.getField("username");
        String password = info.getField("psw");
        String dbName = info.getField("dbname");
        if (this.db_type.equals("SQLSERVER")) {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } else if (db_type.toUpperCase().startsWith("ORACLE")) {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        }

        if (this.conn == null) {
            if ("SQLSERVER".equals(this.db_type)) {
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                url = ip;
                this.conn = DriverManager.getConnection(url, username, password);
            } else {
                Class.forName("oracle.jdbc.driver.OracleDriver");
                url = ip;
                this.conn = DriverManager.getConnection(url, username, password);
            }
        }

        this.conn.isClosed();
        this.conn.setAutoCommit(false);
        this.stmt = this.conn.createStatement();
    }

    public void commit() {
        try {
            this.conn.commit();
        } catch (SQLException var2) {
            this.rollback();
        }

    }

    public ResultSet queryBySql(String sql) throws SQLException {
        if ("ORACLE".equals(this.db_type)) {
            sql = sql.replaceAll("isnull", "nvl");
        }

        ResultSet rs = this.stmt.executeQuery(sql);
        return rs;
    }

    public void addBatch(String sql) throws SQLException, Exception {
        this.stmt.addBatch(sql);
    }

    public void clearBatch() throws SQLException, Exception {
        this.stmt.clearBatch();
    }

    public void excuteBatch() throws SQLException {
        this.stmt.executeBatch();
    }

    public void rollback() {
        try {
            this.conn.rollback();
        } catch (SQLException var2) {
            var2.printStackTrace();
        }

    }

    public int updateBySql(String sql) throws Exception {
        return this.stmt.executeUpdate(sql);
    }

    public void updateBySqls(List<String> lstSql) throws Exception {
        this.clearBatch();
        Iterator var3 = lstSql.iterator();

        while (var3.hasNext()) {
            String sql = (String) var3.next();
            this.addBatch(sql);
        }

        this.excuteBatch();
    }
}
