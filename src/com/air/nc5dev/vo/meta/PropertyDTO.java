package com.air.nc5dev.vo.meta;

import com.air.nc5dev.util.meta.consts.DataTypeStyleConverter;
import com.air.nc5dev.util.meta.consts.PropertyDataTypeEnum;
import com.air.nc5dev.util.meta.consts.VisibilityConverter;
import lombok.Data;
import nc.vo.pub.VOStatus;
import nc.vo.pub.lang.UFDateTime;

import java.io.Serializable;

//com.yonyou.studio.mdp.database.model.PropertyVO
@Data
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
    private Boolean isActive = true;
    private Boolean hided = false;
    private Boolean nullable = true;
    private Boolean readOnly = false;
    private Integer attrLength;
    private String attrMaxValue;
    private String attrMinValue;
    private String modifier;
    private UFDateTime modifyTime;
    private String name;
    private Integer precise;
    private String refModelName;
    private Integer attrsequence; //sequence
    private Integer visibility = 0;
    private Integer versionType = 0;
    private String typeName;
    private Boolean fixedLength = false;
    private Boolean calculation = false;
    private Boolean isAuthen = true;
    private String resid;
    private Boolean customattr;
    private Boolean notSerialize = false;
    private Boolean dynamicattr;
    private String dynamicTable;
    private Boolean accesspower;
    private String accesspowergroup;
    private String industry;
    private Boolean industryChanged = false;
    private String createIndustry;
    private String modifyIndustry = "0";
    private Boolean isSource;

    Boolean isAuthorization = true;
    String dbtype;
    String fieldType;
    String fieldName;
    String typeDisplayName;
    Boolean forLocale = false;
    Boolean isDefaultDimensionAttribute = false;
    Boolean isDefaultMeasureAttribute = false;
    Boolean isFeature = false;
    Boolean isGlobalization = false;
    Boolean isKey = false;
    Boolean isShare = false;


    /**
     * @see VOStatus
     */
    transient int state;
    transient String dataTypeStyleName;
    transient String visibilityName;

    public void fixDisplays() {
        DataTypeStyleConverter dataTypeStyleConverter = new DataTypeStyleConverter();
        VisibilityConverter visibilityConverter = new VisibilityConverter();
        setDbtype(PropertyDataTypeEnum.ofTypeDefualt(getDataType()).getDbtype());
        setTypeDisplayName(PropertyDataTypeEnum.ofTypeDefualt(getDataType()).getTypeDisplayName());
        setTypeName(PropertyDataTypeEnum.ofTypeDefualt(getDataType()).getTypeName());
        setFieldType(PropertyDataTypeEnum.ofTypeDefualt(getDataType()).getFieldType());
        setDataTypeStyleName(dataTypeStyleConverter.getConvertBefor(getDataTypeStyle()));
        setVisibilityName(visibilityConverter.getNameOfVisibility(getVisibility()));
        setFieldName(getName());
    }

    @Override
    public String toString() {
        return String.format("%s-%s-%s", fieldName, displayName, typeDisplayName);
    }

    @Override
    public PropertyDTO clone() throws CloneNotSupportedException {
        return (PropertyDTO) super.clone();
    }
}
