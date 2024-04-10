package com.air.nc5dev.vo.meta;

import lombok.Data;
import nc.vo.pub.lang.UFDateTime;

import java.io.Serializable;

/**
 * 实体列vo
 */
//com.yonyou.studio.mdp.database.model.ColumnVO
@Data
public class ColumnDTO implements Serializable, Cloneable {
    private String id;
    private String SQLDateType;
    private UFDateTime createTime;
    private String creator;
    private String defaultValue;
    private String description;
    private String displayName;
    private String help;
    private Integer incrementSeed;
    private Integer incrementStep;
    private Boolean isActive;
    private Boolean identitied;
    private Boolean pkey;
    private Boolean nullable;
    private Integer columnlength;
    private String modifier;
    private UFDateTime modifyTime;
    private String name;
    private Integer precise;
    private Integer columnSequence;
    private String tableID;
    private Integer versionType;
    private String resid;
    private String groupID;
    private int columnType;
    private Boolean forLocale;
    private Boolean dynamic;
    private String dynamicTable;
    private String propertyID;
    private String attr_Industry = "";
    private Boolean isSource;

}
