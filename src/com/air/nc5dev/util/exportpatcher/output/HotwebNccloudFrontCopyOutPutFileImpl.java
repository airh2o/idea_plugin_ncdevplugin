package com.air.nc5dev.util.exportpatcher.output;

import cn.hutool.core.util.StrUtil;
import com.air.nc5dev.util.ExecUtil;
import com.air.nc5dev.util.IoUtil;
import com.air.nc5dev.vo.FileContentVO;
import com.air.nc5dev.util.idea.LogUtil;
import com.air.nc5dev.vo.ExportContentVO;

import java.io.File;
import java.io.IOException;

/**
 * </br>
 * </br>
 * </br>
 *
 * @author 唐粟 Email:209308343@qq.com
 * @date 2024/11/26 0026 19:05
 * @project
 * @Version
 */
public class HotwebNccloudFrontCopyOutPutFileImpl extends SimpleCopyOutPutFileImpl {
    public void build(ExportContentVO contentVO, FileContentVO file) {
        try {
            contentVO.indicator.setText("强制删除前端hotwebs的dist后执行" + contentVO.getNpmRunCommand() + "中...");
            IoUtil.cleanUpDirFiles(new File(file.getFileTo()));
            String cm = ExecUtil.npmBuild(file.getFile()
                    , (line) -> {
                        contentVO.indicator.setText("npm building:" + StrUtil.removeAllLineBreaks(line));
                    }
                    , contentVO.getNpmRunCommand()
            );
            LogUtil.infoAndHide("前端 " + contentVO.getNpmRunCommand() + ": " + cm);
        } catch (IOException e) {
            LogUtil.error("build前端hotwebs/nccloud失败:" + e.getMessage(), e);
        }
    }
}
