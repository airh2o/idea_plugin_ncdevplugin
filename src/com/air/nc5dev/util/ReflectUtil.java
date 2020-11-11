package com.air.nc5dev.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.exceptions.UtilException;
import cn.hutool.core.lang.SimpleCache;
import com.air.nc5dev.exception.BusinessException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * 对象工具类，提供反射等 <br/>
 * <br/>
 * <br/>
 *
 * @author 唐粟 Email:209308343@qq.com
 * @date 2020/9/15 15:53
 * @project
 * @Version
 */
public class ReflectUtil extends cn.hutool.core.util.ReflectUtil {
    /**
     * 缓存
     */
    private static final SimpleCache<String, Optional> CACHES = new SimpleCache<>();

    /**
     * 根据  接口实现类 或 属性名一样的其他vo  的所有同名属性的值 复制到 目标对象里！            <br>
     * 浅拷贝！   忽略字段的大小写!!!!!!!        <br>
     *
     * @param source
     * @param tovo
     */
    public static void copy2VO(Object source, Object tovo) {
        copy2VO(source, tovo, CopyOptions.create().ignoreCase());
    }

    /**
     * 根据  接口实现类 或 属性名一样的其他vo  的所有同名属性的值 复制到 目标对象里！            <br>
     * 浅拷贝！   忽略字段的大小写!!!!!!!     <br>
     *
     * @param source
     * @param tovoClz
     */
    public static <T> T copy2VO(Object source, Class<T> tovoClz) {
        if (source == null) {
            return null;
        }

        T tovo = newInstance(tovoClz);
        copy2VO(source, tovo);
        return tovo;
    }

    /**
     * 根据  接口实现类 或 属性名一样的其他vo  的所有同名属性的值 复制到 目标对象里 ！            <br>
     * 浅拷贝！ 忽略字段的大小写!!!!!!!        <br>
     *
     * @param source
     * @param tovoClz
     */
    public static <T> List<T> copy2VOs(List source, Class<T> tovoClz) {
        return copy2VOs(source, tovoClz, CopyOptions.create().ignoreCase());
    }

    /**
     * 根据  接口实现类 或 属性名一样的其他vo  的所有同名属性的值 复制到 目标对象里 ！ <br>
     * 浅拷贝！    忽略字段的大小写!!!!!!!      <br>
     * 跳过报错的字段不设置 然后继续处理 且不报错！          <br>
     *
     * @param source
     * @param tovo
     */
    public static void copy2VOIngnoreError(Object source, Object tovo) {
        copy2VO(source, tovo, CopyOptions.create().ignoreCase().ignoreError());
    }

    /**
     * 根据  接口实现类 或 属性名一样的其他vo  的所有同名属性的值 复制到 目标对象里 ！ <br>
     * 浅拷贝！    忽略字段的大小写!!!!!!!      <br>
     * 跳过报错的字段不设置 然后继续处理 且不报错！          <br>
     *
     * @param sourceList
     * @param tovoClz
     */
    public static <T> List<T> copy2VOsIngnoreError(List sourceList, Class<T> tovoClz) {
        return copy2VOs(sourceList, tovoClz, CopyOptions.create().ignoreCase().ignoreError());
    }

    /**
     * 根据  接口实现类 或 属性名一样的其他vo  的所有同名属性的值 复制到 目标对象里 ！ <br>
     * 浅拷贝！        <br>
     * 跳过报错的字段不设置 然后继续处理 且不报错！          <br>
     *
     * @param source
     * @param tovo
     * @param copyOptions 复制选项
     */
    public static void copy2VO(Object source, Object tovo, CopyOptions copyOptions) {
        if (tovo == null || source == null) {
            return;
        }

        BeanUtil.copyProperties(source, tovo, copyOptions);
    }

    /**
     * vo 转 map , 忽略null， 驼峰保留不另加下划线
     *
     * @param vo
     * @return
     */
    public static Map<String, Object> vo2Map(Object vo) {
        return vo2Map(vo, false, true);
    }

