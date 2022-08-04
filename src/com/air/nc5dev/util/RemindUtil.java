package com.air.nc5dev.util;

import com.air.nc5dev.util.idea.ProjectUtil;
import com.intellij.openapi.ui.Messages;

import javax.swing.*;
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
    private static Timer timer = new Timer();

    public static synchronized void clear() {
        if (timer == null) {
            return;
        }
        timer.cancel();
        timer = null;
    }

    public static synchronized void add(boolean loop, long time, TimeUnit unit, String title, String msg) {
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();

        if (loop) {
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    //  Messages.showInfoMessage(msg, title);
                    int i = JOptionPane.showConfirmDialog(null, msg
                            , title + " (点击 否 立即终止定时提醒)"
                            , JOptionPane.ERROR_MESSAGE);
                    if (JOptionPane.NO_OPTION == i) {
                       clear();
                    }
                }
            }, unit.toMillis(time), unit.toMillis(time));
        } else {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    int i = JOptionPane.showConfirmDialog(null, msg
                            , title + " (点击 否 立即终止定时提醒)"
                            , JOptionPane.ERROR_MESSAGE);
                    if (JOptionPane.NO_OPTION == i) {
                        clear();
                    }
                }
            }, unit.toMillis(time));
        }
    }
}
