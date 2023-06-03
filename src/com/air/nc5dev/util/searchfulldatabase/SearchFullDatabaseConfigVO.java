package com.air.nc5dev.util.searchfulldatabase;

import com.air.nc5dev.ui.SearchFullDataBaseDialog;
import com.air.nc5dev.vo.NCDataSourceVO;
import com.intellij.openapi.progress.ProgressIndicator;
import lombok.Builder;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Air
 * @date 2018年11月21日14:07:01
 * @since v1
 */
@Data
@Builder
public class SearchFullDatabaseConfigVO {
    public String key;
    public ProgressIndicator indicator;
    public boolean fastQuery;
    public String tableListSql;
    @Builder.Default
    public Set<String> skipColumns = new HashSet<>();
    public int threadNum;
    public SearchFullDataBaseDialog dialog;
    /**
     * like匹配模式：
     * 0 like '%内容%' , 1 like '内容%' , 2 like '%内容', 3 ='内容'
     */
    @Builder.Default
    public int likeType = 0;
    public NCDataSourceVO dataSource;
}
