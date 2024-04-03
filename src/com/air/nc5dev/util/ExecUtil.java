package com.air.nc5dev.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.system.SystemUtil;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.function.Consumer;

/**
 * <br>
 * <br>
 * <br>
 *
 * @author 唐粟 Email:209308343@qq.com
 * @date 2021/11/18 0018 16:59
 * @project
 * @Version
 */
public class ExecUtil {
    /**
     * 执行一个 系统命令，并且返回结果
     *
     * @param command
     * @return
     * @throws IOException
     */
    public static String execAndWait(String command) throws IOException {
        Process process = Runtime.getRuntime().exec(command);
        BufferedReader bufferedReader = new BufferedReader
                (new InputStreamReader(process.getInputStream(), Charset.forName("GBK")));
        PrintWriter out = new PrintWriter(new BufferedOutputStream(process.getOutputStream()));
        String line;
        StringBuilder sb = new StringBuilder(2000);
        while ((line = bufferedReader.readLine()) != null) {
            sb.append(line + "\n");
        }
        process.destroy();

        return sb.toString();
    }

    /**
     * 执行 npm 命令
     *
     * @param path    文件夹
     * @param npm     npm名称 比如 npm
     * @param runName 命令名字 比如 build
     * @return
     */
    public static String npmRun(String path, String npm, String runName) throws IOException {
        String c = "cmd /c "
                + " cd " + path
                + " & npm run build ";

        if (SystemUtil.getOsInfo().isLinux()) {

        } else if (SystemUtil.getOsInfo().isMac()) {

        } else if (SystemUtil.getOsInfo().isWindows()) {

        }

        return execAndWait(c);
    }

    /**
     * 执行 npm 命令
     *
     * @param path    文件夹
     * @param npm     npm名称   npm
     * @param runName 命令名字   build
     * @return
     */
    public static String npmBuild(String path) throws IOException {
        return npmRun(path, "npm", "build");
    }

    /**
     * 执行 npm 命令
     *
     * @param path    文件夹
     * @param npm     npm名称   npm
     * @param runName 命令名字   build
     * @return
     */
    public static String npmBuild(String path, Consumer<String> newLine, String command) throws IOException {
        return npmRun(path, command, newLine);
    }

    /**
     * 执行 npm 命令
     *
     * @param path    文件夹
     * @param npm     npm名称 比如 npm
     * @param runName 命令名字 比如 build
     * @return
     */
    public static String npmRun(String path, String command, Consumer<String> newLine) throws IOException {
        String c = "cmd /c "
                + "  " + StrUtil.sub(path, 0, StrUtil.indexOf(path, ':') + 1)
                + " & cd " + path
                + " & " + command;

        if (SystemUtil.getOsInfo().isLinux()) {

        } else if (SystemUtil.getOsInfo().isMac()) {

        } else if (SystemUtil.getOsInfo().isWindows()) {

        }

        return execAndWait(c, newLine);
    }

    /**
     * 执行一个 系统命令，并且返回结果
     *
     * @param command
     * @return
     * @throws IOException
     */
    public static String execAndWait(String command, Consumer<String> newLine) throws IOException {
        Process process = Runtime.getRuntime().exec(command);
        BufferedReader bufferedReader = new BufferedReader
                (new InputStreamReader(process.getInputStream(), Charset.forName("GBK")));
        PrintWriter out = new PrintWriter(new BufferedOutputStream(process.getOutputStream()));
        String line;
        StringBuilder sb = new StringBuilder(2000);
        while ((line = bufferedReader.readLine()) != null) {
            sb.append(line + "\n");
            newLine.accept(line);
        }
        process.destroy();

        return sb.toString();
    }
}
