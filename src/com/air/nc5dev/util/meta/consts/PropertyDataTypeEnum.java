package com.air.nc5dev.util.meta.consts;

import lombok.Getter;

/**
 * 数据库类型  <br/>
 * <br/>
 * <br/>
 *
 * @author air Email:209308343@qq.com
 * @date 2020年8月18日 10:06:44
 * @project
 * @Version
 */
@Getter
public enum PropertyDataTypeEnum {
    BS000010000100001001("String", "String", "BS000010000100001001", "varchar", "varchar"),
    BS000010000100001051("UFID", "UFID", "BS000010000100001051", "char", "char"),
    BS000010000100001004("Integer", "Integer", "BS000010000100001004", "int", "int"),
    BS000010000100001031("UFDouble", "UFDouble", "BS000010000100001031", "decimal", "decimal"),
    BS000010000100001032("UFBoolean", "UFBoolean", "BS000010000100001032", "char", "char"),
    BS000010000100001033("UFDate", "UFDate", "BS000010000100001033", "char", "char"),
    BS000010000100001037("UFDate_begin", "UFDate_begin", "BS000010000100001037", "char", "char"),
    BS000010000100001038("UFDate_end", "UFDate_end", "BS000010000100001038", "char", "char"),
    BS000010000100001039("UFLiteralDate", "UFLiteralDate", "BS000010000100001039", "char", "char"),
    BS000010000100001034("UFDateTime", "UFDateTime", "BS000010000100001034", "char", "char"),
    BS000010000100001036("UFTime", "UFTime", "BS000010000100001036", "char", "char"),
    BS000010000100001040("BigDecimal", "BigDecimal", "BS000010000100001040", "decimal", "decimal"),
    BS000010000100001052("UFMoney", "UFMoney", "BS000010000100001052", "decimal", "decimal"),
    BS000010000100001055("图片", "IMAGE", "BS000010000100001055", "image", "image"),
    BS000010000100001053("BLOB", "BLOB", "BS000010000100001053", "image", "image"),
    BS000010000100001059("自由项", "CUSTOM", "BS000010000100001059", "varchar", "varchar"),
    BS000010000100001030("备注", "MEMO", "BS000010000100001030", "varchar", "varchar"),
    BS000010000100001058("多语文本", "Multilangtext", "BS000010000100001058", "varchar", "varchar"),
    BS000010000100001056("自定义项", "CUSTOM", "BS000010000100001056", "varchar", "varchar");


    public static final PropertyDataTypeEnum agest(String key, PropertyDataTypeEnum ifnull) {
        for (PropertyDataTypeEnum e : values()) {
            if (
                    e.dataType.equalsIgnoreCase(key)
                            || e.name().equalsIgnoreCase(key)
                            || e.typeDisplayName.equalsIgnoreCase(key)
                            || e.typeName.equalsIgnoreCase(key)
                            || e.dbtype.equalsIgnoreCase(key)
                            || e.fieldType.equalsIgnoreCase(key)
            ) {
                return e;
            }
        }

        return ifnull;
    }

    /**
     * 根据类型 获得 对象，没有返回null
     *
     * @param type
     * @return
     */
    public static final PropertyDataTypeEnum ofType(String dataType) {
        for (PropertyDataTypeEnum e : values()) {
            if (e.dataType.equals(dataType)) {
                return e;
            }
        }

        return null;
    }

    public static final PropertyDataTypeEnum ofTypeDefualt(String dataType) {
        for (PropertyDataTypeEnum e : values()) {
            if (e.dataType.equals(dataType)) {
                return e;
            }
        }

        return BS000010000100001001;
    }

    public static final PropertyDataTypeEnum ofDbtype(String dbtype) {
        for (PropertyDataTypeEnum e : values()) {
            if (e.dbtype.equals(dbtype)) {
                return e;
            }
        }

        return null;
    }

    /**
     * 是否是 这个类型
     *
     * @param type
     * @return
     */
    public boolean is(String type) {
        return this.dataType.equals(dataType);
    }

    /**
     * 是否是 这个类型
     *
     * @param type
     * @return
     */
    public boolean isIgnoreCase(String type) {
        return this.dataType.equalsIgnoreCase(dataType);
    }

    /**
     * 是否是 这个类型
     *
     * @param type
     * @return
     */
    public boolean is(PropertyDataTypeEnum type) {
        return type != null && this.dataType.equals(type.dataType);
    }

    PropertyDataTypeEnum(String typeDisplayName, String dataType, String dbtype) {
        this.typeDisplayName = typeDisplayName;
        this.typeName = typeDisplayName;
        this.dataType = dataType;
        this.dbtype = dbtype;
        this.fieldType = dbtype;
    }

    PropertyDataTypeEnum(String typeDisplayName, String dataType, String dbtype, String fieldType) {
        this.typeDisplayName = typeDisplayName;
        this.typeName = typeDisplayName;
        this.dataType = dataType;
        this.dbtype = dbtype;
        this.fieldType = fieldType;
    }

    PropertyDataTypeEnum(String typeDisplayName, String typeName, String dataType, String dbtype, String fieldType) {
        this.typeDisplayName = typeDisplayName;
        this.typeName = typeName;
        this.dataType = dataType;
        this.dbtype = dbtype;
        this.fieldType = fieldType;
    }

    public final String typeDisplayName;
    public final String typeName;
    public final String dataType;
    public final String dbtype;
    public final String fieldType;
}
