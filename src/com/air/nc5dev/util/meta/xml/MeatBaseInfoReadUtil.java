package com.air.nc5dev.util.meta.xml;

import cn.hutool.core.exceptions.UtilException;
import com.air.nc5dev.exception.BusinessException;
import com.air.nc5dev.util.ReflectUtil;
import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.util.XmlUtil;
import com.air.nc5dev.vo.meta.ComponentDTO;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.StringReader;

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
        if (bmfXml.startsWith("\uFEFF")) {
            bmfXml = StringUtil.removeStart(bmfXml, "\uFEFF");
        }

        ComponentDTO d = new ComponentDTO();
        Element root = null;
        try {
            Document doc = XmlUtil.readXML(bmfXml);
            root = XmlUtil.getRootElement(doc);
        } catch (Throwable e) {
            //出错了
            e.printStackTrace();
            System.err.println("dom4j读取失败，换sax试试。");

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            try {
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document document = db.parse(new ByteArrayInputStream(bmfXml.getBytes()));
                NodeList component = document.getElementsByTagName("component");
                if (component.getLength() < 1) {
                    return null;
                }
                root = (Element) component.item(0);
            } catch (Throwable ex) {
                ex.printStackTrace();
                System.err.println("sax都读取失败，摆烂 不要这个了。");
            }
        }

        NamedNodeMap attrs = root.getAttributes();
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
