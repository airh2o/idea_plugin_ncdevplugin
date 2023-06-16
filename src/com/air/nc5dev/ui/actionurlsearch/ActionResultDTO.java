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
        return displayText("\n");
    }
    public String displayText(String warp) {
        return
                "URL 名称:"+warp + name + ""+ warp +
                        "label 描述:"+warp + label + ""+warp +
                        "clazz 类名称:"+warp + clazz + ""+warp +
                        "from 来源:"+warp + getFromName() + ""+warp +
                        "type 类型:"+warp + getTypeName() + ""+warp +
                        "appcode 应用编码:"+warp + appcode + ""+warp +
                        "xmlPath 配置文件路径:"+warp + xmlPath + ""+warp +
                        "column 配置文件所在列:"+warp + column + ""+warp +
                        "row 配置文件所在行:"+warp + row + ""+warp +
                        "auth 配置文件路径:"+warp + authPath + ""+warp +
                        "score 匹配度得分:"+warp + score + ""+warp +
                        "project 工程路径:"+warp + project + ""+warp+
                        (type == TYPE_ACTION ? "/nccloud/" + name.replace('.', '/') + ".do" : "")
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
