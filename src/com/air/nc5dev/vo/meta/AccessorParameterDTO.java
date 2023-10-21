package com.air.nc5dev.vo.meta;

import lombok.Data;

import java.io.Serializable;

//com.yonyou.studio.mdp.database.model.AccessorParameterVO
@Data
public class AccessorParameterDTO implements Serializable, Cloneable {
    private String id;
    private String paraValue;
    private Integer assosequence;
    private Integer versionType;
    private String industry;

}
