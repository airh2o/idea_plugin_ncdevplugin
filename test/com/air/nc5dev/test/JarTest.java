package com.air.nc5dev.test;

import com.air.nc5dev.util.StringUtil;

import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 *
 * </br>
 * </br>
 * </br>
 *
 * @author 唐粟 Email:209308343@qq.com
 * @date 2022/2/16 0016 11:52
 * @project
 * @Version
 */
public class JarTest {
    public static void main(String[] args) throws Exception {
        JarFile jf = new JarFile("I:\\runtime\\NCC2105HOME_GUIZHOUXIJIU\\hotwebs\\nccloud\\WEB-INF\\lib\\uiali_nccloud.jar");
        Enumeration<JarEntry> es = jf.entries();
        if (es == null) {
            return;
        }

        while (es.hasMoreElements()) {
            JarEntry e = es.nextElement();
            String name = e.getName().toLowerCase();
            if (StringUtil.startsWith(name, "yyconfig/") && StringUtil.endsWith(name, ".xml")) {
                if (StringUtil.contains(name, "/action/")) {

                }else if (StringUtil.contains(name, "/authorize/")) {

                }
            }
        }
    }
}
