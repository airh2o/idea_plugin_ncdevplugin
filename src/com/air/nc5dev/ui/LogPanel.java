package com.air.nc5dev.ui;

import com.air.nc5dev.acion.LogMoveDownAction;
import com.air.nc5dev.service.ui.IMeassgeConsole;
import com.air.nc5dev.service.ui.impl.MeassgeConsoleImpl;
import com.air.nc5dev.util.Actions;
import com.air.nc5dev.util.idea.ProjectUtil;
import com.air.nc5dev.util.subscribe.PubSubUtil;
import com.air.nc5dev.util.subscribe.impl.LogWinAutoTail;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.tools.SimpleActionGroup;

import javax.swing.*;

public class LogPanel extends SimpleToolWindowPanel {
  public static final String ID = "ToolWindowFactory";
  private final ToolWindow toolWindow;
  private final Project project;

  private ActionToolbar mainToolbar;

  public LogPanel(ToolWindow toolWindow, Project project) {
    super(false, true);
    this.toolWindow = toolWindow;
    this.project = project;

    addLogActions();
    addToolbar();
    addConsole();

  }

  private void addToolbar() {
    ActionGroup actionGroup = createActionGroup();
    mainToolbar = ActionManager.getInstance().createActionToolbar(ID, actionGroup, false);
    mainToolbar.setTargetComponent(this);
    Box toolBarBox = Box.createHorizontalBox();
    toolBarBox.add(mainToolbar.getComponent());

    super.setToolbar(toolBarBox);
    mainToolbar.getComponent().setVisible(true);
  }

  private static ActionGroup createActionGroup() {
    Actions actions = Actions.getInstance();
    SimpleActionGroup actionGroup = new SimpleActionGroup();
    actionGroup.add(actions.cleanConsole());
    actionGroup.add(actions.getTopLineAction());
    LogMoveDownAction tailLineAction = actions.getTailLineAction();
    actionGroup.add(tailLineAction);
    actionGroup.add(actions.getShowWhenErrorAction());

    PubSubUtil.subscribe(MeassgeConsoleImpl.KEY, new LogWinAutoTail(tailLineAction));

    return actionGroup;
  }

  private void addLogActions() {
    DefaultActionGroup group = new DefaultActionGroup();
    ((ToolWindowEx) toolWindow).setAdditionalGearActions(group);
  }

  private void addConsole() {
    ConsoleView consoleView = ProjectUtil.getService(project, IMeassgeConsole.class).getConsoleView();
    super.setContent(consoleView.getComponent());
  }
}
