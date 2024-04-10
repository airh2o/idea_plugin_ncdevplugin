package com.air.nc5dev.acion;

import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.air.nc5dev.acion.base.AbstractIdeaAction;
import com.air.nc5dev.util.CollUtil;
import com.air.nc5dev.util.IoUtil;
import com.air.nc5dev.util.ProjectNCConfigUtil;
import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.util.V;
import com.air.nc5dev.util.XmlUtil;
import com.air.nc5dev.util.idea.LogUtil;
import com.air.nc5dev.util.idea.ProjectUtil;
import com.air.nc5dev.vo.UpmFileVO;
import com.air.nc5dev.vo.meta.ComponentAggVO;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.compiled.ClsClassImpl;
import com.intellij.psi.impl.compiled.ClsElementImpl;
import com.intellij.psi.impl.source.PsiClassImpl;
import com.intellij.psi.impl.source.PsiJavaFileBaseImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class OpenIntefaceInNCHOMEUpmFileAction extends AbstractIdeaAction {
    /**
     * key= 项目， value=( key=接口类名， value=upm文件路径描述 )
     */
    public static Map<Project, Map<String, Set<UpmFileVO>>> CACHE = new ConcurrentHashMap<>();

    @Override
    protected void doHandler(AnActionEvent e) {
        Collection<String> clzs = getSelectedFile(e);

        Task.Backgroundable backgroundable = new Task.Backgroundable(e.getProject(), "正在搜索...请耐心等待(第一次搜索会初始化扫描HOME中文件" +
                "...初始化只会运行一次)") {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {
                    CACHE.putIfAbsent(e.getProject(), new ConcurrentHashMap<>());
                    Map<String, Set<UpmFileVO>> map = CACHE.get(e.getProject());
                    if (map.isEmpty()) {
                        init(indicator, e.getProject());
                    }

                    indicator.setText("搜索项目文件中...");
                    List<File> xmls = V.get(IoUtil.getAllFiles(
                            new File(e.getProject().getBasePath())
                            , true
                            , f -> f.isFile() || (
                                    !(
                                            f.getName().equalsIgnoreCase(".idea")
                                                    || f.getName().equalsIgnoreCase(".git")
                                                    || f.getName().equalsIgnoreCase(".settings")
                                                    || f.getName().equalsIgnoreCase(".svn")
                                    )
                                            && f.getParentFile().getName().equals("META-INF")
                            )
                            , ".upm", ".xml", ".aop"), new ArrayList<>());
                    for (File xml : xmls) {
                        if (indicator.isCanceled()) {
                            return;
                        }

                        read2CacheNoError(xml.getParentFile().getParentFile().getName(), xml, indicator,
                                e.getProject());
                    }

                    for (String clz : clzs) {
                        if (indicator.isCanceled()) {
                            return;
                        }
                        indicator.setText("正在打开类的upm等配置文件:" + clz);
                        doOpen(e.getProject(), map, indicator, clz);
                    }
                } finally {
                }
            }
        };
        backgroundable.setCancelText("停止任务");
        backgroundable.setCancelTooltipText("停止这个任务");
        ProgressManager.getInstance().run(backgroundable);
    }

    public static void doOpen(Project project, Map<String, Set<UpmFileVO>> map
            , ProgressIndicator indicator, String clz) {
        if (indicator.isCanceled() || CollUtil.isEmpty(map)) {
            return;
        }

        Set<UpmFileVO> vos = map.get(clz);
        if (CollUtil.isEmpty(vos)) {
            return;
        }
        indicator.setText("正在打开满足条件的文件: " + vos.size());

        for (UpmFileVO v : vos) {
            if (indicator.isCanceled()) {
                return;
            }

            ProjectUtil.openFile(project, v.getPath(), v.getRow(), v.getColumn());
        }
    }

    public static void init(ProgressIndicator indicator, Project project) {
        CACHE.putIfAbsent(project, new ConcurrentHashMap<>());
        Map<String, Set<UpmFileVO>> map = CACHE.get(project);

        if (indicator.isCanceled()) {
            map.clear();
            return;
        }

        File home = new File(ProjectNCConfigUtil.getNCHomePath(project));
        if (!home.isDirectory()) {
            return;
        }

        indicator.setText("搜索NCHOME中:" + home.getPath());
        File modules = new File(home, "modules");
        if (!modules.isDirectory()) {
            return;
        }

        File[] fs = modules.listFiles();
        if (CollUtil.isEmpty(fs)) {
            return;
        }


        for (File f : fs) {
            indicator.setText("搜索NCHOME的模块中:" + f.getPath());
            if (!f.isDirectory()) {
                continue;
            }
            if (indicator.isCanceled()) {
                map.clear();
                return;
            }

            File metainf = new File(f, "META-INF");
            if (!metainf.isDirectory()) {
                continue;
            }

            File[] mfs = metainf.listFiles();
            if (CollUtil.isEmpty(mfs)) {
                continue;
            }

            for (File mf : mfs) {
                if (indicator.isCanceled()) {
                    map.clear();
                    return;
                }

                if (!mf.isFile()) {
                    continue;
                }

                String name = mf.getName().toLowerCase();
                if (!StrUtil.endWithAny(name, ".upm", ".xml", ".aop")) {
                    continue;
                }

                read2CacheNoError(f.getName(), mf, indicator, project);
            }
        }
    }

    protected static void read2CacheNoError(String moduleName, File f, ProgressIndicator indicator, Project project) {
        try {
            read2Cache(moduleName, f, indicator, project);
        } catch (Throwable e) {
            LogUtil.error(e.getMessage(), e);
        }
    }

    protected static void read2Cache(String moduleName, File f, ProgressIndicator indicator, Project project) {
        String name = f.getName().toLowerCase();
        Document doc = XmlUtil.readXML(f);
        if (doc == null) {
            return;
        }

        List<String> xmls = FileUtil.readUtf8Lines(f);
        CACHE.putIfAbsent(project, new ConcurrentHashMap<>());
        Map<String, Set<UpmFileVO>> map = CACHE.get(project);
        Element module = XmlUtil.getRootElement(doc);
        if (name.endsWith(".upm") || name.endsWith(".xml")) {
            List<Element> publics = XmlUtil.getElements(module, "public");
            for (Element p : publics) {
                List<Element> components = XmlUtil.getElements(p, "component");
                for (Element component : components) {
                    Element interfacee = XmlUtil.getElement(component, "interface");
                    Element implementation = XmlUtil.getElement(component, "implementation");
                    String interfaceName = null;
                    String implementationName = null;
                    if (interfacee != null) {
                        interfaceName = StringUtil.trim(interfacee.getTextContent());
                    }
                    if (implementation != null) {
                        implementationName = StringUtil.trim(implementation.getTextContent());
                    }
                    if (interfaceName == null) {
                        interfaceName = implementationName;
                    }
                    if (implementationName == null) {
                        implementationName = interfaceName;
                    }

                    if (interfaceName == null && implementationName == null) {
                        continue;
                    }
                    Element ele = interfacee == null ? implementation : interfacee;

                    int row = 0;
                    for (int i = 0; i < xmls.size(); i++) {
                        if (StringUtil.contains(xmls.get(i), interfaceName)) {
                            row = i;
                            break;
                        }
                    }

                    map.putIfAbsent(interfaceName, new ConcurrentHashSet<>());
                    map.putIfAbsent(implementationName, new ConcurrentHashSet<>());
                    UpmFileVO v = new UpmFileVO().setModule(moduleName)
                            .setType(UpmFileVO.TYPE_NORMAL)
                            .setPath(f.getPath())
                            .setInterfaceName(interfaceName)
                            .setImplName(implementationName)
                            .setRow(row)
                            .setColumn(3)
                            .setTag("public");
                    ComponentAggVO.attr2VO(v, component, null);
                    map.get(interfaceName).add(v);
                    map.get(implementationName).add(v);
                }
            }

            List<Element> privates = XmlUtil.getElements(module, "private");
            for (Element p : privates) {
                List<Element> components = XmlUtil.getElements(p, "component");
                for (Element component : components) {
                    Element interfacee = XmlUtil.getElement(component, "interface");
                    Element implementation = XmlUtil.getElement(component, "implementation");
                    String interfaceName = null;
                    String implementationName = null;
                    if (interfacee != null) {
                        interfaceName = StringUtil.trim(interfacee.getTextContent());
                    }
                    if (implementation != null) {
                        implementationName = StringUtil.trim(implementation.getTextContent());
                    }
                    if (interfaceName == null) {
                        interfaceName = implementationName;
                    }
                    if (implementationName == null) {
                        implementationName = interfaceName;
                    }

                    if (interfaceName == null && implementationName == null) {
                        continue;
                    }
                    Element ele = interfacee == null ? implementation : interfacee;

                    int row = 0;
                    for (int i = 0; i < xmls.size(); i++) {
                        if (StringUtil.contains(xmls.get(i), interfaceName)) {
                            row = i;
                            break;
                        }
                    }

                    map.putIfAbsent(interfaceName, new ConcurrentHashSet<>());
                    map.putIfAbsent(implementationName, new ConcurrentHashSet<>());
                    UpmFileVO v = new UpmFileVO().setModule(moduleName)
                            .setType(UpmFileVO.TYPE_NORMAL)
                            .setPath(f.getPath())
                            .setInterfaceName(interfaceName)
                            .setImplName(implementationName)
                            .setRow(row)
                            .setColumn(3)
                            .setTag("private");
                    ComponentAggVO.attr2VO(v, component, null);
                    map.get(interfaceName).add(v);
                    map.get(implementationName).add(v);
                }
            }
        } else if (name.endsWith(".aop")) {
            List<Element> aopsList = XmlUtil.getElements(module, "aops");
            for (Element aops : aopsList) {
                List<Element> aspects = XmlUtil.getElements(aops, "aspect");
                for (Element aspect : aspects) {
                    String className = aspect.getAttribute("class");
                    String component = aspect.getAttribute("component");

                    if (StrUtil.isBlank(className) || StrUtil.isBlank(component)) {
                        continue;
                    }

                    int row = 0;
                    for (int i = 0; i < xmls.size(); i++) {
                        if (StringUtil.contains(xmls.get(i), component)) {
                            row = i;
                            break;
                        }
                    }

                    map.putIfAbsent(className, new ConcurrentHashSet<>());
                    map.putIfAbsent(component, new ConcurrentHashSet<>());
                    UpmFileVO v = new UpmFileVO().setModule(moduleName)
                            .setType(UpmFileVO.TYPE_AOP)
                            .setPath(f.getPath())
                            .setInterfaceName(component)
                            .setImplName(className)
                            .setRow(row)
                            .setColumn(3);
                    map.get(className).add(v);
                    map.get(component).add(v);
                }
            }
        }
    }

    @Nullable
    public Collection<String> getSelectedFile(AnActionEvent e) {
        HashSet<String> clzs = new HashSet<>();
        PsiElement psiElement = e.getDataContext().getData(CommonDataKeys.PSI_ELEMENT);
        if (psiElement == null) {
            psiElement = e.getDataContext().getData(CommonDataKeys.PSI_FILE);
        }

        if (psiElement instanceof ClsClassImpl) {
            ClsClassImpl clze = (ClsClassImpl) psiElement;
            clzs.add(clze.getQualifiedName());
        } else if (psiElement instanceof PsiJavaFileBaseImpl) {
            PsiJavaFileBaseImpl clze = (PsiJavaFileBaseImpl) psiElement;
            PsiClass[] cs = clze.getClasses();
            if (cs != null) {
                for (PsiClass c : cs) {
                    clzs.add(c.getQualifiedName());
                }
            }
        } else if (psiElement instanceof PsiClassImpl) {
            PsiClassImpl clze = (PsiClassImpl) psiElement;
            clzs.add(clze.getQualifiedName());
        }

        return clzs;
    }

    @Override
    public void update(final @NotNull AnActionEvent e) {
        super.update(e);

        e.getPresentation().setEnabled(!getSelectedFile(e).isEmpty());
    }
}
