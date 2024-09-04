package com.air.nc5dev.decompiler;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.air.nc5dev.ui.decompiler.NCDecompilerDialog;
import com.air.nc5dev.util.StringUtil;
import com.intellij.openapi.progress.ProgressIndicator;
import lombok.Data;
import lombok.experimental.Accessors;
import org.jd.core.v1.ClassFileToJavaSourceDecompiler;

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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Scanner;
import java.util.Set;
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
        log("\n开始处理home: " + nchome + " 输出到: " + out);

        for (int i = 0; i < dialog.getPathsTableModel().getRowCount(); i++) {
            if (indicator.isCanceled()) {
                return;
            }
            setRowResult(i, "处理中");

            if (!("true".equalsIgnoreCase(StringUtil.getSafeString(dialog.getPathsTableModel().getValueAt(i, 0)))
                    || "y".equalsIgnoreCase(StringUtil.getSafeString(dialog.getPathsTableModel().getValueAt(i, 0))))) {
                setRowResult(i, "跳过");
                continue;
            }

            File dir = new File((String) dialog.getPathsTableModel().getValueAt(i, 1));
            try {
                log(getColoredString(31, 40, "开始处理文件夹: ** " + dir));
                decompiler(dir);
            } finally {
                setRowResult(i, "完成");
            }
        }
    }

    public void setRowResult(final int row, String str) {
        SwingUtilities.invokeLater(() -> dialog.getPathsTableModel().setValueAt(str, row, 2));
    }

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

            indicator.setText("处理文件: " + f.getPath());
            if (f.getName().toLowerCase().endsWith(".class")) {
                decompilerClass(f);
            } else if (f.getName().toLowerCase().endsWith(".jar")) {
                decompilerJar(f);
            }
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
        log("\n**反编译: " + filePath + "  >>>  ");

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
        }
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
        append(s);
        append("\n" + ExceptionUtil.stacktraceToString(e));

        indicator.setText(s);

        if (outlog) {
            FileUtil.appendString(s + ExceptionUtil.stacktraceToString(e), logf, ENCODING);
        }
    }

    private void log(String s) {
        indicator.setText(s);

        append(s);

        if (outlog) {
            FileUtil.appendString(s, logf, ENCODING);
        }
    }

    private void append(String s) {
        SwingUtilities.invokeLater(() -> {
            textArea.append("\n" + s);
            textArea.setCaretPosition(textArea.getDocument().getLength());
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
