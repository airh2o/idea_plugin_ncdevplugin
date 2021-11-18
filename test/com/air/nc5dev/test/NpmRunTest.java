package com.air.nc5dev.test;

import com.air.nc5dev.util.ExecUtil;

import java.io.*;
import java.nio.charset.Charset;

/**
 * </br>
 * </br>
 * </br>
 *
 * @author 唐粟 Email:209308343@qq.com
 * @date 2021/11/18 0018 16:33
 * @project
 * @Version
 */
public class NpmRunTest {
    public static void main(String[] args) throws Exception {
        System.out.println(ExecUtil.execAndWait("cmd /c "
                + " cd I:\\\\projects\\\\iuap\\\\guizhouxijiu\\\\XJNCC20210927\\\\hotwebs"
                + " & npm run build "
        ));
    }


}
