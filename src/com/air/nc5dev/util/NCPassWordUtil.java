package com.air.nc5dev.util;

import com.air.nc5dev.enums.NcVersionEnum;
import com.air.nc5dev.util.ncutils.NC5xEncode;
import com.air.nc5dev.util.ncutils.NC6xEncode;

/**
 * <br/>
 * <br/>
 * <br/>
 *
 * @author 唐粟 Email:209308343@qq.com
 * @date 2020/11/9 11:51
 * @project
 * @Version
 */
public class NCPassWordUtil {
    /**
     * 解码密码明文,支持NC5系列和部分特殊用户
     *
     * @param pass
     * @param expars
     * @return
     */
    public static String decode(String pass, Object... expars) {
        if (NcVersionEnum.NC5.equals(ProjectNCConfigUtil.getNCVerSIon())
                || NcVersionEnum.U8Cloud.equals(ProjectNCConfigUtil.getNCVerSIon())) {
            return new NC5xEncode().decode(pass);
        }

        if (NcVersionEnum.NC6.equals(ProjectNCConfigUtil.getNCVerSIon())
                || NcVersionEnum.NCC.equals(ProjectNCConfigUtil.getNCVerSIon())) {
            return new NC6xEncode().decode(pass, expars);
        }

        return pass;
    }

    /**
     * 加密 密码
     *
     * @param text
     * @return
     */
    public static String encode(String pass, Object... expars) {
        if (NcVersionEnum.NC5.equals(ProjectNCConfigUtil.getNCVerSIon())
                || NcVersionEnum.U8Cloud.equals(ProjectNCConfigUtil.getNCVerSIon())) {
            return new NC5xEncode().encode(pass);
        }

        if (NcVersionEnum.NC6.equals(ProjectNCConfigUtil.getNCVerSIon())
                || NcVersionEnum.NCC.equals(ProjectNCConfigUtil.getNCVerSIon())) {
            return new NC6xEncode().encode(pass, expars);
        }

        return pass;
    }
}
