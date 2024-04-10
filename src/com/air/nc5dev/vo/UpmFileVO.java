package com.air.nc5dev.vo;

import com.intellij.openapi.module.Module;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 接口 ejb 文件 或者 aop 等 配置信息
 */
@Data
@Accessors(chain = true)
public class UpmFileVO implements Serializable, Cloneable {
    /**
     * 普通接口ejb实现配置
     */
    public static int TYPE_NORMAL = 0;
    /**
     * AOP文件
     */
    public static int TYPE_AOP = 1;

    int type = TYPE_NORMAL;
    /**
     * upm文件路径
     */
    String path;
    /**
     * 行号
     */
    int row;
    /**
     * 列号
     */
    int column;
    /**
     * 接口
     */
    String interfaceName;
    /**
     * 实现类(或aop切面类)
     */
    String implName;
    /**
     * 模块
     */
    String module;
    String tag;
    String remote;
    String tx;
    String priority;
    String singleton;
    String supportAlias;




}
