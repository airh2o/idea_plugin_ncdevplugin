package com.air.nc5dev.nccrequstsearch;

import com.air.nc5dev.util.CollUtil;
import com.air.nc5dev.util.NCCActionRefreshUtil;
import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.util.idea.LogUtil;
import com.air.nc5dev.util.idea.ProjectUtil;
import com.air.nc5dev.vo.NCCActionInfoVO;
import com.google.common.base.Joiner;
import com.intellij.ide.util.gotoByName.ChooseByNameBase;
import com.intellij.ide.util.gotoByName.ChooseByNameItemProvider;
import com.intellij.ide.util.gotoByName.ChooseByNamePopup;
import com.intellij.ide.util.gotoByName.ChooseByNameViewModel;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.util.Processor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * </br>
 * </br>
 * </br>
 *
 * @author 唐粟 Email:209308343@qq.com
 * @date 2022/2/15 0015 11:06
 * @project
 * @Version
 */
@Getter
public class RequestMappingItemProvider implements ChooseByNameItemProvider {
    /**
     * 所有的 url信息, key=project getBasePath(),value={key=url完整地址，value=url信息}
     */
    public static final Map<String, Map<String, NCCActionInfoVO>> ALL_ACTIONS = new ConcurrentHashMap<>(3_0000);
    /**
     * 所有的 url信息, key=project getBasePath(),value=是否已经扫描了 nchome信息
     */
    private static Map<String, AtomicBoolean> INITED_MAP = new ConcurrentHashMap<>();
    private static final List<NCCActionInfoVO> EMPTY_LIST = Collections.unmodifiableList(CollUtil.emptyList());
    static volatile RequestMappingItemProvider me;
    RequestMappingModel requestMappingModel;

    public RequestMappingItemProvider(RequestMappingModel requestMappingModel) {
        this.requestMappingModel = requestMappingModel;
        me = this;
    }

    public RequestMappingItemProvider() {
        me = this;
    }

    public static RequestMappingItemProvider getMe() {
        if (me == null) {
            me = new RequestMappingItemProvider();
        }
        return me;
    }

    public List<String> filterNames(@NotNull ChooseByNameBase chooseByNameBase
            , @NotNull String[] names, @NotNull String inputStr) {
        return new ArrayList<>();
    }

    public static String getKey(Project project) {
        if (project == null) {
            return "";
        }

        return project.getBasePath();
    }

    /**
     * 用户输入文本后，执行搜索！
     *
     * @param chooseByNameBase
     * @param inputStr
     * @param b
     * @param cancelled
     * @param processor
     * @return
     */
    public boolean filterElements(@NotNull ChooseByNameBase chooseByNameBase, @NotNull String inputStr
            , boolean everywhere, @NotNull ProgressIndicator cancelled
            , @NotNull Processor<Object> processor) {
        if (chooseByNameBase.getProject() != null) {
            chooseByNameBase.getProject().putUserData(ChooseByNamePopup.CURRENT_SEARCH_PATTERN, inputStr);
        }

       /* GlobalSearchScope searchScope = FindSymbolParameters.searchScopeFor(chooseByNameBase.getProject(),
       everywhere);
        FindSymbolParameters parameters = new FindSymbolParameters(inputStr, inputStr, searchScope, null);*/

        List<NCCActionInfoVO> vos = search(chooseByNameBase, inputStr, everywhere, cancelled, processor);
        cancelled.checkCanceled();
        if (!com.intellij.util.containers.ContainerUtil.process(vos, processor)) {
            return false;
        }

        return vos.isEmpty();
    }

    public boolean filterElements(com.intellij.ide.util.gotoByName.ChooseByNameViewModel chooseByNameBase
            , java.lang.String inputStr, boolean everywhere, com.intellij.openapi.progress.ProgressIndicator cancelled
            , com.intellij.util.Processor processor) {
        if (chooseByNameBase.getProject() != null) {
            chooseByNameBase.getProject().putUserData(ChooseByNamePopup.CURRENT_SEARCH_PATTERN, inputStr);
        }

        ProjectUtil.setProject(chooseByNameBase.getProject());
        List<NCCActionInfoVO> vos = search(chooseByNameBase.getProject(), inputStr, everywhere, cancelled, processor);

        cancelled.checkCanceled();
        if (!com.intellij.util.containers.ContainerUtil.process(vos, processor)) {
            return false;
        }

        return vos.isEmpty();
    }

