package com.air.nc5dev.nccrequstsearch;

import com.air.nc5dev.util.CollUtil;
import com.air.nc5dev.util.NCCActionRefreshUtil;
import com.air.nc5dev.util.StringUtil;
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

    @NotNull
    @Override
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
    @Override
    public boolean filterElements(@NotNull ChooseByNameBase chooseByNameBase, @NotNull String inputStr
            , boolean everywhere, @NotNull ProgressIndicator cancelled
            , @NotNull Processor<Object> processor) {
        if (chooseByNameBase.getProject() != null) {
            chooseByNameBase.getProject().putUserData(ChooseByNamePopup.CURRENT_SEARCH_PATTERN, inputStr);
        }

       /* GlobalSearchScope searchScope = FindSymbolParameters.searchScopeFor(chooseByNameBase.getProject(), everywhere);
        FindSymbolParameters parameters = new FindSymbolParameters(inputStr, inputStr, searchScope, null);*/

        List<NCCActionInfoVO> vos = search(chooseByNameBase, inputStr, everywhere, cancelled, processor);

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
     * @param everywhere
     * @param cancelled
     * @param processor
     * @return
     */
    private List<NCCActionInfoVO> search(ChooseByNameBase chooseByNameBase, String inputStr
            , boolean everywhere, ProgressIndicator cancelled, Processor<Object> processor) {
        initScan(chooseByNameBase);

        if (StringUtil.isBlank(inputStr)) {
            return EMPTY_LIST;
        }

        if (ALL_ACTIONS.isEmpty()) {
            return EMPTY_LIST;
        }

        Project project = chooseByNameBase.getProject();
        Map<String, Map<String, NCCActionInfoVO>> all = ALL_ACTIONS;

        if (!everywhere) {
            if (project == null) {
                return EMPTY_LIST;
            }

            all = new HashMap<>();
            all.put(project.getBasePath(), ALL_ACTIONS.get(project.getBasePath()));
        }

        //匹配
        String txt = StringUtil.replaceChars(inputStr, '/', '.');
        txt = StringUtil.replaceAll(txt, " ", ".");
        txt = StringUtil.replaceChars(txt, '\\', '.');
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

        vo.setScore(score);
        return score;
    }

    /**
     * 初始化 扫描！
     *
     * @param chooseByNameBase
     */
    private void initScan(ChooseByNameBase chooseByNameBase) {
        Project p = chooseByNameBase.getProject();
        if (p == null) {
            return;
        }

        if (inited) {
            return;
        }

        inited = true;
        //没有搜索过 NCHOME，那么搜索一次，此后不搜索
        NCCActionRefreshUtil.loadNCHome(p);
        //载入项目的，放最后，优先级最高
        NCCActionRefreshUtil.reloadProjectAction(p);
    }
}
