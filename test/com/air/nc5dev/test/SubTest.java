package test.com.air.nc5dev.test;

import com.air.nc5dev.util.IoUtil;
import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.util.subscribe.PubSubUtil;
import com.air.nc5dev.util.subscribe.itf.ISubscriber;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

/**
 * </br>
 * </br>
 * </br>
 * </br>
 *
 * @author air Email: 209308343@qq.com
 * @version NC505, JDK1.5+, V1.0
 * @date 2020/2/4 0004 15:31
 * @project
 */
public class SubTest {
    public static void main(String[] args) throws Exception {
        t06();
    }

    private static void t06() throws Exception {
        Properties properties = new Properties();
        properties.load(new FileInputStream("C:\\Users\\air\\IdeaProjects\\NC5DevToolIdea\\test\\com\\air\\nc5dev\\test\\1.prop"));
        String s = "nc.bs.uap.srm.wsdlutils.calbodydoc.CalbodyDoc_portType";
        String cn = s;
        int left = 0;
        while ((left = cn.lastIndexOf('.')) > 0) {
            cn = cn.substring(0, cn.lastIndexOf('.'));
            System.out.println(cn + " : " + properties.getProperty(cn));

        }
    }

    private static void t05() throws Exception {
        File f = new File("E:\\temp\\home20200204\\modules\\u8cwebapi\\lib\\public_u8cwebapi.jar");


        JarInputStream in = new JarInputStream(new FileInputStream(f));

        JarFile jarFile = new JarFile(f);
        System.out.println(jarFile.getEntry("nc/bs/u8cwebapi/utils/Assrt.class"));


        URLClassLoader classLoader = new URLClassLoader(new URL[]{
                f.toURL()
        });

        Class<?> aClass = classLoader.loadClass("nc.bs.u8cwebapi.utils.Assrt");
        System.out.println(aClass.getName());
    }

    private static void tjar() throws Exception {
        String p = "C:\\Users\\air\\IdeaProjects\\aaa\\patchers\\exportpatcher-2020-03-24-14-07\\aaa\\classes";
        File dir = new File(p);
        Manifest minf = null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintWriter printWriter = new PrintWriter(byteArrayOutputStream);
        printWriter.println("Manifest-Version" + ": " + "1.0");
        printWriter.println("Class-Path" + ": " + "");
        printWriter.println("Main-Class" + ": " + "");
        printWriter.println("Name" + ": " + dir.getName());
        printWriter.println("Specification-Title" + ": " + "");
        printWriter.println("Specification-Version" + ": " + "1.0");
        printWriter.println("Specification-Vendor" + ": " + "");
        printWriter.println("Implementation-Title" + ": " + "");
        printWriter.println("Implementation-Version" + ": " + "");
        printWriter.println("Implementation-Vendor" + ": " + "");
        printWriter.println("CreateDate" + ": " + DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .format(LocalDateTime.now()));
        printWriter.println("Created-By" + ": " + "");
        printWriter.println("Created-ByIde" + ": " + "IDEA-plugin-nc5devtoolidea");
        printWriter.println("  by air Email 209308343@qq.com,QQ 209308343");
        printWriter.println("IDEA-plugin-nc5devtoolidea-github" + ": " + "https://");
        printWriter.println(" gitee.com/yhlx/idea_plugin_nc5devplugin");
        printWriter.println("");
        printWriter.flush();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        minf = new Manifest(byteArrayInputStream);
        printWriter.close();


        File jarFile = new File(dir.getParentFile(), "1.jar");
        IoUtil.makeJar(dir, jarFile, minf, new String[]{".java"});
    }

    private static void t4() {
        String a = "nc.ui.arap.pf.DzTakeF1BillSourceDLG";

        a = a.substring(a.indexOf('.') + 1);
        a = a.substring(a.indexOf('.') + 1);
        System.out.println(a.substring(0, a.indexOf('.')));
    }

    private static void t2() throws Exception {
        File classFile = new File("F:\\temp\\temps\\1\\c1$cc1.class");
        File sourcePackge = new File("F:\\temp\\temps\\1");
        String classFileName = classFile.getName().substring(0, classFile.getName().lastIndexOf('.'));
        if (classFileName.indexOf('$') > 0) {
            classFileName = classFileName.substring(0, classFileName.indexOf('$'));
        }
        classFileName += ".java";
        if (new File(sourcePackge, classFileName).exists()) {
            System.out.println(classFileName);
        } else {
            System.out.println("没找到");
        }
    }

    private static void t1() throws Exception {
        final String key = "SubTest";

        final AtomicInteger threadNum = new AtomicInteger(0);
        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                PubSubUtil.subscribe(key,
                        new ISubscriber<Integer>() {
                            int i = threadNum.incrementAndGet();

                            @Override
                            public void accept(Integer msg) {
                                System.out.println(i + " 收到数字： " + msg.intValue() + "     Thread=" + Thread.currentThread().getName());
                                try {
                                    //Thread.sleep(100);
                                } catch (Exception e) {
                                }
                            }
                        });
            }).run();
        }

        // PubSubUtil.publish(key, 44);

        //  System.out.println("完成同步推送");

        for (int i = 0; i < 100; i++) {
            PubSubUtil.publishAsync(key, i);
        }

        System.out.println("完成异步推送");
    }
}
