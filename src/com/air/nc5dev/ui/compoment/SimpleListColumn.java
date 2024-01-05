package com.air.nc5dev.ui.compoment;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author air Email:209308343@qq.com
 * @version NC505, JDK1.5
 * @date 2019年12月11日 上午9:38:01
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SimpleListColumn {
    /**
     * 字符串
     */
    public static int TYPE_STRING = 0;
    /**
     * 整数
     */
    public static int TYPE_INT = 1;
    /**
     * 大文本
     */
    public static int TYPE_TXTAREA = 9;

    /**
     * @return
     */
    String value();

    /**
     * 隐藏
     *
     * @return
     */
    boolean hide() default false;

    /**
     * 可编辑
     *
     * @return
     */
    boolean editbale() default false;

    /**
     * 列位置， 越小越优先
     *
     * @return
     */
    int sort() default 1000;

    /**
     * 类型
     *
     * @return
     */
    int type() default TYPE_STRING;


    /**
     * 是否id字段
     *
     * @return
     */
    boolean id() default false;
}
