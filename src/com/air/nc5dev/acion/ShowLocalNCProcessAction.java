package com.air.nc5dev.acion;

import com.air.nc5dev.util.ExceptionUtil;
import com.air.nc5dev.util.IoUtil;
import com.air.nc5dev.util.ProjectNCConfigUtil;
import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.util.idea.ProjectUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

public class ShowLocalNCProcessAction extends AnAction {

    @Override
    public void actionPerformed(final AnActionEvent e) {
        try {
            final String exe = "bin" + File.separatorChar + "jps.exe";
            File javaBin = IoUtil.getJavaHomePathFile(ProjectNCConfigUtil.getNCHomePath()
                    , exe);
            if (null == javaBin) {
                ProjectUtil.warnNotification("路径不存在: " + javaBin.getPath(), e.getProject());
                return;
            }

            Process exec = Runtime.getRuntime().exec(new String[]{javaBin.getPath(), "-mlvV"});
            Scanner scanner = new Scanner(exec.getInputStream());
            ArrayList<String> outs = new ArrayList<>(100);
            while (scanner.hasNextLine()) {
                outs.add(scanner.nextLine());
            }

            exec.getInputStream().close();
            exec.destroy();
            final ArrayList<String> list = new ArrayList<>(100);
            outs.forEach(line -> {
                if (line.contains("ufmiddle.start.tomcat.StartDirectServer")) {
                    //服务端 -Dnc.server.location
                    final String key = "-Dnc.server.location=";
                    int localIndex = line.indexOf(key);
                    localIndex += key.length();
                    int endIndex = line.indexOf(" ", localIndex + 1);
                    endIndex = endIndex < 1 ? line.length() : endIndex;
                    list.add("服务 端: pid=     " + line.substring(0, line.indexOf(" ")) + "  | 路径= "
                            + line.substring(localIndex, endIndex)
                    );
                } else if (line.contains("nc.starter.test.JStarter")) {
                    //Swing端
                    final String key = "-Dnc.jstart.port";
                    int localIndex = line.indexOf(key);
                    localIndex += key.length();
                    int endIndex = line.indexOf(" ", localIndex + 1);
                    endIndex = endIndex < 1 ? line.length() : endIndex;
                    list.add("Swing端: pid=     " + line.substring(0, line.indexOf(" ")) + "  | 端口= "
                            + line.substring(localIndex, endIndex)
                    );
                }
            });

            if (list.isEmpty()) {
                ProjectUtil.infoNotification("没有找到任何进程", e.getProject());
            } else {
                final StringBuilder stringBuilder = new StringBuilder(10000);
                list.forEach(pid -> {
                    stringBuilder.append(pid).append("\n");
                });

                Messages.showInfoMessage(stringBuilder.toString(), "找到的本机相关NC进程列表:");
            }
        } catch (Exception iae) {
            ProjectUtil.errorNotification(ExceptionUtil.getExcptionDetall(iae), e.getProject());
        }
    }

    private boolean isWindows() {
        return StringUtil.get(System.getProperty("os.name")).toLowerCase().contains("windows");
    }
}
