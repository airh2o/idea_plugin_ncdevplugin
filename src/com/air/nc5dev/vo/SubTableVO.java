package com.air.nc5dev.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * </br>
 * </br>
 * </br>
 *
 * @author 唐粟 Email:209308343@qq.com
 * @date 2021/11/16 0016 15:56
 * @project
 * @Version
 */
@Data
@Builder
public class SubTableVO {
    private String tableName;
    private String fkColumn;
    private String whereCondition;
    private String sqlNo;
    private List<String> children;
}
