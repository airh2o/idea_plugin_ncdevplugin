package com.air.nc5dev.vo.meta;

import lombok.Data;

import java.util.List;

/**
 * 实体vo 扩充版本 <br>
 * <br>
 * <br>
 *
 * @author 唐粟 Email:209308343@qq.com 微信yongyourj
 * @date 2023/12/3 0003 11:56
 * @project
 * @Version
 */
@Data
public class ClassExtInfoDTO extends ClassDTO {
    List<PropertyDTO> perperties;
    //单据信息
    String pk_billtypecode;
    String billtypename;
    String nodecode;
    //NC6 xml配置信息
    String fun_name;
    String paramvalue;
    //NCC 前端页面信息
    String pagecode;
    String pageurl;


    public ClassExtInfoDTO() {
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
