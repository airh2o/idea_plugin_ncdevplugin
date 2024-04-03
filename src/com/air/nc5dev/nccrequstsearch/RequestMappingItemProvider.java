package com.air.nc5dev.nccrequstsearch;

import com.air.nc5dev.util.CollUtil;
import com.air.nc5dev.util.NCCActionRefreshUtil;
import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.util.V;
import com.air.nc5dev.util.idea.LogUtil;
import com.air.nc5dev.util.idea.ProjectUtil;
import com.air.nc5dev.vo.NCCActionInfoVO;
import com.google.common.base.Joiner;
import com.intellij.ide.actions.searcheverywhere.FoundItemDescriptor;
import com.intellij.ide.util.gotoByName.ChooseByNameBase;
import com.intellij.ide.util.gotoByName.ChooseByNameItemProvider;
import com.intellij.ide.util.gotoByName.ChooseByNamePopup;
import com.intellij.ide.util.gotoByName.ChooseByNameViewModel;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.util.Processor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;


/**
 * <br>
 * <br>
 * <br>
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

    public List<NCCActionInfoVO> search(Project project, String inputStr, boolean everwhere) {
        return search(project, inputStr, everwhere, null, null);
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
    public List<NCCActionInfoVO> search(Project project, String inputStr, boolean everwhere
            , ProgressIndicator progressIndicator
            , Processor consumer) {
        progressIndicator = V.get(progressIndicator, new NullProgressIndicator());
        consumer = V.get(consumer, new NullProcessor());

        if (progressIndicator.isCanceled()) {
            return EMPTY_LIST;
        }

        progressIndicator.setText("扫描工程项目action中...");
        initScan(project);
        progressIndicator.setText("正在搜索中...");

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
        } else if (inputStr.charAt(1) == '!') {
            everwhere = false;
            inputStr = inputStr.charAt(0) + inputStr.substring(2);
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

        Set<String> keys = all.keySet();
        for (String key : keys) {
            progressIndicator.setText("正在搜索中..." + key);
            if (progressIndicator.isCanceled()) {
                return matchs;
            }

            Map<String, NCCActionInfoVO> vs = all.get(key);
            Collection<NCCActionInfoVO> acs = vs.values().stream()
                    .sorted((a, b) -> a.getName() != null ? a.getName().compareTo(b.getName())
                            : StringUtil.get(a.getLabel()).compareTo(b.getLabel()))
                    .collect(Collectors.toList());
            for (NCCActionInfoVO vo : acs) {
                progressIndicator.setText("正在搜索中..." + vo.getName());
                if (progressIndicator.isCanceled()) {
                    return matchs;
                }

                String matchStr = like(vo.getName(), vo, str);
                if (matchStr != null) {
                    matchs.add(vo);

                    consumer.process(new FoundItemDescriptor(vo, vo.getScore()));
                }
            }
        }

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
                && StringUtil.startsWith(vo.getProject(), "NCHOME")) {
            return null;
        }

        if (StringUtil.startsWith(str, "nccloud")) {
            str = StringUtil.removeStart(str, "nccloud");
        }
        if (StringUtil.endsWith(str, ".do")) {
            str = StringUtil.removeEnd(str, ".do");
        }

        int score = 0;
        String urlName = vo.getName().toLowerCase().indexOf("/nccloud/") > -1 ?
                vo.getName().toLowerCase().substring(vo.getName().toLowerCase().indexOf("/nccloud/") + 9)
                : vo.getName().toLowerCase();
        urlName = StringUtil.replaceAll(urlName, "/", ".");
        String className = vo.getClazz().toLowerCase();
        String label = vo.getLabel() == null ? "" : vo.getLabel().toLowerCase();
        boolean anyMatch = str.charAt(0) == '^';
        if (anyMatch) {
            str = str.substring(1);
        }

        String match = null;
        if (urlName.equals(str)) {
            score = 20;
            match = urlName;
        } else if (className.equals(str)) {
            score = 20;
            match = className;
        } else if (label.equals(str)) {
            score = 20;
            match = label;
        } else {
            String[] inputStrs = StringUtil.split(str, ".");

            int i = like(urlName, inputStrs, anyMatch);
            if (i > 0) {
                match = urlName;
                score += i;
            }
            if ((i = like(className, inputStrs, anyMatch)) > 0) {
                match = className;
                score += i;
            }
            if ((i = like(label, inputStrs, anyMatch)) > 0) {
                match = label;
                score += i;
            } else {
            }

            if (str.contains("来自:NCHOME")) {
                if (vo.getFrom() != NCCActionInfoVO.FROM_HOME) {
                    match = null;
                    score = 0;
                } else {
                    if (match == null) {
                        match = str;
                    }
                    score += vo.getFrom();
                }
            }

            if (str.contains("来自:工程项目")) {
                if (vo.getFrom() != NCCActionInfoVO.FROM_SRC) {
                    match = null;
                    score = 0;
                } else {
                    if (match == null) {
                        match = str;
                    }
                    score += vo.getFrom();
                }
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

    public int like(String match, String[] ss, boolean anyMatch) {
        int i = 0;
        if (ss == null || StringUtil.isBlank(match)) {
            return i;
        }

        String full = Joiner.on('.').skipNulls().join(ss);
        if (match.equals(full)) {
            return 15;
        }

        if (match.startsWith(full) || match.endsWith(full) || match.contains(full)) {
            return 10;
        }

      //  List<String> mm = StringUtil.split(match, '.');
        HashSet<String> set = CollUtil.newHashSet(ss);
        int matchsize = 0;
        for (String s : ss) {
            if (match.contains(s)) {
                i += 2;
                ++matchsize;
            }
        }

        if (!anyMatch && matchsize < ss.length) {
            return 0;
        }

        if (i > 0) {
            return i;
        }

//        for (String s : ss) {
//            if (StringUtil.contains(match, s)) {
//                return 1;
//            }
//        }

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


    public static class NullProgressIndicator implements ProgressIndicator {

        @Override
        public void start() {

        }

        @Override
        public void stop() {

        }

        @Override
        public boolean isRunning() {
            return true;
        }

        @Override
        public void cancel() {

        }

        @Override
        public boolean isCanceled() {
            return false;
        }

        @Override
        public void setText(@NlsContexts.ProgressText String s) {

        }

        @Override
        public @NlsContexts.ProgressText String getText() {
            return "";
        }

        @Override
        public void setText2(@NlsContexts.ProgressDetails String s) {

        }

        @Override
        public @NlsContexts.ProgressDetails String getText2() {
            return null;
        }

        @Override
        public double getFraction() {
            return 0;
        }

        @Override
        public void setFraction(double v) {

        }

        @Override
        public void pushState() {

        }

        @Override
        public void popState() {

        }

        @Override
        public boolean isModal() {
            return false;
        }

        @Override
        public @NotNull
        ModalityState getModalityState() {
            return null;
        }

        @Override
        public void setModalityProgress(@Nullable ProgressIndicator progressIndicator) {

        }

        @Override
        public boolean isIndeterminate() {
            return false;
        }

        @Override
        public void setIndeterminate(boolean b) {

        }

        @Override
        public void checkCanceled() {

        }

        @Override
        public boolean isPopupWasShown() {
            return false;
        }

        @Override
        public boolean isShowing() {
            return false;
        }
    }

    public static class NullProcessor implements Processor {
        @Override
        public boolean process(Object o) {
            return true;
        }
    }

}
