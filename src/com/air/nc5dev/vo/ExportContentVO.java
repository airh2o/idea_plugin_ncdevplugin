package com.air.nc5dev.vo;

import com.air.nc5dev.enums.NcVersionEnum;
import com.air.nc5dev.util.CollUtil;
import com.air.nc5dev.util.ExportNCPatcherUtil;
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
import lombok.Data;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public transient AnActionEvent event;
    /**
     * 输出路径
     */
    public String outPath;
    /**
     * 当前项目
     */
    public transient Project project;
    /**
     * 所有的模块们
     */
    public transient List<Module> modules;
    /**
     * 模块文件夹根路径 ： 模块对象
     */
    public transient HashMap<String, Module> moduleHomeDir2ModuleMap = Maps.newHashMap();
    /**
     * key 模块， value 模块补丁导出配置文件
     */
    public transient HashMap<Module, ExportConfigVO> module2ExportConfigVoMap = Maps.newHashMap();

    public ProgressIndicator indicator;
    /**
     * 是否sql文本去重
     */
    public boolean filtersql = true;
    /**
     * 是否强制链接数据库抽取脚本
     */
    public boolean rebuildsql = false;
    /**
     * 是否混淆源码内容
     */
    public boolean reWriteSourceFile = false;
    /**
     * 选择的数据源
     */
    public int data_source_index = 0;
    /**
     * NC版本
     */
    public NcVersionEnum ncVersion = ProjectNCConfigUtil.getNCVersion();
    /**
     * 是否执行 npm命令
     */
    public boolean reNpmBuild = true;
    /**
     * 是否云管家格式
     */
    public boolean format4Ygj = true;
    /**
     * 是否导出的原始补丁文件夹打包成zip
     */
    public boolean zip = true;
    /**
     * 是否打包zip后删除导出的原始补丁文件夹
     */
    public boolean deleteDir = true;
    /**
     * 是否 导出选定的文件
     */
    public boolean selectExport = false;
    /**
     * key=模块文件夹路径， value=模块对象
     */
    public transient Map<String, Module> moduleDirPath2ModuleMap = Maps.newHashMap();

    /**
     * key=模块名， value=强制指定导出补丁的模块名称
     */
    public Map<String, String> moduleName2ExportModuleNameMap = Maps.newHashMap();
    /**
     * 选择的要导出的模块们
     */
    public transient List<Module> selectExportModules = Lists.newArrayList();
    /**
     * key=选择的文件， value=文件所在模块
     */
    public transient Map<String, Module> selectFile2ModuleMap = Maps.newHashMap();
    /**
     * 选择的要导出的指定的文件们
     */
    public List<String> selectFiles = Lists.newArrayList();
    /**
     * 是否导出前端代码 (resources)
     */
    public boolean exportResources = true;
    /**
     * 是否导出public和private
     */
    public boolean exportModules = true;
    /**
     * 是否导出sql
     */
    public boolean exportSql = true;
    /**
     * 是否只保留合并的那个全量sql文件
     */
    public boolean onleyFullSql = true;
    /**
     * 是否保存本此导出配置到工程
     */
    public boolean saveConfig = true;
    /**
     * 补丁名称
     */
    public String name;
    /**
     * 是否强制不压缩成jar文件
     */
    public boolean no2Jar = false;
    /**
     * 是否导出client
     */
    public boolean exportHotwebsClass = true;
    /**
     * 前端源码项目路径
     */
    public String hotwebsResourcePath;
    /**
     * npm要执行的命令
     */
    public String npmRunCommand;
    /**
     * 是否导出模块下的resources文件夹
     */
    public boolean exportModuleResources = true;
    /**
     * 是否导出模块下的lib文件夹
     */
    public boolean exportModuleLib = true;
    /**
     * 是否导出模块下的META-INF文件夹
     */
    public boolean exportModuleMeteinfo = true;
    /**
     * 是否导出模块下的METADATA文件夹
     */
    public boolean exportModuleMetadata = true;

    ///////// 云管家
    String applyVersion;
    String department;
    String provider;
    boolean deploy = true;
    boolean appleyjar = true;
    String no;
    String v;
    //////////END
    /**
     * 所有要输出的文件列表
     */
    public volatile ArrayList<FileContentVO> outFiles = new ArrayList<>(1_000);

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

        //模块文件夹根路径 ： 模块对象
        for (Module module : ms) {
            moduleHomeDir2ModuleMap.put(new File(module.getModuleFilePath()).getParent(), module);
        }

        //排除配置文件里设置的 不需要的模块
        moduleHomeDir2ModuleMap.forEach((path, module) -> {
            ExportConfigVO configVO = ExportNCPatcherUtil.loadExportConfig(path, module);

            configVO.toJar = configVO.toJar && !no2Jar;
            module2ExportConfigVoMap.put(module, configVO);
        });

        selectExportModules.addAll(CollUtil.toList(ms));
        HashMap<String, Module> path2ModuleMap = Arrays.stream(ms)
                .filter(m -> m.getModuleFile() != null)
                .collect(Collectors.toMap(
                        o -> o.getModuleFile().getParent().getPath()
                        , o -> o
                        , (o1, o2) -> o2
                        , HashMap::new
                ));

        //排序， 路径最长 放最前面！
        ArrayList<String> modulesPathList = new ArrayList();
        modulesPathList.addAll(path2ModuleMap.keySet());
        modulesPathList.sort((s1, s2) -> s2.length() - s1.length());

        for (String select : getSelectFiles()) {
            for (String modulesPath : modulesPathList) {
                String path = StringUtil.replaceChars(modulesPath, "/", File.separator);
                path = StringUtil.replaceChars(path, "\\", File.separator);
                if (StringUtil.startsWith(select, path)) {
                    moduleDirPath2ModuleMap.put(modulesPath, path2ModuleMap.get(modulesPath));
                    selectFile2ModuleMap.put(select, path2ModuleMap.get(modulesPath));
                    break;
                }
            }
        }
    }

    public void addOutFiles(FileContentVO file) {
        if (outFiles == null) {
            outFiles = new ArrayList<>(1000);
        }
        outFiles.add(file);
    }
}
