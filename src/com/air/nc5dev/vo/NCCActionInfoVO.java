package com.air.nc5dev.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
public class NCCActionInfoVO {
    String name;
    String label;
    String clazz;
    String appcode;
    /**
     * xml 注册文件
     */
    String xmlPath;
    /**
     * 项目路径
     */
    String project;

    transient int score;

    @Override
    public String toString() {
        return "name=" + name +
                ", label=" + label +
                ", clazz=" + clazz +
                ", appcode=" + appcode +
                ", xmlPath=" + xmlPath
                ;
    }
}