    public List<String> filterNames(ChooseByNameViewModel chooseByNameViewModel
            , String[] names, String pattern) {
        return new ArrayList<>();
    }

    /**
     * 搜索
     *
     * @param chooseByNameBase
     * @param inputStr
     * @param everwhere
     * @param cancelled
     * @param processor
     * @return
     */
    private List<NCCActionInfoVO> search(Project project, String inputStr
            , boolean everwhere, ProgressIndicator cancelled, Processor<Object> processor) {
        return search(project, inputStr, everwhere);
    }


    /**
     * 搜索
     *
     * @param chooseByNameBase
     * @param inputStr
     * @param everwhere
     * @param cancelled
     * @param processor
     * @return
     */
    public List<NCCActionInfoVO> search(Project project, String inputStr, boolean everwhere) {
        initScan(project);

        if (StringUtil.isBlank(inputStr)) {
            return EMPTY_LIST;
        }

        if (ALL_ACTIONS.isEmpty()) {
            return EMPTY_LIST;
        }

        Map<String, Map<String, NCCActionInfoVO>> all = new HashMap<>();
        Map<String, NCCActionInfoVO> map = ALL_ACTIONS.get(getKey(project));
        all.put(getKey(project), map);

        if (CollUtil.isEmpty(map)) {
            return EMPTY_LIST;
        }

        inputStr = inputStr.trim();
        if (inputStr.charAt(0) == '!') {
            everwhere = false;
            inputStr = inputStr.substring(1);
        }

        if (StringUtil.isBlank(inputStr)) {
            return EMPTY_LIST;
        }

        if (filterProjectUrls(project, everwhere, all, map)) {
            return EMPTY_LIST;
        }

        //匹配
        final String str = warpKey(inputStr);

        final List<NCCActionInfoVO> matchs = new ArrayList<>(1000);
        all.forEach((k, v) -> {
            v.forEach((name, vo) -> {
                String matchStr = like(name, vo, str);
                if (matchStr != null) {
                    matchs.add(vo);
                }
            });
        });

        matchs.sort((c1, c2) -> c2.getScore() - c1.getScore());
        return matchs;
    }

    private boolean filterProjectUrls(Project project, boolean everwhere,
                                      Map<String, Map<String, NCCActionInfoVO>> all, Map<String, NCCActionInfoVO> map) {
        if (!everwhere) {
            if (project == null) {
                return true;
            }
            Map<String, NCCActionInfoVO> map2 = new HashMap<>();

            Set<String> keys = map.keySet();
            for (String key : keys) {
                if (map.get(key).getFrom() == NCCActionInfoVO.FROM_SRC) {
                    map2.put(key, map.get(key));
                }
            }

            if (CollUtil.isEmpty(map2)) {
                return true;
            }

            all.put(getKey(project), map2);
        }
        return false;
    }


    /**
     * 搜索
     *
     * @param chooseByNameBase
     * @param inputStr
     * @param everywhere
     * @param cancelled
     * @param processor
     * @return
     */
    private List<NCCActionInfoVO> search(ChooseByNameBase chooseByNameBase, String inputStr
            , boolean everywhere, ProgressIndicator cancelled, Processor<Object> processor) {
        return search(chooseByNameBase.getProject(), inputStr, everywhere, cancelled, processor);
    }

    /**
     * 判断 搜索的字符串，在指定的 请求里的匹配权重分数
     *
     * @param name
     * @param vo
     * @param str
     * @return
     */
    private String like(String name, NCCActionInfoVO vo, String str) {
        if ((requestMappingModel != null && requestMappingModel.onlySearchPorjectUrl)
                && vo.getProject().startsWith("NCHOME")) {
            return null;
        }

        if (StringUtil.startsWith(str, "nccloud")) {
            str = StringUtil.removeStart(str, "nccloud");
        }
        if (StringUtil.endsWith(str, ".do")) {
            str = StringUtil.removeEnd(str, ".do");
        }

        int score = 0;
        String urlName = vo.getName().toLowerCase();
        String className = vo.getClazz().toLowerCase();
        String label = vo.getLabel() == null ? "" : vo.getLabel().toLowerCase();

        String match = null;
        if (urlName.equals(str)) {
            score = 10000;
            match = urlName;
        } else if (className.equals(str)) {
            score = 10000;
            match = className;
        } else if (label.equals(str)) {
            score = 10000;
            match = label;
        } else {
            String[] inputStrs = StringUtil.split(str, ".");
            int i = like(urlName, inputStrs);
            if (i > 0) {
                match = urlName;
                score += i;
            }
            if ((i = like(className, inputStrs)) > 0) {
                match = className;
                score += i;
            }
            if ((i = like(label, inputStrs)) > 0) {
                match = label;
                score += i;
            } else {
            }
        }

        if (score > 0) {
            score += vo.getFrom();
        }

        if (match != null) {
            vo.setScore(score);
        } else {
            vo.setScore(0);
        }

        return match;
    }

