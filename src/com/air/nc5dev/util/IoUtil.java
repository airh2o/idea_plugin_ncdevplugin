package com.air.nc5dev.util;

import cn.hutool.core.io.FileUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.jetbrains.annotations.NotNull;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipOutputStream;

/**
 * IO tool
 */
public final class IoUtil extends cn.hutool.core.io.IoUtil {
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
     * 替换f的根路径base 为dir的
     *
     * @param f
     * @param dir
     * @return
     */
    public static final File replaceDir(File f, File base, File dir) {
        String p = f.getPath();
        String bp = base.getPath();
        String np = dir.getPath();

        return new File(StringUtil.replace(p, bp, np));
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
            all.addAll(getAllJarFiles(f, true));
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
            all.addAll(getAllJarFiles(f, true));
        }
        f = new File(ncHome, "external" + File.separatorChar + "classes");
        if (f.exists()) {
            all.add(f);
        }
        f = new File(ncHome, "external" + File.separatorChar + "lib");
        if (f.exists()) {
            all.addAll(getAllJarFiles(f, true));
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
            all.addAll(getAllJarFiles(f, true));
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
            all.addAll(getAllJarFiles(f, true));
        }

        return all;
    }

    /**
     * 获得所有NC的公共 jar路径
     *
     * @param ncHome
     * @return
     */
    @Deprecated
    public static final ArrayList<File> serachAllNcLibsJars(File ncHome) {
        ArrayList<File> all = new ArrayList<File>();

        File f = new File(ncHome, "\\lib");
        if (f.exists()) {
            all.addAll(getAllJarFiles(f, false));
        }
        f = new File(ncHome, "\\ejb");
        if (f.exists()) {
            all.addAll(getAllJarFiles(f, false));
        }
        f = new File(ncHome, "\\ejb");
        if (f.exists()) {
            all.addAll(getAllJarFiles(f, false));
        }
        f = new File(ncHome, "\\external\\classes");
        if (f.exists()) {
            all.add(f);
        }
        f = new File(ncHome, "\\external");
        if (f.exists()) {
            all.addAll(getAllJarFiles(f, false));
        }
        f = new File(ncHome, "\\driver");
        if (f.exists()) {
            all.addAll(getAllJarFiles(f, false));
        }
        f = new File(ncHome, "\\middleware");
        if (f.exists()) {
            all.addAll(getAllJarFiles(f, false));
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
        //增加数据交换支持 eg:
        //D:\runtimes\U8Cloud_HEXIN\modules\dm\client\extension\classes\
        all.addAll(serachAllNcClass(new File(ncHome, "modules")
                , "client" + File.separatorChar + "extension"
                        + File.separatorChar + "classes"
                , false));
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
     * 获取NC的 所有 模块的 所有  NCC class文件夹
     *
     * @param ncHome
     * @return
     */
    public static final ArrayList<File> serachAllNCCClass(File ncHome) {
        return serachAllNcClass(new File(ncHome
                        , "hotwebs" + File.separatorChar + "nccloud" + File.separatorChar + "WEB-INF")
                , "classes", false);
    }

    /**
     * 获取NC的 所有 模块的 所有 NCC jar文件
     *
     * @param ncHome
     * @return
     */
    public static final ArrayList<File> serachAllNCCJars(File ncHome) {
        return new ArrayList<>(getAllJarFiles(new File(ncHome
                , "hotwebs"
                + File.separatorChar + "nccloud"
                + File.separatorChar + "WEB-INF"
                + File.separatorChar + "lib"
        ), true));
    }

    /**
     * 获取NC的 所有 模块的 所有    NCCloud_Library
     *
     * @param ncHome
     * @return
     */
    public static final ArrayList<File> serachNCCloud_Library(File ncHome) {
        //I:\runtime\NCCHOME202005_DONGGUANZHENGJUAN\hotwebs\nccloud\WEB-INF
        ArrayList<File> all = serachAllNCCJars(ncHome);

        File f = new File(ncHome, "hotwebs"
                + File.separatorChar + "nccloud"
                + File.separatorChar + "WEB-INF"
                + File.separatorChar + "classes"
        );
        if (f.isDirectory()) {
            all.add(f);
        }

        f = new File(ncHome, "hotwebs"
                + File.separatorChar + "nccloud"
                + File.separatorChar + "WEB-INF"
                + File.separatorChar + "extend"
        );

        if (f.isDirectory()) {
            all.add(f);
        }

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
            all.addAll(getAllJarFiles(f, true));
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
            all.addAll(getAllJarFiles(f, true));
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

        if (!ncModules.isDirectory()) {
            return all;
        }

        File[] listFiles = ncModules.listFiles();

        if (CollUtil.isEmpty(listFiles)) {
            return all;
        }

        Arrays.stream(listFiles).forEach(dir -> {
            if (dir.isDirectory()) {
                File f = new File(dir, File.separatorChar + dirName);
                if (f.exists()) {
                    if (isJarDir) {
                        all.addAll(getAllJarFiles(f, false));
                    } else {
                        all.add(f);
                    }
                }
            }
        });
        return all;
    }


    /**
     * 所有一个 文件夹类所有的jar文件
     *
     * @param dir
     * @param hasChiled true 会搜索子目录！
     * @return
     */
    public static List<File> getAllJarFiles(File dir, boolean hasChiled) {
        return getAllFiles(dir, hasChiled, ".jar");
    }

    /**
     * 所有一个 文件夹类所有的指定后缀名文件
     *
     * @param dir
     * @param hasChiled   true 会搜索子目录！
     * @param fileEndFixs 文件名后缀
     * @return
     */
    public static List<File> getAllFiles(@NotNull File dir, boolean hasChiled, @NotNull final String... fileEndFixs) {
        List<File> ar = getAllFiles(dir, hasChiled);
        if (null == ar) {
            return new ArrayList<>();
        }

        return ar.stream().filter(file
                -> {
            boolean mach = file.exists() && file.isFile();
            boolean endMach = false;
            for (String fileEndFix : fileEndFixs) {
                endMach = file.getName().toLowerCase().endsWith(fileEndFix);
                if (endMach) {
                    break;
                }
            }
            return mach && endMach;
        }).collect(Collectors.toList());
    }


    /**
     * 搜索一个文件夹内 所有的末级子文件夹！
     *
     * @param dir
     * @return
     */
    public static List<File> getAllLastDirs(@NotNull File dir) {
        List<File> all = new LinkedList<>();
        File[] files = dir.listFiles();
        if (null == files) {
            return all;
        }

        for (File file : files) {
            if (!file.isDirectory()) {
                continue;
            }

            if (Stream.of(file.listFiles()).anyMatch(e -> e.isDirectory())) {
                all.addAll(getAllLastDirs(file));
            } else {
                all.add(file);
            }
        }

        return all;
    }

    /**
     * 搜索一个文件夹内 所有有源文件的包路径
     *
     * @param dir
     * @return
     */
    public static List<File> getAllLastPackges(@NotNull File dir) {
        List<File> all = new LinkedList<>();
        File[] files = dir.listFiles();
        if (null == files) {
            return all;
        }

        for (File file : files) {
            if (!file.isDirectory()) {
                continue;
            }

            if (Stream.of(file.listFiles()).anyMatch(e -> e.isFile())) {
                all.add(file);
            }

            if (Stream.of(file.listFiles()).anyMatch(e -> e.isDirectory())) {
                all.addAll(getAllLastPackges(file));
            }
        }

        return all;
    }

    /**
     * 把一个文件夹 本目录当前级次内所有文件复制到指定的路径（不会复制下级文件夹！）        </br>
     * </br>
     * </br>
     * </br>
     *
     * @return void
     * @author air Email: 209308343@qq.com
     * @date 2020/1/16 0016 20:07
     * @Param [fromDir, toDir]
     */
    public static final void copyAllFile(@NotNull File fromDir, @NotNull final File toDir) {
        Stream.of(fromDir.listFiles()).forEach(file -> {
            if (!file.isFile()) {
                return;
            }
            copyFile(file, toDir);
        });
    }

    /**
     * 把一个文件 复制到指定的文件夹里 </br>
     * 会自动创建不存在的文件夹      </br>
     * </br>
     * </br>
     *
     * @return void
     * @author air Email: 209308343@qq.com
     * @date 2020/1/16 0016 20:07
     * @Param [fromDir, toDir]
     */
    public static final void copyFile(@NotNull File from, @NotNull final File to) {
        /*if (!from.isFile()) {
            return;
        }
        File outFile = new File(dir, from.getName());
        if (!outFile.getParentFile().exists()) {
            outFile.getParentFile().mkdirs();
        }
        try {
            Files.copy(from.toPath(), outFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        to.mkdirs();

        FileUtil.copy(from, to, true);
    }

    /**
     * 所有一个 文件夹类所有的文件
     *
     * @param dir
     * @param hasChiled true 包含子文件夹
     * @return
     */
    public static List<File> getAllFiles(File dir, boolean hasChiled) {
        File[] fs = dir.listFiles();

        ArrayList<File> ar = new ArrayList<File>(null == fs ? 1 : fs.length);

        try {
            File f;
            for (int i = 0; i < fs.length; i++) {
                f = fs[i];
                if (f.isFile()) {
                    ar.add(fs[i]);
                } else if (f.isDirectory() && hasChiled) {
                    ar.addAll(getAllFiles(f, hasChiled));
                }
            }
        } catch (Exception e) {
        }

        return ar;
    }

    /**
     * 输出Jar文件
     *
     * @param dir         root 文件夹or文件
     * @param outputFile  压缩后的文件
     * @param minf
     * @param skipSuffixs String数组，要跳过的 文件类型后缀： 比如 ["zip","rar"]
     */
    public static void makeJar(File dir, File outputFile, Manifest minf, String[] skipSuffixs) {
        JarOutputStream jos = null;
        try {
            jos = new JarOutputStream(new FileOutputStream(outputFile), minf);
            // 设置压缩包注释
            jos.setComment("create by idea plugin , power by air 209308343@qq.com");

            List<File> allFiles = getAllFiles(dir, true);
            if (skipSuffixs == null) {
                skipSuffixs = new String[0];
            }
            if (allFiles == null) {
                allFiles = Collections.emptyList();
            }
            int count = -1;
            BufferedInputStream bis = null;
            byte[] cache = new byte[1024];
            boolean skip = false;
            for (File f : allFiles) {
                if (f.equals(outputFile)) {
                    continue;
                }

                skip = false;
                for (String skipSuffix : skipSuffixs) {
                    if (f.getName().toLowerCase().endsWith(skipSuffix)) {
                        skip = true;
                        break;
                    }
                }

                if (skip) {
                    continue;
                }

                bis = new BufferedInputStream(new FileInputStream(f), 1024);
                String entityPath = StringUtil.replaceAll(f.getPath(), File.separator, "/")
                        .substring(dir.getPath().length());

                if (entityPath.charAt(0) == '/'
                        || entityPath.charAt(0) == File.separatorChar) {
                    //我TM也很无语，这里为啥可能多个 / 导致类加载器不识别
                    entityPath = entityPath.substring(1);
                }

                jos.putNextEntry(new JarEntry(entityPath));
                while ((count = bis.read(cache, 0, 1024)) != -1) {
                    jos.write(cache, 0, count);
                }
                jos.closeEntry();
                bis.close();
            }

            //2次调用 保证字节完全写入磁盘。
            jos.flush();
            jos.finish();
        } catch (
                IOException e) {
            e.printStackTrace();
        } finally {
            if (jos != null) {
                try {
                    jos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * 初始化压缩包信息并开始进行压缩
     *
     * @param inputFile   需要压缩的文件或文件夹
     * @param outputFile  压缩后的文件
     * @param type        压缩类型
     * @param skipSuffixs String数组，要跳过的 文件类型后缀： 比如 ["zip","rar"]
     */
    public static void zip(File inputFile, File outputFile, CompressType type, String[] skipSuffixs) {
        ZipOutputStream zos = null;
        try {
            if (type == CompressType.ZIP) {
                zos = new ZipOutputStream(new FileOutputStream(outputFile));
            } else if (type == CompressType.JAR) {
                zos = new JarOutputStream(new FileOutputStream(outputFile));
            } else {
                zos = new ZipOutputStream(new FileOutputStream(outputFile));
            }
            // 设置压缩包注释
            zos.setComment("create by idea plugin , power by air 209308343@qq.com");
            zipFile(zos, inputFile, null, skipSuffixs);

            //2次调用 保证字节完全写入磁盘。
            zos.flush();
            zos.finish();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (zos != null) {
                try {
                    zos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 如果是单个文件，那么就直接进行压缩。如果是文件夹，那么递归压缩所有文件夹里的文件
     *
     * @param zos         压缩输出流
     * @param inputFile   需要压缩的文件
     * @param path        需要压缩的文件在压缩包里的路径
     * @param skipSuffixs String数组，要跳过的 文件类型后缀： 比如 ["zip","rar"]
     */
    public static void zipFile(ZipOutputStream zos, File inputFile, String path, String[] skipSuffixs) {
        if (inputFile.isDirectory()) {
            // 记录压缩包中文件的全路径
            String p = null;
            File[] fileList = inputFile.listFiles();
            for (int i = 0; i < fileList.length; i++) {
                File file = fileList[i];
                // 如果路径为空，说明是根目录
                if (path == null || path.isEmpty()) {
                    p = file.getName();
                } else {
                    p = path + File.separator + file.getName();
                }
                // 如果是目录递归调用，直到遇到文件为止
                zipFile(zos, file, p, skipSuffixs);
            }
        } else if (inputFile.isFile()) {
            String lowerCaseFileName = inputFile.getName().toLowerCase();
            if (null != skipSuffixs && skipSuffixs.length > 0) {
                for (String skipSuffix : skipSuffixs) {
                    if (lowerCaseFileName.endsWith(skipSuffix)) {
                        return;
                    }
                }
            }
            zipSingleFile(zos, inputFile, path);
        }
    }

    /**
     * 压缩单个文件到指定压缩流里
     *
     * @param zos       压缩输出流
     * @param inputFile 需要压缩的文件
     * @param path      需要压缩的文件在压缩包里的路径
     * @throws FileNotFoundException
     */
    public static void zipSingleFile(ZipOutputStream zos, File inputFile, String path) {
        try {
            InputStream in = new FileInputStream(inputFile);
            zos.putNextEntry(new JarEntry(path));
            write(in, zos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从输入流写入到输出流的方便方法 【注意】这个函数只会关闭输入流，且读写完成后会调用输出流的flush()函数，但不会关闭输出流！
     *
     * @param input
     * @param output
     */
    private static void write(InputStream input, OutputStream output) {
        int len = -1;
        byte[] buff = new byte[1024];
        try {
            while ((len = input.read(buff)) != -1) {
                output.write(buff, 0, len);
            }
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 把给定的文件夹 里所有文件和文件夹删除！        </br>
     * </br>
     * </br>
     * </br>
     *
     * @return void
     * @author air Email: 209308343@qq.com
     * @date 2020/2/9 0009 15:56
     * @Param [file]
     */
    public static void cleanUpDirFiles(File dir) {
        if (!dir.exists()) {//判断是否待删除目录是否存在
            return;
        }

        deleteFileAll(dir);

        dir.mkdirs();
    }

    /**
     * 删除 文件夹
     * file：文件名
     */
    public static void deleteFileAll(File dir) {
        if (dir.exists()) {
            File files[] = dir.listFiles();
            if (files == null) {
                files = new File[0];
            }
            int len = files.length;
            for (int i = 0; i < len; i++) {
                if (files[i].isDirectory()) {
                    deleteFileAll(files[i]);
                } else {
                    files[i].delete();
                }
            }
            dir.delete();
        }
    }

    /**
     * 获得jdk的基本路径中某个特定文件        </br>
     * 优先使用nchome的ufjdk 如果没有就查询运行态，如果在没有就查询 JAVA_HOME 然后 JDK_HOME的环境变量配置 最后都没有 返回null       </br>
     * </br>
     * </br>
     *
     * @param fileRelativePath 相对于 jdk根目录的相对路径
     * @return void
     * @author air Email: 209308343@qq.com
     * @date 2020/3/5 0005 12:05
     * @Param ncHomePath nchome路径
     */
    public static File getJavaHomePathFile(String ncHomePath, String fileRelativePath) {
        File javaHome = new File(System.getProperty("java.home"));
        File javaBin = null;

        if (StringUtil.notEmpty(ncHomePath)) {
            File ufjdkHome = new File(ncHomePath + File.separatorChar + "ufjdk");
            javaBin = new File(ufjdkHome, fileRelativePath);
            if (javaBin.exists()) {
                return javaBin;
            }
        }

        javaBin = new File(javaHome, fileRelativePath);
        if (javaBin.exists()) {
            return javaBin;
        }

        //尝试智能匹配路径 like : C:\Program Files\Java\jdk1.8.0_191\jre\bin\jconsole.exe -> to正确路径 (最多往上跳2级)
        javaHome = javaHome.getParentFile();
        javaBin = new File(javaHome, fileRelativePath);
        if (javaBin.exists()) {
            return javaBin;
        }

        javaHome = javaHome.getParentFile();
        javaBin = new File(javaHome, fileRelativePath);
        if (javaBin.exists()) {
            return javaBin;
        }

        //获取 环境变量
        String envValue = System.getenv().get("JAVA_HOME");
        if (StringUtil.notEmpty(envValue)) {
            javaBin = new File(envValue, fileRelativePath);
            if (javaBin.exists()) {
                return javaBin;
            }
        }

        envValue = System.getenv().get("JDK_HOME");
        if (StringUtil.notEmpty(envValue)) {
            javaBin = new File(envValue, fileRelativePath);
            if (javaBin.exists()) {
                return javaBin;
            }
        }

        return null;
    }

    private IoUtil() {
    }

    /**
     * 复制NC元数据文件
     *
     * @param old
     * @param newFile
     */
    public static void copyNCMateFile(File old, File newFile) throws IOException {
        org.dom4j.Document document = XmlUtil.resetMeateIds(old);
        OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(newFile), "UTF-8");
        document.write(out);
        out.flush();
        out.close();
    }

    /**
     * 判断2个文件是否是 没有任何差异(大小 修改时间)
     *
     * @param f
     * @param tof
     * @return
     */
    public static boolean isNoChange(File f, File tof) {
        try {
            if (f.isFile() && tof.isFile()) {
                return f.length() == tof.length() && f.lastModified() == tof.lastModified();
                // return DigestUtils.md5Hex(new FileInputStream(f)).equals(DigestUtils.md5Hex(new FileInputStream(tof)));
            }

           /* if (f.isDirectory() && tof.isDirectory()) {
                List<File> afs = getAllFiles(f, true);
                HashSet<Object> aSet = new HashSet<>();
                for (File af : afs) {
                    aSet.add(DigestUtils.md5Hex(new FileInputStream(af)));
                }

                List<File> bfs = getAllFiles(tof, true);
                HashSet<Object> bSet = new HashSet<>();
                for (File bf : bfs) {
                    bSet.add(DigestUtils.md5Hex(new FileInputStream(bf)));
                }

                return aSet.equals(bSet);
            }*/
        } catch (Throwable e) {
            return false;
        }

        return false;
    }
}


/**
 * 压缩类型枚举
 *
 * @author Log
 */
enum CompressType {
    //	GZIP是用于UNIX系统的文件压缩，在Linux中经常会使用到*.gz的文件，就是GZIP格式
    ZIP, JAR, GZIP
}