package com.air.nc5dev.vo.meta;

import lombok.Data;
import nc.vo.pub.lang.UFDateTime;

import java.io.Serializable;

//com.yonyou.studio.mdp.database.model.DbRelationVO
@Data
public class DbRelationDTO implements Serializable, Cloneable {
    private String id;
    private UFDateTime createTime;
    private String creator;
    private String description;
    private String displayName;
    private String endCardinality;
    private String endFieldID;
    private String endTableID;
    private String help;
    private String modifier;
    private UFDateTime modifyTime;
    private String name;
    private String startCardinality;
    private String startFieldID;
    private String startTableID;
    private Integer versionType;
    private String resid;
    private Integer assType = null;
    private String startAttrID = null;
    Boolean isForeignKey;
}
