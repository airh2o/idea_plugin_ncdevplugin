package com.air.nc5dev.codecheck;

import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.changes.CommitContext;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory;
import org.jetbrains.annotations.NotNull;

/**
 * </br>
 * </br>
 * </br>
 *
 * @author air Email:209308343@qq.com
 * @date 2022/8/12 0012 15:27
 * @project
 * @Version
 */
public class NCCheckinHandlerFactoryImpl extends CheckinHandlerFactory {
    @NotNull
    @Override
    public CheckinHandler createHandler(@NotNull CheckinProjectPanel checkinProjectPanel,
                                        @NotNull CommitContext commitContext) {
        return new NCCheckinHandlerImpl(checkinProjectPanel, commitContext);
    }
}
