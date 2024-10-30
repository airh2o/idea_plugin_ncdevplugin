package com.air.nc5dev.decompiler;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.air.nc5dev.ui.decompiler.NCDecompilerDialog;
import com.air.nc5dev.util.CollUtil;
import com.air.nc5dev.util.IoUtil;
import com.air.nc5dev.util.StringUtil;
import com.intellij.openapi.progress.ProgressIndicator;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.Desktop;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * </br>
 * </br>
 * </br>
 *
 * @author 唐粟 Email:209308343@qq.com
 * @date 2024/9/2 17:20
 * @project
 * @Version
 */
@Data
@Accessors(chain = true)
public class DecompilerUtil {
    private static final String JARSPLIT = "::jar::";
    String nchome;
    String out;
    boolean jarNameUseOut;
    boolean outlog;
    String ENCODING;
    File logf;
    JFrame root;
    JTextArea textArea;
    ProgressIndicator indicator;
    NCDecompilerDialog dialog;
    volatile boolean autoLogScr = false;
    volatile int threadNum = Runtime.getRuntime().availableProcessors() >> 2;
    volatile boolean doubleThread = false;

    public static void main(String[] args) {
        //   testColor();
        // System.setProperty("java.home", ".");
        DecompilerUtil d = new DecompilerUtil();
        d.root = new JFrame("批量反编译NC目录代码 v1, 感谢使用！有外包或者开发需求请联系");
        double h = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
        double w = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
        d.root.setSize(800, 600);
        d.root.setBounds((int) ((w - d.root.getWidth()) / 2)
                , (int) ((h - d.root.getHeight()) / 2)
                , d.root.getWidth()
                , d.root.getHeight());
        d.textArea = new JTextArea();
        d.root.add(new JScrollPane(d.textArea));
        d.root.setVisible(true);
        d.root.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        d.run();
    }

