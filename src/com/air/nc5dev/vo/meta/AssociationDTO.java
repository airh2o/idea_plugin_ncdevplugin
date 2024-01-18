package com.air.nc5dev.vo.meta;

import lombok.Data;
import nc.vo.pub.lang.UFDateTime;

import java.io.Serializable;

@Data
public class AssociationDTO implements Serializable, Cloneable {
    private static final long serialVersionUID = 9002173691572743940L;
    private String id;
    private String componentID;
    private String creator;
    private UFDateTime createTime;
    private String endCardinality;
    private String startBeanID;
    private String endElementID;
    private Boolean isActive;
    private String modifier;
    private UFDateTime modifyTime;
    private String name;
    private String startCardinality;
    private String startElementID;
    private Integer type;
    private Integer versionType;
    public String srcAttributeid;
    private String industry = "0";


    String industryChanged = "false";
    String isSource = "false";
    String createIndustry = "0";
    String description;
    String help;
    String source;
    String displayName;
    String modifyIndustry;

    String target; // ==endElementID
}
