package com.air.nc5dev.vo;

import com.air.nc5dev.util.ProjectNCConfigUtil;
import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.util.V;
import com.intellij.pom.Navigatable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * <br>
 * <br>
 * <br>
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
    public static final int FROM_SRC = 1;

    /**
     * action
     */
    public static final int TYPE_ACTION = 0;
    /**
     * upm
     */
    public static final int TYPE_UPM = 1;
    /**
     * 非UPM的Servlet
     */
    public static final int TYPE_SERVLET = 2;
    /**
     * SpringMVC注解接口
     */
    public static final int TYPE_SPRINTMVC = 3;
    /**
     * Jaxrs注解接口
     */
    public static final int TYPE_JAXRS = 4;

    public String simplename;
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


    @Builder.Default
    public ArrayList<Navigatable> navigatables = new ArrayList<>();

    @Override
    public String toString() {
        return
                clazz +
                        " |来自:" + (from == FROM_HOME ? "NCHOME" : "工程项目") +
                        " |URL:" + getSimplename() +
                        " |名称:" + V.get(label) +
                        " |其他信息:" + V.get(appcode) +
                        " " + V.get(xmlPath) +
                        " " + score
                ;
    }

    public void setName(String name) {
        this.name = name;

        getSimplename();
    }

    public String getSimplename(){
        if (simplename != null) {
            return simplename;
        }

        simplename = StringUtil.removeStart(StringUtil.removeStart(name,
                ProjectNCConfigUtil.getNCClientIP()), ":" + ProjectNCConfigUtil.getNCClientPort());
        return simplename;
    }
}
