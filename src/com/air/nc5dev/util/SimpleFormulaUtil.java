package com.air.nc5dev.util;

import cn.hutool.core.util.StrUtil;
import com.air.nc5dev.util.idea.LogUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import nc.vo.pub.BusinessException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * 一个简单的 公式变量 语法计算器 <br/>
 * <br/>
 * <br/>
 * <p>
 * 1. 公式 格式 ,  eg:    <br/>
 * 名字是：${name}，性别是:${sex},爱好第二个是:${likes.2.name}                       <br/>
 * 这种简单的取值。 但是不支持 公式嵌套公式哈                                             <br/>
 *
 * @author 唐粟 Email:209308343@qq.com
 * @date 2020/9/18 16:56
 * @project
 * @Version
 */
public class SimpleFormulaUtil {
    /**
     * 最小一个变量公式里， 如果多层取值，子属性调用符
     */
    public static final char SPLIT_CHAR = '.';

    /**
     * 计算公式，得到 String 结果
     *
     * @param formula   公式
     * @param variables 变量们
     * @param defulat   默认值
     * @return
     */
    public static String executeFormula(String formula
            , Object variables
            , String defulat
            , BiFunction<String, Object, Object> evaluation) throws BusinessException, NoSuchFieldException {
        if (StrUtil.isEmpty(formula)) {
            return defulat;
        }

        ArrayList<String> strs = splitByVariable(formula);
        StringBuilder re = new StringBuilder(formula.length() << 1);
        String s;

        for (int i = 0; i < strs.size(); i++) {
            s = strs.get(i);
            if (!isVariable(s)) {
                re.append(s);
                continue;
            }

            re.append(evaluation.apply(s, variables));
        }
        return re.toString();
    }


    /**
     * 计算公式，得到 String 结果
     *
     * @param formula   公式
     * @param variables 变量们
     * @param defulat   默认值
     * @return
     */
    public static String executeFormula(String formula
            , Object variables
            , String defulat) throws BusinessException, NoSuchFieldException {
        if (StrUtil.isEmpty(formula)) {
            return defulat;
        }

        ArrayList<String> strs = splitByVariable(formula);
        StringBuilder re = new StringBuilder(formula.length() << 1);
        String s;

        for (int i = 0; i < strs.size(); i++) {
            s = strs.get(i);
            if (!isVariable(s)) {
                re.append(s);
                continue;
            }

            re.append(evaluation(s, variables));
        }

        LogUtil.output("执行公式 " + formula + " -> " + re.toString() + "  ,参数=" + JSON.toJSONString(variables));

        return re.toString();
    }

    /**
     * 计算公式，得到  结果
     *
     * @param formula   公式
     * @param variables 变量们
     * @return
     */
    public static <T> T executeFormula(String formula
            , Object variables) throws BusinessException, NoSuchFieldException {
        if (StrUtil.isEmpty(formula)) {
            return null;
        }

        return (T) executeFormula(formula, variables, null);
    }

    /**
     * 对单个公式，进行求值。    <br/>
     * eg： 第一个喜欢的是：${likes.0.name}，第二个喜欢的是：${likes.1.name}             <br/>
     * 需要调用2次此方法，第一次传入:     ${likes.0.name},你的vo之类的对象                       <br/>
     * <br/>
     * <br/>
     * <br/>
     *
     * @param s
     * @param variables
     * @return
     */
    public static String evaluation(String s, Object variables) throws BusinessException, NoSuchFieldException {
        if (variables == null || StrUtil.isEmpty(s)) {
            return null;
        }

        s = releseSingleVariableKey(s);
        String[] paths = StrUtil.splitToArray(s, SPLIT_CHAR);

        Object o = variables;
        for (int i = 0; i < paths.length; i++) {
            o = evaluationOne(paths[i], o);
        }

        return o == null ? null : o.toString();
    }

    /**
     * 对单个公式里单层的属性名，进行求值。    <br/>
     * eg： 第一个喜欢的是：${likes.0.name}，第二个喜欢的是：${likes.1.name}             <br/>
     * 需要调用2次此方法，第一次传入:     likes,你的vo之类的对象                       <br/>
     * <br/>
     * <br/>
     * <br/>
     *
     * @param s
     * @param variables
     * @return
     */
    public static Object evaluationOne(String s, Object variables) throws BusinessException, NoSuchFieldException {
        return evaluationOne(s, variables, true);
    }

