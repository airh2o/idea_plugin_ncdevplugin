package com.air.nc5dev.util;

import cn.hutool.core.util.StrUtil;
import com.air.nc5dev.util.idea.LogUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import nc.vo.pub.AggregatedValueObject;
import nc.vo.pub.BusinessException;
import nc.vo.pub.CircularlyAccessibleValueObject;
import nc.vo.pub.SuperVO;
import nc.vo.pub.lang.UFBoolean;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDateTime;
import nc.vo.pub.lang.UFDouble;
import nc.vo.trade.pub.IExAggVO;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 对象工具类，提供bean的复制等 <br/>
 * <br/>
 *
 * @author air Email:209308343@qq.com
 * @version NC505, JDK1.5
 * @date 2019年12月5日 下午4:13:13
 */
public class ObjUtil {
    static Map<Class, List<Field>> getClassAllDeclaredFieldsCache = new ConcurrentHashMap();
    static Map<Class, List<Method>> getClassAllMethodCache = new ConcurrentHashMap();
    static Map<Object, Object> getSetObjectFiledValueHandleCache = new ConcurrentHashMap();

    public static <T> T mustNotNull(T obj, String msg) {
        if (obj == null) {
            throw new NullPointerException(msg);
        }

        return obj;
    }

    /**
     * 把一个 double 的小数位 按指定位数 返回 ，舍弃多余的小数位
     *
     * @param d
     * @return
     */
    public static UFDouble setUFDoublScale(UFDouble d, int deep, int model) {
        return d == null ? null : d.setScale(deep, model);
    }

    /**
     * 把指定的数字字段 有值的且不是负数的，变成负数
     *
     * @param vo
     * @param attrs
     */
    public static void uFDouble2Negative(CircularlyAccessibleValueObject vo, String... attrs) {
        if (vo == null) {
            return;
        }

        if (CollUtil.isEmpty(attrs)) {
            return;
        }

        UFDouble d;
        for (String attr : attrs) {
            attr = attr.toLowerCase();

            d = (UFDouble) vo.getAttributeValue(attr);
            if (d == null || d.doubleValue() <= 0) {
                continue;
            }

            d = UFDouble.ZERO_DBL.sub(d);
            vo.setAttributeValue(attr, d);
        }
    }

    /**
     * 用from的非null字段的值 填充 to的属性名和属性类型一样的字段
     *
     * @param from
     * @param to
     * @param skips 跳过的,大小写不敏感
     */
    public static void fillBySourceVONotNull(CircularlyAccessibleValueObject from, CircularlyAccessibleValueObject to
            , String... skips) {
        fillBySourceVO(from, to, new Functions<Object, Boolean>() {
            @Override
            public Boolean apply(Object... pars) {
                return CollUtil.getFirst(pars, 3) != null;
            }
        }, skips);
    }

    /**
     * 用from的非空(null 或者 空字符串 或者 UFDouble=0)字段的值 填充 to的属性名和属性类型一样的字段
     *
     * @param from
     * @param to
     * @param skips 跳过的,大小写不敏感
     */
    public static void fillBySourceVONotNullAndEmpty(CircularlyAccessibleValueObject from, CircularlyAccessibleValueObject to
            , String... skips) {
        fillBySourceVO(from
                , to
                , new Functions<Object, Boolean>() {
                    @Override
                    public Boolean apply(Object... pars) {
                        Object v = CollUtil.getFirst(pars, 3);
                        if (v == null) {
                            return false;
                        }

                        if (v instanceof String) {
                            return StrUtil.isNotBlank((CharSequence) v);
                        }

                        if (v instanceof UFDouble) {
                            return !UFDouble.ZERO_DBL.equals(v);
                        }

                        return true;
                    }
                }
                , skips);
    }

    /**
     * 用from的 字段的值 填充 to的属性名和属性类型一样的字段
     *
     * @param from
     * @param to
     * @param set   返回true复制，fals不复制此字段， 参数列表 from,to,字段名,字段值
     * @param skips 跳过的,大小写不敏感
     */
    public static void fillBySourceVO(CircularlyAccessibleValueObject from, CircularlyAccessibleValueObject to
            , Functions<Object, Boolean> set, String... skips) {
        if (from == null || to == null) {
            return;
        }
        if (skips == null) {
            skips = new String[0];
        }

        String[] ns = from.getAttributeNames();
        Set<String> ns2 = CollUtil.asSet(to.getAttributeNames());

        Object obj;
        boolean skipName = false;
        for (String n : ns) {
            obj = from.getAttributeValue(n);

            skipName = false;
            for (String skip : skips) {
                if (skip.equalsIgnoreCase(n)) {
                    skipName = true;
                    break;
                }
            }

            if (skipName) {
                continue;
            }

            if (set.apply(from, to, n, obj)) {
                to.setAttributeValue(n, obj);
            }
        }
    }


    /**
     * 这是因为有些代码不知道为啥 数组类型根本不是对象本身类型 还要强转。
     *
     * @param aggs
     * @param <T>
     * @return
     */
    public static <T extends AggregatedValueObject> T[] fixArrayTypes(T[] aggs) {
        if (aggs == null) {
            return aggs;
        }

        T[] naggs = aggs.getClass().getName().equals("[L" + aggs[0].getClass().getName() + ';') ? aggs
                : (T[]) Array.newInstance(aggs[0].getClass(), aggs.length);

        for (int i = 0; i < aggs.length; i++) {
            T agg = aggs[i];
            naggs[i] = agg;

            if (agg instanceof IExAggVO) {
                IExAggVO eagg = (IExAggVO) agg;
                String[] cs = eagg.getTableCodes();
                if (cs == null) {
                    continue;
                }

                for (String c : cs) {
                    CircularlyAccessibleValueObject[] bs = eagg.getTableVO(c);
                    if (bs == null) {
                        continue;
                    }

                    if (bs.getClass().getName().equals("[L" + bs[0].getClass().getName() + ';')) {
                        continue;
                    }

                    CircularlyAccessibleValueObject[] nbs = (CircularlyAccessibleValueObject[]) Array.newInstance(bs[0].getClass(), bs.length);
                    for (int x = 0; x < bs.length; x++) {
                        nbs[x] = bs[x];
                    }

                    eagg.setTableVO(c, nbs);
                }
            } else {
                CircularlyAccessibleValueObject[] bs = aggs[i].getChildrenVO();
                if (bs == null) {
                    continue;
                }

                if (bs.getClass().getName().equals("[L" + bs[0].getClass().getName() + ';')) {
                    continue;
                }

                CircularlyAccessibleValueObject[] nbs = (CircularlyAccessibleValueObject[]) Array.newInstance(bs[0].getClass(), bs.length);
                for (int x = 0; x < bs.length; x++) {
                    nbs[i] = bs[i];
                }

                naggs[i].setChildrenVO(nbs);
            }
        }

        return naggs;
    }

    public static void setScale(AggregatedValueObject agg, int scale, int roundModel) {
        if (agg == null) {
            return;
        }

        setScale(agg.getParentVO(), scale, roundModel);

        CircularlyAccessibleValueObject[] cs = agg.getChildrenVO();
        if (cs == null) {
            return;
        }

        for (CircularlyAccessibleValueObject c : cs) {
            setScale(c, scale, roundModel);
        }
    }

    public static void setScale(CircularlyAccessibleValueObject vo, int scale, int roundModel) {
        if (vo == null) {
            return;
        }

        Set<String> vs = getFullAttrNames(vo);
        if (vs == null) {
            return;
        }

        for (String v : vs) {
            Object vu = vo.getAttributeValue(v);
            if (vu == null) {
                continue;
            }

            if (vu instanceof UFDouble) {
                ((UFDouble) vu).setScale(scale, roundModel);
            }
        }
    }

