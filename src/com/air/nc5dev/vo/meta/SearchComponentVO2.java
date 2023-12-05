package com.air.nc5dev.vo.meta;

import lombok.Data;

import java.util.List;

/**
 * </br>
 * </br>
 * </br>
 *
 * @author 唐粟 Email:209308343@qq.com 微信yongyourj
 * @date 2023/12/3 0003 12:31
 * @project
 * @Version
 */
@Data
public class SearchComponentVO2 extends SearchComponentVO {
    List<? extends ClassDTO> classDTOS;

    public SearchComponentVO2() {
        super();

        setIndustry(null);
    }
}
