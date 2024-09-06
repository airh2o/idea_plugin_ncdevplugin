/*
package com.air.nc5dev.decompiler;

import org.jd.core.v1.api.loader.Loader;
import org.jd.core.v1.api.loader.LoaderException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

*/
/**
 * </br>
 * </br>
 * </br>
 *
 * @author 唐粟 Email:209308343@qq.com
 * @date 2024/9/2 17:24
 * @project
 * @Version
 *//*

public class MyLoader implements Loader {
    InputStream is;
    byte[] bytes;

    public MyLoader(InputStream is) throws RuntimeException{
        this.is = is;

        if (is != null) {
            try (InputStream in = is;
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[1024];
                int read = in.read(buffer);

                while (read > 0) {
                    out.write(buffer, 0, read);
                    read = in.read(buffer);
                }

                bytes = out.toByteArray();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public boolean canLoad(String internalName) {
        return true;
    }

    @Override
    public byte[] load(String internalName) throws LoaderException {
        return bytes;
    }
}
*/
