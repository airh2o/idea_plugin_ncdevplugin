package com.air.nc5dev.util.meta.xml;

import cn.hutool.core.exceptions.UtilException;
import com.air.nc5dev.exception.BusinessException;
import com.air.nc5dev.util.ReflectUtil;
import com.air.nc5dev.util.XmlUtil;
import com.air.nc5dev.vo.meta.ComponentDTO;
import org.w3c.dom.*;

import java.io.File;

/**
 * </br>
 * </br>
 * </br>
 *
 * @author 唐粟 Email:209308343@qq.com 微信yongyourj
 * @date 2023/10/21 0021 13:18
 * @project
 * @Version
 */
public class MeatBaseInfoReadUtil {
    public static ComponentDTO readComponentInfo(String bmfXml) {
        Document doc = XmlUtil.readXML(bmfXml);
        Element root = XmlUtil.getRootElement(doc);
        NamedNodeMap attrs = root.getAttributes();
        ComponentDTO d = new ComponentDTO();
        for (int i = 0; i < attrs.getLength(); i++) {
            Node item = attrs.item(i);
            try {
                ReflectUtil.setFieldValueAutoConvert(d, item.getNodeName(), item.getTextContent());
            } catch (Throwable e) {
            }
        }
        return d;
    }
}
