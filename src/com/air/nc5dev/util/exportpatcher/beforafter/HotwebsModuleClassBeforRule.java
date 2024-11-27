package com.air.nc5dev.util.exportpatcher.beforafter;

import cn.hutool.core.util.StrUtil;
import com.air.nc5dev.util.IoUtil;
import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.util.exportpatcher.searchs.AbstractContentSearchImpl;
import com.air.nc5dev.vo.FileContentVO;
import com.air.nc5dev.vo.ExportConfigVO;
import com.air.nc5dev.vo.ExportContentVO;

import java.io.File;

/**
 * 处理  hotwebs结构      </br>
 * </br>
 * </br>
 *
 * @author 唐粟 Email:209308343@qq.com
 * @date 2024/11/26 0026 20:11
 * @project
 * @Version
 */
public class HotwebsModuleClassBeforRule extends AbstarctBeforRule {

    @Override
    public void doBefor(ExportContentVO contentVO, FileContentVO fileContentVO) {
        if (fileContentVO.getModule() == null) {
            return;
        }

        ExportConfigVO configVO = contentVO.module2ExportConfigVoMap.get(fileContentVO.getModule().getModule());

        if (FileContentVO.NAME_HOTWEBS_NCCLOUD_RESOURCE.equals(fileContentVO.getName())) {

            return;
        }

        if (configVO.clientToModuleHotwebs && AbstractContentSearchImpl.NC_TYPE_CLIENT.equals(fileContentVO.getName())) {
            java2Hotwebs(contentVO, fileContentVO, configVO);
            return;
        }

        if (configVO.privateToModuleHotwebs && AbstractContentSearchImpl.NC_TYPE_PRIVATE.equals(fileContentVO.getName())) {
            java2Hotwebs(contentVO, fileContentVO, configVO);
            return;
        }

        if (configVO.publicToModuleHotwebs && AbstractContentSearchImpl.NC_TYPE_PUBLIC.equals(fileContentVO.getName())) {
            java2Hotwebs(contentVO, fileContentVO, configVO);
            return;
        }
    }

    private void java2Hotwebs(ExportContentVO contentVO
            , FileContentVO fileContentVO
            , ExportConfigVO configVO
    ) {
        File hotwebs = new File(new File(contentVO.outPath).getParent(), "hotwebs");

        if (StringUtil.isAllNotBlank(fileContentVO.getFile(), fileContentVO.getFileTo())) {
            fileContentVO.setFileTo(hotwebs.getPath()
                    + File.separatorChar + configVO.getModuleHotwebsName()
                    + File.separatorChar + "WEB-INF"
                    + File.separatorChar + "classes"
                    + IoUtil.rigthPathRemovePrefix(fileContentVO.getFile(), fileContentVO.getSrcTop())
            );
        }
        if (StringUtil.isAllNotBlank(fileContentVO.getSrcFile(), fileContentVO.getSrcFileTo())) {
            fileContentVO.setFileTo(hotwebs.getPath()
                    + File.separatorChar + configVO.getModuleHotwebsName()
                    + File.separatorChar + "WEB-INF"
                    + File.separatorChar + "classes"
                    + IoUtil.rigthPathRemovePrefix(fileContentVO.getFile(), fileContentVO.getSrcTop())
            );
        }
    }
}
