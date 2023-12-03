package com.air.nc5dev.vo;

import com.air.nc5dev.vo.meta.ClassDTO;
import com.air.nc5dev.vo.meta.EnumValueDTO;
import com.air.nc5dev.vo.meta.SearchComponentVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    String groupName;

    List<Module> modules;

    Map<String, ClassDTO> classMap;
    Map<String, SearchComponentVO> compomentIdMap;
    Map<String, List<EnumValueDTO>> classId2EnumValuesMap;
    Map<String, DataDictionaryAggVO.Module> id2ModuleMap;
    ArrayList<DataDictionaryAggVO.Module> allModules;

    /**
     * select id, name ,displayname, parentmoduleid from md_module ;
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Module implements Serializable, Cloneable {
        String id;
        String name;
        String displayname;
        String fullClassName;
        String aggFullClassName;
        String parentmoduleid;
        @Builder.Default
        int type = 0;
        /**
         * 孩子
         */
        @Builder.Default
        List<Module> childs = new ArrayList<>();
        /**
         * 元数据
         */
       /* @Builder.Default
        List<SearchComponentVO> metas = new ArrayList<>();*/
    }


}
