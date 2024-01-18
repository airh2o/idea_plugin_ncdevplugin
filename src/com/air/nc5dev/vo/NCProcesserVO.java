package com.air.nc5dev.vo;

import com.air.nc5dev.ui.compoment.SimpleListColumn;
import com.air.nc5dev.util.NCPassWordUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <br/>
 * <br/>
 * <br/>
 *
 * @author 唐粟 Email:209308343@qq.com
 * @date 2020/11/9 13:42
 * @project
 * @Version
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NCProcesserVO {
    @SimpleListColumn(id = true, value = "PID进程ID")
    String pid;
    @SimpleListColumn("类型")
    String type;
    @SimpleListColumn("端口")
    String port;
    @SimpleListColumn("HOME")
    String home;
    @SimpleListColumn("路径")
    String path;
}
