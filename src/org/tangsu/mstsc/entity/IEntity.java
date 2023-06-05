package org.tangsu.mstsc.entity;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import com.google.common.collect.Sets;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public interface IEntity extends Serializable, Cloneable {
    String getTableName();

    String getIdName();

    Set<String> getFieadNames();

    static Set<String> getFieadNames(Class clz) {
        HashSet<String> ns = Sets.newHashSet();
        Field[] fs = ClassUtil.getDeclaredFields(clz);
        for (Field f : fs) {
            if (Modifier.isStatic(f.getModifiers())
                    || Modifier.isFinal(f.getModifiers())
                    || Modifier.isTransient(f.getModifiers())
            ) {
                continue;
            }
            ns.add(ReflectUtil.getFieldName(f));
        }
        return ns;
    }
}
