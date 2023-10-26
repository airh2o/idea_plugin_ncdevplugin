package com.air.nc5dev.util.meta.xml;

import cn.hutool.core.exceptions.UtilException;
import com.air.nc5dev.exception.BusinessException;
import com.air.nc5dev.util.ReflectUtil;
import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.util.XmlUtil;
import com.air.nc5dev.vo.meta.ClassDTO;
import com.air.nc5dev.vo.meta.ComponentDTO;
import com.air.nc5dev.vo.meta.PropertyDTO;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.StringReader;
import java.util.*;

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

        ComponentDTO d = new ComponentDTO();

        attr2VO(d, root, null);

        return d;
    }

    public static ComponentDTO readEntityList(String bmfXml) {
        if (bmfXml.startsWith("\uFEFF")) {
            bmfXml = StringUtil.removeStart(bmfXml, "\uFEFF");
        }

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

        ArrayList<ClassDTO> cs = new ArrayList<>();
        ComponentDTO d = new ComponentDTO();
        d.setClassDTOS(cs);

        attr2VO(d, root, null);

        if (root.getElementsByTagName("celllist").getLength() < 1) {
            return d;
        }

        Element celllist = (Element) root.getElementsByTagName("celllist").item(0);
        NodeList ets = celllist.getElementsByTagName("entity");
        if (ets.getLength() < 1) {
            return d;
        }

        HashMap<String, String> entityMaping = new HashMap();
        entityMaping.put("keyAttributeId", "keyAttribute");
        entityMaping.put("tableName", "defaultTableName");
        entityMaping.put("czlist", "refModelName");
        entityMaping.put("isExtend", "isExtendBean");

        HashMap<String, String> perpertisMaping = new HashMap();
        perpertisMaping.put("isHide", "hided");
        perpertisMaping.put("isNullable", "nullable");
        perpertisMaping.put("isReadOnly", "readOnly");
        perpertisMaping.put("accessStrategy", "accessorClassName");
        perpertisMaping.put("maxValue", "attrMaxValue");
        perpertisMaping.put("minValue", "attrMinValue");
        perpertisMaping.put("length", "attrLength");
        perpertisMaping.put("sequence", "attrsequence");
        perpertisMaping.put("dynamic", "dynamicattr");

        for (int i = 0; i < ets.getLength(); i++) {
            Node node = ets.item(i);
            if (!(node instanceof Element)) {
                continue;
            }

            Element entity = (Element) node;

            ClassDTO c = new ClassDTO();

            cs.add(c);
            c.setPerperties(new LinkedList<>());
            attr2VO(c, entity, entityMaping);

            //读取属性列表
            NodeList ats = entity.getElementsByTagName("attribute");
            if (ats.getLength() < 1) {
                continue;
            }

            for (int x = 0; x < ats.getLength(); x++) {
                Node anode = ats.item(x);
                if (!(node instanceof Element)) {
                    continue;
                }

                Element attribute = (Element) anode;
                PropertyDTO p = new PropertyDTO();
                attr2VO(p, attribute, perpertisMaping);

                c.getPerperties().add(p);

                if (p.getAttrsequence() == null) {
                    p.setAttrsequence(1000000);
                }
            }

            c.getPerperties().sort((p1, p2) -> p1.getAttrsequence().compareTo(p2.getAttrsequence()));
        }

        return d;
    }

    private static void attr2VO(Object d, Element e, Map<String, String> nameMaping) {
        NamedNodeMap attrs = e.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Node item = attrs.item(i);
            try {
                String fieldName = item.getNodeName();

                if (nameMaping != null) {
                    if (nameMaping.get(fieldName) != null) {
                        fieldName = nameMaping.get(fieldName);
                    }
                }

                ReflectUtil.setFieldValueAutoConvert(d, fieldName, item.getTextContent());
            } catch (Throwable ex) {
            }
        }
    }
}
