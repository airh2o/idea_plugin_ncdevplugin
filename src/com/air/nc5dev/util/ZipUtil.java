package com.air.nc5dev.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.regex.Matcher;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
/***
  *    zip压缩工具类       </br>
  *       本类是一个不知名字的大佬写的    </br>
  *           </br>
  *           </br>
  * @author air Email: 209308343@qq.com
  * @date 2020/1/4 0004 13:03
  * @Param
  * @return
 */
public class ZipUtil {

    public ZipUtil() {
    }

    public static String toZip(String exportPath, String patchName) throws RuntimeException {
        String path = (new File(exportPath)).getPath();
        String basePath = (new File(exportPath)).getPath();
        String[] strings = path.split(Matcher.quoteReplacement(File.separator));
        String fileName = strings[strings.length - 1];
        path = path.replace(fileName, "");
        File file = new File(exportPath);
        String zipName = path + fileName + "_" + patchName + ".zip";

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(zipName);
            CheckedOutputStream cos = new CheckedOutputStream(fileOutputStream, new CRC32());
            ZipOutputStream out = new ZipOutputStream(cos);
            compress(file, out, basePath);
            out.close();
            return zipName;
        } catch (Exception var11) {
            throw new RuntimeException("zip 打包失败 : " + var11.getMessage());
        }
    }

    private static void compress(File file, ZipOutputStream out, String basePath) {
        if (file.isDirectory()) {
            compressDirectory(file, out, basePath);
        } else {
            compressFile(file, out, basePath);
        }

    }

    private static void compressDirectory(File dir, ZipOutputStream out, String basePath) {
        if (dir.exists()) {
            File[] files = dir.listFiles();

            for(int i = 0; i < files.length; ++i) {
                compress(files[i], out, basePath);
            }

        }
    }

    private static void compressFile(File file, ZipOutputStream out, String basePath) {
        if (file.exists()) {
            try {
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
                String filePath = file.getPath().replace(basePath + File.separator, "");
                ZipEntry entry = new ZipEntry(filePath);
                out.putNextEntry(entry);
                byte[] data = new byte[8192];

                int count;
                while((count = bis.read(data, 0, 8192)) != -1) {
                    out.write(data, 0, count);
                }

                bis.close();
            } catch (Exception var8) {
                throw new RuntimeException(var8);
            }
        }
    }
}
