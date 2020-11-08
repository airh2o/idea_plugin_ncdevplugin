package com.air.nc5dev.acion;

import com.air.nc5dev.acion.base.AbstractIdeaAction;
import com.air.nc5dev.util.IoUtil;
import com.air.nc5dev.util.ProjectNCConfigUtil;
import com.air.nc5dev.util.idea.LogUtil;
import com.air.nc5dev.util.idea.ProjectUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class ShowLocalNCProcessAction extends AbstractIdeaAction {
    @Override
    protected void doHandler(AnActionEvent e) throws IOException {
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
                String key = "-Dnc.server.location=";
                int localIndex = line.indexOf(key);
                localIndex += key.length();
                int endIndex = line.indexOf(" ", localIndex + 1);
                endIndex = endIndex < 1 ? line.length() : endIndex;
                String home = line.substring(localIndex, endIndex);

                key = "-Dnc.http.port=";
                int i = line.indexOf(key);
                String port = "";
                try {
                    port = line.substring(i + key.length(), line.indexOf(" ", i));
                } catch (Exception e1) {
                    LogUtil.error(e1.toString(), e1);
                }

                list.add("服务端: pid: " + line.substring(0, line.indexOf(" ")) + "  | 路径: " + home
                        + "  | 端口: " + port
                );
            } else if (line.contains("nc.starter.test.JStarter")) {
                //Swing端
                String key = "-Dnc.jstart.port=";
                int i = line.indexOf(key);
                String port = "80";
                try {
                    port = line.substring(i + key.length(), line.indexOf(" ", i));
                } catch (Exception e1) {
                    LogUtil.error(e1.toString(), e1);
                }

                key = "-Dnc.jstart.server=";
                i = line.indexOf(key);
                String ip = "127.0.0.1";
                try {
                    ip = line.substring(i + key.length(), line.indexOf(" ", i));
                } catch (Exception e1) {
                    LogUtil.error(e1.toString(), e1);
                }

                list.add("Swing端: pid: " + line.substring(0, line.indexOf(" ")) + "  | 端口: " + port
                        + " | IP:" + ip
                );
            }
        });

        if (list.isEmpty()) {
            LogUtil.error("没有找到任何进程");
        } else {
            final StringBuilder stringBuilder = new StringBuilder(10000);
            list.forEach(pid -> {
                stringBuilder.append(pid).append("\n");
            });

            Messages.showInfoMessage(stringBuilder.toString(), "找到的本机相关NC进程列表:");
        }
    }
}
