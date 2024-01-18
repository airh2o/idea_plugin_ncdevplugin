package org.tangsu.mstsc.dao;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.air.nc5dev.util.StringUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Data;
import org.tangsu.mstsc.MstscApplication;
import org.tangsu.mstsc.entity.IEntity;

import java.io.File;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

@Data
public class BaseDao {
    Connection conn;
    String dbFile;

    public static volatile String BASEDIR = new File(StringUtil.get(System.getProperty("user.home"),
            System.getProperty("java.home")))
            .getAbsolutePath();

    public static String getDefualtBaseDir() {
        return BASEDIR;
    }

    public static String getDefualtDBFile() {
        return getDefualtBaseDir() + File.separator + "nc5dev_info_MstscMangger.db";
    }

    public BaseDao() throws Exception {
        dbFile = getDefualtDBFile();
        init();
    }

    public BaseDao(String dbFile) throws Exception {
        this.dbFile = dbFile;
        if (StringUtil.isBlank(this.dbFile)) {
            this.dbFile = getDefualtDBFile();
        }
        init();
    }

    public <T extends IEntity> List<T> findAll(String sql, Class<T> clz) throws InstantiationException
            , IllegalAccessException, SQLException {
        LinkedList<T> all = Lists.newLinkedList();

        T obj = clz.newInstance();
        List<Map<String, Object>> rows = queryRows(sql);

        for (Map<String, Object> row : rows) {
            obj = clz.newInstance();
            BeanUtil.copyProperties(row, obj);
            all.add(obj);
        }

        return all;
    }

    public <T extends IEntity> List<T> findAll(Class<T> clz) throws InstantiationException
            , IllegalAccessException, SQLException {
        T obj = clz.newInstance();
        return findAll(buildSelectSql(obj), clz);
    }

    public static <T extends IEntity> String buildSelectSql(T obj) {
        return "select * from {table} where dr=0 "
                .replace("{table}", obj.getTableName())
                .replace("{idname}", obj.getIdName());
    }

    public static <T extends IEntity> String buildCreateTableSql(T obj) {
        String sql = String.format("create table %s ( ", obj.getTableName());

        Set<String> fs = obj.getFieadNames();
        boolean frist = true;
        for (String f : fs) {
            if (!frist) {
                sql += ",";
            }

            frist = false;

            if (f.equals("dr")) {
                sql += " dr INT default 0 ";
                continue;
            }

            Field field = ReflectUtil.getField(obj.getClass(), f);
            if (field.getType().isAssignableFrom(CharSequence.class)
                    || field.getType() == char.class
                    || field.getType() == String.class
                    || field.getType() == Character.class
            ) {
                sql += f + " TEXT ";
            } else if (field.getType() == int.class
                    || field.getType() == long.class
                    || field.getType() == byte.class
                    || field.getType() == Byte.class
                    || field.getType() == Integer.class
                    || field.getType() == Long.class
            ) {
                sql += f + " INT ";
            } else if (field.getType() == float.class
                    || field.getType() == Float.class
                    || field.getType() == double.class
                    || field.getType() == Double.class
            ) {
                sql += f + " REAL ";
            } else {
                sql += f + " TEXT ";
            }

            if (f.equals(obj.getIdName())) {
                sql += " primary key ";
            }
        }

        return sql + " ) ";
    }

    public <T extends IEntity> T getById(String id, Class<T> clz) throws InstantiationException
            , IllegalAccessException, SQLException {
        T obj = clz.newInstance();
        Map<String, Object> row = queryRow(
                "select * from {table} where {idname}={id}"
                        .replace("{table}", obj.getTableName())
                        .replace("{idname}", obj.getIdName())
                        .replace("{id}", id)
        );

        BeanUtil.copyProperties(row, obj);

        return obj;
    }

    public <T extends IEntity> int deleteLogicById(String id, Class<T> clz) throws InstantiationException
            , IllegalAccessException, SQLException {
        T obj = clz.newInstance();
        String sql = "update {table} set dr=1 where {idname}={id}"
                .replace("{table}", obj.getTableName())
                .replace("{idname}", obj.getIdName())
                .replace("{id}", id);
        Statement st = null;
        try {
            st = conn.createStatement();
            return st.executeUpdate(sql);
        } finally {
            IoUtil.close(st);
        }
    }

    public <T extends IEntity> int deleteById(String id, Class<T> clz) throws InstantiationException
            , IllegalAccessException, SQLException {
        T obj = clz.newInstance();

        String sql = "delete from {table} where {idname}='{id}'"
                .replace("{table}", obj.getTableName())
                .replace("{idname}", obj.getIdName())
                .replace("{id}", id);

        Statement st = null;
        try {
            st = conn.createStatement();
            return st.executeUpdate(sql);
        } finally {
            IoUtil.close(st);
        }
    }

