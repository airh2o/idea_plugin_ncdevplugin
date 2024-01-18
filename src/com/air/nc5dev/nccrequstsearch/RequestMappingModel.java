package com.air.nc5dev.nccrequstsearch;


import com.air.nc5dev.ui.actionurlsearch.ActionResultDTO;
import com.air.nc5dev.ui.actionurlsearch.ActionResultListTable;
import com.air.nc5dev.util.ReflectUtil;
import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.vo.NCCActionInfoVO;
import com.intellij.ide.util.gotoByName.ChooseByNameItemProvider;
import com.intellij.ide.util.gotoByName.FilteringGotoByModel;
import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * </br>
 * </br>
 * </br>
 *
 * @author 唐粟 Email:209308343@qq.com
 * @date 2022/2/15 0015 11:09
 * @project
 * @Version
 */
@Data
public class RequestMappingModel extends FilteringGotoByModel implements DumbAware {
    volatile boolean onlySearchPorjectUrl;
    AnActionEvent e;
    volatile String info;

    public RequestMappingModel(@NotNull Project project, @NotNull List<ChooseByNameContributor> list, AnActionEvent e) {
        super(project, list);
        this.e = e;
    }

    @NotNull
    @Override
    public ChooseByNameItemProvider getItemProvider(@Nullable PsiElement context) {
        return new RequestMappingItemProvider(this);
    }

    @Nullable
    @Override
    protected Object filterValueFor(NavigationItem navigationItem) {
        return null;
    }

    @Override
    public String getPromptText() {
        return "输入请求地址nccloud/aim/acceptance/querylist.do或acceptance.querylist等(输入!开头意思是 查询项目src源码+NCHOME的action)";
    }

    @NotNull
    @Override
    public String getNotInMessage() {
        return StringUtil.isBlank(info) ? "搜索完成" : info;
    }

    @NotNull
    @Override
    public String getNotFoundMessage() {
        return "没有匹配到任何结果!";
    }

    @Nullable
    @Override
    public String getCheckBoxName() {
        return "包含NCHOME(是否也搜索HOME.不勾选 只搜索工程不搜索NCHOME.搜索内容用!开头视为勾选.)";
    }

    @Override
    public boolean loadInitialCheckBoxState() {
        return onlySearchPorjectUrl;
    }

    @Override
    public void saveInitialCheckBoxState(boolean b) {
        onlySearchPorjectUrl = b;
    }

    @NotNull
    @Override
    public String[] getSeparators() {
        return new String[0];
    }

    @Nullable
    @Override
    public String getFullName(@NotNull Object o) {
        NCCActionInfoVO v = (NCCActionInfoVO) o;
        ActionResultListTable.openClass(null, getProject(), ReflectUtil.copy2VO(v, ActionResultDTO.class));
        return v.getClazz();
    }

    @Override
    public boolean willOpenEditor() {
        return true;
    }
}
