package com.air.nc5dev.acion.base;

import com.air.nc5dev.util.ProjectNCConfigUtil;
import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.util.idea.LogUtil;
import com.air.nc5dev.util.idea.ProjectUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * 这是IDEA action顶级抽象类，请所有的action继承他        </br>
 * </br>
 * </br>
 * </br>
 *
 * @author air Email: 209308343@qq.com
 * @date 2020/3/18 0018 8:53
 * @Param
 * @return
 */
public abstract class AbstractIdeaAction extends AnAction {
    public AbstractIdeaAction() {
        super();
    }

    public AbstractIdeaAction(@Nullable String text, @Nullable String description, @Nullable Icon icon) {
        super(text, description, icon);
    }

    private static boolean isWindowsSystem = isIsWindowsSystem();

    @Override
    public void actionPerformed(AnActionEvent e) {
        //设置默认项目
        ProjectUtil.setProject(e.getProject());

        try {
            // 自动重新加载一次 插件配置文件
            ProjectNCConfigUtil.initConfigFile(e.getProject());

            doHandler(e);
        } catch (Exception e1) {
            LogUtil.error("emmmmm 出现异常,淡定不要惊慌,如果影响使用请QQ209308343通知我: ", e1);
        }
    }

    /**
     * 当前是否是windows系统         </br>
     * </br>
     * </br>
     * </br>
     *
     * @return boolean
     * @author air Email: 209308343@qq.com
     * @date 2020/3/18 0018 8:55
     * @Param []
     */
    protected boolean isWindows() {
        return isWindowsSystem;
    }

    /**
     * 当前是否是windows系统         </br>
     * </br>
     * </br>
     * </br>
     *
     * @return boolean
     * @author air Email: 209308343@qq.com
     * @date 2020/3/18 0018 8:55
     * @Param []
     */
    protected static boolean isIsWindowsSystem() {
        return StringUtil.get(System.getProperty("os.name")).toLowerCase().contains("windows");
    }

    /**
     * 事件处理方法      </br>
     * </br>
     * </br>
     * </br>
     *
     * @return void
     * @author air Email: 209308343@qq.com
     * @date 2020/3/18 0018 8:55
     * @Param [e]
     */
    protected abstract void doHandler(AnActionEvent e) throws Exception;
}
