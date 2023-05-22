package com.air.nc5dev.nccrequstsearch;

import com.air.nc5dev.util.CollUtil;
import com.air.nc5dev.util.NCCActionRefreshUtil;
import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.util.idea.LogUtil;
import com.air.nc5dev.vo.NCCActionInfoVO;
import com.intellij.ide.util.gotoByName.ChooseByNameBase;
import com.intellij.ide.util.gotoByName.ChooseByNameItemProvider;
import com.intellij.ide.util.gotoByName.ChooseByNamePopup;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.util.Processor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


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
@AllArgsConstructor
@NoArgsConstructor
public class RequestMappingItemProvider implements ChooseByNameItemProvider {
    /**
     * 所有的 url信息, key=project getBasePath(),value={key=url完整地址，value=url信息}
     */
    public static final Map<String, Map<String, NCCActionInfoVO>> ALL_ACTIONS = new ConcurrentHashMap<>(3_0000);
    //是否已经扫描了 nchome信息
    private static volatile boolean inited = false;
    private static final List<NCCActionInfoVO> EMPTY_LIST = Collections.unmodifiableList(CollUtil.emptyList());

    RequestMappingModel requestMappingModel;

    public List<String> filterNames(@NotNull ChooseByNameBase chooseByNameBase
            , @NotNull String[] names, @NotNull String inputStr) {
        return new ArrayList<>();
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

        List<NCCActionInfoVO> vos = search(chooseByNameBase.getProject(), inputStr, everywhere, cancelled, processor);

        cancelled.checkCanceled();
        if (!com.intellij.util.containers.ContainerUtil.process(vos, processor)) {
            return false;
        }

        return vos.isEmpty();
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
        initScan(project);

        if (StringUtil.isBlank(inputStr)) {
            return EMPTY_LIST;
        }

        if (ALL_ACTIONS.isEmpty()) {
            return EMPTY_LIST;
        }

        Map<String, Map<String, NCCActionInfoVO>> all = new HashMap<>();
        Map<String, NCCActionInfoVO> map = ALL_ACTIONS.get(project.getBasePath());
        all.put(project.getBasePath(), map);

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

        if (!everwhere) {
            if (project == null) {
                return EMPTY_LIST;
            }
            Map<String, NCCActionInfoVO> map2 = new HashMap<>();

            Set<String> keys = map.keySet();
            for (String key : keys) {
                if (map.get(key).getFrom() == NCCActionInfoVO.FROM_SRC) {
                    map2.put(key, map.get(key));
                }
            }

            if (CollUtil.isEmpty(map2)) {
                return EMPTY_LIST;
            }

            all.put(project.getBasePath(), map2);
        }

        //匹配
        String txt = StringUtil.replaceChars(inputStr, "/", ".");
        txt = StringUtil.replaceAll(txt, " ", ".");
        txt = StringUtil.replaceChars(txt, "\\", ".");
        txt = txt.toLowerCase();
        final String str = txt;

        final List<NCCActionInfoVO> matchs = new ArrayList<>(1000);
        all.forEach((k, v) -> {
            v.forEach((name, vo) -> {
                if (like(name, vo, str) > 0) {
                    matchs.add(vo);
                }
            });
        });

        matchs.sort((c1, c2) -> c1.getScore() - c2.getScore());
        return matchs;
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
    private int like(String name, NCCActionInfoVO vo, String str) {
        if (requestMappingModel.onlySearchPorjectUrl && vo.getProject().startsWith("NCHOME")) {
            return -1;
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

        if (urlName.equals(str) || className.equals(str) || label.equals(str)) {
            score = 10000;
        } else {
            String[] inputStrs = StringUtil.split(str, ".");
            for (String input : inputStrs) {
                if (StringUtil.contains(urlName, input)) {
                    score += 10;
                } else if (StringUtil.contains(className, input)) {
                    ++score;
                } else if (StringUtil.contains(label, input)) {
                    ++score;
                } else {
                    score = 0;
                    break;
                }
            }
        }

        score += vo.getFrom();

        vo.setScore(score);
        return score;
    }

    /**
     * 初始化 扫描！
     *
     * @param chooseByNameBase
     */
    private void initScan(Project p) {
        if (p == null) {
            return;
        }

        if (inited) {
            //载入项目的，放最后，优先级最高
            requestMappingModel.setInfo("正在扫描项目src源码的action列表...");
            NCCActionRefreshUtil.reloadProjectAction(p);
            requestMappingModel.setInfo("搜索完成");
            return;
        }

        inited = true;
        //没有搜索过 NCHOME，那么搜索一次，此后不搜索
        requestMappingModel.setInfo("正在扫描整个NCHOME的action列表...");
        LogUtil.infoAndHide("正在初始化扫描整个NCHOME(hotwebs下nccloud的,请不要移动jar到modules里面!)的action列表...");
        NCCActionRefreshUtil.loadNCHome(p);
        //载入项目的，放最后，优先级最高
        requestMappingModel.setInfo("正在扫描项目src源码的action列表...");
        NCCActionRefreshUtil.reloadProjectAction(p);

        requestMappingModel.setInfo("搜索完成");
    }
}
