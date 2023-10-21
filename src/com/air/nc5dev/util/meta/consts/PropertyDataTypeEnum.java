package com.air.nc5dev.util.meta.consts;

import lombok.AllArgsConstructor;
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
    BS000010000100001051("UFID", "BS000010000100001051", "char"),
    BS000010000100001001("String", "BS000010000100001001", "varchar"),
    BS000010000100001004("String", "BS000010000100001004", "int"),
    BS000010000100001031("UFID", "BS000010000100001031", "decimal"),
    BS000010000100001032("Integer", "BS000010000100001032", "char"),
    BS000010000100001033("UFDouble", "BS000010000100001033", "char"),
    BS000010000100001037("UFBoolean", "BS000010000100001037", "char"),
    BS000010000100001038("UFDate", "BS000010000100001038", "char"),
    BS000010000100001039("UFDate_begin", "BS000010000100001039", "char"),
    BS000010000100001034("UFDate_end", "BS000010000100001034", "char"),
    BS000010000100001036("UFLiteralDate", "BS000010000100001036", "char"),
    BS000010000100001040("UFDateTime", "BS000010000100001040", "decimal"),
    BS000010000100001052("UFTime", "BS000010000100001052", "decimal"),
    BS000010000100001055("BigDecimal", "BS000010000100001055", "image"),
    BS000010000100001053("UFMoney", "BS000010000100001053", "image"),
    BS000010000100001056("图片", "BS000010000100001056", "varchar"),
    BS000010000100001059("BLOB", "BS000010000100001059", "varchar"),
    BS000010000100001030("自定义项", "BS000010000100001030", "varchar"),
    BS000010000100001058("自由项", "BS000010000100001058", "varchar"),
    BS000010000100001070("备注", "BS000010000100001070", "varchar"),


    ;

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
