package com.air.nc5dev.vo.meta;

import com.air.nc5dev.util.meta.consts.DataTypeStyleConverter;
import com.air.nc5dev.util.meta.consts.PropertyDataTypeEnum;
import com.air.nc5dev.util.meta.consts.VisibilityConverter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import nc.vo.pub.VOStatus;
import nc.vo.pub.lang.UFDateTime;

import java.io.Serializable;
import java.util.Objects;

/**
 * 实体属性vo
 *
 * @Author air Email: 209308343@qq.com
 * @Description
 * @Date 2024/4/10 0010 9:53
 **/
//com.yonyou.studio.mdp.database.model.PropertyVO
@Getter
@Setter
@Accessors(chain = true)
public class PropertyDTO implements Serializable, Cloneable {
    private String id;
    private String accessorClassName;
    private String classID;
    private UFDateTime createTime;
    private String creator;
    private String dataType;
    private Integer dataTypeStyle;
    private String defaultValue;
    private String description;
    private String displayName;
    private String help;
    private Boolean isActive;
    private Boolean hided;
    private Boolean nullable;
    private Boolean readOnly;
    private Integer attrLength;
    private String attrMaxValue;
    private String attrMinValue;
    private String modifier;
    private UFDateTime modifyTime;
    private String name;
    private Integer precise;
    private String refModelName;
    private Integer attrsequence; //sequence
    private Integer visibility;
    private Integer versionType;
    private String typeName;
    private Boolean fixedLength;
    private Boolean calculation;
    private Boolean isAuthen;
    private String resid;
    private Boolean customattr;
    private Boolean notSerialize;
    private Boolean dynamicattr;
    private String dynamicTable;
    private Boolean accesspower;
    private String accesspowergroup;
    private String industry;
    private Boolean industryChanged;
    private String createIndustry;
    private String modifyIndustry;
    private Boolean isSource;

    Boolean isAuthorization;
    String dbtype;
    String fieldType;
    String fieldName;
    String typeDisplayName;
    Boolean forLocale;
    Boolean isDefaultDimensionAttribute;
    Boolean isDefaultMeasureAttribute;
    Boolean isFeature;
    Boolean isGlobalization;
    Boolean isKey;
    Boolean isShare;

    //引用类型描述
    String refModelDesc;
    //字段类型描述
    String fileTypeDesc;
    int ordernum = 1000086;

    /**
     * @see VOStatus
     */
    transient int state;
    transient String dataTypeStyleName;
    transient String visibilityName;

    public void fixDisplays(boolean full) {
        DataTypeStyleConverter dataTypeStyleConverter = new DataTypeStyleConverter();
        VisibilityConverter visibilityConverter = new VisibilityConverter();

        PropertyDataTypeEnum propertyDataTypeEnum = full ? PropertyDataTypeEnum.ofTypeDefualt(getDataType())
                : PropertyDataTypeEnum.ofType(getDataType());
        if (propertyDataTypeEnum != null) {
            setDbtype(propertyDataTypeEnum.getDbtype());
            setTypeDisplayName(propertyDataTypeEnum.getTypeDisplayName());
            setTypeName(propertyDataTypeEnum.getTypeName());
            setFieldType(propertyDataTypeEnum.getFieldType());
        }

        setDataTypeStyleName(dataTypeStyleConverter.getConvertBefor(getDataTypeStyle()));
        setVisibilityName(visibilityConverter.getNameOfVisibility(getVisibility()));
        setFieldName(getName());

        forLocale = false;
        isDefaultDimensionAttribute = false;
        isDefaultMeasureAttribute = false;
        isFeature = false;
        isGlobalization = false;
        isKey = false;
        isShare = false;
        isAuthorization = true;
        modifyIndustry = "0";
        industry = "0";
        industryChanged = false;
        notSerialize = false;
        isAuthen = true;
        calculation = false;
        fixedLength = false;
        versionType = 0;
        visibility = 0;
        isActive = true;
        hided = false;
        nullable = true;
        readOnly = false;
    }

    public void fixDisplays() {
        fixDisplays(true);
    }

    @Override
    public String toString() {
        return String.format("%s-%s-%s", fieldName, displayName, typeDisplayName);
    }

    @Override
    public PropertyDTO clone() throws CloneNotSupportedException {
        return (PropertyDTO) super.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PropertyDTO that = (PropertyDTO) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
