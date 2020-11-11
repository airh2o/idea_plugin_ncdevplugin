//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.air.nc5dev.util.ncutils.nc5x;

import com.air.nc5dev.exception.BusinessException;
import com.air.nc5dev.vo.NcUserVo;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

public class NC6xEncode {
    /**
     * 为了能区分用户密码是否被md5加密完，在被md5加密的串前面加前缀
     **/
    public final static String MD5PWD_PREFIX = "U_U++--V";

    //最初的密码前缀过于简单，但为了兼容先保持一段时间。 逐渐替换
    @Deprecated
    public final static String MD5PWD_PREFIX_Deprecated = "md5";

    public NC6xEncode() {
    }

    public String decode(String s, Object... expars) {
        return "NC6以上不支持逆解密码";
    }

    public String encode(String s, Object... expars) {
        return getEncodedPassword((NcUserVo) expars[0], s);
    }

    /**
     * 由UserVO 和 明文密码获得 加密后的用户密码
     *
     * @param user
     * @param expresslyPWD 明文密码
     * @return 加密后的密码串
     * @throws BusinessException
     */
    public static String getEncodedPassword(NcUserVo user, String expresslyPWD) throws BusinessException {
        if (user == null || StringUtils.isBlank(user.getId()))
            throw new BusinessException("illegal arguments");
        if (StringUtils.isNotBlank(expresslyPWD) && expresslyPWD.startsWith(MD5PWD_PREFIX))
            return expresslyPWD;

        String codecPWD = DigestUtils.md5Hex(user.getId() + StringUtils.stripToEmpty(expresslyPWD));

        return MD5PWD_PREFIX + codecPWD;
    }
}
