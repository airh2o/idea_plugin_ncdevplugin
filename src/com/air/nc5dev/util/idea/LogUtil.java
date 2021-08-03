package com.air.nc5dev.util.idea;

import com.air.nc5dev.service.ui.IMeassgeConsole;
import com.intellij.openapi.diagnostic.Logger;

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
    public static final Logger LOGGER = Logger.getInstance(LogUtil.class);


    public static void debug(String msg) {
        IMeassgeConsole service = ProjectUtil.getService(IMeassgeConsole.class);
        if (service == null) {
            LOGGER.debug("com.air.nc5dev.util.idea.ProjectUtil.getService(java.lang.Class<T>) null for msg: " + msg);
            return;
        }

        service.debug(msg);
    }

    public static boolean debugEnabled() {
        IMeassgeConsole service = ProjectUtil.getService(IMeassgeConsole.class);
        if (service == null) {
            LOGGER.debug("com.air.nc5dev.util.idea.ProjectUtil.getService(java.lang.Class<T>) null for debugEnabled ");
            return true;
        }

        return service.debugEnabled();
    }

    public static void info(String msg) {
        IMeassgeConsole service = ProjectUtil.getService(IMeassgeConsole.class);
        if (service == null) {
            LOGGER.info("com.air.nc5dev.util.idea.ProjectUtil.getService(java.lang.Class<T>) null for msg: " + msg);
            return;
        }

        service.info(msg);
    }

    public static void error(String msg) {
        IMeassgeConsole service = ProjectUtil.getService(IMeassgeConsole.class);
        if (service == null) {
            LOGGER.error("com.air.nc5dev.util.idea.ProjectUtil.getService(java.lang.Class<T>) null for msg: " + msg);
            return;
        }

        service.error(msg);
    }

    public static void error(String msg, Throwable t) {
        IMeassgeConsole service = ProjectUtil.getService(IMeassgeConsole.class);
        if (service == null) {
            LOGGER.debug("com.air.nc5dev.util.idea.ProjectUtil.getService(java.lang.Class<T>) null for msg: " + msg, t);
            return;
        }

        service.error(msg, t);
    }

    public static void clear() {
        IMeassgeConsole service = ProjectUtil.getService(IMeassgeConsole.class);
        if (service == null) {
            LOGGER.debug("com.air.nc5dev.util.idea.ProjectUtil.getService(java.lang.Class<T>) null for clear");
            return;
        }

        service.clear();
    }
}
