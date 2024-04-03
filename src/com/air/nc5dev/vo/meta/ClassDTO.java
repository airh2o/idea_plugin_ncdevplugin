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


    public String id;
    public String accessorClassName;
    public Integer classType;
    public String componentID;
    public UFDateTime createTime;
    public String creator;
    public String description;
    public String displayName;
    public String fullClassName;
    public String help;
    public Boolean isActive;
    public String keyAttribute;
    public String modifier;
    public UFDateTime modifyTime;
    public String name;
    public String parentClassID;
    public Integer precise;
    public String refModelName;
    public String returnType;
    public Integer versionType = 0;
    public Boolean isPrimary;
    public String defaultTableName;
    public String stereoType;
    public List<String> StereoTypeList;
    public Boolean isAuthen = true;
    public String resid;
    public String bizItfImpClassName;
    public String modInfoClassName;
    public Boolean isCreateSQL = true;
    public Boolean isExtendBean = false;
    public String resmodule;
    public Boolean fixedLength;
    public String userDefClassName;
    public String industry = "0";
    public Boolean industryChanged = false;
    public Boolean isSource = false;
    public String createIndustry = "0";
    public String modifyIndustry = "0";
    public Integer height = 100;
    public Integer width = 100;
    public Integer x;
    public Integer y;

    public String aggFullClassName;
    public String dataType;
    public String dbtype;
    public String typeDisplayName;
    public String typeName;
    public String visibility = "public";

    public transient List<PropertyDTO> perperties;

    public void setIndustry(String industry) {
        if (StringUtil.isBlank(industry)) {
            industry = "0";
        }

        this.industry = industry;
    }
}
