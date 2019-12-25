package com.air.nc5dev.util;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import nc.vo.pub.BusinessException;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;

/***
 *    NC 补丁导出 工具类       </br>
 *           </br>
 *           </br>
 *           </br>
 * @author air Email: 209308343@qq.com
 * @date 2019/12/25 0025 16:00
 * @Param
 * @return
 */
public class ExportPatcherUtil {
    private final String PATH_CLIENT;
    private final String PATH_PUBLIC;
    private final String PATH_PRIVATE;
    private final String PATH_WEB_INF;
    private final String PATH_CLASSES;
    private final String PATH_MODULES;
    private final String PATH_META_INF;
    private final String PATH_EXTEND;
    private final String PATH_SRC;
    private final String TYPE_JAVA;
    private final String TYPE_CLASS;
    private final String TYPE_XML;
    private final String TYPE_UPM;
    private final String FILE_MODULE;
    private final String NAME_MODULE;
    private String PATH_HOTWEBS;
    private String PATH_REPLACEMENT;
    private String patchName;
    private String exportPath;
    private AnActionEvent event;
    private String zipName;

    public ExportPatcherUtil(String patchName, String exportPath, AnActionEvent event) {
        this.PATH_CLIENT = File.separator + "client";
        this.PATH_PUBLIC = File.separator + "public";
        this.PATH_PRIVATE = File.separator + "private";
        this.PATH_WEB_INF = File.separator + "WEB-INF";
        this.PATH_CLASSES = File.separator + "classes";
        this.PATH_MODULES = File.separator + "modules";
        this.PATH_META_INF = File.separator + "META-INF";
        this.PATH_EXTEND = File.separator + "extend";
        this.PATH_SRC = File.separator + "src";
        this.TYPE_JAVA = ".java";
        this.TYPE_CLASS = ".class";
        this.TYPE_XML = ".xml";
        this.TYPE_UPM = ".upm";
        this.FILE_MODULE = "module.xml";
        this.NAME_MODULE = "name";
        this.PATH_HOTWEBS = File.separator + "hotwebs";
        this.PATH_REPLACEMENT = File.separator + "replacement";
        this.zipName = "";
        this.event = event;
        this.exportPath = exportPath + File.separator + "patch_" + System.currentTimeMillis();
        this.patchName = patchName;
    }

    public void exportPatcher(VirtualFile[] selectFile) throws Exception {
        Project project = this.event.getProject();
        Map<String, Module> moduleMap = new HashMap();
        Module[] modules = ModuleManager.getInstance(project).getModules();
        Module[] var5 = modules;
        int var6 = modules.length;

        for (int var7 = 0; var7 < var6; ++var7) {
            Module module = var5[var7];
            moduleMap.put(module.getName(), module);
        }

        String projectPath = project.getBasePath();
        Map<String, Set<String>> modulePathMap = new HashMap();
        VirtualFile[] var25 = selectFile;
        int var27 = selectFile.length;

        for (int var9 = 0; var9 < var27; ++var9) {
            VirtualFile file = var25[var9];
            String path = (new File(file.getPath())).getPath();
            String moduleName = ModuleUtil.findModuleForFile(file, project).getName();
            Set<String> fileUrlSet = (Set) modulePathMap.get(moduleName);
            if (fileUrlSet == null) {
                fileUrlSet = new HashSet();
                modulePathMap.put(moduleName, fileUrlSet);
            }

            this.getFileUrl(path, (Set) fileUrlSet);
        }

        Set<String> classNameSet = new HashSet();
        Set<String> moduleSet = new HashSet();
        Iterator var29 = modulePathMap.keySet().iterator();

        while (var29.hasNext()) {
            String moduleName = (String) var29.next();
            Module module = (Module) moduleMap.get(moduleName);
            Set<String> fileUrlSet = (Set) modulePathMap.get(moduleName);
            CompilerModuleExtension instance = CompilerModuleExtension.getInstance(module);
            VirtualFile outPath = instance.getCompilerOutputPath();
            if (outPath == null) {
                throw new BusinessException("please set output folder or rebuild module !\n module:" + moduleName);
            }

            String compilerOutputUrl = instance.getCompilerOutputPath().getPath();
            String ncModuleName = this.getNCModuleName(module);
            Iterator var17 = fileUrlSet.iterator();

            while (var17.hasNext()) {
                String fileUrl = (String) var17.next();
                File fromFile = new File(fileUrl);
                String fileName = fromFile.getName();
                if (fileName.endsWith(".java")) {
                    this.exportJava(moduleName, ncModuleName, compilerOutputUrl, fromFile);
                    String classPath = fileUrl.split(Matcher.quoteReplacement(this.PATH_SRC))[1];
                    if (classPath.startsWith(this.PATH_CLIENT)) {
                        classPath = classPath.replace(this.PATH_CLIENT, "");
                    }

                    if (classPath.startsWith(this.PATH_PUBLIC)) {
                        classPath = classPath.replace(this.PATH_PUBLIC, "");
                    }

                    if (classPath.startsWith(this.PATH_PRIVATE)) {
                        classPath = classPath.replace(this.PATH_PRIVATE, "");
                    }

                    String className = classPath.substring(1).replaceAll(Matcher.quoteReplacement(File.separator), ".");
                    classNameSet.add(className);
                } else if (fileName.endsWith(".xml")) {
                    this.exportXml(moduleName, ncModuleName, compilerOutputUrl, fromFile);
                } else if (fileName.endsWith(".upm")) {
                    this.exportUpm(moduleName, ncModuleName, fromFile);
                }
            }

            moduleSet.add(ncModuleName);
        }

        this.zipName = ZipUtil.toZip(this.exportPath, this.patchName);
        this.delete(new File(this.exportPath));
    }

