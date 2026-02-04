package com.air.nc5dev.util;

/**
 * <br/>
 * <br/>
 * <br/>
 *
 * @author 唐粟 Email:209308343@qq.com
 * @date 2024/1/26 15:29
 * @project
 * @Version
 */
public interface Functions<T, R> {
    R apply(T... ts);
}
