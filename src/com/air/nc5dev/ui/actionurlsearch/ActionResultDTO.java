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

    public String displayText() {
        return
                "URL 名称=" + name + "\n" +
                        "label 描述=" + label + "\n" +
                        "clazz 类名称=" + clazz + "\n" +
                        "from 来源=" + getFromName() + "\n" +
                        "type 类型=" + getTypeName() + "\n" +
                        "appcode 应用编码=" + appcode + "\n" +
                        "xmlPath 配置文件路径=" + xmlPath + "\n" +
                        "column 配置文件所在列=" + column + "\n" +
                        "row 配置文件所在行=" + row + "\n" +
                        "score 匹配度得分=" + score + "\n" +
                        "project 工程路径=" + project + "\n"
                ;
    }

    public String getTypeName() {
        if (TYPE_ACTION == getType()) {
            return "NCC(BIP) Action类";
        } else if (TYPE_UPM == getType()) {
            return "NC upm或xml配置的servlet";
        }

        return getType() + "";
    }

    public String getFromName() {
        if (FROM_SRC == getFrom()) {
            return "工程源码";
        } else if (FROM_HOME == getFrom()) {
            return "NC HOME";
        }

        return getFrom() + "";
    }


}
