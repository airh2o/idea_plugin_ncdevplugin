package com.air.nc5dev.ui;

import com.air.nc5dev.util.RemindUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.concurrent.TimeUnit;

/***
 *   设置定时提醒 弹框UI        </br>
 *           </br>
 *           </br>
 *           </br>
 * @author air Email: 209308343@qq.com
 * @date 2019/12/25 0025 15:29
 * @Param
 * @return
 */
public class SetingRemindDialog
        extends DialogWrapper {
    //全局标识， 是否有导出任务未处理完
    private static boolean isRuning = false;
    private JPanel contentPane;
    private AnActionEvent event;
    private JTextField textField_saveName;
    private JTextField textField_savePath;
    private SetingRemindPanel setingRemindPanel = new SetingRemindPanel();


    public SetingRemindDialog(AnActionEvent event) {
        super(event.getProject());
        this.event = event;
        init();
        setTitle("设置定时提醒");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return setingRemindPanel;
    }

    /***
     *   点击了确认    </br>
     *           </br>
     *           </br>
     * @author air Email: 209308343@qq.com
     * @date 2019/12/25 0025 16:17
     * @Param []
     * @return void
     */
    public void onOK() {

        String title = setingRemindPanel.textArea_title.getText();
        String msg = setingRemindPanel.textArea_msg.getText();
        TimeUnit[] tuns = new TimeUnit[]{
                TimeUnit.SECONDS
                , TimeUnit.MINUTES
                , TimeUnit.HOURS
                , TimeUnit.DAYS
        };
        TimeUnit timeUnit = tuns[setingRemindPanel.comboBox_timeUnit.getSelectedIndex()];
        long time = Long.parseLong(setingRemindPanel.textField_time.getText());

        RemindUtil.add(time, timeUnit, title, msg);

        dispose();
    }

    @Nullable
    @Override
    protected String getDimensionServiceKey() {
        return this.getClass().getName();
    }

    private void onCancel() {
        dispose();
    }
}
