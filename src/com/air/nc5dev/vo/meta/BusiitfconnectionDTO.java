package com.air.nc5dev.vo.meta;

import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.util.V;
import lombok.Data;

import java.io.Serializable;

/**
 * <busimap> 标签 （ ）
 * <br>
 *
 * @Author air Email: 209308343@qq.com
 * @Description
 * @Date 2023/10/20 0020 13:55
 **/
@Data
public class BusiitfconnectionDTO implements Serializable, Cloneable {
    private String bizInterfaceID;
    private String intAttrID;
    private String classID;
    private String classAttrID;
    private String bizItfImpClassName;
    private String classAttrPath;


    String componentID;
    String createIndustry = "0";
    String createTime = V.nowDateTime();
    String creator;
    String description;
    String displayName;
    String help;
    String id = StringUtil.uuid();
    String industryChanged = "false";
    String isSource = "false";
    String modifier = "yonyouBQ";
    String modifyIndustry = "0";
    String modifyTime = V.nowDateTime();
    String name = "busiItf connection";
    String source;
    String realsource;
    String versionType = "0";
    String target;
    String realtarget;
}
