package com.air.nc5dev.vo.meta;

import lombok.Data;

import java.io.Serializable;

//com.yonyou.studio.mdp.database.model.AccessorParameterVO
@Data
public class AccessorParameterDTO implements Serializable, Cloneable {
    String id;
    String paraValue;
    Integer assosequence;
    Integer versionType;
    String industry;
    String name;
    String displayName;
}
