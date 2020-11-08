package com.air.nc5dev.bean;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;

import java.util.HashMap;
import java.util.List;

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
public class ExportContentVO {
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


}
