package com.air.nc5dev.vo.meta;

import lombok.Data;

import java.util.List;

/**
 * </br>
 * </br>
 * </br>
 *
 * @author 唐粟 Email:209308343@qq.com 微信yongyourj
 * @date 2023/12/3 0003 11:56
 * @project
 * @Version
 */
@Data
public class ClassDTO2 extends ClassDTO {
    List<PropertyDTO> perperties;

    public ClassDTO2() {
        super();

        setVersionType(null);
        setIsAuthen(null);
        setIsCreateSQL(null);
        setIndustry(null);
        setIndustryChanged(null);
        setCreateIndustry(null);
        setModifyIndustry(null);
        setHeight(null);
        setWidth(null);
        setVisibility(null);
    }
}
