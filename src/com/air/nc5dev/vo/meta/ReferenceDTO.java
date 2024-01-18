package com.air.nc5dev.vo.meta;

import lombok.Data;
import nc.vo.pub.lang.UFDateTime;

import java.io.Serializable;

//com.yonyou.studio.mdp.database.model.BizItfMapVO
@Data
public class ReferenceDTO implements Serializable, Cloneable {
    private String componentID;
    private String id;
    private String moduleName;
    private String mdFilePath;
    private String refId;
    private String createIndustry = "0";
    private String modifyIndustry = "0";
    private UFDateTime createTime = new UFDateTime(System.currentTimeMillis());
    private UFDateTime modifyTime = new UFDateTime(System.currentTimeMillis());
    private String creator = "yonyouBQ";
    private String modifier = "yonyouBQ";
    private String description;
    private String resid;
    private String displayName;
    private String fullClassName;

    String name;
    String help;
    String isSource = "true";
    String industryChanged = "false";
    String versionType = "0";
    String visibility = "public";
    Integer width = 100;
    Integer height = 100;
    int x = 200;
    int y = 200;
}
