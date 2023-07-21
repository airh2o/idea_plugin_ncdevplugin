package com.air.nc5dev.util.searchfulldatabase;

import com.air.nc5dev.util.ExceptionUtil;
import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.util.idea.LogUtil;
import lombok.Data;

import javax.swing.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 创建执行搜索的线程工具类
 *
 * @author Air
 * @date 2018年11月21日14:38:39
 * @since v1
 */
@Data
public class TaskFather {
    /**
     * 开始搜索
     */
    public void start() {
        //先查询出所有的表
        try {
            if (StringUtil.isBlank(config.getTableListSql())
                    || StringUtil.isBlank(config.getKey())) {
                config.getDialog().getButton_Search().setText("开始搜索");
                return;
            }
            final String input = config.getKey();
            DbConnectionFactory dbConnectionFactory = new DbConnectionFactory();
            Connection conn = dbConnectionFactory.getConnection(config.getDataSource(), config.indicator);
            Statement st = conn.createStatement();
            String sql = config.getTableListSql();
            if (StringUtil.isBlank(sql)) {
                finalshError("查询表名SQL语句错误!");
                return;
            }
            ResultSet rs = st.executeQuery(sql);
            ArrayList<String> tabs = new ArrayList<>(500);
            while (rs.next()) {
                tabs.add(rs.getString(1));
            }
            if (tabs.size() < 1) {
                finalshError("数据库没有任何用户表!");
                return;
            }
            results.clear();
            updateTable();
            isStop = false;

            results.clear();
            total.set(0);
            config.getDialog().getThreadProgressBar().setValue(0);
            config.getDialog().getThreadProgressBar().setMaximum(tabs.size());
            String msg = String.format("进度: %s / %s, %s ", 0, tabs.size(), 0);
            //  config.getDialog().getThreadProgressBar().setString(msg);
            setMsg(msg);

            int threadMax = config.getThreadNum();
            calcSingleThreadTabNum(tabs.size(), threadMax);
            dbConnectionFactory.setMax(5);

            final AtomicInteger threadNum = new AtomicInteger(0);
            threadPool = new ThreadPoolExecutor(totalThread
                    , totalThread + 1
                    , 0L
                    , TimeUnit.MILLISECONDS
                    , new LinkedBlockingQueue<>(totalThread)
                    , r -> new Thread(r, "Search_Database_Full_thread_" + threadNum.incrementAndGet()));
            //开始实例化线程进行查询
            ArrayList<ArrayList<String>> threadsTabs = new ArrayList<>(totalThread);
            ArrayList<String> threadTabs;
            for (int i = 0; i < totalThread; i++) {
                threadTabs = new ArrayList<>(singleThreadTabNum);
                int index = i * singleThreadTabNum;
                if (index < tabs.size()) {
                    for (int x = 0; x < singleThreadTabNum; x++) {
                        if (index < tabs.size()) {
                            threadTabs.add(tabs.get(index++));
                        } else {
                            break;
                        }
                    }
                }
                threadsTabs.add(threadTabs);
            }
            threadsTabs.stream().forEach(threadTableNameList -> {
                TableDao dao = new TableDao(this, threadTableNameList);
                dao.setFast(config.isFastQuery());
                threadPool.execute(() -> {
                    dao.queryTableAllColumn(input);
                    checkState();
                });
            });
            threadPool.shutdown();
            setMsg("当前正在搜索中线程数: " + threadPool.getActiveCount());
        } catch (Throwable e) {
            finalshError("发生错误终止搜索: "+ ExceptionUtil.stacktraceToString(e, 25));
            e.printStackTrace();
        }
    }

