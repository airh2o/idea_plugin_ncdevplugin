package com.air.nc5dev.vo.meta;

import lombok.Data;
import nc.vo.pub.lang.UFDateTime;

import java.io.Serializable;
import java.util.List;

//com.yonyou.studio.mdp.database.model.ComponentVO
@Data
public class ComponentDTO implements Serializable, Cloneable {
    private String id;
    private UFDateTime createTime;
    private String creator;
    private String description;
    private String displayName;
    private String help;
    private String modifier;
    private UFDateTime modifyTime;
    private String name;
    private String namespace;
    private String ownModule;
    private String version;
    private Integer versionType;
    private String resid;
    private String resmodule;
    private Boolean preload;
    private Boolean isbizmodel;
    private Boolean fromSourceBmf;
    private String industry = "0";
    private String industryName;
    private Boolean industryIncrease;
    private String createIndustry;
    private String modifyIndustry;
    private String conRouterType;
    private String mainEntity;
    private String gencodestyle;
    private String programcode;
    private String filePath;
    String metaType;


    transient List<? extends ClassDTO> classDTOS;
}
