package com.air.nc5dev.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.air.nc5dev.nccrequstsearch.RequestMappingItemProvider;
import com.air.nc5dev.ui.actionurlsearch.ActionResultListTable;
import com.air.nc5dev.util.idea.ProjectUtil;
import com.air.nc5dev.vo.NCCActionInfoVO;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.impl.libraries.LibraryEx;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.psi.*;
import com.intellij.psi.impl.compiled.ClsFileImpl;
import com.intellij.psi.impl.compiled.ClsJavaCodeReferenceElementImpl;
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex;
import com.intellij.psi.impl.java.stubs.index.JavaSuperClassNameOccurenceIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectAndLibrariesScope;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * </br>
 * </br>
 * </br>
 *
 * @author 唐粟 Email:209308343@qq.com
 * @date 2022/2/16 0016 9:16
 * @project
 * @Version
 */
public class NCCActionRefreshUtil {

    public static void reloadProjectAction(Project project) {
        if (project == null) {
            return;
        }

  /*      ProjectUtil.setProject(project);
        if (NcVersionEnum.NCC != ProjectNCConfigUtil.getNCVerSIon()) {

        }*/

        Module[] modules = ModuleManager.getInstance(project).getModules();
        if (modules == null) {
            return;
        }

        for (Module module : modules) {
            File moduleDir = new File(module.getModuleFilePath()).getParentFile();
            File src = new File(moduleDir, "src");
            File client = new File(src, "client");
            File yyconfig = new File(client, "yyconfig");
            if (client.isDirectory()) {
                //yyconfig/modules/
                loadDir4yyconfig(project, yyconfig, NCCActionInfoVO.FROM_SRC);
            }

            File metainf = new File(moduleDir, "META-INF");
            if (metainf.isDirectory()) {
                loadDir4upm(project, metainf, NCCActionInfoVO.FROM_SRC);
            }

            loadServletNoUpm4Src(project, module);
            loadSpringmvc4Src(project, module);
            loadJaxrs4Src(project, module);
        }
    }

    public static void loadJaxrs4Src(Project project, Module module) {
        loadJaxrs(project, module);
    }

    public static void loadSpringmvc4Src(Project project, Module module) {
        loadSpringmvc(project, module);
    }

    public static void loadServletNoUpm4Src(Project project, Module module) {
        loadServletNoUpm(project, module);
    }

    private static void loadDir4upm(Project project, File metainf, int from) {
        if (!metainf.isDirectory()) {
            return;
        }

        File[] fs = metainf.listFiles();
        if (CollUtil.isEmpty(fs)) {
            return;
        }

        for (File f : fs) {
            if (!f.isFile()
                    || !StringUtil.containsAny(f.getName().toLowerCase(), ".xml", ".upm")) {
                continue;
            }

            loadUpm(project, f, from);
        }
    }