    /**
     * 开始搜索
     */
    public void syncRun() {
        //先查询出所有的表
        try {
            if (StringUtil.isBlank(config.getTableListSql())
                    || StringUtil.isBlank(config.getKey())) {
                config.getDialog().getButton_Search().setText("开始搜索");
                return;
            }
            final String input = config.getKey();
            DbConnectionFactory dbConnectionFactory = new DbConnectionFactory();
            Connection conn = dbConnectionFactory.getConnection(config.getDataSource(), config.indicator);
            Statement st = conn.createStatement();
            String sql = config.getTableListSql();
            if (StringUtil.isBlank(sql)) {
                finalshError("查询表名SQL语句错误!");
                return;
            }
            ResultSet rs = st.executeQuery(sql);
            ArrayList<String> tabs = new ArrayList<>(500);
            while (rs.next()) {
                tabs.add(rs.getString(1));
            }
            if (tabs.size() < 1) {
                finalshError("数据库没有任何用户表!");
                return;
            }
            results.clear();
            updateTable();
            isStop = false;

            results.clear();
            total.set(0);
            config.getDialog().getThreadProgressBar().setValue(0);
            config.getDialog().getThreadProgressBar().setMaximum(tabs.size());
            String msg = String.format("进度: %s / %s, %s ", 0, tabs.size(), 0);
            //  config.getDialog().getThreadProgressBar().setString(msg);
            setMsg(msg);

            dbConnectionFactory.setMax(5);

            TableDao dao = new TableDao(this, tabs);
            dao.setFast(config.isFastQuery());
            dao.queryTableAllColumn(input);
            setMsg("搜索结束");
            stopAll();
            checkState();
        } catch (Throwable e) {
            finalshError("发生错误终止搜索: " + ExceptionUtil.stacktraceToString(e, 25));
            e.printStackTrace();
        }
    }

    public void finalsh(String msg) {
        setMsg(msg);
    }

    public void finalshError(String msg) {
        setMsg(msg);
        LogUtil.error(msg);
        stopAll();
    }

    public void checkState() {
        if (threadPool != null && threadPool.getActiveCount() > 0) {
            setMsg("当前正在搜索中线程数: " + threadPool.getActiveCount());
            return;
        }

        if (!isStop) {
            return;
        }

        finalsh("所有线程已完成, 搜索完成!");
        config.getDialog().getThreadProgressBar().setValue(config.getDialog().getThreadProgressBar().getMaximum());
        //  config.getDialog().getThreadProgressBar().setString("完成");
        config.getDialog().getButton_Search().setEnabled(true);
        config.getDialog().getButton_Search().setText("开始搜索");
        config.getDialog().getTaskFatherAtomicReference().set(null);
        updateTable();
    }

    /**
     * 计算 一共需要多少个进程, 每个进程分配多少数量
     *
     * @param size
     * @param threadMax
     */
    private void calcSingleThreadTabNum(int size, int threadMax) {
        totalThread = threadMax;
        if (size <= threadMax) {
            singleThreadTabNum = 1;
            totalThread = size;
        }
        int cf = size / threadMax;
        if (cf * threadMax == size) {
            singleThreadTabNum = cf;
        } else {
            int moreEechThread = ((size - (cf * threadMax)) / threadMax);
            moreEechThread = moreEechThread == 0 ? 1 : moreEechThread + 1;
            singleThreadTabNum = cf + moreEechThread;
        }
    }

    /**
     * 终止所有搜索线程
     */
    public void stopAll() {
        isStop = true;
        config.getDialog().getButton_Search().setEnabled(false);
    }

    public boolean isStop() {
        return isStop;
    }

    /**
     * 添加一个搜索结果
     *
     * @param re
     */
    public synchronized void addSearchResult(SearchResultVO re) {
        results.add(re);
        int total = this.total.incrementAndGet();
        JProgressBar threadProgressBar = config.getDialog().getThreadProgressBar();
        String msg = String.format("进度: %s / %s,  表:%s 列:%s"
                , total
                , threadProgressBar.getMaximum()
                , re.getTable()
                , re.getField()
        );
        threadProgressBar.setValue(total);
        setMsg(msg);
        updateTable();
    }

    /**
     * 根据搜索结果 更新表格UI
     */
    public void updateTable() {
        config.getDialog().setSelectTableDatas(getResults());
    }

    public void setMsg(String msg) {
        // threadProgressBar.setString(msg);
        config.getIndicator().setText(msg);
        SwingUtilities.invokeLater(() -> config.getDialog().getTextArea_msg().append("\n" + msg));
    }

    public TaskFather(SearchFullDatabaseConfigVO config) {
        this.config = config;
        this.results = new CopyOnWriteArrayList<>();
    }

    private SearchFullDatabaseConfigVO config;
    private int singleThreadTabNum = 1;
    private int totalThread = 1;
    private ThreadPoolExecutor threadPool;
    private List<SearchResultVO> results;
    private volatile boolean isStop = false;
    AtomicInteger total = new AtomicInteger(0);
}
