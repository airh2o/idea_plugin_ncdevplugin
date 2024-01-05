package com.air.nc5dev.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.swing.JOptionPane;
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
    private static TaskVO taskVO = null;

    public static synchronized void clear() {
        if (timer == null) {
            return;
        }
        timer.cancel();
        timer = null;
        taskVO = null;
    }

    public static synchronized void add(boolean loop, long time, TimeUnit unit, String title, String msg) {
        taskVO = TaskVO.builder()
                .loop(loop)
                .time(time)
                .unit(unit)
                .title(title)
                .msg(msg)
                .build();

        add(taskVO);
    }

    public static synchronized void add(final TaskVO vo) {
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        taskVO = vo;

        if (vo.loop) {
            timer.scheduleAtFixedRate(new TimerTask() {
                                          @Override
                                          public void run() {
                                              //  Messages.showInfoMessage(msg, title);
                                              int i = JOptionPane.showConfirmDialog(null
                                                      , vo.msg
                                                      , vo.title + " (点击 否 立即终止定时提醒)"
                                                      , JOptionPane.ERROR_MESSAGE);
                                              if (JOptionPane.NO_OPTION == i) {
                                                  clear();
                                              } else {
                                                  clear();
                                                  add(vo);
                                              }
                                          }
                                      }
                    , vo.unit.toMillis(vo.time)
                    , vo.unit.toMillis(vo.time));
        } else {
            timer.schedule(new TimerTask() {
                               @Override
                               public void run() {
                                   int i = JOptionPane.showConfirmDialog(null
                                           , vo.msg
                                           , vo.title + " (点击 否 立即终止定时提醒)"
                                           , JOptionPane.ERROR_MESSAGE);
                                   if (JOptionPane.NO_OPTION == i) {
                                       clear();
                                   } else {
                                       clear();
                                       add(vo);
                                   }
                               }
                           }
                    , vo.unit.toMillis(vo.time));
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TaskVO {
        boolean loop;
        long time;
        TimeUnit unit;
        String title;
        String msg;
    }
}
