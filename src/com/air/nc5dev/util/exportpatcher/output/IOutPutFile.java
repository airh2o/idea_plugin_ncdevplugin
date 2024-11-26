package com.air.nc5dev.util.exportpatcher.output;

import com.air.nc5dev.vo.FileContentVO;
import com.air.nc5dev.vo.ExportContentVO;

import java.util.function.BiConsumer;

/**
 * </br>
 * </br>
 * </br>
 *
 * @author 唐粟 Email:209308343@qq.com
 * @date 2024/11/26 0026 19:01
 * @project
 * @Version
 */
public interface IOutPutFile {
    void outPut(ExportContentVO contentVO
            , FileContentVO file
            , BiConsumer<ExportContentVO, FileContentVO> befor
            , BiConsumer<ExportContentVO, FileContentVO> after
    );
}
