package com.air.nc5dev.codecheck;

import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.changes.CommitContext;
import com.intellij.openapi.vcs.checkin.CheckinHandler;

/**
 * <br>
 * <br>
 * <br>
 *
 * @author air Email:209308343@qq.com
 * @date 2022/8/12 0012 15:27
 * @project
 * @Version
 */
public class NCCheckinHandlerImpl extends CheckinHandler {
    CheckinProjectPanel checkinProjectPanel;
    CommitContext commitContext;

    public NCCheckinHandlerImpl(CheckinProjectPanel checkinProjectPanel, CommitContext commitContext) {
        this.checkinProjectPanel = checkinProjectPanel;
        this.commitContext = commitContext;
    }




}
