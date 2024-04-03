package com.air.nc5dev.editor.bmf.languageinfo;

import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

/**
 * bmf 文件类型 <br>
 * <br>
 * <br>
 * <br>
 *
 * @author air Email: 209308343@qq.com
 * @version NC505, JDK1.5+, V1.0
 * @date 2020/1/17 0017 19:26
 * @project
 */
public class BmfMDPFileType extends LanguageFileType {
    public static final BmfMDPFileType INSTANCE = new BmfMDPFileType();

    private BmfMDPFileType() {
        super(BmfMDPLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "元数据文件";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "NC bmf元数据文件";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "bmf";
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return BmfMDPIcons.FILE;
    }
}
