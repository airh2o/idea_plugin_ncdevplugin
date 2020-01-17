package com.air.nc5dev.editor.bmf.languageinfo;

import com.intellij.lang.Language;

/**
 *  NC 元数据语言， bmf文件 </br>
 * </br>
 * </br>
 * </br>
 *
 * @author air Email: 209308343@qq.com
 * @version NC505, JDK1.5+, V1.0
 * @date 2020/1/17 0017 19:23
 * @project
 */
public class BmfMDPLanguage extends Language{
    public static final BmfMDPLanguage INSTANCE = new BmfMDPLanguage();

    private BmfMDPLanguage() {
        super("bmfmdp");
    }
}
