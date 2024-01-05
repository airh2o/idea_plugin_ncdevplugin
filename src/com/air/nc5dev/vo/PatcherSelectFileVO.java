package com.air.nc5dev.vo;

import com.intellij.openapi.vfs.VirtualFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * </br>
 * </br>
 * </br>
 *
 * @author 唐粟 Email:209308343@qq.com 微信yongyourj
 * @date 2023/12/1 0001 16:08
 * @project
 * @Version
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PatcherSelectFileVO implements Serializable, Cloneable {
    public String path;
    public String memo;
    @Builder.Default
    public Integer sort = 0;
    public transient VirtualFile file;
}
