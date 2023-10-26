package com.air.nc5dev.ui.adddeffiled;

import com.air.nc5dev.util.ReflectUtil;
import com.air.nc5dev.vo.meta.ClassDTO;
import com.air.nc5dev.vo.meta.PropertyDTO;
import lombok.Data;

import java.util.List;

/**
 * </br>
 * </br>
 * </br>
 *
 * @author 唐粟 Email:209308343@qq.com 微信yongyourj
 * @date 2023/10/23 0023 11:45
 * @project
 * @Version
 */
@Data
public class ClassComboxDTO {
    int order1;
    String name;
    String displayName;
    String ownModule;
    String namespace;
    String id;
    String fileVersion;
    String fileName;
    String metaType;
    String filePath;


    transient List<PropertyDTO> perperties;

    public static ClassComboxDTO ofClassDTO(ClassDTO c) {
        ClassComboxDTO a = new ClassComboxDTO();

        ReflectUtil.copy2VO(c, a);
        a.setPerperties(c.getPerperties());

        return a;
    }


    @Override
    public String toString() {
        return String.format("%s-%s-%s", ownModule, name, displayName);
    }
}