    public void delete(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            File[] var3 = children;
            int var4 = children.length;

            for (int var5 = 0; var5 < var4; ++var5) {
                File child = var3[var5];
                this.delete(child);
            }

            file.delete();
        } else {
            file.delete();
        }

    }

    private void creatNMCLog(Set<String> moduleSet, Set<String> classNameSet) throws BusinessException {
        long time = System.currentTimeMillis();
        Date date = new Date(time);
        SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateStr = sd.format(date);
        String id = UUID.randomUUID().toString();
        String modifyClasses = "";
        Iterator var15 = classNameSet.iterator();

        while (var15.hasNext()) {
            String className = (String) var15.next();
            if (className.endsWith(".java")) {
                className = className.replace(".java", "");
                modifyClasses = modifyClasses + "," + className;
            }
        }

        if (StringUtils.isNotBlank(modifyClasses)) {
            modifyClasses = modifyClasses.substring(1);
        }

        String modifyModules = "";

        String moduleName;
        for (Iterator var19 = moduleSet.iterator(); var19.hasNext(); modifyModules = modifyModules + "," + moduleName) {
            moduleName = (String) var19.next();
        }

        if (StringUtils.isNotBlank(modifyModules)) {
            modifyModules = modifyModules.substring(1);
        }
    }

    private String getNCModuleName(Module module) {
        String ncModuleName = null;
        String modulePath = module.getModuleFile().getParent().getPath();

        try {
            File file = new File(modulePath + this.PATH_META_INF + File.separator + "module.xml");
            if (file.exists()) {
                InputStream in = new FileInputStream(file);
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(in);
                Element root = doc.getDocumentElement();
                ncModuleName = root.getAttribute("name");
            }
        } catch (Exception var10) {
            ;
        }

        return ncModuleName;
    }

    private void exportXml(String moduleName, String ncModuleName, String compilerOutputUrl, File fromFile) throws Exception {
        String patchPath = fromFile.getPath();
        String toPath;
        toPath = this.exportPath + this.PATH_REPLACEMENT
                + this.PATH_MODULES + File.separator
                + ncModuleName + File.separator
                + this.PATH_CLIENT + this.PATH_CLASSES;
        String className = patchPath.split(Matcher.quoteReplacement(this.PATH_SRC))[1].replace(this.PATH_CLIENT, "");
        this.outPatcher(moduleName, compilerOutputUrl + className, toPath + className);
    }

    private void exportUpm(String moduleName, String ncModuleName, File fromFile) throws Exception {
        String fileName = fromFile.getName();
        String toPath = this.exportPath + this.PATH_REPLACEMENT + this.PATH_MODULES + File.separator + ncModuleName + this.PATH_META_INF;
        this.outPatcher(moduleName, fromFile.getPath(), toPath + File.separator + fileName);
    }

    private void exportJava(String moduleName, String ncModuleName, String compilerOutputUrl, File fromFile) throws Exception {
        String toPath = null;
        String patchPath = fromFile.getPath();
        String className = patchPath.split(Matcher.quoteReplacement(this.PATH_SRC))[1].replace(".java", ".class");
        String javaName = patchPath.split(Matcher.quoteReplacement(this.PATH_SRC))[1];
        String modulePath;
        if (StringUtils.isNotBlank(ncModuleName)) {
            modulePath = this.exportPath + this.PATH_REPLACEMENT + this.PATH_MODULES + File.separator + ncModuleName + File.separator;
            if (patchPath.contains(this.PATH_CLIENT)) {
                className = className.replace(this.PATH_CLIENT, "");
                javaName = javaName.replace(this.PATH_CLIENT, "");
                toPath = modulePath + this.PATH_CLIENT + this.PATH_CLASSES;
            } else if (patchPath.contains(this.PATH_PUBLIC)) {
                className = className.replace(this.PATH_PUBLIC, "");
                javaName = javaName.replace(this.PATH_PUBLIC, "");
                toPath = modulePath + this.PATH_CLASSES;
            } else if (patchPath.contains(this.PATH_PRIVATE)) {
                className = className.replace(this.PATH_PRIVATE, "");
                javaName = javaName.replace(this.PATH_PRIVATE, "");
                toPath = modulePath + this.PATH_META_INF + this.PATH_CLASSES;
            }
        } else {
            modulePath = File.separator + "main" + File.separator + "java";
            className = className.replace(modulePath, "");
            javaName = javaName.replace(modulePath, "");
            toPath = this.exportPath + this.PATH_WEB_INF + this.PATH_CLASSES;
        }

        if (fromFile.lastModified() > (new File(compilerOutputUrl + className)).lastModified()) {
            throw new BusinessException(className.substring(1).replace(File.separator, ".") + " is old,\n please rebuild : " + moduleName);
        } else {
            this.outPatcher(moduleName, compilerOutputUrl + className, toPath + className);
            if (javaName.endsWith(".java")) {
                this.outPatcher(moduleName, fromFile.getPath(), toPath + javaName);
            }

        }
    }

    private void getFileUrl(String elementPath, Set<String> fileUrlSet) {
        if (elementPath.contains(this.PATH_SRC) || elementPath.contains(this.PATH_META_INF)) {
            File file = new File(elementPath);
            if (file.isDirectory()) {
                File[] childrenFile = file.listFiles();
                File[] var5 = childrenFile;
                int var6 = childrenFile.length;

                for (int var7 = 0; var7 < var6; ++var7) {
                    File childFile = var5[var7];
                    this.getFileUrl(childFile.getPath(), fileUrlSet);
                }
            } else if (elementPath.endsWith(".java") || elementPath.endsWith(".xml") || elementPath.endsWith(".upm")) {
                fileUrlSet.add(elementPath);
            }
        }

    }

    private void outPatcher(String moduleName, String srcPath, String toPath) throws Exception {
        File from = new File(srcPath);
        if (!from.exists()) {
            throw new BusinessException("please build : " + moduleName);
        } else {
            File to = new File(toPath);
            FileUtil.copy(from, to);
            String fileName = from.getName().substring(0, from.getName().length() - 6);
            File[] var7 = from.getParentFile().listFiles();
            int var8 = var7.length;

            for (int var9 = 0; var9 < var8; ++var9) {
                File f = var7[var9];
                if (f.getName().startsWith(fileName + "$")) {
                    FileUtil.copy(f, new File(to.getParent() + File.separator + f.getName()));
                }
            }

        }
    }

    public String getExportPath() {
        return this.exportPath;
    }

    public String getZipName() {
        return this.zipName;
    }
}
