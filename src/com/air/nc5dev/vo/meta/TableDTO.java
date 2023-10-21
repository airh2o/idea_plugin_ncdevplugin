package com.air.nc5dev.vo.meta;

import lombok.Data;
import nc.vo.pub.lang.UFDateTime;

import java.io.Serializable;
import java.util.List;

//com.yonyou.studio.mdp.database.model.TableVO
@Data
public class TableDTO implements Serializable, Cloneable {
    private String id;
    private UFDateTime createTime;
    private String creator;
    private String databaseID;
    private String description;
    private String displayName;
    private String help;
    private Boolean isActive;
    private Boolean fromSourceBmf;
    private String modifier;
    private UFDateTime modifyTime;
    private String name;
    private Integer versionType;
    private String stereoType;
    private List<String> StereoTypeList;
    private String resmodule;
    private String resid;
    private String parentTable;
    private Boolean isExtendTable;
    private String entityID;
    private String industry;
    private String createIndustry = "0";
    private Boolean isSource;

}
