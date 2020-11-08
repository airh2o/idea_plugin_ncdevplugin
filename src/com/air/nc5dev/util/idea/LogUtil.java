package com.air.nc5dev.util.idea;

import com.air.nc5dev.service.ui.IMeassgeConsole;

/**
 * 消息日志 console 工具方法 <br/>
 * <br/>
 * <br/>
 *
 * @author 唐粟 Email:209308343@qq.com
 * @date 2020/11/8 8:49
 * @project
 * @Version
 */
public class LogUtil {
    public static void debug(String msg) {
        ProjectUtil.getService(IMeassgeConsole.class).debug(msg);
    }

    public static boolean debugEnabled() {
        return ProjectUtil.getService(IMeassgeConsole.class).debugEnabled();
    }

    public static void info(String msg) {
        ProjectUtil.getService(IMeassgeConsole.class).info(msg);
    }

    public static void error(String msg) {
        ProjectUtil.getService(IMeassgeConsole.class).error(msg);
    }

    public static void error(String msg, Throwable t) {
        ProjectUtil.getService(IMeassgeConsole.class).error(msg, t);
    }

    public static void clear() {
        ProjectUtil.getService(IMeassgeConsole.class).clear();
    }
}
