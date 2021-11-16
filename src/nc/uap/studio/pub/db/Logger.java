package nc.uap.studio.pub.db;

import com.air.nc5dev.util.idea.LogUtil;

/**
 * 兼容
 */
public class Logger {
    public Logger() {
    }

    public static void error(String msg, Exception e) {
        LogUtil.error(msg);
    }

    public static void error(String format) {
        LogUtil.error(format);
    }

    public static boolean isDebugEnabled() {
        return false;
    }

    public static void debug(String msg) {
        LogUtil.debug(msg);
    }

    public static void info(String msg) {
        LogUtil.info(msg);
    }

    public static void warn(String msg) {
        LogUtil.info(msg);
    }

    public static void log(int level, String msg, Throwable throwable) {
        if (throwable == null) {
            LogUtil.info(msg);
            return;
        }
        LogUtil.error(msg, throwable);
    }
}