    public boolean run() {
        try {
            log(getColoredString(32, 40, "感谢使用！有外包或者开发需求请联系")
                    + "\t" +
                    getColoredString(31, 47, "QQ 209308343")
                    + "\t" +
                    getColoredString(31, 47, "微信 yongyourj") +
                    "\n全职独立远程开发顾问，U8Cloud NC系列(NC57 NC65等) NCCloud BIP 项目主导案例130家以上。" +
                    "\n 用友开发全部高级以上认证。参与过多个超过千万的重大项目" +
                    "。 \nJava web springcloud 微服务 前后端 H5app 钉钉 微信 企业微信 飞书 泛微 致远 go pytone 均可开发。" +
                    "\n开源项目地址: https://gitee.com/yhlx/idea_plugin_nc5devplugin" +
                    "\n\n");

            decompiler();
        } catch (Throwable e) {
            logerr(e.toString(), e);
        } finally {
            try {
                if (logf != null
                        && JOptionPane.showConfirmDialog(null, "是否打开日志文件?"
                        , "提示", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    Desktop.getDesktop().open(logf);
                }
            } catch (Throwable e) {
            }
        }

        return true;
    }


    public void decompiler() throws Throwable {
        //下载jad
        File cfr = new File(System.getProperty("user.home"), "cfr-0.152.jar");
        log("正在下载cfr..." + cfr.getPath() + "  ，如果没有外网或者下载失败，可把文件放入这个位置即可！");

        if (!cfr.isFile()) {
            HttpUtil.downloadFile("https://www.benf.org/other/cfr/cfr-0.152.jar", cfr);
        }
        log("下载成功....");

        AtomicInteger threadFlag = new AtomicInteger(0);
        if (threadNum < 1) {
            threadNum = Runtime.getRuntime().availableProcessors();
        }

        if (threadNum > 1) {
            ArrayList<Integer> indexs = new ArrayList<>();
            for (int i = 0; i < dialog.getPathsTableModel().getRowCount(); i++) {
                indexs.add(i);
            }

            List<List<Integer>> indexListList = CollUtil.split(indexs, threadNum);
            for (List<Integer> ids : indexListList) {
                if (indicator.isCanceled()) {
                    return;
                }

                new Thread(() -> {
                    for (Integer idx : ids) {
                        if (indicator.isCanceled()) {
                            return;
                        }

                        try {
                            processRow2(idx, cfr);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }, DecompilerUtil.class.getSimpleName() + "-DIR-" + threadFlag.incrementAndGet()).start();
            }
        } else {
            for (int i = 0; i < dialog.getPathsTableModel().getRowCount(); i++) {
                if (indicator.isCanceled()) {
                    return;
                }

                processRow2(i, cfr);
            }
        }
    }

    private void processRow2(int i, File cfr) throws InterruptedException {
        setRowResult(i, "处理中");
        if (!("true".equalsIgnoreCase(StringUtil.getSafeString(dialog.getPathsTableModel().getValueAt(i, 0)))
                || "y".equalsIgnoreCase(StringUtil.getSafeString(dialog.getPathsTableModel().getValueAt(i, 0))))) {
            setRowResult(i, "跳过");
            return;
        }

        File ncHomeDir = new File(nchome);
        File javaExe = new File(ncHomeDir, "ufjdk" + File.separatorChar + "bin" + File.separatorChar + "java.exe");
        if (!javaExe.isFile()) {
            javaExe = new File(ncHomeDir, "ufjdk" + File.separatorChar + "bin" + File.separatorChar + "java");
        }

        File dir = new File((String) dialog.getPathsTableModel().getValueAt(i, 1));
        try {
            List<File> fs = IoUtil.getAllFiles(dir, true, ".class", ".jar");
            if (CollUtil.isEmpty(fs)) {
                return;
            }

            // log(getColoredString(31, 40, "开始处理文件夹: ** (共有文件" + fs.size() + "个)" + dir));
            File outDir = new File(out
                    + File.separatorChar + StrUtil.removePrefix(dir.getPath(), ncHomeDir.getPath())
            );
            int x = 1;
            for (File file : fs) {
                if (indicator.isCanceled()) {
                    return;
                }

                log(getColoredString(31, 40, "开始处理文件夹: 第" + (x) + "个 剩余:" + (fs.size() - x) + "个(共有文件" + fs.size() + "个)"));

                setRowResult(i, "处理中:" + x + "/" + fs.size());

                if (file.getName().toLowerCase().endsWith(".jar")) {
                    //反编译！   java -jar D:\develop\java逆向\cfr_0_122.jar "%%i" --caseinsensitivefs true  --outputdir "%%~di%%~pi%%~ni"
                    excuteShell(String.format("%s -jar \"%s\" \"%s\" --caseinsensitivefs true  --outputdir \"%s\""
                            , javaExe.isFile() ? javaExe.getPath() : "java"
                            , cfr.getPath()
                            , file.getPath()
                            , outDir.getPath()));
                } else {
                    //class java -jar D:\develop\java逆向\cfr_0_122.jar %1 --caseinsensitivefs true  --outputdir "%~d1%~p1%~n1"
                    excuteShell(String.format("%s -jar \"%s\" \"%s\" --caseinsensitivefs true  --outputdir \"%s\""
                            , javaExe.isFile() ? javaExe.getPath() : "java"
                            , cfr.getPath()
                            , file.getPath()
                            , outDir.getPath()));
                }
                ++x;
            }
        } finally {
            setRowResult(i, "完成");
        }
    }

    private void excuteShell(String shell) {
        try {
            String result = RuntimeUtil.execForStr(shell);
            log(result, false);
        } catch (Throwable e) {
            logerr(shell, e);
        }
    }

    public void decompiler1() throws Throwable {
        log("\n开始处理home: " + nchome + " 输出到: " + out);
        AtomicInteger threadFlag = new AtomicInteger(0);
        if (threadNum < 1) {
            threadNum = Runtime.getRuntime().availableProcessors();
        }
        //idea 不允许执行线程池！
 /*       ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(threadNum
                , threadNum
                , 60L
                , TimeUnit.SECONDS
                , new ArrayBlockingQueue<Runnable>(threadNum << 2)
                , r -> new Thread(DecompilerUtil.class.getSimpleName() + "-" + threadFlag.incrementAndGet())
        );*/

        if (doubleThread) {
            ArrayList<Integer> indexs = new ArrayList<>();
            for (int i = 0; i < dialog.getPathsTableModel().getRowCount(); i++) {
                indexs.add(i);
            }

            List<List<Integer>> indexListList = CollUtil.split(indexs, threadNum);
            for (List<Integer> ids : indexListList) {
                if (indicator.isCanceled()) {
                    return;
                }

                new Thread(() -> {
                    for (Integer idx : ids) {
                        if (indicator.isCanceled()) {
                            return;
                        }

                        try {
                            processRow(idx);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }, DecompilerUtil.class.getSimpleName() + "-DIR").start();
            }
        } else {
            for (int i = 0; i < dialog.getPathsTableModel().getRowCount(); i++) {
                if (indicator.isCanceled()) {
                    return;
                }

                processRow(i);
            }
        }
    }

    private void processRow(int i) throws InterruptedException {
        setRowResult(i, "处理中");
        if (!("true".equalsIgnoreCase(StringUtil.getSafeString(dialog.getPathsTableModel().getValueAt(i, 0)))
                || "y".equalsIgnoreCase(StringUtil.getSafeString(dialog.getPathsTableModel().getValueAt(i, 0))))) {
            setRowResult(i, "跳过");
            return;
        }

        File dir = new File((String) dialog.getPathsTableModel().getValueAt(i, 1));
        try {
            List<File> fs = IoUtil.getAllFiles(dir, true, ".class", ".jar");
            if (CollUtil.isEmpty(fs)) {
                return;
            }

            log(getColoredString(31, 40, "开始处理文件夹: ** (共有文件" + fs.size() + "个)" + dir));

            List<List<File>> fileListList = CollUtil.split(fs, threadNum);
            CountDownLatch countDownLatch = new CountDownLatch(fileListList.size());
            SwingUtilities.invokeLater(() -> getDialog().getLabelInfo().setText("正在启动" + fileListList.size() + "个线程执行任务: "));
            AtomicInteger startI = new AtomicInteger();
            for (final List<File> files : fileListList) {
                new Thread(() -> {
                    try {
                        SwingUtilities.invokeLater(() -> getDialog().getLabelInfo().setText(getDialog().getLabelInfo().getText()
                                + " ,线程运行中:" + startI.incrementAndGet()));
                        for (File f : files) {
                            decompilerFile(f);
                        }
                    } finally {
                        countDownLatch.countDown();
                    }
                }, DecompilerUtil.class.getSimpleName() + "-FILE").start();
            }
            countDownLatch.await();
        } finally {
            setRowResult(i, "完成");
        }
    }

    public void setRowResult(final int row, String str) {
        SwingUtilities.invokeLater(() -> dialog.getPathsTableModel().setValueAt(str, row, 2));
    }

    @Deprecated
    public void decompiler(File dir) throws Exception {
        if (dir == null || !dir.exists()) {
            return;
        }

        File[] fs = dir.listFiles();
        for (File f : fs) {
            if (indicator.isCanceled()) {
                return;
            }

            if (f.isDirectory()) {
                decompiler(f);
                continue;
            }

            decompilerFile(f);
        }
    }

    public void decompilerFile(File jarOrClass) {
        indicator.setText("处理文件: " + jarOrClass.getPath());
        try {
            if (jarOrClass.getName().toLowerCase().endsWith(".class")) {
                decompilerClass(jarOrClass);
            } else if (jarOrClass.getName().toLowerCase().endsWith(".jar")) {
                decompilerJar(jarOrClass);
            }
        } catch (Throwable e) {
            logerr("文件反编译失败: " + jarOrClass.getPath() + " err=" + e.toString(), e);
        }
    }

    public void decompilerJar(File f) throws Exception {
        JarFile jarFile = new JarFile(f);
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            if (indicator.isCanceled()) {
                return;
            }

            JarEntry entry = entries.nextElement();

            if (!entry.isDirectory()) {
                if (entry.getName().toLowerCase().endsWith(".class")) {
                    decompiler(jarFile.getInputStream(entry), f.getPath() + JARSPLIT + entry.getName());
                }
            }
        }
    }

    public void decompilerClass(File f) throws Exception {
        decompiler(new FileInputStream(f), f.getPath());
    }

    public void decompiler(InputStream in, String filePath) throws Exception {
      /*  log("\n**反编译: " + filePath + "  >>>  ");

        try {
            String classFullName = filePath;
            if (filePath.contains(JARSPLIT)) {
                String[] fs = filePath.split(JARSPLIT);
                classFullName = StrUtil.removeSuffixIgnoreCase(fs[1], ".class");
            } else {
                classFullName = StrUtil.removeSuffixIgnoreCase(
                        StrUtil.removePrefixIgnoreCase(filePath, nchome)
                        , ".class");

                classFullName = classFullName.substring(classFullName.indexOf("classes") + 8);
            }

            classFullName = StrUtil.replace(classFullName, File.separator, "/");
            classFullName = StrUtil.replace(classFullName, "\\", "/");

            ClassFileToJavaSourceDecompiler decompiler
                    = new ClassFileToJavaSourceDecompiler();
            MyPrinter printer = new MyPrinter();
            MyLoader loader = new MyLoader(in);
            decompiler.decompile(loader
                    , printer
                    , classFullName);

            String source = printer.toString();

            if (StrUtil.isBlank(source)) {
                return;
            }

            //输出文件
            String className = filePath;
            if (filePath.contains(JARSPLIT)) {
                String[] fs = filePath.split(JARSPLIT);
                className = StrUtil.replace(fs[0], nchome, out);

                if (!jarNameUseOut) {
                    className = new File(className).getParentFile().getPath();
                }

                className = className + File.separatorChar + StrUtil.removeSuffixIgnoreCase(fs[1], ".class") + ".java";
            } else {
                className = StrUtil.replace(filePath, nchome, out);
                className = StrUtil.removeSuffixIgnoreCase(className, ".class") + ".java";
            }
            File cls = new File(className);//new File(base, System.currentTimeMillis() + new Random().nextInt(1000) + ".java");

            FileUtil.writeString(source, cls, ENCODING);

            log("输出: " + cls.getPath());
        } catch (Throwable e) {
            logerr(getColoredString(31, 40, "!!反编译失败: " + filePath + " 错误:" + e.toString() + "  in: "), e);
        }*/
    }

    public static Set<File> loadPaths(String nchome) {
        Set<File> paths = new LinkedHashSet<>();
        if (!new File(nchome).isDirectory()) {
            return paths;
        }

        paths.add(new File(nchome, "external"));
        paths.add(new File(nchome, "external" + File.separatorChar + "classes"));

        paths.add(new File(nchome, "lib"));
        paths.add(new File(nchome, "framework"));
        paths.add(new File(nchome, "middleware"));

        File[] fs = null;
        try {
            File hotwebs = new File(nchome, "hotwebs");
            fs = hotwebs.listFiles();
            for (File f : fs) {
                if (!f.isDirectory()) {
                    continue;
                }

                File a = new File(f, "WEB-INF" + File.separatorChar + "lib");
                if (a.isDirectory()) {
                    paths.add(a);
                }
                a = new File(f, "WEB-INF" + File.separatorChar + "classes");
                if (a.isDirectory()) {
                    paths.add(a);
                }
            }
        } catch (Throwable e) {
        }

        try {
            File modules = new File(nchome, "modules");
            fs = modules.listFiles();
            for (File f : fs) {
                if (!f.isDirectory()) {
                    continue;
                }

                paths.add(new File(f, "lib"));
                paths.add(new File(f, "classes"));

                paths.add(new File(f, "client" + File.separatorChar + "lib"));
                paths.add(new File(f, "client" + File.separatorChar + "classes"));

                paths.add(new File(f, "META-INF" + File.separatorChar + "lib"));
                paths.add(new File(f, "META-INF" + File.separatorChar + "classes"));
            }
        } catch (Throwable e) {
        }
        return paths;
    }

    private void logerr(String s, Throwable e) {
        indicator.setText(s);
        append(s);
        append("\n" + ExceptionUtil.stacktraceToString(e));
        if (outlog) {
            FileUtil.appendString(s + ExceptionUtil.stacktraceToString(e), logf, ENCODING);
        }
    }

    private void log(String s) {
        log(s, true);
    }

    private void log(String s, boolean toIndicator) {
        if (toIndicator) {
            indicator.setText(s);
        }
        append(s);
        if (outlog) {
            FileUtil.appendString(s, logf, ENCODING);
        }
    }

    private void append(String s) {
        SwingUtilities.invokeLater(() -> {
            if (textArea.getLineCount() > 4000) {
                textArea.setText(null);
            }

            textArea.append("\n" + s);
            if (autoLogScr) {
                textArea.setCaretPosition(textArea.getDocument().getLength());
            }
        });
    }

    public static String getColoredString(int color, int fontType, String content) {
        // return String.format("\033[%d;%dm%s\033[0m", color, fontType, content);
        return content;
    }

    public static void testColor() {
        for (int i = 0; i < 7; i++) {
            System.out.println(getColoredString(31 + i, 4, "颜色控制 -> " + colorMap.get(31 + i)));
        }
        for (int i = 0; i < 8; i++) {
            System.out.println(getColoredString(40 + i, 3, "背景控制 -> " + colorMap.get(40 + i)));
        }
        System.out.println(String.format("\033[%d;%d;%dm%s\033[0m", 1, 97, 40, "文 字 背 景 "));
    }

    private static final HashMap<Integer, String> colorMap = new HashMap<>() {
        {
            put(31, "红色字体");
            put(32, "绿色字体");
            put(33, "黄色字体");
            put(34, "蓝色字体");
            put(35, "紫色字体");
            put(36, "青色字体");
            put(37, "灰色字体");
            put(40, "黑色背景");
            put(41, "红色背景");
            put(42, "绿色背景");
            put(43, "黄色背景");
            put(44, "蓝色背景");
            put(45, "紫色背景");
            put(46, "青色背景");
            put(47, "灰色背景");
        }
    };

}
