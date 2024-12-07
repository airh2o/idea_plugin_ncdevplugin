package com.air.nc5dev.util.exportpatcher.output;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.air.nc5dev.util.IoUtil;
import com.air.nc5dev.vo.FileContentVO;
import com.air.nc5dev.vo.ExportContentVO;

import java.io.File;
import java.util.HashMap;
import java.util.function.BiConsumer;

/**
 * </br>
 * </br>
 * </br>
 *
 * @author 唐粟 Email:209308343@qq.com
 * @date 2024/11/26 0026 19:04
 * @project
 * @Version
 */
public abstract class AbstractOutPutFileImpl implements IOutPutFile {
    @Override
    public void outPut(ExportContentVO contentVO
            , FileContentVO file
            , BiConsumer<ExportContentVO, FileContentVO> befor
            , BiConsumer<ExportContentVO, FileContentVO> after) {

        if (befor != null) {
            befor.accept(contentVO, file);
        }

        if (file.getBefor() != null) {
            file.getBefor().accept(contentVO, file);
        }

        if (StrUtil.isAllNotBlank(file.getFile(), file.getFileTo())) {
            doOutPut(contentVO, file, new File(file.getFile()), new File(file.getFileTo()));
        }

        if (StrUtil.isAllNotBlank(file.getSrcFile(), file.getSrcFileTo())) {
            doOutPut(contentVO, file, new File(file.getSrcFile()), new File(file.getSrcFileTo()));
        }

        if (after != null) {
            after.accept(contentVO, file);
        }

        if (file.getAfter() != null) {
            file.getAfter().accept(contentVO, file);
        }
    }

    public void doOutPut(ExportContentVO contentVO, FileContentVO file, File from, File to) {
        if (file.getFilter() != null && !file.getFilter().apply(from)) {
            return;
        }

        if (from.isDirectory()) {
             //  System.out.println(from + "  1->  " + to);
            if (file.isHasChildDir()) {
                IoUtil.copyAllFileAndDir(from, to, file.getFilter());
            }else{
                IoUtil.copyAllFile(from, to, file.getFilter());
            }
        } else {
           //  System.out.println(from + "  2->  " + to);
            IoUtil.copyFile(from, to);
        }
    }
}