    /**
     * 对单个公式里单层的属性名，进行求值。    <br/>
     * eg： 第一个喜欢的是：${likes.0.name}，第二个喜欢的是：${likes.1.name}             <br/>
     * 需要调用2次此方法，第一次传入:     likes,你的vo之类的对象                       <br/>
     * <br/>
     * <br/>
     * <br/>
     *
     * @param s
     * @param variables
     * @param nullSkip  true null调用直接返回null不报错
     * @return
     */
    public static Object evaluationOne(String s, Object variables, boolean nullSkip) throws BusinessException,
            NoSuchFieldException {
        if (variables == null || StrUtil.isEmpty(s)) {
            return null;
        }

        Class<?> vClz = variables.getClass();
        if (Map.class.isAssignableFrom(vClz)) {
            Map m = (Map) variables;
            return m.get(s);
        }

        if (vClz.isArray()) {
            if (!isInteger(s)) {
                throw new BusinessException("数组参数只能用整形下标索引取值！");
            }

            Integer index = toInt(s, null);

            Object[] ars = (Object[]) variables;
            if (index == null
                    || index < 0
                    || index >= ars.length) {
                throw new BusinessException("数组参数下标索引不存在取值！");
            }

            return ars[index];
        }

        if (List.class.isAssignableFrom(vClz)) {
            if (!isInteger(s)) {
                throw new BusinessException("数组参数只能用整形下标索引取值！");
            }

            Integer index = toInt(s, null);

            List ars = (List) variables;
            if (index == null
                    || index < 0
                    || index >= ars.size()) {
                throw new BusinessException("数组参数下标索引不存在取值！");
            }

            return ars.get(index);
        }

        //说明是对象的属性
        return getFieldValue(variables, s);
    }

    /**
     * 把一个类似 ${name} 的 变量 变成 name 这个key
     *
     * @param varkey
     * @return
     */
    public static String releseSingleVariableKey(String varkey) {
        if (StrUtil.isEmpty(varkey)) {
            return varkey;
        }

        varkey = varkey.trim();

        if (varkey.charAt(0) != '$' || varkey.charAt(1) != '{') {
            return varkey;
        }

        varkey = varkey.substring(2, varkey.length() - 1);
        return varkey;
    }

    /**
     * 分析字符串里 提取出 普通字符串和  模板公式 字符串
     *
     * @param str
     * @return
     */
    public static ArrayList<String> splitByVariable(String str) {
        ArrayList<String> ss = Lists.newArrayList();
        char[] chars = str.toCharArray();
        StringBuilder txt = new StringBuilder();
        StringBuilder va = new StringBuilder();
        boolean in = false;
        char c;
        for (int i = 0; i < chars.length; i++) {
            c = chars[i];
            if (in && c != '}') {
                va.append(c);
                continue;
            }

            if (c == '$' && chars[i + 1] == '{') {
                in = true;
                va.append(c);
                if (txt.length() > 0) {
                    ss.add(txt.toString());
                    txt.delete(0, txt.length());
                }

                continue;
            }

            if (in && c == '}') {
                in = false;
                va.append(c);
                if (va.length() > 0) {
                    ss.add(va.toString());
                    va.delete(0, va.length());
                }

                continue;
            }

            txt.append(c);
        }

        if (txt.length() > 0) {
            ss.add(txt.toString());
        }

        if (va.length() > 0) {
            ss.add(va.toString());
        }

        return ss;
    }

    /**
     * 是否是 模板变量
     *
     * @param str
     * @return
     */
    public static boolean isVariable(String str) {
        return StrUtil.isNotBlank(str) && str.startsWith("${") && str.endsWith("}");
    }


    /**
     * 判断String是否是整数<br/>
     * 支持10进制
     *
     * @param s String
     * @return 是否为整数
     */
    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    /**
     * 转换为int<br/>
     * 如果给定的值为空，或者转换失败，返回默认值<br/>
     * 转换失败不会报错
     *
     * @param value        被转换的值
     * @param defaultValue 转换错误时的默认值
     * @return 结果
     */
    public static Integer toInt(Object value, Integer defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }

        final String valueStr = value == null ? null : value.toString();

        if (StrUtil.isEmpty(valueStr)) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(valueStr.trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * 获取字段值
     *
     * @param obj       对象，static字段则此字段为null
     * @param fieldName 字段
     * @return 字段值
     * @throws 包装IllegalAccessException异常
     */
    public static Object getFieldValue(Object obj, String fieldName) throws NoSuchFieldException, BusinessException {
        if (null == fieldName) {
            return null;
        }
        if (obj instanceof Class) {
            // 静态字段获取时对象为null
            obj = null;
        }

        return ObjUtil.getObjectFiledValue(obj, fieldName);
    }
}
