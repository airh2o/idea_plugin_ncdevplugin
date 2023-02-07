package com.air.nc5dev.vo;

import cn.hutool.core.util.StrUtil;
import com.air.nc5dev.enums.NcVersionEnum;
import com.air.nc5dev.util.CollUtil;
import com.air.nc5dev.util.ProjectNCConfigUtil;
import com.air.nc5dev.util.StringUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import lombok.Data;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 补丁导出， 环境上下文vo <br/>
 * <br/>
 * <br/>
 *
 * @author 唐粟 Email:209308343@qq.com
 * @date 2020/11/8 11:34
 * @project
 * @Version
 */
@Data
public class ExportContentVO {
    /**
     * 右键弹出菜单点击的触发按钮
     */
    public static String EVENT_POPUP_CLICK = "ProjectViewPopup";

    /**
     * 触发操作的事件， 可能null
     */
    public AnActionEvent event;
    /**
     * 输出路径
     */
    public String outPath;
    /**
     * 当前项目
     */
    public Project project;
    /**
     * 所有的模块们
     */
    public List<Module> modules;
    /**
     * 模块文件夹根路径 ： 模块对象
     */
    public HashMap<String, Module> moduleHomeDir2ModuleMap = Maps.newHashMap();
    /**
     * key 模块， value 模块补丁导出配置文件
     */
    public HashMap<Module, ExportConfigVO> module2ExportConfigVoMap = Maps.newHashMap();
    /**
     * 被设置成不导出的模块
     */
    public List<Module> ignoreModules = Lists.newLinkedList();

    public ProgressIndicator indicator;

    public boolean filtersql = true;
    public boolean rebuildsql = false;
    public boolean reWriteSourceFile = false;
    public int data_source_index = 0;
    public NcVersionEnum ncVersion = ProjectNCConfigUtil.getNCVerSIon();
    public boolean reNpmBuild = true;
    public boolean format4Ygj = true;
    /**
     * 是否 导出选定的文件
     */
    public boolean selectExport = false;
    public Map<String, Module> selectModules = Maps.newHashMap();
    public Map<String, Module> selectFile2ModuleMap = Maps.newHashMap();
    public List<String> selectFiles = Lists.newArrayList();

    public void init() {
        if (isSelectExport()) {
            initSelectModuleAndFiles();
        }
        selectExport = EVENT_POPUP_CLICK.equals(event.getPlace());
    }

    public void initSelectModuleAndFiles() {
        initSelectFiles();
        initSelectModules();
    }

    public void initSelectFiles() {
        VirtualFile[] selects = LangDataKeys.VIRTUAL_FILE_ARRAY.getData(event.getDataContext());
        if (CollUtil.isEmpty(selects)) {
            return;
        }

        Module[] ms = ModuleManager.getInstance(project).getModules();
        HashMap<String, Module> path2ModuleMap = Arrays.stream(ms).collect(Collectors.toMap(
                o -> o.getModuleFile().getParent().getPath()
                , o -> o
                , (o1, o2) -> o2
                , HashMap::new
        ));

        for (VirtualFile select : selects) {
            String path = StringUtil.replaceChars(select.getPath(), "/", File.separator);
            path = StringUtil.replaceChars(path, "\\", File.separator);
            selectFiles.add(path);
        }
    }

    public void initSelectModules() {
        Module[] ms = ModuleManager.getInstance(project).getModules();
        HashMap<String, Module> path2ModuleMap = Arrays.stream(ms).collect(Collectors.toMap(
                o -> o.getModuleFile().getParent().getPath()
                , o -> o
                , (o1, o2) -> o2
                , HashMap::new
        ));

        for (String select : getSelectFiles()) {
            for (String modulesPath : path2ModuleMap.keySet()) {
                String path = StringUtil.replaceChars(modulesPath, "/", File.separator);
                path = StringUtil.replaceChars(path, "\\", File.separator);
                if (StringUtil.startsWith(select, path)) {
                    selectModules.put(modulesPath, path2ModuleMap.get(modulesPath));
                    selectFile2ModuleMap.put(select, path2ModuleMap.get(modulesPath));
                    break;
                }
            }
        }
    }
}
