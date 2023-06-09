package com.air.nc5dev.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * </br>
 * </br>
 * </br>
 *
 * @author 唐粟 Email:209308343@qq.com
 * @date 2022/2/15 0015 14:38
 * @project
 * @Version
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NCCActionInfoVO implements Serializable, Cloneable {
    /**
     * 来自NC的home
     */
    public static final int FROM_HOME = 0;
    /**
     * 来自 工程源码里
     */
    public static final int FROM_SRC = 1000;
    /**
     * upm
     */
    public static final int TYPE_UPM = 1;
    /**
     * action
     */
    public static final int TYPE_ACTION = 0;

    public String name;
    public String label;
    public String clazz;
    public String appcode;
    /**
     * xml 注册文件
     */
    public String xmlPath;
    public String authPath;
    /**
     * 项目路径
     */
    public String project;
    public int from;
    public int type;
    /**
     * 文件列数
     */
    public int column;
    /**
     * 文件行数量
     */
    public int row;

    public int score;
    public int auth_column;
    public int auth_row;

    @Override
    public String toString() {
        return "NCCActionInfoVO{" +
                "name='" + name + '\'' +
                ", label='" + label + '\'' +
                ", clazz='" + clazz + '\'' +
                ", appcode='" + appcode + '\'' +
                ", xmlPath='" + xmlPath + '\'' +
                ", project='" + project + '\'' +
                ", from=" + from +
                ", type=" + type +
                ", column=" + column +
                ", row=" + row +
                ", score=" + score +
                '}';
    }
}
