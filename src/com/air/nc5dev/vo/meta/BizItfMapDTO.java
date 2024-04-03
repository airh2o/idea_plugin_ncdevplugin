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
//com.yonyou.studio.mdp.database.model.BizItfMapVO
@Data
public class BizItfMapDTO implements Serializable, Cloneable {
    /**
     * busiitfid 是引用的接口实体id
     */
    private String bizInterfaceID;
    /**
     * 是引用的接口的属性(字段)id或实体的属性id （md_property的id）
     */
    private String intAttrID;
    /**
     * 3. cellid 是实体id
     */
    private String classID;
    /**
     * attrid 是业务接口属性映射-映射属性 的(实体的)字段id
     */
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
    String attrpathid;

    public BizItfMapDTO clone() {
        try {
            return (BizItfMapDTO) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
