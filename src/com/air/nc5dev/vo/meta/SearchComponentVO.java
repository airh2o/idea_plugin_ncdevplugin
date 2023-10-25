package com.air.nc5dev.vo.meta;

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
 * @date 2023/10/19 0019 15:43
 * @project
 * @Version
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchComponentVO extends ComponentDTO implements Serializable, Cloneable {
    Integer order1;
    String fileVersion;
    String fileName;
    String filePath;

    //实体的信息
    transient String classId;
    transient String className;
}
