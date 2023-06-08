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
                "URL 名称:\n" + name + "\n\n" +
                        "label 描述:\n" + label + "\n\n" +
                        "clazz 类名称:\n" + clazz + "\n\n" +
                        "from 来源:\n" + getFromName() + "\n\n" +
                        "type 类型:\n" + getTypeName() + "\n\n" +
                        "appcode 应用编码:\n" + appcode + "\n\n" +
                        "xmlPath 配置文件路径:\n" + xmlPath + "\n\n" +
                        "column 配置文件所在列:\n" + column + "\n\n" +
                        "row 配置文件所在行:\n" + row + "\n\n" +
                        "score 匹配度得分:\n" + score + "\n\n" +
                        "project 工程路径:\n" + project + "\n\n"
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
