package com.air.nc5dev.util;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.*;
import java.util.function.Function;

/**
 * 集合工具 <br/>
 * <br/>
 * <br/>
 *
 * @author 唐粟 Email:209308343@qq.com
 * @date 2020/8/29 16:44
 * @project
 * @Version
 */
public class CollUtil extends cn.hutool.core.collection.CollUtil {
    /**
     * 转换成一个map，ks key的数组， vs value的数组，如果ks 的长度大于vs，多余的用null作为值，vs多余ks 多余的vs丢弃
     *
     * @param ks
     * @param vs
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K, V> HashMap<K, V> asMap(K[] ks, V[] vs) {
        return asMap(Arrays.asList(ks), Arrays.asList(vs));
    }

    /**
     * 转换成一个map，ks key的数组， vs value的数组，如果ks 的长度大于vs，多余的用null作为值，vs多余ks 多余的vs丢弃
     *
     * @param ks
     * @param vs
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K, V> HashMap<K, V> asMap(List<K> ks, List<V> vs) {
        if (isEmpty(ks)) {
            return new HashMap<>();
        }

        if (vs == null) {
            vs = Collections.emptyList();
        }

        HashMap<K, V> map = new HashMap(ks.size() << 1);
        for (int i = 0; i < ks.size(); i++) {
            if (i < vs.size()) {
                map.put(ks.get(i), vs.get(i));
            } else {
                map.put(ks.get(i), null);
            }
        }

        return map;
    }

    /**
     * 转换成一个map，list元素视为： key value 这样循环
     *
     * @param kvs
     * @return
     */
    public static <T> HashMap<T, T> asMap(T... kvs) {
        return asMap(Arrays.asList(kvs));
    }

    /**
     * 转换成一个map
     *
     * @param keySupplier key生成器，参数会传递当前循环到的value的数组下标
     * @param values      value 数组
     * @param <K>         转换后的  map的key类型
     * @return
     */
    public static <K, V> HashMap<K, V> asMapWithKeySupplier(Function<Integer, K> keySupplier, V... values) {
        if (values == null || values.length < 1) {
            return new HashMap();
        }

        HashMap<K, V> map = new HashMap(values.length);
        for (int i = 0; i < values.length; i++) {
            map.put(keySupplier.apply(i), values[i]);
        }

        return map;
    }


    /**
     * 转换成一个map，list元素视为： key value 这样循环
     *
     * @param kvs
     * @return
     */
    public static HashMap asMap(List kvs) {
        if (isEmpty(kvs)) {
            return new HashMap<>();
        }

        HashMap map = new HashMap(kvs.size());
        for (int i = 0; i < kvs.size(); i++) {
            if (i < kvs.size()) {
                map.put(kvs.get(i), kvs.get(++i));
            } else {
                map.put(kvs.get(i), null);
            }
        }

        return map;
    }

    /**
     * 查询集合里，属性值 是value的 第一个 元素
     *
     * @param vos
     * @param fieldName
     * @param value
     * @return
     */
    public static <T> T find(Collection<T> vos, String fieldName, Object value) {
        Object v;
        for (T vo : vos) {
            v = ReflectUtil.getFieldValue(vo, fieldName);
            if (value == null) {
                if (v == null) {
                    return vo;
                }
            } else if (value.equals(v)) {
                return vo;
            }
        }

        return null;
    }

    /**
     * 判断数组是否  null 或者 空
     *
     * @param arr
     * @return
     */
    public static boolean isEmpty(Object[] arr) {
        return arr == null || arr.length < 1;
    }

    /**
     * 判断数组是否含有一个元素及以上
     *
     * @param arr
     * @return
     */
    public static boolean isNotEmpty(Object[] arr) {
        return !isEmpty(arr);
    }

    /**
     * 添加元素到 数组的末尾 ，返回新的数组
     *
     * @param arr
     * @param adds
     * @return
     */
    public static <T> T[] addAll(T[] arr, T... adds) {
        return ArrayUtil.addAll(arr, adds);
    }


    /**
     * 加入全部
     *
     * @param <T>        集合元素类型
     * @param collection 被加入的集合 {@link Collection}
     * @param iterable   要加入的内容{@link Iterable}
     * @return 原集合
     */
    public static <T> Collection<T> addAllColls(Collection<T> collection, Iterable<T>... iterables) {
        if (iterables == null) {
            return collection;
        }

        Collection<T> r = collection;
        for (Iterable<T> i : iterables) {
            if (i == null) {
                continue;
            }

            r = addAll(r, i.iterator());
        }

        return r;
    }

