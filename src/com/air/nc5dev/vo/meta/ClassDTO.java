package com.air.nc5dev.vo.meta;

import com.air.nc5dev.util.StringUtil;
import lombok.Data;
import nc.vo.pub.lang.UFDateTime;

import java.io.Serializable;
import java.util.List;

//com.yonyou.studio.mdp.database.model.ClassVO
@Data
public class ClassDTO implements Serializable, Cloneable {

    public static Integer CLASSTYPE_ENTITY = 201;
    public static Integer CLASSTYPE_ENUMERATE = 203;
    public static Integer CLASSTYPE_BUSINESSITF = 206;


    private String id;
    private String accessorClassName;
    private Integer classType;
    private String componentID;
    private UFDateTime createTime;
    private String creator;
    private String description;
    private String displayName;
    private String fullClassName;
    private String help;
    private Boolean isActive;
    private String keyAttribute;
    private String modifier;
    private UFDateTime modifyTime;
    private String name;
    private String parentClassID;
    private Integer precise;
    private String refModelName;
    private String returnType;
    private Integer versionType = 0;
    private Boolean isPrimary;
    private String defaultTableName;
    private String stereoType;
    private List<String> StereoTypeList;
    private Boolean isAuthen = true;
    private String resid;
    private String bizItfImpClassName;
    private String modInfoClassName;
    private Boolean isCreateSQL = true;
    private Boolean isExtendBean;
    private String resmodule;
    private Boolean fixedLength;
    private String userDefClassName;
    private String industry = "0";
    private Boolean industryChanged = false;
    private Boolean isSource = true;
    private String createIndustry = "0";
    private String modifyIndustry = "0";
    private Integer height = 100;
    private Integer width = 100;
    private Integer x;
    private Integer y;

    String aggFullClassName;
    String dataType;
    String dbtype;
    String typeDisplayName;
    String typeName;
    String visibility = "public";

    transient List<PropertyDTO> perperties;

    public void setIndustry(String industry) {
        if (StringUtil.isBlank(industry)) {
            industry = "0";
        }

        this.industry = industry;
    }
}
