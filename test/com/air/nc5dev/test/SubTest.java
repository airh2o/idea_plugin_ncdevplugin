package test.com.air.nc5dev.test;

import com.air.nc5dev.util.subscribe.PubSubUtil;
import com.air.nc5dev.util.subscribe.itf.ISubscriber;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

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
        t2();

    }

    private static void t2() throws Exception{
        File classFile = new File("F:\\temp\\temps\\1\\c1$cc1.class");
        File sourcePackge = new File("F:\\temp\\temps\\1");
        String classFileName = classFile.getName().substring(0, classFile.getName().lastIndexOf('.'));
        if(classFileName.indexOf('$') > 0){
            classFileName = classFileName.substring(0, classFileName.indexOf('$'));
        }
        classFileName += ".java";
        if(new File(sourcePackge, classFileName).exists()){
            System.out.println(classFileName);
        }else{
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
