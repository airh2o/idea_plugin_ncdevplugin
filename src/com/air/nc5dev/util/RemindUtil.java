package com.air.nc5dev.util;

import com.intellij.openapi.ui.Messages;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * <br/>
 * <br/>
 * <br/>
 *
 * @author 唐粟 Email:209308343@qq.com
 * @date 2021/1/29 0029 18:48
 * @project
 * @Version
 */
public class RemindUtil {
    private static final Timer timer = new Timer();

    public static void clear() {
        timer.purge();
    }

    public static void add(long time, TimeUnit unit, String title, String msg) {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Messages.showInfoMessage(msg, title);
            }
        }, unit.toMillis(time));
    }
}