    public int like(String match, String[] ss) {
        int i = 0;
        if (ss == null || StringUtil.isBlank(match)) {
            return i;
        }

        String full = Joiner.on('.').skipNulls().join(ss);
        if (match.equals(full)) {
            return 100;
        }

        if (match.startsWith(full) || match.endsWith(full)) {
            return 50;
        }

        List<String> mm = StringUtil.split(match, '.');
        HashSet<String> set = CollUtil.newHashSet(ss);
        for (String s : mm) {
            if (set.contains(s)) {
                ++i;
            }
        }

        if (i > 0) {
            return i;
        }

        for (String s : ss) {
            if (!StringUtil.contains(match, s)) {
                return 1;
            }
        }

        return i;
    }

    /**
     * 初始化 扫描！
     *
     * @param chooseByNameBase
     */
    public void initScan(Project p) {
        if (p == null) {
            return;
        }

        synchronized (p) {
            AtomicBoolean inited = INITED_MAP.get(p.getBasePath());
            if (inited == null) {
                inited = new AtomicBoolean(false);
                INITED_MAP.put(p.getBasePath(), inited);
            }

            if (inited.get()) {
                //载入项目的，放最后，优先级最高
                info("正在扫描项目src源码的action列表...");
                NCCActionRefreshUtil.reloadProjectAction(p);
                info("项目本身扫描完成");
                return;
            }

            //没有搜索过 NCHOME，那么搜索一次，此后不搜索
            info("正在初始化扫描整个NCHOME(hotwebs下nccloud的,请不要移动jar到modules里面!)的action列表...");

            NCCActionRefreshUtil.loadNCHome(p);

            //载入项目的，放最后，优先级最高
            info("正在扫描项目src源码的action列表...");

            NCCActionRefreshUtil.reloadProjectAction(p);

            info("初始化扫描完成");

            inited.compareAndSet(false, true);
        }
    }

    public void info(String msg) {
        if (requestMappingModel != null) {
            requestMappingModel.setInfo(msg);
        }
        LogUtil.infoAndHide(msg);
    }

    public Collection<String> suggest(String inputStr, Project project, boolean everwhere, int size) {
        HashSet<String> re = new HashSet(size << 1);

        if (StringUtil.isBlank(inputStr)) {
            return re;
        }

        if (ALL_ACTIONS.isEmpty()) {
            return re;
        }

        Map<String, Map<String, NCCActionInfoVO>> all = new HashMap<>();
        Map<String, NCCActionInfoVO> map = ALL_ACTIONS.get(getKey(project));
        all.put(getKey(project), map);

        if (CollUtil.isEmpty(map)) {
            return re;
        }

        inputStr = inputStr.trim();
        if (inputStr.charAt(0) == '!') {
            everwhere = false;
            inputStr = inputStr.substring(1);
        }

        if (filterProjectUrls(project, everwhere, all, map)) {
            return re;
        }

        //匹配
        final String str = warpKey(inputStr);

        final List<NCCActionInfoVO> matchs = new ArrayList<>(size << 1);
        Set<String> keys = all.keySet();
        Map<String, NCCActionInfoVO> v;
        int i = 0;
        for (String k : keys) {
            v = all.get(k);
            Set<String> keys2 = v.keySet();
            for (String k2 : keys2) {
                String matchStr = like(k2, v.get(k2), str);
                if (matchStr != null) {
                    re.add(matchStr);

                    if (re.size() >= size) {
                        break;
                    }
                }
            }

            if (re.size() >= size) {
                break;
            }
        }

        return re;
    }

    private String warpKey(String inputStr) {
        String txt = StringUtil.replaceChars(inputStr, "/", ".");
        txt = StringUtil.replaceAll(txt, " ", ".");
        txt = StringUtil.replaceChars(txt, "\\", ".");
        txt = txt.toLowerCase();
        return txt;
    }
}
