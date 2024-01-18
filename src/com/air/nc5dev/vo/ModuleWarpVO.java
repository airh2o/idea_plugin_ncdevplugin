package com.air.nc5dev.vo;

import com.air.nc5dev.util.NCPassWordUtil;
import com.intellij.openapi.module.Module;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class ModuleWarpVO implements Serializable, Cloneable {
    Module module;
    String configName;

    public ModuleWarpVO(Module module) {
        this.module = module;
        setConfigName(module.getName());
    }
}