    public static void trimZero(AggregatedValueObject agg) {
        if (agg == null) {
            return;
        }

        trimZero(agg.getParentVO());

        CircularlyAccessibleValueObject[] cs = agg.getChildrenVO();
        if (cs == null) {
            return;
        }

        for (CircularlyAccessibleValueObject c : cs) {
            trimZero(c);
        }
    }

    public static void trimZero(CircularlyAccessibleValueObject vo) {
        if (vo == null) {
            return;
        }

        Set<String> vs = getFullAttrNames(vo);
        if (vs == null) {
            return;
        }

        for (String v : vs) {
            Object vu = vo.getAttributeValue(v);
            if (vu == null) {
                continue;
            }

            if (vu instanceof UFDouble) {
                ((UFDouble) vu).setTrimZero(true);
            }
        }
    }

    public static <T extends CircularlyAccessibleValueObject> ArrayList<Map<String, Object>> vos2Map(Collection<T> aggs) {
        if (aggs == null) {
            return null;
        }

        ArrayList<Map<String, Object>> as = com.google.common.collect.Lists.newArrayList();

        for (CircularlyAccessibleValueObject agg : aggs) {
            as.add(vo2Map(agg));
        }

        return as;
    }

    public static Map<String, Object> vo2MapHasNull(CircularlyAccessibleValueObject h) {
        HashMap<String, Object> map = Maps.newHashMap();

        if (h != null) {
            Set<String> ns = null;

            try {
                ns = getFullAttrNames(h);
            } catch (Throwable e) {
                ns = CollUtil.asSet(h.getAttributeNames());
            }

            if (ns != null) {
                for (String n : ns) {
                    Object v = null;
                    try {
                        v = h.getAttributeValue(n);
                    } catch (Throwable e) {
                        e.printStackTrace();
                        continue;
                    }

                    map.put(n, v);
                }
            }
        }

        return map;
    }

