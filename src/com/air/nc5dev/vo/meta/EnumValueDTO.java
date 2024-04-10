package com.air.nc5dev.vo.meta;

import lombok.Data;

import java.io.Serializable;

/**
 * 枚举-值vo
 *
 * @Author air Email: 209308343@qq.com
 * @Description
 * @Date 2024/4/10 0010 9:53
 **/
//com.yonyou.studio.mdp.database.model.interfaces.IEnumValueVO
@Data
public class EnumValueDTO implements Serializable, Cloneable {
    private String id;
    private String description;
    private String name;
    private String value;
    private Integer versionType;
    private Boolean hidden;
    private String resid;
    private Integer enumsequence;
    private String industry = "0";
    private Boolean industryChanged;
    private String createIndustry;
    private String modifyIndustry;
    private Boolean isSource;
    String itemId;

}
