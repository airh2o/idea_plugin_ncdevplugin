package com.air.nc5dev.util;

import cn.hutool.core.io.FileUtil;
import com.air.nc5dev.nccrequstsearch.RequestMappingItemProvider;
import com.air.nc5dev.util.idea.ProjectUtil;
import com.air.nc5dev.vo.NCCActionInfoVO;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
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
        }
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
                vo.setName("/service/" + c.getAttribute("name"));
                vo.setLabel(StringUtil.get(c.getAttribute("label")) + "  -UPM文件配置的Servlet");
                vo.setAppcode("UPM文件配置的Servlet");
                vo.setProject(project != null ? project.getBasePath() : null);
                vo.setFrom(from);
                vo.setType(NCCActionInfoVO.TYPE_UPM);

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
                        vo.setProject("NCHOME:" + (project != null ? project.getBasePath() : null));

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