    public <T extends IEntity> int updateById(T obj) throws InstantiationException
            , IllegalAccessException, SQLException {
        Set<String> ns = obj.getFieadNames();
        StringBuilder sql = new StringBuilder(1000);
        sql.append("update ")
                .append(obj.getTableName())
                .append(" set ");

        ArrayList<Object> pars = Lists.newArrayList();
        for (String n : ns) {
            Object v = ReflectUtil.getFieldValue(obj, n);
            if (v != null) {
                if (!pars.isEmpty()) {
                    sql.append(',');
                }
                pars.add(v);
                sql.append(n).append("=? ");
            }
        }

        sql.append(" where ").append(obj.getIdName()).append("=? ");
        pars.add(ReflectUtil.getFieldValue(obj, obj.getIdName()));

        PreparedStatement st = null;
        try {
            System.out.println(sql.toString());
            st = conn.prepareStatement(sql.toString());

            for (int i = 0; i < pars.size(); i++) {
                st.setObject(i + 1, pars.get(i));
            }

            return st.executeUpdate();
        } finally {
            IoUtil.close(st);
        }
    }

    public <T extends IEntity> int insert(T obj) throws InstantiationException
            , IllegalAccessException, SQLException {
        Set<String> ns = obj.getFieadNames();
        StringBuilder sql = new StringBuilder(1000);
        StringBuilder vs = new StringBuilder(1000);
        sql.append("insert into ")
                .append(obj.getTableName())
                .append(" ( ");
        vs.append(" ) values( ");

        ArrayList<Object> pars = Lists.newArrayList();
        for (String n : ns) {
            Object v = ReflectUtil.getFieldValue(obj, n);
            if (v != null) {
                if (!pars.isEmpty()) {
                    sql.append(',');
                    vs.append(',');
                }
                pars.add(v);
                sql.append(n);
                vs.append('?');
            }
        }

        PreparedStatement st = null;
        try {
            String sqls = sql.append(vs).append(" ) ").toString();
            System.out.println(sqls);
            st = conn.prepareStatement(sqls);

            for (int i = 0; i < pars.size(); i++) {
                st.setObject(i + 1, pars.get(i));
            }

            return st.executeUpdate();
        } finally {
            IoUtil.close(st);
        }
    }

    public Map<String, Object> queryRow(String sql) throws SQLException {
        HashMap<String, Object> row = Maps.newHashMap();
        Statement st = null;
        ResultSet rs = null;
        try {
            st = conn.createStatement();
            rs = st.executeQuery(sql);

            while (rs.next()) {
                ResultSetMetaData rm = rs.getMetaData();
                for (int i = 0; i < rm.getColumnCount(); i++) {
                    row.put(getStr(rm.getColumnLabel(i + 1), rm.getColumnName(i + 1)), rs.getObject(i + 1));
                }
                break;
            }
        } finally {
            IoUtil.close(st);
            IoUtil.close(rs);
        }

        return row;
    }

    public int update(String sql) throws SQLException {
        Statement st = null;
        try {
            st = conn.createStatement();
            return st.executeUpdate(sql);
        } finally {
            IoUtil.close(st);
        }
    }

    public List<Map<String, Object>> queryRows(String sql) throws SQLException {
        ArrayList<Map<String, Object>> rows = Lists.newArrayList();
        Statement st = null;
        ResultSet rs = null;
        try {
            st = conn.createStatement();
            rs = st.executeQuery(sql);
            ResultSetMetaData rm = rs.getMetaData();
            while (rs.next()) {
                HashMap<String, Object> row = Maps.newHashMap();
                rows.add(row);
                for (int i = 0; i < rm.getColumnCount(); i++) {
                    row.put(getStr(rm.getColumnLabel(i + 1), rm.getColumnName(i + 1)), rs.getObject(i + 1));
                }
            }
        } finally {
            IoUtil.close(st);
            IoUtil.close(rs);
        }

        return rows;
    }

    public void close() throws SQLException {
        if (!conn.getAutoCommit()) {
            conn.rollback();
        }
        IoUtil.close(conn);
    }

    public void submit() throws SQLException {
        submitNoClose();
        IoUtil.close(conn);
    }

    public void submitNoClose() throws SQLException {
        if (!conn.getAutoCommit()) {
            conn.commit();
        }
    }

    public void init() throws Exception {
        Class.forName("org.sqlite.JDBC");
        conn = DriverManager.getConnection("jdbc:sqlite:" + dbFile);
        conn.setAutoCommit(false);
    }

    public static String getStr(String... ss) {
        if (ss == null) {
            return null;
        }

        for (String s : ss) {
            if (StrUtil.isNotBlank(s)) {
                return s;
            }
        }
        return null;
    }
}