    public static Map<String, Object> vo2Map(CircularlyAccessibleValueObject h) {
        HashMap<String, Object> map = Maps.newHashMap();

        if (h != null) {
            Set<String> ns = null;

            try {
                ns = getFullAttrNames(h);
            } catch (Throwable e) {
                ns = CollUtil.asSet(h.getAttributeNames());
            }

            if (ns != null) {
                for (String n : ns) {
                    Object v = null;
                    try {
                        v = h.getAttributeValue(n);
                        if (v == null) {
                            continue;
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                        continue;
                    }

                    map.put(n, v);
                }
            }
        }

        return map;
    }

    public static <T extends AggregatedValueObject> List<Map<String, Object>> aggs2Map(Collection<T> aggs) {
        if (aggs == null) {
            return null;
        }

        List<Map<String, Object>> as = com.google.common.collect.Lists.newArrayList();

        for (AggregatedValueObject agg : aggs) {
            as.add(agg2Map(agg));
        }

        return as;
    }

    public static Map<String, Object> agg2Map(AggregatedValueObject agg) {
        if (agg == null) {
            return null;
        }

        HashMap<String, Object> map = Maps.newHashMap();

        CircularlyAccessibleValueObject h = agg.getParentVO();
        if (h != null) {
            Set<String> ns = getFullAttrNames(h);
            if (ns != null) {
                HashMap<String, Object> hm = Maps.newHashMap();
                map.put("head", hm);
                for (String n : ns) {
                    Object v = h.getAttributeValue(n);
                    if (v == null) {
                        continue;
                    }

                    hm.put(n, v);
                }
            }
        }

        HashMap<String, List<Map<String, Object>>> bodys = Maps.newHashMap();
        map.put("bodys", bodys);
        if (agg instanceof IExAggVO) {
            IExAggVO eagg = (IExAggVO) agg;
            String[] ts = eagg.getTableCodes();
            if (CollUtil.notEmpty(ts)) {
                for (String code : ts) {
                    CircularlyAccessibleValueObject[] cs = eagg.getTableVO(code);
                    if (CollUtil.notEmpty(cs)) {
                        List<Map<String, Object>> blist = Lists.newArrayList();
                        bodys.put(code, blist);
                        for (CircularlyAccessibleValueObject b : cs) {
                            HashMap<String, Object> bm = Maps.newHashMap();
                            blist.add(bm);

                            Set<String> ns = getFullAttrNames(b);
                            if (ns == null) {
                                continue;
                            }
                            for (String n : ns) {
                                Object v = b.getAttributeValue(n);
                                if (v == null) {
                                    continue;
                                }

                                bm.put(n, v);
                            }
                        }
                    }
                }
            }
        } else {
            CircularlyAccessibleValueObject[] bs = agg.getChildrenVO();
            if (CollUtil.notEmpty(bs)) {
                List<Map<String, Object>> blist = Lists.newArrayList();
                bodys.put("BODY", blist);

                for (CircularlyAccessibleValueObject b : bs) {
                    HashMap<String, Object> bm = Maps.newHashMap();
                    blist.add(bm);

                    Set<String> ns = getFullAttrNames(b);
                    if (ns == null) {
                        continue;
                    }
                    for (String n : ns) {
                        Object v = b.getAttributeValue(n);
                        if (v == null) {
                            continue;
                        }

                        bm.put(n, v);
                    }
                }
            }
        }

        return map;
    }

    public static <AGG extends AggregatedValueObject, HVO extends CircularlyAccessibleValueObject
            , BVO extends CircularlyAccessibleValueObject> List<AGG> toAggs(JSONArray jsonArray, Class<AGG> aggClass
            , Class<HVO> hvoClass, Class<BVO> bvoClass) {
        if (jsonArray == null || jsonArray.size() < 1) {
            return new ArrayList();
        }

        ArrayList<AGG> aggs = Lists.newArrayListWithExpectedSize(jsonArray.size());
        JSONObject jbagg = null;
        JSONObject jbh = null;
        JSONArray jsonBody = null;
        AGG agg = null;
        HVO h;
        BVO b;
        List<BVO> bs;
        for (int i = 0; i < jsonArray.size(); i++) {
            try {
                jbagg = jsonArray.getJSONObject(i);
            } catch (Throwable e) {
            }
            if (jbagg == null) {
                continue;
            }
            agg = null;
            try {
                jbh = jbagg.containsKey("parent") ? jbagg.getJSONObject("parent") : jbagg.getJSONObject("head");
                if (jbh != null) {
                    agg = aggClass.newInstance();
                    aggs.add(agg);
                    h = hvoClass.newInstance();
                    agg.setParentVO(h);

                    copyJsonFields2Bean(jbh, h);
                }
            } catch (Throwable e) {
            }

            try {
                if (jbagg.get("bodys") instanceof JSONObject) {
                    jsonBody = jbagg.getJSONObject("bodys").getJSONArray("BODY");
                } else {
                    jsonBody = jbagg.getJSONArray("bodys");
                }

                if (jsonBody != null && jsonBody.size() > 0) {
                    bs = new ArrayList();
                    for (int x = 0; x < jsonBody.size(); x++) {
                        try {
                            jbh = jsonBody.getJSONObject(x);
                            if (jbh != null) {
                                b = bvoClass.newInstance();
                                bs.add(b);
                                copyJsonFields2Bean(jbh, b);
                            }
                        } catch (Throwable e) {
                        }
                    }

                    if (!bs.isEmpty()) {
                        if (agg == null) {
                            agg = aggClass.newInstance();
                            aggs.add(agg);
                        }
                        agg.setChildrenVO(bs.toArray((CircularlyAccessibleValueObject[]) Array.newInstance(bvoClass, 0)));
                    }
                }
            } catch (Throwable e) {
            }
        }

        return aggs;
    }

    /**
     * 把一个JsonObject 节点属性 复制到  另一个对象
     * <br/>
     * <br/>
     *
     * @param json JsonObject
     * @param to   要复制到这个对象
     * @throws BusinessException
     * @author air Email:209308343@qq.com
     * @date 2019年12月5日 下午4:14:53
     */
    public static void copyJsonFields2Bean(JsonObject json, Object to) throws BusinessException {
        if (json == null) {
            return;
        }
        Class<? extends Object> toClass = to.getClass();
        Set<Entry<String, JsonElement>> jsonEntrys = json.entrySet();
        Iterator<Entry<String, JsonElement>> jsonEntrysIterator = jsonEntrys.iterator();
        Entry<String, JsonElement> jsonEntry;
        while (jsonEntrysIterator.hasNext()) {
            jsonEntry = jsonEntrysIterator.next();
            if (null == jsonEntry.getValue().getAsString()) {
                continue;
            }

            setObjFiledValueAuto(to, jsonEntry.getKey(), jsonEntry.getValue().getAsString());
        }
    }

    public static void copyJsonFields2Bean(JSONObject json, Object to) throws BusinessException {
        if (json == null) {
            return;
        }
        Class<? extends Object> toClass = to.getClass();
        Set<String> keys = json.keySet();
        for (String key : keys) {
            if (json.getString(key) == null) {
                continue;
            }

            setObjFiledValueAuto(to, key, json.getString(key));
        }
    }

    /**
     * 把一个Map 节点属性 复制到  另一个对象
     * <br/>
     * <br/>
     *
     * @param map Map
     * @param to  要复制到这个对象
     * @throws BusinessException
     * @author air Email:209308343@qq.com
     * @date 2019年12月5日 下午4:14:53
     */
    public static void copyMap2Bean(Map map, Object to) throws BusinessException {
        Class<? extends Object> toClass = to.getClass();
        Set<Entry<String, Object>> entrys = map.entrySet();
        Iterator<Entry<String, Object>> entrysIterator = entrys.iterator();
        Entry<String, Object> entry;
        Field f;
        while (entrysIterator.hasNext()) {
            entry = entrysIterator.next();
            if (null == entry.getValue() || null == entry.getValue().toString()) {
                continue;
            }

            setObjFiledValueAuto(to, entry.getKey(), entry.getValue().toString());
        }
    }


    /**
     * 把一个 对象的 属性 复制到  另一个对象<br/>
     * 支持NC的m_<br/>
     * <br/>
     *
     * @param from 要复制的
     * @param to   要复制到这个对象
     * @throws BusinessException
     * @author air Email:209308343@qq.com
     * @date 2019年12月5日 下午4:14:53
     */
    public static void copyFields(Object from, Object to) throws BusinessException {
        copyFields(from, to, null);
    }

    /**
     * 把一个 对象的 属性 复制到  另一个对象<br/>
     * 支持NC的m_<br/>
     * <br/>
     *
     * @param from 要复制的
     * @param to   要复制到这个对象
     * @throws BusinessException
     * @author air Email:209308343@qq.com
     * @date 2019年12月5日 下午4:14:53
     */
    public static void copyFields(Object from, Object to, String... skipFieldNames) throws BusinessException {
        if (from == null || to == null) {
            return;
        }

        if (skipFieldNames == null) {
            skipFieldNames = new String[0];
        }
        Set<String> skipFieldNameSet = CollUtil.asSet(skipFieldNames);

        Map<String, Object> kvs = new HashMap(101);
        if (from instanceof Map) {
            kvs = (Map<String, Object>) from;
        } else {
            Field[] fields = from.getClass().getDeclaredFields();
            Field f;
            Object value;
            for (int i = 0; i < fields.length; i++) {
                f = fields[i];
                if (Modifier.isStatic(f.getModifiers())) {
                    continue;
                }
                value = getObjectFiledValue(from, f.getName());
                if (null == value) {
                    continue;
                }

                kvs.put(f.getName(), value);

                if (f.getName().startsWith("m_")) {
                    kvs.put(f.getName().substring(2), value);
                }
            }
        }

        if (to instanceof Map) {
            Map toMap = (Map) to;
            Set<String> keys = kvs.keySet();
            for (String key : keys) {
                if (skipFieldNameSet.contains(key)) {
                    continue;
                }
                toMap.put(key, kvs.get(key));
            }
        } else {
            Set<String> keys = kvs.keySet();
            for (String key : keys) {
                if (skipFieldNameSet.contains(key)) {
                    continue;
                }
                setObjFiledValueAuto(to, key, kvs.get(key));
            }
        }
    }

    /**
     * 把一个 NC SuperVO 对象的 属性 复制到  另一个NC SuperVO对象 <br/>
     * <br/>
     * <br/>
     *
     * @param from 要复制的
     * @param to   要复制到这个对象
     * @throws BusinessException
     * @author air Email:209308343@qq.com
     * @date 2019年12月5日 下午4:14:53
     */
    public static void copySuperVOFields(SuperVO from, SuperVO to) throws BusinessException {
        copySuperVOFields(from, to, null);
    }

    /**
     * 把一个 NC SuperVO 对象的 属性 复制到  另一个NC SuperVO对象 <br/>
     * <br/>
     * <br/>
     *
     * @param from           要复制的
     * @param to             要复制到这个对象
     * @param skipFieldNames 要跳过的字段(可修改list！)
     * @throws BusinessException
     * @author air Email:209308343@qq.com
     * @date 2019年12月5日 下午4:14:53
     */
    public static void copySuperVOFields(SuperVO from, SuperVO to, List<String> skipFieldNames) throws BusinessException {
        Set<String> attributeNames = getFullAttrNames(from);
        Object value;

        boolean isSikp = false;
        for (String attributeName : attributeNames) {
            isSikp = false;
            if (null != skipFieldNames && !skipFieldNames.isEmpty()) {
                for (String skipFieldName : skipFieldNames) {
                    if (attributeName.equals(skipFieldName)) {
                        isSikp = true;
                        break;
                    }
                }
            }

            if (isSikp) {
                skipFieldNames.remove(attributeName);
                continue;
            }

            value = from.getAttributeValue(attributeName);
            if (null == value) {
                to.setAttributeValue(attributeName, value);
                continue;
            }

            setObjectFiledValue(to, attributeName, value);
        }
    }

    /**
     * 设置一个对象 的字段的值  <br/>
     * <br/>
     *
     * @param name  字段名字
     * @param value 值
     * @throws BusinessException
     * @author air Email:209308343@qq.com
     * @date 2019-8-17 下午1:32:18
     */
    public static void setObjectFiledValue(Object vo, String name, Object value) throws BusinessException {
        Class<?> c = vo.getClass();
        if (vo instanceof Map) {
            ((Map) vo).put(name, value);
            return;
        }

        try {
            Method m = null;
            Field filed = null;
            Object setHandle = null;

            String cacheKey = c.getName() + ":SET:" + name;
            setHandle = getSetObjectFiledValueHandleCache.get(cacheKey);
            if (setHandle == null) {
                String name2 = name;
                if (name.startsWith("m_")) {
                    name2 = name.substring(2);
                }

                List<Method> ms = getClassAllMethod(c);
                for (Method method : ms) {
                    if (method.getName().equalsIgnoreCase("set" + name)
                            || method.getName().equalsIgnoreCase("set" + name2)) {
                        m = method;
                        getSetObjectFiledValueHandleCache.put(cacheKey, m);
                        break;
                    }
                }

                setHandle = getSetObjectFiledValueHandleCache.get(cacheKey);
                if (setHandle == null) {
                    filed = getClassField(c, name);
                    if (filed == null) {
                        filed = getClassField(c, "m_" + name);
                    }

                    if (filed != null) {
                        getSetObjectFiledValueHandleCache.put(cacheKey, filed);
                    }
                }

                setHandle = getSetObjectFiledValueHandleCache.get(cacheKey);
            }

            if (setHandle != null) {
                if (setHandle instanceof Field) {
                    filed = (Field) setHandle;
                } else if (setHandle instanceof Method) {
                    Method getHandleMethod = (Method) setHandle;
                    if (CollUtil.length(getHandleMethod.getParameterTypes()) < 2) {
                        m = getHandleMethod;
                    }
                }
            }

            if (null != m) {
                m.invoke(vo, value);
            } else if (filed != null) {
                filed.setAccessible(true);
                filed.set(vo, value);
            } else if (vo instanceof CircularlyAccessibleValueObject) {
                ((CircularlyAccessibleValueObject) vo).setAttributeValue(name, value);
            }

        } catch (Exception e) {
            throw new BusinessException(vo.getClass().getName()
                    + " 的属性名=" + name + " 不能赋值为=" + value
                    + " " + e.getMessage());
        }
    }

    /**
     * 设置一个对象 的字段的值  <br/>
     * 不报错<br/>
     *
     * @param name  字段名字
     * @param value 值
     * @throws BusinessException
     * @author air Email:209308343@qq.com
     * @date 2019-8-17 下午1:32:18
     */
    public static void setObjectFiledValueNoError(Object vo, String name, Object value) throws BusinessException {
        try {
            setObjFiledValueAuto(vo, name, value);
        } catch (Exception e) {
            LogUtil.error(e.toString(), e);
        }
    }

    /**
     * 获取一个对象 的字段的值  <br/>
     * 支持NC的 m_<br/>
     *
     * @param vo   对象
     * @param name 字段名字
     * @throws BusinessException
     * @author air Email:209308343@qq.com
     * @date 2019-8-17 下午1:32:18
     */
    public static Object getObjectFiledValue(Object vo, String name) throws BusinessException {
        try {
            if (vo == null) {
                return null;
            }

            if (vo instanceof Map) {
                return ((Map) vo).get(name);
            }

            Class<?> c = vo.getClass();
            Method m = null;
            Field filed = null;
            Object getHandle = null;

            String cacheKey = c.getName() + ":GET:" + name;
            getHandle = getSetObjectFiledValueHandleCache.get(cacheKey);
            if (getHandle == null) {
                String name2 = name;
                if (StrUtil.startWith(name, "m_")) {
                    name = name.substring(2);
                }

                List<Method> ms = getClassAllMethod(c);
                for (Method method : ms) {
                    if (method.getName().equalsIgnoreCase("get" + name)
                            || method.getName().equalsIgnoreCase("get" + name2)) {
                        m = method;
                        getSetObjectFiledValueHandleCache.put(cacheKey, m);
                        break;
                    }
                }

                getHandle = getSetObjectFiledValueHandleCache.get(cacheKey);
                if (getHandle == null) {
                    filed = getClassField(c, name);
                    if (filed == null) {
                        filed = getClassField(c, "m_" + name);
                    }

                    if (filed != null) {
                        getSetObjectFiledValueHandleCache.put(cacheKey, filed);
                    }
                }

                getHandle = getSetObjectFiledValueHandleCache.get(cacheKey);
            }

            if (getHandle != null) {
                if (getHandle instanceof Field) {
                    filed = (Field) getHandle;
                } else if (getHandle instanceof Method) {
                    Method getHandleMethod = (Method) getHandle;
                    if (CollUtil.length(getHandleMethod.getParameterTypes()) < 1) {
                        m = getHandleMethod;
                    }
                }
            }

            Object v = null;
            if (v == null && null != m) {
                v = m.invoke(vo);
            } else if (v == null && filed != null) {
                filed.setAccessible(true);
                v = filed.get(vo);
            } else if (v == null && vo instanceof CircularlyAccessibleValueObject) {
                v = ((CircularlyAccessibleValueObject) vo).getAttributeValue(name);
            }

            return v;
        } catch (Exception e) {
            Throwable ex = ExceptionUtil.getTopCase(e);
            throw new BusinessException(vo.getClass().getName() + " 的属性名=" + name
                    + " 获取值失败:" + ExceptionUtil.getMessageLines(ex, 10));
        }
    }


    /**
     * 获得一个类中所有方法列表，直接反射获取，无缓存
     *
     * @param beanClass             类
     * @param withSuperClassMethods 是否包括父类的方法列表
     * @return 方法列表
     * @throws SecurityException 安全检查异常
     */
    public static List<Method> getMethodsDirectly(Class<?> beanClass, boolean withSuperClassMethods) throws SecurityException {
        ArrayList<Method> allMethods = new ArrayList();
        Class<?> searchType = beanClass;
        Method[] declaredMethods;
        while (searchType != null) {
            declaredMethods = searchType.getDeclaredMethods();
            if (declaredMethods != null) {
                for (Method declaredMethod : declaredMethods) {
                    allMethods.add(declaredMethod);
                }
            }

            searchType = withSuperClassMethods ? searchType.getSuperclass() : null;
        }

        return allMethods;
    }

    /**
     * 安全的获取一个 Class的 字段（不会抛出异常 没有返回null）
     * <br/>
     * 如果是NC的VO 会再次尝试 前缀加 m_ 获取一次                     <br/>
     *
     * @param c    类
     * @param name 字段名字
     * @return
     * @author air Email:209308343@qq.com
     * @date 2019年12月5日 下午7:12:54
     */
    public static Field getClassField(Class<?> c, String name) {
        List<Field> fs = getClassAllDeclaredFields(c);
        Field f = null;
        Field mf = null;
        for (int i = 0; i < fs.size(); i++) {
            Field field = fs.get(i);
            if (field.getName().equals(name)) {
                f = field;
                break;
            } else if (field.getName().equals("m_" + name)) {
                mf = field;
            }
        }

        return null == f ? mf : f;
    }

    /**
     * 设置一个bean的字段的值 ,自动 判断类型
     * <br/>
     * <br/>
     *
     * @param name
     * @param value
     * @throws BusinessException
     * @author air Email:209308343@qq.com
     * @date 2019年12月6日08:40:57
     */
    public static void setObjFiledValueAuto(Object vo
            , String name, Object v) throws BusinessException {
        String value = StringUtil.getSafeString(v);
        String name2 = name;
        if (name.startsWith("m_")) {
            name2 = name.substring(2);
        }

        try {
            Class<? extends Object> c = vo.getClass();
            Method m = null;
            Method setAttributeValue = null;
            Field filed = null;
            Object setHandle = null;
            String cacheKey = c.getName() + ":SET:" + name;
            setHandle = getSetObjectFiledValueHandleCache.get(cacheKey);
            String cacheTypeKey = cacheKey + ":TYPE";
            Class<?> type = (Class) getSetObjectFiledValueHandleCache.get(cacheTypeKey);
            if (setHandle == null) {
                List<Method> ms = getClassAllMethod(c);
                for (Method method : ms) {
                    if (method.getName().equalsIgnoreCase("set" + name)
                            || method.getName().equalsIgnoreCase("set" + name2)) {
                        m = method;
                        getSetObjectFiledValueHandleCache.put(cacheKey, m);
                        getSetObjectFiledValueHandleCache.put(cacheTypeKey, m.getParameterTypes()[0]);
                        break;
                    }

                    if (m == null && "setAttributeValue".equalsIgnoreCase(method.getName())
                            && method.getParameterTypes().length == 2) {
                        m = method;
                        getSetObjectFiledValueHandleCache.put(cacheKey, m);
                    }
                }

                if (m == null || CollUtil.length(m.getParameterTypes()) > 1) {
                    filed = getClassField(c, name);
                    if (filed == null) {
                        filed = getClassField(c, "m_" + name);
                    }

                    if (filed != null) {
                        getSetObjectFiledValueHandleCache.put(cacheTypeKey, filed.getType());
                    }
                }

                setHandle = getSetObjectFiledValueHandleCache.get(cacheKey);
                if (setHandle == null) {
                    filed = getClassField(c, name);
                    if (filed == null) {
                        filed = getClassField(c, "m_" + name);
                    }

                    if (filed != null) {
                        getSetObjectFiledValueHandleCache.put(cacheKey, filed);
                        getSetObjectFiledValueHandleCache.put(cacheTypeKey, filed.getType());
                    }
                }

                setHandle = getSetObjectFiledValueHandleCache.get(cacheKey);
                type = (Class) getSetObjectFiledValueHandleCache.get(cacheTypeKey);
            }

            if (setHandle != null) {
                if (setHandle instanceof Field) {
                    filed = (Field) setHandle;
                } else if (setHandle instanceof Method) {
                    Method getHandleMethod = (Method) setHandle;

                    if (CollUtil.length(getHandleMethod.getParameterTypes()) < 2) {
                        m = getHandleMethod;
                    } else {
                        m = null;
                        setAttributeValue = getHandleMethod;
                    }
                }
            }

            if (null != value && null != type) {
                if (type.equals(String.class)) {
                    v = value;
                } else if (type.equals(UFDate.class)) {
                    v = new UFDate(value);
                } else if (type.equals(UFDateTime.class)) {
                    if (StrUtil.isNotBlank(value) && value.length() > 8 && value.length() < 11) {
                        value += " 00:00:00";
                    }
                    v = new UFDateTime(value);
                }/* else if (type.equals(UFLiteralDate.class)) {
                    v = new UFLiteralDate(value);
                }*/ else if (type.equals(UFBoolean.class)) {
                    v = "Y".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value) ? UFBoolean.TRUE : UFBoolean.FALSE;
                } else if (type.equals(UFDouble.class)) {
                    v = new UFDouble(value);
                } else if (type.equals(Integer.class)) {
                    v = Integer.valueOf(value);
                } else if (type.equals(Boolean.class)) {
                    v = "Y".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value) ? Boolean.TRUE : Boolean.FALSE;
                } else if (type.equals(Double.class)) {
                    v = Double.valueOf(value);
                } else if (type.equals(int.class)) {
                    v = Integer.valueOf(value);
                } else if (type.equals(double.class)) {
                    v = Double.valueOf(value);
                } else if (type.equals(boolean.class)) {
                    v = "Y".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value) ? true : false;
                } else if (type.equals(char.class)) {
                    v = String.valueOf(value).charAt(0);
                } else if (type.equals(byte.class)) {
                    v = Byte.valueOf(value);
                } else if (type.equals(float.class)) {
                    v = Float.valueOf(value);
                }
            }

