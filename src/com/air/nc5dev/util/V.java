package com.air.nc5dev.util;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 值 工具类 <br/>
 * <br/>
 * <br/>
 *
 * @author 唐粟 Email:209308343@qq.com
 * @date 2020/8/22 14:43
 * @project
 * @Version
 */
public class V {
    ////////////////// 对象 字符串 null处理 //////////////////////

    /**
     * Object: 如果 v == null 返回 ifnull 否则返回 v
     *
     * @param v      值
     * @param ifNull 值 == null 需要返回的
     * @return
     */
    public static <T> T get(T v, T ifNull) {
        return null == v ? ifNull : v;
    }

    /**
     * 字符串:  如果 s == null 返回 ""空串 否则返回 s
     *
     * @param s
     * @return
     */
    public static String get(String s) {
        return null == s ? "" : s;
    }

    /**
     * Integer: 如果 o == null 返回 0 否则返回 o
     *
     * @param o
     * @return
     */
    public static int get(Integer o) {
        return null == o ? 0 : o;
    }

    /**
     * int: 如果 a <= 0 返回 b 否则返回 a
     *
     * @param a
     * @param b
     * @return
     */
    public static int getGtZero(int a, int b) {
        return a <= 0 ? b : a;
    }

    /**
     * List: 如果list ！= null 返回他自己，否则返回一个空list
     *
     * @param list
     * @return
     */
    public static <T> List<T> get(List<T> list) {
        return null == list ? Collections.emptyList() : list;
    }

    /**
     * Map: 如果 map ！= null 返回他自己，否则返回一个空  map
     *
     * @param map
     * @return
     */
    public static <K, V> Map<K, V> get(Map<K, V> map) {
        return null == map ? Collections.emptyMap() : map;
    }

    /**
     * Set: 如果 Set ！= null 返回他自己，否则返回一个空  Set
     *
     * @param set
     * @return
     */
    public static <T> Set<T> get(Set<T> set) {
        return null == set ? Collections.emptySet() : set;
    }

    /**
     * Bolean: 如果 s == null 返回 false 否则返回 s的布尔值
     *
     * @param s
     * @return
     */
    public static boolean is(Boolean s) {
        return get(s, Boolean.FALSE);
    }

    /**
     * 不是null
     *
     * @param o
     * @return
     */
    public static boolean notnull(Object o) {
        return null != o;
    }

    /**
     * 是null
     *
     * @param o
     * @return
     */
    public static boolean isnull(Object o) {
        return null == o;
    }

    /**
     * Bolean: 如果 s == null || < 1 返回 false 否则返回 true
     *
     * @param s
     * @return
     */
    public static boolean is(Integer s) {
        return get(s, 0) > 0;
    }

    /**
     * Object转String: 如果  v == null 返回 null 否则返回 v.toString()
     *
     * @param v
     * @return
     */
    public static String str4Obj(Object v) {
        return null == v ? null : v.toString();
    }

    /**
     * Object转int: 如果 o == null 返回 0 否则返回 o
     *
     * @param o
     * @return
     */
    public static int getInt(Object o) {
        return ConvertUtil.toInt(o, 0);
    }

