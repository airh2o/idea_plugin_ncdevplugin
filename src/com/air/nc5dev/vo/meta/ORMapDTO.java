package com.air.nc5dev.vo.meta;

import lombok.Data;

import java.io.Serializable;

//com.yonyou.studio.mdp.database.model.ORMapVO
@Data
public class ORMapDTO implements Serializable, Cloneable {
    private String attributeID;
    private String classID;
    private String columnID;
    private String tableID;

}
