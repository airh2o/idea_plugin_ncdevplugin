package com.air.nc5dev.enums;

/**
 * NC版本 <br/>
 * <br/>
 * <br/>
 *
 * @author 唐粟 Email:209308343@qq.com
 * @date 2020/11/9 11:53
 * @project
 * @Version
 */
public enum NcVersionEnum {
    NC5,
    NC6,
    NCC,
    BIP,
    U8Cloud,
    ;


    public static boolean isNCCOrBIP(NcVersionEnum v){
        return NCC == v || BIP == v;
    }
}