            if (null != m) {
                m.invoke(vo, v);
            } else if (filed != null) {
                filed.setAccessible(true);
                filed.set(vo, v);
            } else if (vo instanceof CircularlyAccessibleValueObject) {
                try {
                    CircularlyAccessibleValueObject vo2 = (CircularlyAccessibleValueObject) vo;
                    vo2.setAttributeValue(name2, v);
                } catch (Throwable e) {
                    Throwable ex = ExceptionUtil.getTopCase(e);
                    LogUtil.error("属性名=" + name + ",不能赋值为=" + v, ex);
                }
            } else if (null != setAttributeValue) {
                try {
                    setAttributeValue.invoke(vo, name2, v);
                } catch (Throwable e) {
                    Throwable ex = ExceptionUtil.getTopCase(e);
                    LogUtil.error("属性名=" + name + ",不能赋值为=" + v, ex);
                }
            }

            // 都没有 没救了 肯定没有这个字段 忽略他 不抛错
        } catch (Exception e) {
            Throwable ex = ExceptionUtil.getTopCase(e);
            throw new BusinessException("属性名=" + name
                    + ",不能赋值为=" + v
                    + ",ERROR=" + ExceptionUtil.getMessageLines(ex, 10)
            );
        }
    }

    /**
     * 自动 判断类型 把一个 类型转换成 预期的str
     * <br/>
     * <br/>
     *
     * @param v
     * @throws BusinessException
     * @author air Email:209308343@qq.com
     * @date 2019年12月6日08:40:57
     */
    public static String getValueStrAuto(Object v) throws BusinessException {
        if (v == null) {
            return null;
        }

        Class<?> type = v.getClass();

        if (type.equals(String.class)) {
            return v.toString();
        } else if (type.equals(UFDate.class)) {
            return ((UFDate) v).toString();
        } else if (type.equals(UFDateTime.class)) {
            return ((UFDateTime) v).toString();
        } else if (type.equals(UFBoolean.class)) {
            return String.valueOf(((UFBoolean) v).booleanValue());
        } else if (type.equals(UFDouble.class)) {
            return ((UFDouble) v).toString();
        } else if (type.equals(Integer.class)) {
            return v.toString();
        } else if (type.equals(Boolean.class)) {
            return v.toString();
        } else if (type.equals(Double.class)) {
            return v.toString();
        } else if (type.equals(int.class)) {
            return v.toString();
        } else if (type.equals(double.class)) {
            return v.toString();
        } else if (type.equals(boolean.class)) {
            return v.toString();
        } else if (type.equals(char.class)) {
            return v.toString();
        } else if (type.equals(byte.class)) {
            return v.toString();
        } else if (type.equals(float.class)) {
            return v.toString();
        }

        return v.toString();
    }

    /**
     * 获取 一个类 注解类 特定 注解的字段
     *
     * @param clz
     * @param annotation
     * @param <T>
     * @return key 字段，value 注解对象
     */
    public static <T extends Annotation> Map<Field, T>
    getClassAnnotationField(Class<?> clz, Class<T> annotationClz) {
        List<Field> fields = getClassAllDeclaredFields(clz);
        HashMap<Field, T> map = new HashMap<Field, T>(fields.size() + 1);
        T annoObj;
        for (Field field : fields) {
            if (field.getModifiers() == Modifier.FINAL) {
                continue;
            }

            annoObj = field.getAnnotation(annotationClz);

            if (annoObj != null) {
                map.put(field, annoObj);
            }
        }
        return map;
    }

    /**
     * 获取 所有字段，包括父类
     *
     * @param clz
     * @return
     */
    public static List<Field> getClassAllDeclaredFields(Class<?> clz) {
        Class key = clz;
        if (getClassAllDeclaredFieldsCache.get(key) != null) {
            return getClassAllDeclaredFieldsCache.get(key);
        }

        List<Field> allFields = new LinkedList<Field>();

        Class c = clz;
        while (c != Object.class) {
            allFields.addAll(CollUtil.toList(c.getDeclaredFields()));
            c = c.getSuperclass();
        }

        getClassAllDeclaredFieldsCache.put(key, allFields);

        return allFields;
    }

    ////////////////// 对象 字符串 null处理 //////////////////////

    /**
     * Object: 如果 v == null 返回 ifnull 否则返回 v <br/>
     * 如果是字符串会判断 isblack <br/>
     * 如果是 数字 会判断是否 == 0 <br/>
     *
     * @param v      值
     * @param ifNull 值 == null 需要返回的
     * @return
     */
    public static <T> T get(T v, T ifNull) {
        if (v == null) {
            return ifNull;
        }

        if (v instanceof String) {
            if (StrUtil.isBlank(v.toString())) {
                return ifNull;
            }
        }

        if (v instanceof Number) {
            if (((Number) v).doubleValue() == 0) {
                return ifNull;
            }
        }

        return v;
    }

    /**
     * Object: 如果 v == null 返回 ifnull 否则返回 v <br/>
     *
     * @param v      值
     * @param ifNull 值 == null 需要返回的
     * @return
     */
    public static <T> T getNullIf(T v, T ifNull) {
        return v == null ? ifNull : v;
    }

    /**
     * Object: 如果 v == null 返回 ifnull 否则返回 v
     *
     * @param v      值
     * @param ifNull 值 == null 需要返回的
     * @return
     */
    public static Object getObj(Object v, Object ifNull) {
        return null == v ? ifNull : v;
    }

    /**
     * Object: 如果 v == null 返回 ifnull 否则返回 v (如果是字符串 会判断 是否black)
     *
     * @param v      值
     * @param ifNull 值 == null 需要返回的
     * @return
     */
    public static <T> T getOrEmpty(T v, T ifNull) {
        if (null != v && v instanceof String) {
            return StrUtil.isBlank((String) v) ? ifNull : v;
        }

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
        return null == list ? CollUtil.<T>emptyList() : list;
    }

    /**
     * Map: 如果 map ！= null 返回他自己，否则返回一个空  map
     *
     * @param map
     * @return
     */
    public static <K, V> Map<K, V> get(Map<K, V> map) {
        return null == map ? (Map<K, V>) CollUtil.emptyMap() : map;
    }

    /**
     * Set: 如果 Set ！= null 返回他自己，否则返回一个空  Set
     *
     * @param set
     * @return
     */
    public static <T> Set<T> get(Set<T> set) {
        return null == set ? (Set<T>) CollUtil.emptySet() : set;
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
     * Bolean: 如果 s == null 返回 false 否则返回 s的布尔值
     *
     * @param s
     * @return
     */
    public static boolean is(UFBoolean s) {
        return get(s, UFBoolean.FALSE).booleanValue();
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
     * 返回 true条件：<br/>
     * (此方法用于 用 1,0 或者 true false， 或者 y n 表示 布尔的场景) <br/>
     * 1.s not black <br/>
     * 2.s toLoawer = 'true' <br/>
     * 3.s toLoawer = '1'    <br/>
     * 4.s toLoawer = 'y'    <br/>
     * <br/>
     *
     * @param s
     * @return
     */
    public static boolean is(String s) {
        if (StrUtil.isBlank(s)) {
            return false;
        }

        s = s.trim().toLowerCase();
        return s.equals("true")
                || s.equals("1")
                || s.equals("y");
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
        return getInt(o, 0);
    }

    /**
     * Object转int: 如果 o == null 返回 0 否则返回 o
     *
     * @param o
     * @return
     */
    public static Integer getInt(Object o, Integer defualt) {
        if (o == null) {
            return defualt;
        }

        if (o instanceof Number) {
            return ((Number) o).intValue();
        }

        try {
            return Integer.parseInt(o.toString());
        } catch (Throwable e) {
            return defualt;
        }
    }

    /**
     * Object转BigDecimal后设置小数位后返回： <br/>
     * 根据指定小数位， 格式化 数字， 自动判断原始值类型  <br/>
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

        BigDecimal b = toBigDecimal(value, BigDecimal.ZERO);

        if (null != rm) {
            b.setScale(digits, rm);
        }

        return b;
    }

    /**
     * Object转BigDecimal后设置小数位后返回： <br/>
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
                if (str.equalsIgnoreCase(StrUtil.trim(s))) {
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
        final int count = countCharRepetCount(dateStr, '-');
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
        ArrayList<String> re = new ArrayList(list.size());

        for (Object o : list) {
            re.add(str4Obj(o));
        }

        return re;
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

    /**
     * 把一个对象 转换成 toBigDecimal(8个小数位， 多于的小数 直接扔掉！)
     *
     * @param value
     * @param roundingMode
     * @return
     */
    public static BigDecimal toBigDecimal(Object value, BigDecimal defulat) {
        BigDecimal d = toBigDecimal(value, 8);

        return d == null ? defulat : d;
    }

    /**
     * 把一个对象 转换成 toBigDecimal(8个小数位， 多于的小数 直接扔掉！)
     *
     * @param value
     * @param roundingMode
     * @return null或者解析失败返回 null
     */
    public static BigDecimal toBigDecimalIf(Object value) {
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }

        return toBigDecimal(value);
    }

    /**
     * 获取map里的值，根据map key
     *
     * @param map
     * @param key
     * @param <T>
     * @return
     */
    public static <T> T getMapValue(Map map, Object key) {
        if (map == null) {
            return null;
        }

        return (T) map.get(key);
    }

    /**
     * 获得 字段的 类型class 没有的返回null
     *
     * @param c
     * @param name
     * @return
     */
    public static Class getClassFieldType(Class c, String name) {
        Field f = getClassField(c, name);
        if (f == null) {
            return null;
        }

        return f.getType();
    }

    /**
     * 获取 double的 值字符串, 但是不用科学计数法
     *
     * @param d
     * @return
     */
    public static String getDoubleValueFullString(BigDecimal d) {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setGroupingUsed(false);
        nf.setMinimumFractionDigits(0);
        nf.setMaximumFractionDigits(50000);
        return nf.format(d);
    }

    /**
     * 精确计算 2个 BigDecimal值的加法结果
     *
     * @param d1 BigDecimal
     * @param d2 BigDecimal
     * @return 加法 结果
     */
    public static BigDecimal calc2BigDoubleValueSum(BigDecimal d1, BigDecimal d2) {
        return d1.add(d2);
    }

    /**
     * 精确计算 2个 大double值的 减法 - 结果
     *
     * @param BigDecimal
     * @param BigDecimal
     * @return - 减法 结果(d1 - d2)
     */
    public static BigDecimal calc2BigDoubleValueSub(BigDecimal d1, BigDecimal d2) {
        return d1.subtract(d2);
    }

    /**
     * 精确计算 2个 大double值的 乘法 - 结果
     *
     * @param BigDecimal
     * @param BigDecimal
     * @return - 乘法 结果(d1 * d2)
     */
    public static BigDecimal calc2BigDoubleValueMult(BigDecimal d1, BigDecimal d2) {
        return d1.multiply(d2);
    }

    /**
     * 精确计算 2个 大double值的 除法 - 结果
     *
     * @param BigDecimal
     * @param BigDecimal
     * @return - 除法 结果(d1 / d2)
     */
    public static BigDecimal calc2BigDoubleValueDiv(BigDecimal d1, BigDecimal d2) {
        return d1.divide(d2);
    }

    /**
     * 把一个 double 的小数位 按指定位数 返回 ，舍弃多余的小数位
     *
     * @param d
     * @return
     */
    public static double setDoublScale(double d, int deep) {
        return new BigDecimal(d)
                .setScale(deep, BigDecimal.ROUND_DOWN).doubleValue();
    }

    /**
     * 把一个 Object转换成 double， 调用toString方法
     *
     * @param o
     * @return if null or trim to "" return 0.0d
     */
    public static double object2DoubleValue(Object o) {
        double d = 0.0d;
        if (o != null) {
            String s = o.toString();
            if (!s.trim().isEmpty()) {
                d = Double.parseDouble(s);
            }
        }
        return d;
    }

    /**
     * 转换成 UFDouble ，转换失败 返回 UFDouble.ZERO_DBL
     *
     * @param v
     * @return
     */
    public static UFDouble toUFDouble(Object v) {
        return toUFDouble(v, UFDouble.ZERO_DBL);
    }

    /**
     * 转换成 UFDouble ，转换失败 返回 ifNull
     *
     * @param v
     * @return
     */
    public static UFDouble toUFDouble(Object v, UFDouble ifNull) {
        if (v == null) {
            return ifNull;
        }

        if (v instanceof UFDouble) {
            return (UFDouble) v;
        }

        if (v instanceof Number) {
            return new UFDouble(((Number) v).doubleValue());
        }

        return new UFDouble(v.toString());
    }

    /**
     * 转换成 UFDouble ，转换失败 返回 ifNull
     *
     * @param v
     * @return
     */
    public static UFDouble toUFDoubleNoErr(Object v, UFDouble ifNullErr) {
        try {
            if (v == null) {
                return ifNullErr;
            }

            if (v instanceof UFDouble) {
                return (UFDouble) v;
            }

            if (v instanceof Number) {
                return new UFDouble(((Number) v).doubleValue());
            }

            return new UFDouble(v.toString());
        } catch (Throwable e) {
            return ifNullErr;
        }
    }

    /**
     * 设置小数位,多余小数丢掉
     *
     * @param d
     * @param xsw 小数位
     * @return
     */
    public static UFDouble scale(UFDouble d, int xsw) {
        return null == d ? null : d.setScale(xsw, UFDouble.ROUND_DOWN);
    }

    /**
     * 设置小数位
     *
     * @param d
     * @param xsw      小数位
     * @param runmodel 多余小数处理方式
     * @return
     */
    public static UFDouble scale(UFDouble d, int xsw, int runmodel) {
        return null == d ? null : d.setScale(xsw, runmodel);
    }

    /**
     * 加法
     *
     * @param d1 可传null默认0
     * @param d2 可传null默认0
     * @return
     */
    public static UFDouble sum(Object d1, Object d2) {
        return toUFDouble(d1, UFDouble.ZERO_DBL).add(toUFDouble(d2, UFDouble.ZERO_DBL));
    }

    /**
     * 减法
     *
     * @param d1 可传null默认0
     * @param d2 可传null默认0
     * @return
     */
    public static UFDouble sub(Object d1, Object d2) {
        return toUFDouble(d1, UFDouble.ZERO_DBL).sub(toUFDouble(d2, UFDouble.ZERO_DBL));
    }

    /**
     * 除法
     *
     * @param d1 可传null默认0
     * @param d2 可传null默认1
     * @return
     */
    public static UFDouble div(Object d1, Object d2) {
        return div(d1, d2, 8);
    }

    /**
     * 除法
     *
     * @param d1 可传null默认0
     * @param d2 可传null默认1
     * @return
     */
    public static UFDouble div(Object d1, Object d2, int power) {
        return toUFDouble(d1, UFDouble.ZERO_DBL).div(toUFDouble(d2, UFDouble.ONE_DBL), power);
    }

    /**
     * 乘法
     *
     * @param d1 可传null默认0
     * @param d2 可传null默认1
     * @return
     */
    public static UFDouble multiply(Object d1, Object d2) {
        return toUFDouble(d1, UFDouble.ZERO_DBL).multiply(toUFDouble(d2, UFDouble.ONE_DBL));
    }

    /**
     * 获取类的所有自己和父亲的方法
     *
     * @param c
     * @return
     */
    public static List<Method> getClassAllMethod(Class c) {
        if (c == null || c == Object.class) {
            return CollUtil.emptyList();
        }

        Class key = c;
        List<Method> v = getClassAllMethodCache.get(key);
        if (v != null) {
            return v;
        }

        LinkedList<Method> ms = Lists.newLinkedList();

        Method[] mall;
        do {
            mall = c.getDeclaredMethods();
            c = c.getSuperclass();
            if (CollUtil.isEmpty(mall)) {
                continue;
            }

            for (Method m : mall) {
                //      if (!ms.contains(m)) {
                ms.add(m);
                //    }
            }
        } while (c != Object.class);

        getClassAllMethodCache.put(key, ms);
        return ms;
    }

    /**
     * 把方法上有指定注解的方法 过滤出来
     *
     * @param annotation
     * @param ms
     * @return key 注解对象, value 方法
     */
    public static <T extends Annotation> Map<T, Method> getClassAllMethod(Class<T> annotation, List<Method> ms) {
        if (CollUtil.isEmpty(ms)) {
            return CollUtil.emptyMap();
        }

        Map<T, Method> all = new HashMap(ms.size());
        T an;
        for (Method m : ms) {
            an = m.getAnnotation(annotation);
            if (an == null) {
                continue;
            }

            all.put(an, m);
        }

        return all;
    }

    /**
     * 创建一个实例，不报错，失败返回null
     *
     * @param c
     * @param <T>
     * @return
     */
    public static <T> T newInstance(Class<T> c) {
        try {
            return c.newInstance();
        } catch (InstantiationException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    public static <T> T newInstance(String c) {
        try {
            return (T) Class.forName(c).newInstance();
        } catch (InstantiationException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public static UFDateTime nowUFDateTime() {
        return new UFDateTime(System.currentTimeMillis());
    }

    public static UFDouble getUFDouble(UFDouble a, UFDouble ifNullOrZero) {
        if (a == null) {
            return ifNullOrZero;
        }

        if (UFDouble.ZERO_DBL.compareTo(a) == 0) {
            return ifNullOrZero;
        }

        return a;
    }

    public static boolean isNullOrLessZero(UFDouble a) {
        return a == null || UFDouble.ZERO_DBL.compareTo(a) >= 0;
    }

    public static boolean isNullOrZero(UFDouble a) {
        return a == null || UFDouble.ZERO_DBL.compareTo(a) == 0;
    }

    public static boolean isGtZero(UFDouble a) {
        return !isNullOrLessZero(a);
    }

    /**
     * @param longstr
     * @param errorMsg eg： 格式错误：字符串%s格式错误。
     * @return
     * @throws BusinessException 如果 errorMsg有一个%s 就会自动用longstr format到这里面
     */
    public static long toLong(String longstr, String errorMsg) throws BusinessException {
        return toUFDoubleIfMsg(longstr, errorMsg).longValue();
    }

    /**
     * @param str
     * @param errorMsg eg： 格式错误：字符串%s格式错误。
     * @return
     * @throws BusinessException 如果 errorMsg有一个%s 就会自动用 str format到这里面
     */
    public static int toInt(String str, String errorMsg) throws BusinessException {
        return toUFDoubleIfMsg(str, errorMsg).intValue();
    }

    /**
     * @param str
     * @param errorMsg eg： 格式错误：字符串%s格式错误。
     * @return
     * @throws BusinessException 如果 errorMsg有一个%s 就会自动用 str format到这里面
     */
    public static UFDouble toUFDoubleIfMsg(String str, String errorMsg) throws BusinessException {
        if (StrUtil.isBlank(str)) {
            throw new BusinessException(StrUtil.contains(errorMsg, "%s")
                    ? String.format(errorMsg, str) : errorMsg);
        }

        try {
            return new UFDouble(str);
        } catch (Throwable e) {
            throw new BusinessException(StrUtil.contains(errorMsg, "%s")
                    ? String.format(errorMsg, str) : errorMsg);
        }
    }

    public static boolean isNumber(Object o) {
        if (o == null) {
            return false;
        }

        if (o instanceof Number) {
            return true;
        }

        try {
            UFDouble u = new UFDouble(o.toString());
        } catch (Throwable e) {
            return false;
        }
        return true;
    }


    public static UFDate toUFDate(int year, int month, int day) {
        String s = "" + year + "-";
        if (month < 10) {
            s += "0";
        }
        s += month + "-";
        if (day < 10) {
            s += "0";
        }
        s += day;

        return new UFDate(s);
    }

    public static UFDateTime toUFDateTime(int year, int month, int day, int h, int m, int se) {
        String s = "" + year + "-";
        if (month < 10) {
            s += "0";
        }
        s += month + "-";
        if (day < 10) {
            s += "0";
        }
        s += day + " ";

        if (h < 10) {
            s += "0";
        }
        s += h + ":";

        if (m < 10) {
            s += "0";
        }
        s += m + ":";

        if (se < 10) {
            s += "0";
        }
        s += se;

        return new UFDateTime(s);
    }

    public static UFDate toUFDate(Object d, UFDate defualt) {
        try {
            UFDate v = toUFDate(d);
            return v;
        } catch (Throwable e) {
            return defualt;
        }
    }

    public static UFDate tryToUFDate(Object d, UFDate defualt) {
        try {
            if (d == null) {
                return defualt;
            }

            if (d instanceof UFDate) {
                return (UFDate) d;
            }
            if (d instanceof UFDateTime) {
                return ((UFDateTime) d).getDate();
            }
            if (d instanceof Date) {
                return new UFDate((Date) d);
            }
            if (d instanceof Long) {
                return new UFDate((Long) d);
            }

            if (StrUtil.isBlank(d.toString())) {
                return defualt;
            }

            return new UFDate(d.toString());
        } catch (Throwable e) {
            return defualt;
        }
    }

    public static UFDate toUFDate(Object d) {
        if (d == null) {
            return null;
        }

        if (d instanceof UFDate) {
            return (UFDate) d;
        }
        if (d instanceof UFDateTime) {
            return ((UFDateTime) d).getDate();
        }
        if (d instanceof Date) {
            return new UFDate((Date) d);
        }
        if (d instanceof Long) {
            return new UFDate((Long) d);
        }

        return new UFDate(d.toString());
    }

    public static UFDateTime toUFDateTime(Object d) {
        if (d == null) {
            return null;
        }

        if (d instanceof UFDate) {
            return new UFDateTime(((UFDate) d).toDate());
        }
        if (d instanceof UFDateTime) {
            return ((UFDateTime) d);
        }
        if (d instanceof Date) {
            return new UFDateTime((Date) d);
        }
        if (d instanceof Long) {
            return new UFDateTime((Long) d);
        }

        return new UFDateTime(d.toString());
    }

    public static UFDate getFirstDay4Mon(UFDate d) {
        if (d == null) {
            return null;
        }

        return toUFDate(d.getYear(), d.getMonth(), 1);
    }

    public static UFDateTime getFirstDayTime4Mon(UFDate d) {
        if (d == null) {
            return null;
        }

        return new UFDateTime(toUFDate(d.getYear(), d.getMonth(), 1).toString().substring(0, 10) + " 00:00:00");
    }

    public static String getRowID(CircularlyAccessibleValueObject c) {
        return (String) c.getAttributeValue("rowid");
    }

    public static void setRowID(CircularlyAccessibleValueObject c, String rowid) {
        c.setAttributeValue("rowid", rowid);
    }

    public static UFDateTime getLastDayTime4Mon(UFDate d) {
        if (d == null) {
            return null;
        }

        int year = d.getYear();
        int m = d.getMonth();
        if (d.getMonth() == 12) {
            return new UFDateTime(toUFDate(d.getYear(), 12, 31).toString().substring(0, 10) + " 23:59:59");
        } else {
            ++m;
        }

        return new UFDateTime(toUFDate(year, m, 1).getDateBefore(1).toString().substring(0, 10) + " 23:59:59");
    }

    public static UFDate getLastDay4Mon(UFDate d) {
        if (d == null) {
            return null;
        }

        int year = d.getYear();
        int m = d.getMonth();
        if (d.getMonth() == 12) {
            return new UFDate(toUFDate(d.getYear(), 12, 31).toString().substring(0, 10) + " 23:59:59");
        } else {
            ++m;
        }

        return new UFDate(toUFDate(year, m, 1).getDateBefore(1).toString().substring(0, 10) + " 23:59:59");
    }

    public static boolean isSameMonth(UFDate d1, UFDate d2) {
        if (d1 == null || d2 == null) {
            return false;
        }

        return d1.getYear() == d2.getYear() && d1.getMonth() == d2.getMonth();
    }

    public static boolean isZeroOrNull(UFDouble d) {
        return d == null || UFDouble.ZERO_DBL.compareTo(d) == 0;
    }

    public static UFDate addMonth(UFDate d, int m) {
        if (d == null) {
            return null;
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(d.toDate());
        cal.add(Calendar.MONTH, m);

        return new UFDate(cal.getTime());
    }

    public static Set<String> getFullAttrNames(CircularlyAccessibleValueObject vo) {
        if (vo == null) {
            return null;
        }

        if (vo instanceof SuperVO) {
            return ObjUtil.getSuperVOFullAttrNames((SuperVO) vo);
        }

        return getCircularlyAccessibleValueObjectFullAttrNames(vo);
    }

    public static Set<String> getCircularlyAccessibleValueObjectFullAttrNames(CircularlyAccessibleValueObject vo) {
        HashSet<String> ns = Sets.newHashSet();

        String[] names = vo.getAttributeNames();
        if (names != null) {
            for (String name : names) {
                ns.add(name);
            }
        }

        List<Field> fs = getClassAllDeclaredFields(vo.getClass());
        if (fs != null) {
            for (Field f : fs) {
                ns.add(f.getName());
            }
        }

        return ns;
    }

    public static Set<String> getSuperVOFullAttrNames(SuperVO vo) {
        HashSet<String> ns = Sets.newHashSet();

        String[] names = vo.getAttributeNames();
        if (names != null) {
            for (String name : names) {
                ns.add(name);
            }
        }

        Map<String, Object> valueIndex = getSuperVOValueIndexMap(vo);
        if (valueIndex != null && !valueIndex.isEmpty()) {
            ns.addAll(valueIndex.keySet());
        }

        List<Field> fs = getClassAllDeclaredFields(vo.getClass());
        if (fs != null) {
            for (Field f : fs) {
                ns.add(f.getName());
            }
        }

        return ns;
    }

    public static Map<String, Object> getSuperVOValueIndexMap(SuperVO vo) {
        try {
            Field valueIndex = ObjUtil.getClassField(vo.getClass(), "valueIndex");
            if (valueIndex == null) {
                return null;
            }

            valueIndex.setAccessible(true);

            return (Map<String, Object>) valueIndex.get(vo);
        } catch (Exception e) {
            return null;
        }
    }

    public static String ufDouble2Str(UFDouble d) {
        return ufDouble2Str(d, null);
    }

    public static String ufDouble2Str(UFDouble d, String ifnull) {
        if (d == null) {
            return ifnull;
        }

        d.setTrimZero(true);

        return d.toString();
    }

    public static boolean equalsObj(Object a, Object b) {
        if (a == b) {
            return true;
        }

        if (a == null || b == null) {
            return false;
        }

        return a.equals(b);
    }

    public static void setIfNull(CircularlyAccessibleValueObject c, String key, Object isnull) {
        if (c.getAttributeValue(key) == null) {
            c.setAttributeValue(key, isnull);
        }
    }

    public static boolean isTrue(Object s) {
        if (s == null) {
            return false;
        }

        if (s instanceof Boolean) {
            return ((Boolean) s).booleanValue();
        }

        if (s instanceof UFBoolean) {
            return ((UFBoolean) s).booleanValue();
        }

        return is(s.toString());
    }
}
