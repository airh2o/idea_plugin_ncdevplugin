package org.tangsu.mstsc.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WindowSizeVO implements Serializable,Cloneable {
    int width;
    int hight;

    @Override
    public String toString() {
        return width + "*" + hight;
    }
}