    /**
     * vo 转 map , 忽略null， 驼峰保留不另加下划线
     *
     * @param vos
     * @return
     */
    public static ArrayList<Map<String, Object>> vos2Maps(List<Object> vos) {
        if (CollUtil.isEmpty(vos)) {
            return Lists.newArrayList();
        }

        ArrayList<Map<String, Object>> maps = Lists.newArrayListWithCapacity(vos.size());

        for (int i = 0; i < vos.size(); i++) {
            maps.add(vo2Map(vos.get(i), false, true));
        }

        return maps;
    }

    /**
     * vo 转 map
     *
     * @param vo
     * @param isToUnderlineCase – 是否转换为下划线模式
     * @param ignoreNullValue   - 是否忽略值为空的字段
     * @return
     */
    public static Map<String, Object> vo2Map(Object vo, boolean isToUnderlineCase, boolean ignoreNullValue) {
        if (vo == null) {
            return CollUtil.emptyMap();
        }
        Field[] fs = getFields(vo.getClass());

        if (fs == null || fs.length < 1) {
            return null;
        }

        HashMap<String, Object> map = Maps.newHashMapWithExpectedSize(fs.length);
        return BeanUtil.beanToMap(vo, map, isToUnderlineCase, ignoreNullValue);
    }

    /**
     * 根据tovoClz的属性 从 接口实现类的 所有get方法里 获取值 设置到 vo里 ！            <br>
     * 浅拷贝！         <br>
     *
     * @param sourceList
     * @param tovoClz
     * @param copyOptions 复制选项
     */
    public static <T> List<T> copy2VOs(List sourceList, Class<T> tovoClz, CopyOptions copyOptions) {
        if (tovoClz == null || CollUtil.isEmpty(sourceList)) {
            return CollUtil.emptyList();
        }

        T to;
        ArrayList<T> vos = Lists.newArrayListWithCapacity(sourceList.size());
        for (int i = 0; i < sourceList.size(); i++) {
            to = newInstance(tovoClz);

            copy2VO(sourceList.get(i), to, copyOptions);

            vos.add(to);
        }

        return vos;
    }


    /**
     * 根据tovo的属性 从一个 map  获取值 设置到 vo里 ！            <br>
     * 浅拷贝！ 自动处理掉 字段名里的 _ 去掉他         <br>
     *
     * @param map
     * @param tovo
     */
    public static void map2VO(Map<String, Object> map, Object tovo) {
        map2VO(map, tovo, CopyOptions.create().ignoreCase());
    }

    /**
     * 根据tovo的属性 从一个 map 获取值 设置到 vo里 ！ <br>
     * 浅拷贝！   自动处理掉 字段名里的 _ 去掉他        <br>
     * 跳过报错的字段不设置 然后继续处理 且不报错！          <br>
     *
     * @param map
     * @param tovo
     */
    public static void map2VOIngnoreError(Map<String, Object> map, Object tovo) {
        map2VO(map, tovo, CopyOptions.create().ignoreCase().ignoreError());
    }

    /**
     * 根据tovo的属性 从一个 map  获取值 设置到 vo里 ！            <br>
     * 浅拷贝！ 自动处理掉 字段名里的 _ 去掉他         <br>
     *
     * @param map
     * @param tovo
     * @param copyOptions 复制选项
     */
    public static void map2VO(Map<String, Object> map, Object tovo, CopyOptions copyOptions) {
        if (tovo == null || CollUtil.isEmpty(map)) {
            return;
        }

        //去掉 _
        final char removeChar = '_';
        HashMap<String, Object> newMap = Maps.newHashMapWithExpectedSize(map.size());
        map.forEach((k, v) -> newMap.put(StringUtils.remove(k, removeChar), v));

        BeanUtil.copyProperties(newMap, tovo, copyOptions);
    }