    /**
     * 返回一个空的 ArrayList 对象
     *
     * @return
     */
    public static <T> List<T> emptyList() {
        return Lists.newArrayList();
    }

    /**
     * 返回一个空的 HashMap 对象
     *
     * @return
     */
    public static <K, V> Map<K, V> emptyMap() {
        return Maps.newHashMap();
    }

    /**
     * 返回一个空的 HashSet 对象
     *
     * @return
     */
    public static <T> Set<T> emptySet() {
        return Sets.newHashSet();
    }

    /**
     * 返回一个空的 ArrayList 对象 的 Iterator
     *
     * @param <T>
     * @return
     */
    public static <T> Iterator<T> emptyIterator() {
        return (Iterator<T>) emptyList().iterator();
    }

    /**
     * 把一个 map的key里 所有的中间的 下划线 _ 转换成驼峰方式 {@link StrUtil#toCamelCase(CharSequence)}， 返回去除后的 新的map
     *
     * @param map
     * @param lower 是否key转换驼峰后再转成全小写
     * @return
     */
    public static <V> Map<String, V> keyUnderline2CamelCase(Map<String, V> map, boolean lower) {
        if (isEmpty(map)) {
            return Maps.newHashMap();
        }

        HashMap<String, V> m = Maps.newHashMapWithExpectedSize(map.size());

        map.forEach((k, v) -> {
            String key = StrUtil.toCamelCase(k);
            if (lower) {
                key = key.toLowerCase();
            }
            m.put(key, v);
        });

        return m;
    }

    /**
     * 把一个 map的key里 所有的中间的 下划线 _ 转换成驼峰方式 {@link StrUtil#toCamelCase(CharSequence)}， 返回去除后的 新的map
     *
     * @param map
     * @return
     */
    public static <V> Map<String, V> keyUnderline2CamelCase(Map<String, V> map) {
        return keyUnderline2CamelCase(map, false);
    }

    /**
     * 把多个 map的key里 所有的中间的 下划线 _ 转换成驼峰方式 {@link StrUtil#toCamelCase(CharSequence)}， 返回去除后的 新的map
     *
     * @param maps
     * @return
     */
    public static <V> List<Map<String, V>> keyUnderline2CamelCase(Collection<Map<String, V>> maps) {
        ArrayList<Map<String, V>> list = Lists.newArrayListWithCapacity(maps.size());

        maps.forEach(m -> list.add(keyUnderline2CamelCase(m, false)));

        return list;
    }

    /**
     * 把一个 map的key里 所有的中间的 下划线 _ 转换成驼峰方式且全小写 {@link StrUtil#toCamelCase(CharSequence)}
     * ， 返回去除后的 新的map
     *
     * @param map
     * @return
     */
    public static <V> Map<String, V> keyUnderline2CamelCaseLower(Map<String, V> map) {
        return keyUnderline2CamelCase(map, true);
    }

    /**
     * 把多个 map的key里 所有的中间的 下划线 _ 转换成驼峰方式且全小写 {@link StrUtil#toCamelCase(CharSequence)}， 返回去除后的 新的map
     *
     * @param maps
     * @return
     */
    public static <V> List<Map<String, V>> keyUnderline2CamelCaseLower(Collection<Map<String, V>> maps) {
        ArrayList<Map<String, V>> list = Lists.newArrayListWithCapacity(maps.size());

        maps.forEach(m -> list.add(keyUnderline2CamelCase(m, true)));

        return list;
    }

    /**
     * 对集合按照指定长度分段，每一个段为单独的集合，返回这个集合的列表(子list用LinkedList)
     *
     * @param <T>        集合元素类型
     * @param collection 集合
     * @param size       每个段的长度
     * @return 分段列表
     */
    public static <T> List<List<T>> split2LinkedList(Collection<T> collection, int size) {
        final List<List<T>> result = new ArrayList<>();
        if (cn.hutool.core.collection.CollUtil.isEmpty(collection)) {
            return result;
        }

        LinkedList<T> subList = new LinkedList<>();
        for (T t : collection) {
            if (subList.size() >= size) {
                result.add(subList);
                subList = new LinkedList<>();
            }
            subList.add(t);
        }
        result.add(subList);
        return result;
    }
}
