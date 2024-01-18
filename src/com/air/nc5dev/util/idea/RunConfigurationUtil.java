package com.air.nc5dev.util.idea;

import com.intellij.execution.RunManager;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.impl.RunManagerImpl;
import com.intellij.execution.impl.RunnerAndConfigurationSettingsImpl;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

/**
 * Idea - 运行配置的工具类</br>
 * </br>
 * </br>
 * </br>
 *
 * @author air Email: 209308343@qq.com
 * @version NC505, JDK1.5
 * @date 2019/12/25 0025 8:29
 * @project
 */
public class RunConfigurationUtil {

    /* *
     *  往项目里添加一个     Application的运行项目     </br>
     *           </br>
     *           </br>
     *           </br>
     * @author air Email: 209308343@qq.com
     * @date 2019/12/25 0025 8:37
     * @Param [project] 添加到指定的项目
     * @Param [conf] 运行配置
     * @return void
     */
    public static void addRunJavaApplicationMenu(@Nullable Project project
            , @NotNull ApplicationConfiguration conf, boolean singleton, boolean share) {
        RunManagerImpl runManagerImpl = getRunManagerImpl(project);
        RunnerAndConfigurationSettingsImpl runnerAndConfigurationSettings = new RunnerAndConfigurationSettingsImpl
                (runManagerImpl, conf);
        try {
            runnerAndConfigurationSettings.setSingleton(singleton);
        } catch (Throwable e) {
        }
        try {
            Method setShared = null;
            try {
                setShared = RunnerAndConfigurationSettingsImpl.class.getMethod("setShared", boolean.class);
            } catch (Throwable e) {
            }
            if (setShared == null) {
                try {
                    setShared = RunnerAndConfigurationSettingsImpl.class.getMethod("setShared", Boolean.class);
                } catch (Throwable e) {
                }
            }

            if (setShared != null) {
                setShared.invoke(runnerAndConfigurationSettings, share);
            }
        } catch (Throwable e) {
        }
        runManagerImpl.addConfiguration(runnerAndConfigurationSettings);
    }

    /* *
     *  获取运行管理器         </br>
     *           </br>
     *           </br>
     *           </br>
     * @author air Email: 209308343@qq.com
     * @date 2019/12/25 0025 8:43
     * @Param [project] 指定项目的
     * @return com.intellij.execution.impl.RunManagerImpl
     */
    public static RunManagerImpl getRunManagerImpl(@Nullable Project project) {
        RunManagerImpl runManager = (RunManagerImpl) RunManager.getInstance(null == project ? ProjectUtil
                .getDefaultProject() : project);
        return runManager;
    }


    private RunConfigurationUtil() {
        throw new RuntimeException("cannot instance Util Class!");
    }
}