    /**
     * 根据tovo的属性 从一个 map  获取值 设置到 vo里 ！            <br>
     * 浅拷贝！ 自动处理掉 字段名里的 _ 去掉他         <br>
     *
     * @param maps
     * @param tovoClz
     */
    public static <T> List<T> map2VOs(List<Map<String, Object>> maps, Class<T> tovoClz) {
        return map2VOs(maps, tovoClz, CopyOptions.create().ignoreCase());
    }

    /**
     * 根据tovo的属性 从一个 map  获取值 设置到 vo里 ！            <br>
     * 浅拷贝！ 自动处理掉 字段名里的 _ 去掉他         <br>
     *
     * @param maps
     * @param tovoClz
     */
    public static <T> List<T> map2VOsIngnoreError(List<Map<String, Object>> maps, Class<T> tovoClz) {
        return map2VOs(maps, tovoClz, CopyOptions.create().ignoreCase().ignoreError());
    }

    /**
     * 根据tovo的属性 从  map  获取值 设置到 vo里 ！            <br>
     * 浅拷贝！ 自动处理掉 字段名里的 _ 去掉他         <br>
     *
     * @param maps
     * @param tovoClz
     * @param copyOptions 复制选项
     */
    public static <T> List<T> map2VOs(List<Map<String, Object>> maps, Class<T> tovoClz, CopyOptions copyOptions) {
        if (tovoClz == null || CollUtil.isEmpty(maps)) {
            return CollUtil.emptyList();
        }

        T to;
        ArrayList<T> vos = Lists.newArrayListWithCapacity(maps.size());
        Map<String, Object> map;
        HashMap<String, Object> newMap;
        Set<String> keys;
        final char removeChar = '_';
        for (int i = 0; i < maps.size(); i++) {
            to = newInstance(tovoClz);
            map = maps.get(i);
            newMap = Maps.newHashMapWithExpectedSize(map.size());

            //去掉 _
            keys = map.keySet();
            for (String k : keys) {
                newMap.put(StringUtils.remove(k, removeChar), map.get(k));
            }

            map2VO(newMap, to, copyOptions);

            vos.add(to);
        }

        return vos;
    }

    /**
     * 获得一个类中所有字段列表，包括其父类中的字段, 排除final字段       <br>
     *
     * @param beanClass 类
     * @return 字段列表
     * @throws SecurityException 安全检查异常
     */
    public static Field[] getFieldsNoFinal(Class<?> beanClass) throws SecurityException {
        if (beanClass == null) {
            return new Field[0];
        }

        Field[] fields = getFields(beanClass);
        Field[] emputy = new Field[0];
        if (fields == null) {
            return emputy;
        }

        ArrayList<Field> fs = Lists.newArrayListWithCapacity(fields.length);
        for (Field f : fields) {
            if (!Modifier.isFinal(f.getModifiers())) {
                fs.add(f);
            }
        }

        return fs.toArray(emputy);
    }

    /**
     * 获得一个类中所有字段列表，包括其父类中的字段, 排除final字段和static字段      <br>
     *
     * @param beanClass 类
     * @return 字段列表
     * @throws SecurityException 安全检查异常
     */
    public static Field[] getFieldsNoFinalNoStatic(Class<?> beanClass) throws SecurityException {
        if (beanClass == null) {
            return new Field[0];
        }

        Field[] fields = getFieldsNoFinal(beanClass);
        if (fields.length < 1) {
            return fields;
        }

        ArrayList<Field> fs = Lists.newArrayListWithCapacity(fields.length);
        for (Field f : fields) {
            if (!Modifier.isStatic(f.getModifiers())) {
                fs.add(f);
            }
        }

        return fs.toArray(new Field[0]);
    }

