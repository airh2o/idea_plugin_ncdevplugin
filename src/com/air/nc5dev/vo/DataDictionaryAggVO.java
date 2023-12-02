package com.air.nc5dev.vo;

import com.air.nc5dev.vo.meta.SearchComponentVO;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * </br>
 * </br>
 * </br>
 *
 * @author 唐粟 Email:209308343@qq.com 微信yongyourj
 * @date 2023/12/1 0001 16:08
 * @project
 * @Version
 */
@Data
public class DataDictionaryAggVO implements Serializable, Cloneable {
    /**
     * select version from sm_product_version where versionn is not null;
     */
    String ncVersion;
    NCDataSourceVO ncDataSourceVO;
    String ncHome;
    String projectName;

    List<Module> modules;

    /**
     * select id, name ,displayname, parentmoduleid from md_module ;
     */
    @Data
    public static class Module implements Serializable, Cloneable {
        String id;
        String name;
        String displayname;
        String parentmoduleid;
        /**
         * 孩子
         */
        @Builder.Default
        List<Module> childs = new ArrayList<>();
        /**
         * 元数据
         */
        @Builder.Default
        List<SearchComponentVO> metas = new ArrayList<>();
    }
}