    /**
     * @param project
     * @param f
     * @param from    {@link com.air.nc5dev.vo.NCCActionInfoVO#FROM_SRC 等}
     */
    public static void loadUpm(Project project, File f, int from) {
        if (f == null || !f.isFile()) {
            return;
        }

        Document document = XmlUtil.xmlFile2Document2(f);
        Element rootElement = XmlUtil.getRootElement(document);
        if (rootElement == null) {
            return;
        }

        List<Element> es = new LinkedList<>();

        NodeList publicList = rootElement.getElementsByTagName("public");
        if (publicList != null) {
            for (int i = 0; i < publicList.getLength(); i++) {
                if (publicList.item(i) instanceof Element) {
                    es.add((Element) publicList.item(i));
                }
            }
        }
        List<String> lines = FileUtil.readUtf8Lines(f);
        LinkedList<NCCActionInfoVO> vos = new LinkedList<>();
        for (Element p : es) {
            NodeList compomentList = p.getElementsByTagName("component");

            for (int x = 0; x < compomentList.getLength(); x++) {
                Element c = (Element) compomentList.item(x);
                Map<String, Object> map = XmlUtil.xmlToMap(c);
                if (CollUtil.isEmpty(map)) {
                    continue;
                }

                if (!StringUtil.isAllNotBlank((String) map.get("implementation")
                        , c.getAttribute("name"))
                        || !"NONE".equals(c.getAttribute("tx"))
                        || !"false".equals(c.getAttribute("remote"))
                        || (c.getElementsByTagName("interface") != null && c.getElementsByTagName("interface").getLength() > 0)
                ) {
                    continue;
                }

                NCCActionInfoVO vo = new NCCActionInfoVO();
                vo.setXmlPath(f.getPath());
                vo.setClazz((String) map.get("implementation"));
                vo.setName(ProjectNCConfigUtil.getNCClientIP() + ":"
                        + StringUtil.get(ProjectNCConfigUtil.getNCClientPort(), "80")
                        + "/service/" + c.getAttribute("name"));
                vo.setLabel(StringUtil.get(c.getAttribute("label")) + "  -UPM文件配置的Servlet");
                vo.setAppcode("UPM配置的Servlet");
                vo.setProject(project != null ? project.getBasePath() : null);
                vo.setFrom(from);
                vo.setType(NCCActionInfoVO.TYPE_UPM);
                vo.setClazz(StrUtil.trim(vo.getClazz()));

                matchRowColumn(lines, vo);

                vos.add(vo);
            }
        }

        if (CollUtil.isNotEmpty(vos)) {
            set2Cache(project, vos);
        }
    }

    public static void loadDir4yyconfig(Project project, File yyconfig, int from) {
        File yyconfigModules = new File(yyconfig, "modules");
        if (!yyconfigModules.isDirectory()) {
            return;
        }

        //yyconfig/modules/pu/
        File[] m1s = yyconfigModules.listFiles();
        if (CollUtil.isEmpty(m1s)) {
            return;
        }

        for (File m1 : m1s) {
            //yyconfig/modules/pu/poorder/
            File[] m2s = m1.listFiles();
            if (CollUtil.isEmpty(m2s)) {
                continue;
            }

            for (File m2 : m2s) {
                //yyconfig/modules/pu/poorder/config/
                loadYyconfigmodulesDir(project, m1, m2, from);
            }
        }
    }

    /**
     * 分析 yyconfig/modules/pu/poorder/config/   <br>
     * 下的 action 和 authorize  <br>
     *
     * @param project
     * @param configParentDir
     */
    public static void loadYyconfigmodulesDir(Project project, File dir, File configParentDir, int from) {
        loadConfigDir(dir, configParentDir, project, from);
    }

    /**
     * 分析 比如: src/client/yyconfig/modules/pu/order/config 文件夹 <br>
     *
     * @param dir
     * @param configDir
     * @param project
     */
    public static void loadConfigDir(File dir, File configDir, Project project, int from) {
        File config = new File(configDir, "config");
        if (!config.isDirectory()) {
            return;
        }

        File action = new File(config, "action");
        if (!action.isDirectory()) {
            return;
        }

        File authorize = new File(config, "authorize");

        load(action, authorize, dir, configDir, project, from);
    }

