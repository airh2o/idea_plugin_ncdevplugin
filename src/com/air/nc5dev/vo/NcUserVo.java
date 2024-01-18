package com.air.nc5dev.vo;

import com.air.nc5dev.util.NCPassWordUtil;
import lombok.Data;

/**
 * NC的操作员vo <br/>
 * <br/>
 * <br/>
 *
 * @author 唐粟 Email:209308343@qq.com
 * @date 2020/11/9 13:42
 * @project
 * @Version
 */
@Data
public class NcUserVo {
    /**
     * id
     */
    private String id;
    /**
     * 用户名字
     */
    private String name;
    /**
     * 用户编码-账号
     */
    private String code;
    /**
     * 密码
     */
    private String pass;
    /**
     * 锁定标志
     */
    private String locked;


    @Override
    public String toString() {
        return String.format("%s | %s | %s"
                , code
                , name
                , NCPassWordUtil.decode(getPass(), this)
        );
    }

}