    /**
     * 设置字段值
     * <h1> 字段名不区分大小写！！！ 支持 _ 自动去掉 <h1/>
     *
     * @param obj               对象， 此参数不能为null
     * @param fieldName         字段名字
     * @param value             值， 尽最大努力自动转换对象类型
     * @param ignoreNotHasField 是否忽略 不存在字段
     * @throws UtilException        UtilException 包装IllegalAccessException异常
     * @throws BusinessException 值类型转换失败
     */
    public static void setFieldValueAutoConvert(Object obj, String fieldName, Object value, boolean ignoreNotHasField)
            throws UtilException, BusinessException {
        Field field = null;

        Field[] fs = getFields(obj.getClass());

        String name = StringUtils.replaceChars(fieldName, "_", "").toLowerCase();

        for (Field f : fs) {
            if (f.getName().toLowerCase().equals(name)) {
                field = f;
                break;
            }
        }

        if (field == null) {
            if (ignoreNotHasField) {
                return;
            }

            throw new BusinessException(String.format("字段 %s 不存在!", fieldName));
        }

        setFieldValue(obj, field, ConvertUtil.toType(field.getType(), value));
    }


    /**
     * 设置字段值
     * <h1> 字段名不区分大小写！！！ 支持 _ 自动去掉 <h1/>
     *
     * @param obj       对象， 此参数不能为null
     * @param fieldName 字段名字
     * @param value     值， 尽最大努力自动转换对象类型
     * @throws UtilException        UtilException 包装IllegalAccessException异常
     * @throws BusinessException 值类型转换失败 或字段不存在
     */
    public static void setFieldValueAutoConvert(Object obj, String fieldName, Object value)
            throws UtilException, BusinessException {
        setFieldValueAutoConvert(obj, fieldName, value, false);
    }

    /**
     * 设置字段值. 会自动忽略 不存在的字段不报错
     * <h1> 字段名不区分大小写！！！ 支持 _ 自动去掉 <h1/>
     *
     * @param obj       对象， 此参数不能为null
     * @param fieldName 字段名字
     * @param value     值， 尽最大努力自动转换对象类型
     * @throws UtilException        UtilException 包装IllegalAccessException异常
     * @throws BusinessException 值类型转换失败
     */
    public static void setFieldValueAutoConvertIgnoreNotHasField(Object obj, String fieldName, Object value)
            throws UtilException, BusinessException {
        setFieldValueAutoConvert(obj, fieldName, value, true);
    }

    /**
     * 把一个 VO里的所有 实例字段 不是null的 弄成数组
     *
     * @param vo
     * @return
     */
    public static ArrayList toParmars(Object vo) {
        if (vo == null) {
            return Lists.newArrayList();
        }

        Field[] fs = getFieldsNoFinalNoStatic(vo.getClass());

        ArrayList re = Lists.newArrayListWithCapacity(fs.length);

        Object o;
        for (Field f : fs) {
            o = getFieldValue(vo, f);
            if (o != null) {
                re.add(o);
            }
        }

        return re;
    }

    /**
     * 把一个 VO里的所有 实例字段 不是null的 和 string不是black的 弄成数组
     *
     * @param vo
     * @return
     */
    public static ArrayList toParmarsExcludeBlackString(Object vo) {
        if (vo == null) {
            return Lists.newArrayList();
        }

        Field[] fs = getFieldsNoFinalNoStatic(vo.getClass());

        ArrayList re = Lists.newArrayListWithCapacity(fs.length);

        Object o;
        for (Field f : fs) {
            o = getFieldValue(vo, f);
            if (o != null) {
                if (String.class != f.getType()) {
                    re.add(o);
                } else {
                    if (StringUtils.isNotBlank(o.toString())) {
                        re.add(o);
                    }
                }
            }
        }

        return re;
    }
    /**
     * 把一个 vo里面的  所有的属性全部抹除掉，跳过 skipFields里的属性
     *
     * @param obj
     * @param skipFields 跳过不抹除的属性列表
     */
    public static void warpNull(Object obj, List<String> skipFields) {
        Field[] fs = getFieldsNoFinalNoStatic(obj.getClass());

        for (Field f : fs) {
            if (skipFields.contains(f.getName())) {
                continue;
            }

            setFieldValue(obj, f, null);
        }
    }
}
