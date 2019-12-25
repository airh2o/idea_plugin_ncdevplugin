package com.air.nc5dev.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * IO tool
 */
public final class IoUtil {
    /**
     * 把一个 java的 Properties 输出成文件
     *
     * @param properties
     * @param file
     */
    public static final void wirtePropertis(Properties properties, File file) {
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            properties.store(outputStream, "update file");
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * 获得所有NC的Ant jar路径
     *
     * @param ncHome
     * @return
     */
    public static final ArrayList<File> serachAllNcAntJars(File ncHome) {
        ArrayList<File> all = new ArrayList<File>();

        File f = new File(ncHome, File.separatorChar + "ant");
        if (f.exists()) {
            all.addAll(getAllJarFiles(f));
        }

        return all;
    }

    /**
     * 获得所有NC的公共 Product_Common_Library jar路径
     *
     * @param ncHome
     * @return
     */
    public static final ArrayList<File> serachProduct_Common_LibraryJars(File ncHome) {
        ArrayList<File> all = new ArrayList<>();

        File f = new File(ncHome, "resources");
        if (f.exists()) {
            all.add(f);
        }

        f = new File(ncHome, "lib");
        if (f.exists()) {
            all.addAll(getAllJarFilesIncloudChild(f));
        }
        f = new File(ncHome, "external" + File.separatorChar + "classes");
        if (f.exists()) {
            all.add(f);
        }
        f = new File(ncHome, "external" + File.separatorChar + "lib");
        if (f.exists()) {
            all.addAll(getAllJarFilesIncloudChild(f));
        }

        return all;
    }

    /**
     * 获得所有NC的公共     Middleware_Library  jar路径
     *
     * @param ncHome
     * @return
     */
    public static final ArrayList<File> serachMiddleware_LibraryJars(File ncHome) {
        ArrayList<File> all = new ArrayList<>();

        File f = new File(ncHome, "middleware");
        if (f.exists()) {
            all.addAll(getAllJarFilesIncloudChild(f));
        }

        return all;
    }
    /**
     * 获得所有NC的公共     Framework_Library  jar路径
     *
     * @param ncHome
     * @return
     */
    public static final ArrayList<File> serachFramework_LibraryJars(File ncHome) {
        ArrayList<File> all = new ArrayList<>();

        File f = new File(ncHome, "framework");
        if (f.exists()) {
            all.addAll(getAllJarFilesIncloudChild(f));
        }

        return all;
    }

    /**
     * 获得所有NC的公共 jar路径
     *
     * @param ncHome
     * @return
     */
    public static final ArrayList<File> serachAllNcLibsJars(File ncHome) {
        ArrayList<File> all = new ArrayList<File>();

        File f = new File(ncHome, "\\lib");
        if (f.exists()) {
            all.addAll(getAllJarFiles(f));
        }
        f = new File(ncHome, "\\ejb");
        if (f.exists()) {
            all.addAll(getAllJarFiles(f));
        }
        f = new File(ncHome, "\\ejb");
        if (f.exists()) {
            all.addAll(getAllJarFiles(f));
        }
        f = new File(ncHome, "\\external\\classes");
        if (f.exists()) {
            all.add(f);
        }
        f = new File(ncHome, "\\external");
        if (f.exists()) {
            all.addAll(getAllJarFiles(f));
        }
        f = new File(ncHome, "\\driver");
        if (f.exists()) {
            all.addAll(getAllJarFiles(f));
        }
        f = new File(ncHome, "\\middleware");
        if (f.exists()) {
            all.addAll(getAllJarFiles(f));
        }

        return all;
    }
    /**
     * 获取NC的 所有 模块的 所有    NC_Module_Public_Library
     *
     * @param ncHome
     * @return
     */
    public static final ArrayList<File> serachNC_Module_Public_Library(File ncHome) {
        ArrayList<File> all = serachAllNcPublicClass(ncHome);
        all.addAll(serachAllNcPublicJars(ncHome));
        return all;
    }
    /**
     * 获取NC的 所有 模块的 所有    Module_Client_Library
     *
     * @param ncHome
     * @return
     */
    public static final ArrayList<File> serachModule_Client_Library(File ncHome) {
        ArrayList<File> all = serachAllNcClientClass(ncHome);
        all.addAll(serachAllNcClientJars(ncHome));
        return all;
    }
    /**
     * 获取NC的 所有 模块的 所有    Module_Private_Library
     *
     * @param ncHome
     * @return
     */
    public static final ArrayList<File> serachModule_Private_Library(File ncHome) {
        ArrayList<File> all = serachAllNcPrivateClass(ncHome);
        all.addAll(serachAllNcPrivateJars(ncHome));
        return all;
    }
    /**
     * 获取NC的 所有 模块的 所有    Module_Lang_Library
     *
     * @param ncHome
     * @return
     */
    public static final ArrayList<File> serachModule_Lang_Library(File ncHome) {
        ArrayList<File> all = new ArrayList<>();

        File f = new File(ncHome, "langlib");
        if (f.exists()) {
            all.addAll(getAllJarFilesIncloudChild(f));
        }

        return all;
    }
    /**
     * 获取NC的 所有 模块的 所有    Generated_EJB
     *
     * @param ncHome
     * @return
     */
    public static final ArrayList<File> serachGenerated_EJB(File ncHome) {
        ArrayList<File> all = new ArrayList<>();

        File f = new File(ncHome, "ejb");
        if (f.exists()) {
            all.addAll(getAllJarFilesIncloudChild(f));
        }

        return all;
    }


    /**
     * 获取NC的 所有 模块的 所有 public class文件夹
     *
     * @param ncHome
     * @return
     */
    public static final ArrayList<File> serachAllNcPublicClass(File ncHome) {
        return serachAllNcClass(new File(ncHome, "modules")
                , "classes", false);
    }

    /**
     * 获取NC的 所有 模块的 所有 client class文件夹
     *
     * @param ncHome
     * @return
     */
    public static final ArrayList<File> serachAllNcClientClass(File ncHome) {
        return serachAllNcClass(new File(ncHome, "modules")
                , "client" + File.separatorChar + "classes", false);
    }

    /**
     * 获取NC的 所有 模块的 所有 private class文件夹
     *
     * @param ncHome
     * @return
     */
    public static final ArrayList<File> serachAllNcPrivateClass(File ncHome) {
        return serachAllNcClass(new File(ncHome, "modules")
                , "META-INF" + File.separatorChar + "classes", false);
    }

    /**
     * 获取NC的 所有 模块的 所有 public jar文件
     *
     * @param ncHome
     * @return
     */
    public static final ArrayList<File> serachAllNcPublicJars(File ncHome) {
        return serachAllNcClass(new File(ncHome, "modules")
                , "lib", true);
    }

    /**
     * 获取NC的 所有 模块的 所有 client jar文件
     *
     * @param ncHome
     * @return
     */
    public static final ArrayList<File> serachAllNcClientJars(File ncHome) {
        return serachAllNcClass(new File(ncHome, "modules")
                , "client" + File.separatorChar + "lib", true);
    }

    /**
     * 获取NC的 所有 模块的 所有 private jar文件
     *
     * @param ncHome
     * @return
     */
    public static final ArrayList<File> serachAllNcPrivateJars(File ncHome) {
        return serachAllNcClass(new File(ncHome, "modules")
                , "META-INF" + File.separatorChar + "lib", true);
    }

    /**
     * 获得所有NC的模块 内的 指定的文件夹里的依赖路径
     *
     * @param ncModules NC 产品模块文件夹根路径
     * @param dirName   依赖路径文件夹名字，比如 META-INF + File.separatorChar + lib
     * @param isJarDir  是否是jar文件文件夹，true会搜索所有jar，不然认为是class文件 直接返回这个文件夹
     * @return
     */
    public static final ArrayList<File> serachAllNcClass(File ncModules, String dirName, boolean isJarDir) {
        ArrayList<File> all = new ArrayList<>();
        File[] listFiles = ncModules.listFiles();
        Arrays.stream(listFiles).forEach(dir -> {
            if (dir.isDirectory()) {
                File f = new File(dir, File.separatorChar + dirName);
                if (f.exists()) {
                    if (isJarDir) {
                        all.addAll(getAllJarFiles(f));
                    } else {
                        all.add(f);
                    }
                }
            }
        });
        return all;
    }

    /**
     * 所有一个 文件夹类所有的jar文件， 会搜索子目录！
     *
     * @param dir
     * @return
     */
    private static Collection<? extends File> getAllJarFilesIncloudChild(File dir) {
        File[] fs = dir.listFiles();

        ArrayList<File> ar = new ArrayList<File>(null == fs ? 0 : fs.length);

        try {
            File f;
            for (int i = 0; i < fs.length; i++) {
                f = fs[i];
                if (f.isFile() && f.getName().toLowerCase().endsWith(".jar")) {
                    ar.add(fs[i]);
                } else if (f.isDirectory()) {
                    ar.addAll(getAllJarFilesIncloudChild(f));
                }
            }
        } catch (Exception e) {
        }

        return ar;
    }

    /**
     * 所有一个 文件夹类所有的jar文件，不会搜索子目录！
     *
     * @param dir
     * @return
     */
    public static List<File> getAllJarFiles(File dir) {
        File[] fs = dir.listFiles();

        ArrayList<File> ar = new ArrayList<File>(null == fs ? 0 : fs.length);

        try {
            File f;
            for (int i = 0; i < fs.length; i++) {
                f = fs[i];
                if (f.isFile() && f.getName().toLowerCase().endsWith(".jar")) {
                    ar.add(fs[i]);
                } else if (f.isDirectory()) {
                    ar.addAll(getAllJarFiles(f));
                }
            }
        } catch (Exception e) {
        }

        return ar;
    }

    private IoUtil() {
    }
}