    private static void load(File action, File authorize, File dir, File configDir, Project project, int from) {
        File[] actionFiles = action.listFiles();
        if (CollUtil.isEmpty(actionFiles)) {
            return;
        }

        List<File> actionXmls = Stream.of(actionFiles)
                .filter(f -> StringUtils.endsWithIgnoreCase(f.getName(), ".xml"))
                .collect(Collectors.toList());

        if (CollUtil.isEmpty(actionXmls)) {
            return;
        }

        File[] authorizeFiles = new File[0];
        if (authorize != null) {
            File[] afs = authorize.listFiles();
            if (CollUtil.isNotEmpty(afs)) {
                authorizeFiles = afs;
            }
        }
        List<File> authorizeXmls = Stream.of(authorizeFiles)
                .filter(f -> StringUtils.endsWithIgnoreCase(f.getName(), ".xml"))
                .collect(Collectors.toList());

        Map<String, String> action2AppcodeMap = new HashMap<>(100);
        Map<String, String> action2AuthXmlPathMap = new HashMap<>(100);
        for (File authorizeXml : authorizeXmls) {
            try {
                Document document = XmlUtil.xmlFile2Document2(authorizeXml);
                Element rootElement = XmlUtil.getRootElement(document);
                NodeList authorizeNode = rootElement.getElementsByTagName("authorize");
                if (authorizeNode == null || authorizeNode.getLength() < 1) {
                    continue;
                }
                Node authorizeNode1 = authorizeNode.item(0);
                String appcode = XmlUtil.elementText((Element) authorizeNode1, "appcode");
                Element actions = XmlUtil.getElement((Element) authorizeNode1, "actions");
                if (actions == null) {
                    continue;
                }
                NodeList actionNodeList = actions.getElementsByTagName("action");
                if (actionNodeList == null) {
                    continue;
                }
                for (int i = 0; i < actionNodeList.getLength(); i++) {
                    String textContent = actionNodeList.item(i).getTextContent();
                    if (StringUtil.isBlank(textContent)) {
                        continue;
                    }

                    action2AppcodeMap.put(textContent, appcode);
                    action2AuthXmlPathMap.put(textContent, authorizeXml.getPath());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        List<NCCActionInfoVO> vos = new ArrayList<>(100);
        for (File actionXml : actionXmls) {
            try {
                Document document = XmlUtil.xmlFile2Document2(actionXml);
                Element rootElement = XmlUtil.getRootElement(document);
                NodeList actionList = rootElement.getElementsByTagName("action");
                if (actionList == null) {
                    continue;
                }

                for (int i = 0; i < actionList.getLength(); i++) {
                    Node item = actionList.item(i);
                    NCCActionInfoVO vo = XmlUtil.xmlToBean(item, NCCActionInfoVO.class);
                    if (StringUtil.isBlank(vo.getClazz())
                            || StringUtil.isBlank(vo.getName())) {
                        continue;
                    }
                    vo.setAppcode(action2AppcodeMap.get(vo.getName()));
                    vo.setAuthPath(action2AuthXmlPathMap.get(vo.getName()));
                    vo.setXmlPath(actionXml.getPath());
                    vo.setProject(project != null ? project.getBasePath() : null);
                    vo.setType(NCCActionInfoVO.TYPE_ACTION);
                    vo.setFrom(from);
                    vo.setClazz(StrUtil.trim(vo.getClazz()));
                    vo.setName(ProjectNCConfigUtil.getNCClientIP() + ":" + StringUtil.get(ProjectNCConfigUtil.getNCClientPort(), "80") +
                            "/nccloud/" + vo.getName().replace('.', '/') + ".do");

                    List<String> lines = FileUtil.readUtf8Lines(actionXml);
                    matchRowColumn(lines, vo);

                    vos.add(vo);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        set2Cache(project, vos);

    }

    private static void set2Cache(Project project, List<NCCActionInfoVO> vos) {
        //注册到缓存里
        for (NCCActionInfoVO vo : vos) {
            //所有的 url信息, key=project getBasePath(),value={key=url完整地址，value=url信息}
            RequestMappingItemProvider.ALL_ACTIONS.computeIfAbsent(project == null ? "" : project.getBasePath()
                    , k -> new ConcurrentHashMap<>()).put(vo.getName().toLowerCase(), vo);
        }
    }

    /**
     * 载入 nchome里的 各种url
     *
     * @param project
     */
    public static void loadNCHome(Project project) {
        ProjectUtil.setProject(project);
        ProjectNCConfigUtil.initConfigFile(project);
        File ncHome = ProjectNCConfigUtil.getNCHome();
        if (!ncHome.isDirectory()) {
            return;
        }
        File hotwebs = new File(ncHome, "hotwebs");
        if (!hotwebs.isDirectory()) {
            return;
        }
        File nccloud = new File(hotwebs, "nccloud");
        if (!nccloud.isDirectory()) {
            return;
        }
        File webinf = new File(nccloud, "WEB-INF");
        if (!webinf.isDirectory()) {
            return;
        }

        //读取lib jar
        File lib = new File(webinf, "lib");
        if (lib.isDirectory() && lib.listFiles().length > 0) {
            for (File file : lib.listFiles()) {
                if (file.isFile() && file.getName().toLowerCase().endsWith(".jar")) {
                    loadYyconfigmodulesDir4Jar(project, file);
                }
            }
        }

        //最后载入 yyconfigDir ，优先级比lib里的jar高
        File yyconfigDir = new File(webinf, "extend" + File.separatorChar + "yyconfig");

        if (yyconfigDir.isDirectory()) {
            loadDir4yyconfig(project, yyconfigDir, NCCActionInfoVO.FROM_HOME);
        }

        //载入UPM
        File modules = new File(ncHome, "modules");
        if (!modules.isDirectory()) {
            return;
        }
        File[] ms = modules.listFiles();
        for (File m : ms) {
            if (!m.isDirectory()) {
                continue;
            }

            File metainf = new File(m, "META-INF");
            if (metainf.isDirectory()) {
                loadDir4upm(project, metainf, NCCActionInfoVO.FROM_HOME);
            }
        }

        loadServletNoUpm4NCHOME(project);
        loadSpringmvc4NCHOME(project);
        loadJaxrs4NCHOME(project);
    }

    public static void loadJaxrs4NCHOME(Project project) {
        loadJaxrs(project, null);
    }

    public static void loadSpringmvc4NCHOME(Project project) {
        loadSpringmvc(project, null);
    }

    public static void loadServletNoUpm4NCHOME(Project project) {
        loadServletNoUpm(project, null);
    }

    /**
     * @param project
     * @param module  可空
     * @param from    {@link NCCActionInfoVO#FROM_SRC 等}
     */
    public static void loadServletNoUpm(Project project, Module module) {
        com.intellij.openapi.application.ApplicationManager.getApplication()
                .runReadAction(() -> loadServletNoUpm0(project, module));
    }

    private static void loadServletNoUpm0(Project project, Module module) {
        int from = module == null ? NCCActionInfoVO.FROM_HOME : NCCActionInfoVO.FROM_SRC;
        if (module == null) {
            Module[] ms = ModuleManager.getInstance(project).getModules();
            final LibraryTable.ModifiableModel model =
                    LibraryTablesRegistrar.getInstance().getLibraryTable(project).getModifiableModel();
            for (Module m : ms) {
                LibraryEx library = (LibraryEx) model.getLibraryByName(ProjectNCConfigUtil.LIB_Middleware_Library);
                if (library != null) {
                    module = m;
                    break;
                }
            }
        }

        GlobalSearchScope scope = null;
        if (from == NCCActionInfoVO.FROM_HOME) {
            try {
                scope = new ProjectAndLibrariesScope(project, true);
            } catch (Throwable exception) {
                scope = new ProjectAndLibrariesScope(project);
            }
        } else {
            scope = module.getModuleScope(false);
        }

        if (scope == null) {
            scope = module.getModuleWithLibrariesScope();
        }

        Collection<PsiReferenceList> refList = JavaSuperClassNameOccurenceIndex.getInstance()
                .get("IHttpServletAdaptor"
                        , project
                        , scope
                );

        if (CollUtil.isEmpty(refList)) {
            return;
        }

        RequestMappingItemProvider.ALL_ACTIONS.putIfAbsent(RequestMappingItemProvider.getKey(project)
                , new ConcurrentHashMap<>(30000));
        Map<String, NCCActionInfoVO> actionInfoVOMap =
                RequestMappingItemProvider.ALL_ACTIONS.get(RequestMappingItemProvider.getKey(project));
        for (PsiReferenceList ref : refList) {
            PsiElement pe = ref.getParent().getParent();
            if (!(pe instanceof ClsFileImpl)) {
                continue;
            }

            ClsFileImpl pclz = (ClsFileImpl) pe;
            PsiElement[] cs = pclz.getChildren();
            if (CollUtil.isEmpty(cs)) {
                continue;
            }

            String moduleName = from == NCCActionInfoVO.FROM_SRC ? module.getName() : getNCModule(pclz);

            for (PsiElement pce : cs) {
                if (!(pce instanceof PsiClass)) {
                    continue;
                }

                PsiClass clz = (PsiClass) pce;

                if (!hasInterface(clz, "nc.bs.framework.adaptor.IHttpServletAdaptor")) {
                    continue;
                }

                NCCActionInfoVO v = actionInfoVOMap.values().stream()
                        .filter(vo -> clz.getQualifiedName().equals(vo.getClazz())).findAny().orElse(null);
                if (v != null && v.getType() == NCCActionInfoVO.TYPE_UPM) {
                    continue;
                }

                if (v == null) {
                    v = new NCCActionInfoVO();
                    v.setClazz(clz.getQualifiedName());
                    v.setType(NCCActionInfoVO.TYPE_SERVLET);
                    v.setFrom(from);
                    v.setName(ProjectNCConfigUtil.getNCClientIP() + ":"
                            + StringUtil.get(ProjectNCConfigUtil.getNCClientPort(), "80")
                            + "/servlet/~" + moduleName + "/" + v.getClazz());
                    v.setLabel("非UPM配置的Servlet");
                    v.setXmlPath("");
                    actionInfoVOMap.put(v.getName(), v);
                }

                v.setXmlPath(v.getXmlPath() + " " + pclz.getVirtualFile().getPath());
                v.getNavigatables().add(clz);
            }
        }
    }

    public static String getNCModule(ClsFileImpl pclz) {
        String name = "未知模块";
        if (pclz == null) {
            return name;
        }

        if (pclz.getVirtualFile() == null) {
            return name;
        }

        name = pclz.getVirtualFile().getPath();
        name = StringUtil.replaceAll(name, "/", ".");
        name = StringUtil.replaceAll(name, "\\", ".");
        name = name.substring(2);

        String nchome = ProjectNCConfigUtil.getNCHomePath();
        nchome = StringUtil.replaceAll(nchome, "/", ".");
        nchome = StringUtil.replaceAll(nchome, "\\", ".");
        nchome = nchome.substring(2);

        name = StringUtil.removeStart(name, nchome).trim();
        if (name.charAt(0) == '.') {
            name = name.substring(1);
        }

        return StringUtil.splitToArray(name, '.')[1];
    }

    public static PsiAnnotation getClassAnnotation(PsiClass psiClass, String... qualifiedName) {
        if (qualifiedName.length < 1) {
            return null;
        }
        PsiAnnotation annotation;
        for (String name : qualifiedName) {
            annotation = psiClass.getAnnotation(name);
            if (annotation != null) {
                return annotation;
            }
        }
        List<PsiClass> classes = new ArrayList<>();
        classes.add(psiClass.getSuperClass());
        classes.addAll(Arrays.asList(psiClass.getInterfaces()));
        for (PsiClass superPsiClass : classes) {
            if (superPsiClass == null) {
                continue;
            }
            PsiAnnotation classAnnotation = getClassAnnotation(superPsiClass, qualifiedName);
            if (classAnnotation != null) {
                return classAnnotation;
            }
        }
        return null;
    }

    public static boolean hasInterface(PsiClass clz, String interfacename) {
        if (clz == null) {
            return false;
        }

        PsiClassType[] is = clz.getImplementsListTypes();
        if (is == null) {
            return false;
        }

        for (PsiClassType it : is) {
            if (it.getPsiContext() instanceof ClsJavaCodeReferenceElementImpl) {
                if (interfacename.equals(((ClsJavaCodeReferenceElementImpl) it.getPsiContext()).getCanonicalText())) {
                    return true;
                }
            }
        }

        return false;
    }

    public static void loadJaxrs(Project project, Module module) {
        com.intellij.openapi.application.ApplicationManager.getApplication()
                .runReadAction(() -> loadJaxrs0(project, module));
    }

    //TODO
    private static void loadJaxrs0(Project project, Module module) {
        if (project != null) {
            return;
        }

        int from = module == null ? NCCActionInfoVO.FROM_HOME : NCCActionInfoVO.FROM_SRC;
        if (module == null) {
            Module[] ms = ModuleManager.getInstance(project).getModules();
            final LibraryTable.ModifiableModel model =
                    LibraryTablesRegistrar.getInstance().getLibraryTable(project).getModifiableModel();
            for (Module m : ms) {
                LibraryEx library = (LibraryEx) model.getLibraryByName(ProjectNCConfigUtil.LIB_Middleware_Library);
                if (library != null) {
                    module = m;
                    break;
                }
            }
        }

        GlobalSearchScope scope = null;
        if (from == NCCActionInfoVO.FROM_HOME) {
            try {
                scope = new ProjectAndLibrariesScope(project, true);
            } catch (Throwable exception) {
                scope = new ProjectAndLibrariesScope(project);
            }
        } else {
            scope = module.getModuleScope(false);
        }

        if (scope == null) {
            scope = module.getModuleWithLibrariesScope();
        }

        Collection<PsiAnnotation> refList = JavaAnnotationIndex.getInstance().get(
                "Path"
                , project
                , scope
        );

        if (CollUtil.isEmpty(refList)) {
            return;
        }

        RequestMappingItemProvider.ALL_ACTIONS.putIfAbsent(RequestMappingItemProvider.getKey(project)
                , new ConcurrentHashMap<>(30000));
        Map<String, NCCActionInfoVO> actionInfoVOMap =
                RequestMappingItemProvider.ALL_ACTIONS.get(RequestMappingItemProvider.getKey(project));
        for (PsiAnnotation ref : refList) {
            PsiElement pe = ref.getParent().getParent();
            if (!(pe instanceof PsiClass)) {
                continue;
            }

            PsiClass pclz = (PsiClass) pe;
            PsiElement[] cs = pclz.getChildren();
            if (CollUtil.isEmpty(cs)) {
                continue;
            }

            String moduleName = from == NCCActionInfoVO.FROM_SRC ? module.getName() : getNCModule((ClsFileImpl) pclz);

            for (PsiElement pce : cs) {
                if (!(pce instanceof PsiClass)) {
                    continue;
                }

                PsiClass clz = (PsiClass) pce;

                if (!hasInterface(clz, "nc.bs.framework.adaptor.IHttpServletAdaptor")) {
                    continue;
                }

                NCCActionInfoVO v = actionInfoVOMap.values().stream()
                        .filter(vo -> clz.getQualifiedName().equals(vo.getClazz())).findAny().orElse(null);
                if (v != null && v.getType() == NCCActionInfoVO.TYPE_UPM) {
                    continue;
                }

                if (v == null) {
                    v = new NCCActionInfoVO();
                    v.setClazz(clz.getQualifiedName());
                    v.setType(NCCActionInfoVO.TYPE_JAXRS);
                    v.setFrom(from);
                    v.setName(ProjectNCConfigUtil.getNCClientIP() + ":"
                            + StringUtil.get(ProjectNCConfigUtil.getNCClientPort(), "80")
                            + "/servlet/~" + moduleName + "/" + v.getClazz());
                    v.setLabel("非UPM配置的Servlet");
                    v.setXmlPath("");
                    actionInfoVOMap.put(v.getName(), v);
                }

                v.setXmlPath(v.getXmlPath() + " " + ActionResultListTable.getPsiClassFile(pclz).getPath());
                v.getNavigatables().add(clz);
            }
        }
    }

    public static void loadSpringmvc(Project project, Module module) {
        com.intellij.openapi.application.ApplicationManager.getApplication()
                .runReadAction(() -> loadSpringmvc0(project, module));
    }

    //TODO
    private static void loadSpringmvc0(Project project, Module module) {
        if (project != null) {
            return;
        }

        int from = module == null ? NCCActionInfoVO.FROM_HOME : NCCActionInfoVO.FROM_SRC;
        if (module == null) {
            Module[] ms = ModuleManager.getInstance(project).getModules();
            final LibraryTable.ModifiableModel model =
                    LibraryTablesRegistrar.getInstance().getLibraryTable(project).getModifiableModel();
            for (Module m : ms) {
                LibraryEx library = (LibraryEx) model.getLibraryByName(ProjectNCConfigUtil.LIB_Middleware_Library);
                if (library != null) {
                    module = m;
                    break;
                }
            }
        }

        GlobalSearchScope scope = null;
        if (from == NCCActionInfoVO.FROM_HOME) {
            try {
                scope = new ProjectAndLibrariesScope(project, true);
            } catch (Throwable exception) {
                scope = new ProjectAndLibrariesScope(project);
            }
        }else{
            scope = module.getModuleScope(false);
        }

        if (scope == null) {
            scope = module.getModuleWithLibrariesScope();
        }

        Collection<PsiReferenceList> refList = JavaSuperClassNameOccurenceIndex.getInstance()
                .get("IHttpServletAdaptor"
                        , project
                        , scope
                );

        if (CollUtil.isEmpty(refList)) {
            return;
        }

        RequestMappingItemProvider.ALL_ACTIONS.putIfAbsent(RequestMappingItemProvider.getKey(project)
                , new ConcurrentHashMap<>(30000));
        Map<String, NCCActionInfoVO> actionInfoVOMap =
                RequestMappingItemProvider.ALL_ACTIONS.get(RequestMappingItemProvider.getKey(project));
        for (PsiReferenceList ref : refList) {
            PsiElement pe = ref.getParent().getParent();
            if (!(pe instanceof ClsFileImpl)) {
                continue;
            }

            ClsFileImpl pclz = (ClsFileImpl) pe;
            PsiElement[] cs = pclz.getChildren();
            if (CollUtil.isEmpty(cs)) {
                continue;
            }

            String moduleName = from == NCCActionInfoVO.FROM_SRC ? module.getName() : getNCModule(pclz);

            for (PsiElement pce : cs) {
                if (!(pce instanceof PsiClass)) {
                    continue;
                }

                PsiClass clz = (PsiClass) pce;

                if (!hasInterface(clz, "nc.bs.framework.adaptor.IHttpServletAdaptor")) {
                    continue;
                }

                NCCActionInfoVO v = actionInfoVOMap.values().stream()
                        .filter(vo -> clz.getQualifiedName().equals(vo.getClazz())).findAny().orElse(null);
                if (v != null && v.getType() == NCCActionInfoVO.TYPE_UPM) {
                    continue;
                }

                if (v == null) {
                    v = new NCCActionInfoVO();
                    v.setClazz(clz.getQualifiedName());
                    v.setType(NCCActionInfoVO.TYPE_SERVLET);
                    v.setFrom(from);
                    v.setName(ProjectNCConfigUtil.getNCClientIP() + ":"
                            + StringUtil.get(ProjectNCConfigUtil.getNCClientPort(), "80")
                            + "/servlet/~" + moduleName + "/" + v.getClazz());
                    v.setLabel("非UPM配置的Servlet");
                    v.setXmlPath("");
                    actionInfoVOMap.put(v.getName(), v);
                }

                v.setXmlPath(v.getXmlPath() + " " + pclz.getVirtualFile().getPath());
                v.getNavigatables().add(clz);
            }
        }
    }

    /**
     * jar
     *
     * @param project
     * @param file
     */
    public static void loadYyconfigmodulesDir4Jar(Project project, File jar) {
        JarFile jf = null;
        try {
            jf = new JarFile(jar);
            Enumeration<JarEntry> es = jf.entries();
            if (es == null) {
                return;
            }

            Map<JarEntry, InputStream> actions = new HashMap<>(100);
            Map<InputStream, JarEntry> input2JarEntityMap = new HashMap<>(100);
            while (es.hasMoreElements()) {
                JarEntry e = es.nextElement();
                String name = e.getName().toLowerCase();
                if (StringUtil.startsWith(name, "yyconfig/modules/") && StringUtil.endsWith(name, ".xml")) {
                    if (StringUtil.contains(name, "/action/")) {
                        actions.put(e, jf.getInputStream(e));
                    } else if (StringUtil.contains(name, "/authorize/")) {
                        input2JarEntityMap.put(jf.getInputStream(e), e);
                    }
                }
            }

            if (CollUtil.isEmpty(actions)) {
                return;
            }

            Map<String, String> action2AppcodeMap = new HashMap<>(100);
            Map<String, String> action2AuthXmlPathMap = new HashMap<>(100);
            for (InputStream authorizeXml : input2JarEntityMap.keySet()) {
                try {
                    Document document = XmlUtil.xmlFile2Document2(authorizeXml);
                    Element rootElement = XmlUtil.getRootElement(document);
                    if (rootElement == null) {
                        continue;
                    }
                    NodeList authorizeNode = rootElement.getElementsByTagName("authorize");
                    if (authorizeNode == null || authorizeNode.getLength() < 1) {
                        continue;
                    }
                    Node authorizeNode1 = authorizeNode.item(0);
                    String appcode = XmlUtil.elementText((Element) authorizeNode1, "appcode");
                    Element actionsElement = XmlUtil.getElement((Element) authorizeNode1, "actions");
                    if (actionsElement == null) {
                        continue;
                    }
                    NodeList actionNodeList = actionsElement.getElementsByTagName("action");
                    if (actionNodeList == null) {
                        continue;
                    }
                    for (int i = 0; i < actionNodeList.getLength(); i++) {
                        String textContent = actionNodeList.item(i).getTextContent();
                        if (StringUtil.isBlank(textContent)) {
                            continue;
                        }

                        action2AppcodeMap.put(textContent, appcode);
                        action2AuthXmlPathMap.put(textContent, jar.getPath()
                                + File.separatorChar
                                + input2JarEntityMap.get(authorizeXml).getName());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            List<NCCActionInfoVO> vos = new ArrayList<>(100);
            actions.forEach((jarEntity, input) -> {
                try {
                    byte[] bytes = IoUtil.readBytes(input);

                    List<String> lines = IoUtil.readUtf8Lines(new ByteArrayInputStream(bytes), new ArrayList<>());

                    Document document = XmlUtil.xmlFile2Document2(new ByteArrayInputStream(
                            //  lines.stream().collect(Collectors.joining("")).getBytes(StandardCharsets.UTF_8)
                            bytes
                    ));
                    Element rootElement = XmlUtil.getRootElement(document);
                    NodeList actionList = rootElement.getElementsByTagName("action");
                    if (actionList == null) {
                        return;
                    }

                    for (int i = 0; i < actionList.getLength(); i++) {
                        Node item = actionList.item(i);
                        NCCActionInfoVO vo = XmlUtil.xmlToBean(item, NCCActionInfoVO.class);
                        if (StringUtil.isBlank(vo.getClazz())
                                || StringUtil.isBlank(vo.getName())) {
                            continue;
                        }
                        vo.setAppcode(action2AppcodeMap.get(vo.getName()));
                        vo.setAuthPath(action2AuthXmlPathMap.get(vo.getName()));
                        vo.setXmlPath(jar.getPath() + File.separatorChar + jarEntity.getName());
                        vo.setProject((project != null ? project.getBasePath() : null));
                        vo.setName(ProjectNCConfigUtil.getNCClientIP() + ":"
                                + StringUtil.get(ProjectNCConfigUtil.getNCClientPort(), "80")
                                + "/nccloud/" + vo.getName().replace('.', '/') + ".do");

                        matchRowColumn(lines, vo);

                        vos.add(vo);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            //注册到缓存里
            set2Cache(project, vos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IoUtil.close(jf);
        }
    }

    private static void matchRowColumn(List<String> lines, NCCActionInfoVO vo) {
        for (int x = 0; x < lines.size(); x++) {
            if (lines.get(x).contains(vo.getClazz())) {
                vo.setRow(x);
                vo.setColumn(lines.get(x).indexOf(vo.getClazz()));
                break;
            }
        }
    }


}
