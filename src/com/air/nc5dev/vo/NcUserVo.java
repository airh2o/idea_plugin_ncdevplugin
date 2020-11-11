package com.air.nc5dev.vo;

import com.air.nc5dev.util.NCPassWordUtil;

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
        return name + " | " + NCPassWordUtil.decode(getPass(), this);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getLocked() {
        return locked;
    }

    public void setLocked(String locked) {
        this.locked = locked;
    }
}
