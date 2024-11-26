package com.air.nc5dev.vo;

import com.air.nc5dev.util.exportpatcher.output.IOutPutFile;
import com.air.nc5dev.util.exportpatcher.searchs.AbstractContentSearchImpl;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.File;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * </br>
 * </br>
 * </br>
 *
 * @author 唐粟 Email:209308343@qq.com
 * @date 2024/11/26 0026 19:02
 * @project
 * @Version
 */
@Data
@Accessors(chain = true)
public class FileContentVO {
    public static final String NAME_MODULE_BMF = "module_bmf";
    public static final String NAME_MODULE_RESOURCES = "module_resources";
    public static final String NAME_MODULE_LIB = "module_lib";
    /**
     * hotwebs / nccloud / resources 的前端资源
     */
    public static String NAME_HOTWEBS_NCCLOUD_RESOURCE = "hotwebs/nccloud/resources";
    /**
     * @see AbstractContentSearchImpl 里的各种 NC_TYPE_的值也会是
     */
    String name;

    String file;
    String srcFile;

    String fileTo;
    String srcFileTo;

    ModuleWarpVO module;

    BiConsumer<ExportContentVO, FileContentVO> befor;
    BiConsumer<ExportContentVO, FileContentVO> after;

    //  文件路径，boolean =true 导出， 否则忽略
    Function<File, Boolean> filter;

    IOutPutFile executor;

    String srcTop;
}
