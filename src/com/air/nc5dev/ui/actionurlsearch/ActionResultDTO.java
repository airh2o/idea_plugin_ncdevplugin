package com.air.nc5dev.ui.actionurlsearch;

import com.air.nc5dev.vo.NCCActionInfoVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
public class ActionResultDTO extends NCCActionInfoVO {
    int order1;
}
