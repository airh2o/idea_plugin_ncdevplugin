package com.air.nc5dev.util.searchfulldatabase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 搜索结果bean
 *
 * @author Air
 * @date 2018年11月21日14:07:01
 * @since v1
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchResultVO {
    @Builder.Default
    public boolean select = true;
    public int row;
    public String table;
    public String field;
    public String sql;
}