    /**
     * Object转BigDecimal后设置小数位后返回： <br>
     * 根据指定小数位， 格式化 数字， 自动判断原始值类型  <br>
     *
     * @param value
     * @param digits
     * @param rm     多余小数处理方式,如果null 不处理小数
     * @return
     */
    public static BigDecimal formatNum(Object value, int digits, RoundingMode rm) {
        if (value == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal b = ConvertUtil.toBigDecimal(value, BigDecimal.ZERO);

        if (null != rm) {
            b.setScale(digits, rm);
        }

        return b;
    }

    /**
     * Object转BigDecimal后设置小数位后返回： <br>
     * 根据指定小数位， 格式化 数字， 自动判断原始值类型, 多余小数丢掉不要
     *
     * @param value
     * @param digits
     * @return
     */
    public static BigDecimal formatNum(Object value, int digits) {
        return formatNum(value, digits, RoundingMode.DOWN);
    }


    ///////////  日期时间   /////////////
    /**
     * 默认日期时间格式
     */
    public static final String DEFUALT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    /**
     * 默认日期格式
     */
    public static final String DEFUALT_DATE_FORMAT = "yyyy-MM-dd";
    /**
     * 默认时间格式
     */
    public static final String DEFUALT_TIME_FORMAT = "HH:mm:ss";

    /**
     * 根据默认格式 格式化  日期时间 ： 2020-09-16 10:56:01
     *
     * @param dateTime
     * @return
     */
    public static String formatDateTime(Date dateTime) {
        return formatDateTime(dateTime, DEFUALT_DATETIME_FORMAT);
    }

    /**
     * 根据默认格式 格式化  日期时间 ： 2020-09-16 10:56:01 转换成 java.util.Date
     *
     * @param dateTime
     * @return
     */
    public static Date toDateTime(String dateTime) throws ParseException {
        return new SimpleDateFormat(DEFUALT_DATETIME_FORMAT).parse(dateTime);
    }

    /**
     * 根据默认格式 格式化  日期  ： 2020-09-16
     *
     * @param date
     * @return
     */
    public static String formatDate(Date date) {
        return formatDateTime(date, DEFUALT_DATE_FORMAT);
    }

    /**
     * 根据默认格式 格式化  日期 ： 2020-09-16 转换成 java.util.Date
     *
     * @param date
     * @return
     */
    public static Date toDate(String date) throws ParseException {
        return new SimpleDateFormat(DEFUALT_DATE_FORMAT).parse(date);
    }

    /**
     * 根据默认格式 格式化  时间 ： 10:56:01
     *
     * @param time
     * @return
     */
    public static String formatTime(Date time) {
        return formatDateTime(time, DEFUALT_TIME_FORMAT);
    }

    /**
     * 根据默认格式 格式化  时间 ： 10:56:01 转换成 java.util.Date
     *
     * @param time
     * @return
     */
    public static Date toTime(String time) throws ParseException {
        return new SimpleDateFormat(DEFUALT_TIME_FORMAT).parse(time);
    }

    /**
     * 根据 格式 格式化  日期时间
     *
     * @param date   日期时间  比如 2020-9-16 11:19:24，  2020-9-16 ， 11:19:24
     * @param format jdk格式字符串
     * @return
     */
    public static String formatDateTime(Date date, String format) {
        return new SimpleDateFormat(format).format(date);
    }

    /**
     * 根据最佳猜测解析时间  时间： 09:10 转换成 java.util.Date
     *
     * @param date
     * @return
     */
    public static Date parseTime(String str) throws ParseException {
        char[] chars = str.toCharArray();

        ArrayList<Character> h = Lists.newArrayList();
        ArrayList<Character> m = Lists.newArrayList();
        ArrayList<Character> s = Lists.newArrayList();
        ArrayList<Character> ss = Lists.newArrayList();

        int level = 0;
        for (int i = 0; i < chars.length; i++) {
            if (level > 3) {
                break;
            }

            if (chars[i] >= '0' && chars[i] <= '9') {
                switch (level) {
                    case 0:
                        //小时
                        h.add(chars[i]);
                        break;
                    case 1:
                        //分钟
                        m.add(chars[i]);
                        break;
                    case 2:
                        //秒
                        s.add(chars[i]);
                        break;
                    case 3:
                        //秒之后的
                        ss.add(chars[i]);
                        break;
                }
            } else {
                ++level;
            }
        }

        final char split = ':';

        String par = StringUtils.repeat('H', h.size()) + split
                + StringUtils.repeat('m', m.size()) + split
                + StringUtils.repeat('s', s.size()) + split
                + StringUtils.repeat('S', ss.size());

        str = Joiner.on("").skipNulls().join(h) + split
                + Joiner.on("").skipNulls().join(m) + split
                + Joiner.on("").skipNulls().join(s) + split
                + Joiner.on("").skipNulls().join(ss);

        return new SimpleDateFormat(par).parse(str);
    }

    //////////////////// JDK8 新日期时间类   ///////////////////////////
    /**
     * JDK8 日期时间 默认格式化器
     */
    public static final DateTimeFormatter JDK8_DATETIME_FORMART = DateTimeFormatter.ofPattern(DEFUALT_DATETIME_FORMAT);
    /**
     * JDK8 日期 默认格式化器
     */
    public static final DateTimeFormatter JDK8_DATE_FORMART = DateTimeFormatter.ofPattern(DEFUALT_DATE_FORMAT);
    /**
     * JDK8 时间 默认格式化器
     */
    public static final DateTimeFormatter JDK8_TIME_FORMART = DateTimeFormatter.ofPattern(DEFUALT_TIME_FORMAT);


    /**
     * 根据默认格式 格式化  日期时间 ： 2020-09-16 10:56:01
     *
     * @param dateTime
     * @return
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return JDK8_DATETIME_FORMART.format(dateTime);
    }

    /**
     * JDK8: 根据默认格式 格式化  日期时间 ： 2020-09-16 10:56:01 转换成 java.util.Date
     *
     * @param dateTime
     * @return
     */
    public static LocalDateTime toDateTimeJdk8(String dateTime) {
        return LocalDateTime.parse(dateTime, JDK8_DATETIME_FORMART);
    }

    /**
     * 根据默认格式 格式化  日期  ： 2020-09-16
     *
     * @param date
     * @return
     */
    public static String formatDate(LocalDate date) {
        return JDK8_DATE_FORMART.format(date);
    }

    /**
     * JDK8: 根据默认格式 格式化  日期 ： 2020-09-16 转换成 java.util.Date
     *
     * @param date
     * @return
     */
    public static LocalDate toDateJdk8(String date) {
        return LocalDate.parse(date, JDK8_DATE_FORMART);
    }

    /**
     * 根据默认格式 格式化  时间 ： 10:56:01
     *
     * @param time
     * @return
     */
    public static String formatTime(LocalTime time) {
        return JDK8_TIME_FORMART.format(time);
    }

    /**
     * JDK8: 根据默认格式 格式化  时间 ： 10:56:01 转换成 java.util.Date
     *
     * @param time
     * @return
     */
    public static LocalTime toTimeJdk8(String time) {
        return LocalTime.parse(time, JDK8_TIME_FORMART);
    }

    /////////////////  其他日期时间工具  ////////////
    /**
     * 常见的 时间 日期 JDK格式
     */
    public static String[] JDK_DATE_TIME_FORMARTS = {
            DEFUALT_DATE_FORMAT, DEFUALT_DATETIME_FORMAT, DEFUALT_TIME_FORMAT, "yyyy-MM-dd HH:mm", "yyyy-MM",
            "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm", "yyyy/MM",
            "yyyy.MM.dd", "yyyy.MM.dd HH:mm:ss", "yyyy.MM.dd HH:mm", "yyyy.MM"
    };

    /**
     * 返回当前最新日期时间的字符串 2020-09-17 13:05:13
     *
     * @return
     */
    public static String nowDateTime() {
        return JDK8_DATETIME_FORMART.format(LocalDateTime.now());
    }

    /**
     * 返回当前最新日期时间的时间搓(纳秒)
     *
     * @return
     */
    public static long nowNanoTime() {
        return System.nanoTime();
    }

    /**
     * 返回当前最新日期时间的时间搓(毫秒)
     *
     * @return
     */
    public static long nowMillis() {
        return System.currentTimeMillis();
    }

    /**
     * 返回当前最新日期时间的字符串 2020-09-17
     *
     * @return
     */
    public static String nowDate() {
        return JDK8_DATE_FORMART.format(LocalDate.now());
    }

    /**
     * 返回当前最新时间的字符串  13:05:13
     *
     * @return
     */
    public static String nowTime() {
        return JDK8_TIME_FORMART.format(LocalTime.now());
    }

    /**
     * 返回当前最新日期时间  java.util.Date 如2020-09-17 13:05:13
     *
     * @return
     */
    public static Date now() {
        return new Date();
    }

    /**
     * 是否包含字符串
     *
     * @param str  验证字符串
     * @param strs 字符串组
     * @return 包含返回true
     */
    public static boolean inStringIgnoreCase(String str, String... strs) {
        if (str != null && strs != null) {
            for (String s : strs) {
                if (str.equalsIgnoreCase(StringUtils.trim(s))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 根据时间字符串的格式 来返回猜测的 格式正则
     *
     * @param dateStr
     * @return
     */
    public static String getPatternDateForMartStr(String dateStr) {
        final int count = V.countCharRepetCount(dateStr, '-');
        if (count == 1) {
            if (dateStr.length() == 7) {
                return "yyyy-MM";
            }

            if (dateStr.length() == 6) {
                return "yyyy-M";
            }
        }

        if (count == 2) {
            if (dateStr.length() == 10) {
                return "yyyy-MM-dd";
            }

            if (dateStr.length() == 8) {
                return "yyyy-M-d";
            }

            if (dateStr.length() == 9) {
                if (dateStr.split("-")[1].length() == 1) {
                    return "yyyy-M-dd";
                } else {
                    return "yyyy-MM-d";
                }
            }
        }

        return "yyyy-MM-dd HH:mm:ss";
    }

    /**
     * 判断  c 在 字符串里重复多少次
     *
     * @param str
     * @param c
     * @return
     */
    public static int countCharRepetCount(String str, char c) {
        char[] chars = str.toCharArray();
        int count = 0;
        for (char aChar : chars) {
            if (c == aChar) {
                ++count;
            }
        }

        return count;
    }

    /**
     * 把一个 list的集合，根据父级属性和子集属性 分析称 树状
     *
     * @param vos
     * @param idField     - vo的父id对应的属性名（只支持 数字或字符串！！！！！）
     * @param parentField - 父id的属性（只支持 数字或字符串！！！！！）
     * @param childField  - 子集合的属性名 （支持 java.util.Collection 或 数组）
     * @param <T>
     * @return
     */
    public static <T> List<T> toTree(List<T> vos, String idField, String parentField, String childField) {
        if (CollUtil.isEmpty(vos)) {
            return vos;
        }

        //缓存 ，快速查找父vo的
        HashMap<Object, T> allMap = Maps.newHashMapWithExpectedSize(vos.size());
        for (T vo : vos) {
            allMap.put(ReflectUtil.getFieldValue(vo, idField), vo);
        }

        ArrayList<T> list = Lists.newArrayListWithCapacity(vos.size());
        Object idValue;
        Object pValue;
        Object cValue;
        Collection<T> childs;
        Collection<T> pChilds;
        List<T> directChild;
        HashMap<Object, Collection<T>> id2Childs = Maps.newHashMapWithExpectedSize(vos.size());

        for (T vo : vos) {
            //拿到 id
            idValue = ReflectUtil.getFieldValue(vo, idField);
            //拿到当前vo的 父id
            pValue = ReflectUtil.getFieldValue(vo, parentField);

            childs = id2Childs.get(idValue);
            if (childs == null) {
                childs = Sets.newHashSetWithExpectedSize(vos.size());
                id2Childs.put(idValue, childs);
            }

            pChilds = id2Childs.get(pValue);
            if (pChilds == null) {
                pChilds = Sets.newHashSetWithExpectedSize(vos.size());
                id2Childs.put(pValue, pChilds);
            }

            directChild = findDirectChild(vos, idValue, parentField);
            childs.addAll(directChild);

            if (pValue == null || !allMap.containsKey(pValue)) {
                ///没有父id 根目录！
                list.add(vo);
            } else {
                pChilds.add(vo);
            }
        }

        Class<?> aClass = list.get(0).getClass();
        Field childF = ReflectUtil.getField(aClass, childField);
        boolean isList = Collection.class.isAssignableFrom(childF.getType());
        for (T vo : vos) {
            //拿到 id
            idValue = ReflectUtil.getFieldValue(vo, idField);
            childs = id2Childs.get(idValue);
            if (childs != null) {
                if (isList) {
                    ReflectUtil.setFieldValue(vo, childF, childs);
                } else {
                    ReflectUtil.setFieldValue(vo, childF, toArray(childs));
                }
            }
        }

        return list;
    }

    /**
     * 集合转换成数组，数组类型是集合里元素的类型
     *
     * @param vos
     * @param <T>
     * @return
     */
    public static <T> T[] toArray(Collection<T> vos) {
        Class<?> aClass = vos.iterator().next().getClass();
        T[] os = (T[]) Array.newInstance(aClass, vos.size());

        Iterator<T> iterator = vos.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            os[i++] = iterator.next();
        }

        return os;
    }

    /**
     * 把一个 list的集合，根据父级属性和父id，找到 他的直系第一级孩子
     *
     * @param vos
     * @param id
     * @param parentField
     * @param <T>
     * @return
     */
    public static <T> List<T> findDirectChild(List<T> vos, Object id, String parentField) {
        if (CollUtil.isEmpty(vos)) {
            return Collections.emptyList();
        }

        ArrayList<T> list = Lists.newArrayListWithCapacity(vos.size());
        Object pValue;
        for (T vo : vos) {
            //拿到当前vo的 父id
            pValue = ReflectUtil.getFieldValue(vo, parentField);

            if (pValue == id || id.equals(pValue)) {
                list.add(vo);
            }
        }

        return list;
    }

    /**
     * 根据你指定的 集合，查询里面对象 属性fieldName的值 等于 value的对象，找不到返回null
     *
     * @param vos
     * @param fieldName
     * @param value
     * @return
     */
    public static <T> T find(Collection<T> vos, String fieldName, String value) {
        if (CollUtil.isEmpty(vos)) {
            return null;
        }
        Object fValue;
        for (T vo : vos) {
            if (vo == null) {
                continue;
            }

            fValue = ReflectUtil.getFieldValue(vo, fieldName);

            if (fValue == value) {
                return vo;
            }

            if (value != null && value.equals(fValue)) {
                return vo;
            }
        }

        return null;
    }

    /**
     * 根据你指定的 数组，查询里面对象  值 等于 value的对象，找不到返回null
     *
     * @param values
     * @param value
     * @return
     */
    public static <T> T find(T[] values, T value) {
        if (CollUtil.isEmpty(values)) {
            return null;
        }

        for (T t : values) {
            if (t == value) {
                return t;
            }

            if (value != null && value.equals(t)) {
                return t;
            }
        }

        return null;
    }

    /**
     * 转换成 List string集合
     *
     * @param querySingleColumnRows
     * @return
     */
    public static List<String> toStringList(List<Object> list) {
        ArrayList<String> re = Lists.newArrayListWithCapacity(list.size());

        for (Object o : list) {
            re.add(str4Obj(o));
        }

        return re;
    }

    /**
     * 快速获取一个 完整 uuid
     *
     * @return
     */
    public static String uuid() {
        return UUID.randomUUID().toString();
    }

    /**
     * 快速获取一个 简单 uuid(去掉- _等)
     *
     * @return
     */
    public static String simpleuuid() {
        return StringUtils.remove(StringUtils.remove(UUID.randomUUID().toString(), '-'), '_');
    }

    /**
     * 把一个对象 转换成 toBigDecimal
     *
     * @param value
     * @param sclac
     * @param roundingMode
     * @param defulatValue null或者解析失败返回的值
     * @return
     */
    public static BigDecimal toBigDecimal(Object value, int sclac, RoundingMode roundingMode, BigDecimal defulatValue) {
        if (value == null) {
            return defulatValue;
        }

        BigDecimal b = null;
        try {
            if (value instanceof BigDecimal) {
                b = ((BigDecimal) value);
            } else {
                b = new BigDecimal(value.toString());
            }

            b.setScale(sclac, roundingMode);
        } catch (Exception e) {
        }

        return b == null ? defulatValue : b;
    }

    /**
     * 把一个对象 转换成 toBigDecimal
     *
     * @param value
     * @param sclac
     * @param roundingMode
     * @return null或者解析失败返回 null
     */
    public static BigDecimal toBigDecimal(Object value, int sclac, RoundingMode roundingMode) {
        return toBigDecimal(value, sclac, roundingMode, null);
    }

    /**
     * 把一个对象 转换成 toBigDecimal
     *
     * @param value
     * @param sclac        小数位， 多于的小数 直接扔掉！
     * @param roundingMode
     * @return null或者解析失败返回 null
     */
    public static BigDecimal toBigDecimal(Object value, int sclac) {
        return toBigDecimal(value, sclac, RoundingMode.DOWN);
    }

    /**
     * 把一个对象 转换成 toBigDecimal(8个小数位， 多于的小数 直接扔掉！)
     *
     * @param value
     * @param roundingMode
     * @return null或者解析失败返回 null
     */
    public static BigDecimal toBigDecimal(Object value) {
        return toBigDecimal(value, 8);
    }

    public static boolean isTrue(Object v) {
        if (v == null) {
            return false;
        }

        if (v instanceof Boolean) {
            return ((Boolean) v).booleanValue();
        }

        String s = v.toString().toLowerCase();
        return "1".equals(s) || "true".equals(s) || "y".equals(s);
    }
}
