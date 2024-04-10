package com.air.nc5dev.util.meta.xml;

import com.air.nc5dev.util.CollUtil;
import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.util.XmlUtil;
import com.air.nc5dev.util.idea.LogUtil;
import com.air.nc5dev.util.meta.IMetaBMFReader;
import com.air.nc5dev.vo.meta.AccessorParameterDTO;
import com.air.nc5dev.vo.meta.BizItfMapDTO;
import com.air.nc5dev.vo.meta.ClassDTO;
import com.air.nc5dev.vo.meta.ComponentAggVO;
import com.air.nc5dev.vo.meta.ComponentDTO;
import com.air.nc5dev.vo.meta.EnumValueDTO;
import com.air.nc5dev.vo.meta.PropertyDTO;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 从BMF文件加载元数据组件信息 <br>
 * <br>
 * <br>
 *
 * @author 唐粟 Email:209308343@qq.com 微信yongyourj
 * @date 2023/10/21 0021 13:18
 * @project
 * @Version
 */
public class MetaBFMReadFromBmfFileUtil implements IMetaBMFReader<String> {
    /**
     * bmf元数据文件读取成为 元数据组件aggvo
     *
     * @param source  bmf的xml内容
     * @param project
     * @return
     * @throws SQLException
     */
    @Override
    public ComponentAggVO readAggVO(String source, Project project) throws Exception {
        if (source.startsWith("\uFEFF")) {
            source = StringUtil.removeStart(source, "\uFEFF");
        }
        Document doc = null;
        Element root = null;
        try {
            doc = XmlUtil.readXML(source);
            root = XmlUtil.getRootElement(doc);
        } catch (Throwable e) {
            //出错了
            LogUtil.error("dom4j读取失败，换sax试试。", e);

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            try {
                DocumentBuilder db = dbf.newDocumentBuilder();
                doc = db.parse(new ByteArrayInputStream(source.getBytes()));
                NodeList component = doc.getElementsByTagName("component");
                if (component.getLength() < 1) {
                    return null;
                }
                root = (Element) component.item(0);
            } catch (Throwable ex) {
                LogUtil.error("sax都读取失败，摆烂 不要这个了!", ex);
            }
        }

        ComponentDTO componentVO = readEntityList(root);

        String componentid = componentVO.getId();
        ComponentAggVO agg = ComponentAggVO.builder()
                .componentVO(componentVO)
                .classVOlist(componentVO.getClassDTOS())
                .enumValueVOList(new HashMap<>())
                .propertyVOlist(new HashMap<>())
                .bizItfMapVOlist(new HashMap<>())
                .bizItfImplMapVoList(new HashMap<>())
                .accessorParaVOList(new HashMap<>())
                .build();

        //计算出 需要逻辑计算出来的标签们
        agg.buildAll(doc, project);

        return agg;
    }

    /**
     * 只读取 组件基本信息，实体本身基本信息（不读取字段列表这些）
     *
     * @param bmfXml
     * @return
     */
    public static ComponentDTO readComponentBaseInfo(String bmfXml) {
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
        Map<String, String> componentNameMaping = CollUtil.reverse(MetaAggVOConvertToXmlUtil.NAMEMAPING_COMPONENT);
        Map<String, String> entityNameMaping = CollUtil.reverse(MetaAggVOConvertToXmlUtil.NAMEMAPING_ENTITY);
        Map<String, String> attributeNameMaping =
                CollUtil.reverse(MetaAggVOConvertToXmlUtil.NAMEMAPING_ENTITY_ATTRIBUTE);
        Map<String, String> enumerateNameMaping = CollUtil.reverse(MetaAggVOConvertToXmlUtil.NAMEMAPING_ENUMERATE);
        Map<String, String> enumeratePropertNameMaping =
                CollUtil.reverse(MetaAggVOConvertToXmlUtil.NAMEMAPING_ENUMERATE_PROPERT);
        Map<String, String> bizmapNameMaping = CollUtil.reverse(MetaAggVOConvertToXmlUtil.NAMEMAPING_BIZMAP);
        Map<String, String> accessorPropertiesPropertyMaping =
                CollUtil.reverse(MetaAggVOConvertToXmlUtil.NAMEMAPPER_ACCESSOR_PROPERTIES_PROPERTY);

        ComponentDTO d = new ComponentDTO();
        d.setClassDTOS(new LinkedList<>());
        ComponentAggVO.attr2VO(d, root, componentNameMaping);
        Element celllist = XmlUtil.getElement(root, "celllist");
        List<Element> entitys = XmlUtil.getElements(celllist, "entity");
        for (Element entity : entitys) {
            ClassDTO c = new ClassDTO();
            c.setClassType(ClassDTO.CLASSTYPE_ENTITY);
            c.setPerperties(new LinkedList<>());
            c.setEnumValues(new LinkedList<>());
            c.setBizItfMaps(new LinkedList<>());
            c.setAccessorParameters(new LinkedList<>());

            d.getClassDTOS().add(c);
            ComponentAggVO.attr2VO(c, entity, entityNameMaping);
        }

        List<Element> enumerates = XmlUtil.getElements(celllist, "Enumerate");
        for (Element enumerate : enumerates) {
            ClassDTO c = new ClassDTO();
            c.setClassType(ClassDTO.CLASSTYPE_ENUMERATE);
            c.setPerperties(new LinkedList<>());
            c.setEnumValues(new LinkedList<>());
            c.setBizItfMaps(new LinkedList<>());
            c.setAccessorParameters(new LinkedList<>());

            d.getClassDTOS().add(c);
            ComponentAggVO.attr2VO(c, enumerate, enumerateNameMaping);
        }

        return d;
    }

    /**
     * 读取组件信息和实体等
     *
     * @param bmfXml
     * @return
     */
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

        return readEntityList(root);
    }

    /**
     * 读取组件信息和实体和实体字段列表
     *
     * @param root
     * @return
     */
    @NotNull
    private static ComponentDTO readEntityList(Element root) {
        Map<String, String> componentNameMaping = CollUtil.reverse(MetaAggVOConvertToXmlUtil.NAMEMAPING_COMPONENT);
        Map<String, String> entityNameMaping = CollUtil.reverse(MetaAggVOConvertToXmlUtil.NAMEMAPING_ENTITY);
        Map<String, String> attributeNameMaping =
                CollUtil.reverse(MetaAggVOConvertToXmlUtil.NAMEMAPING_ENTITY_ATTRIBUTE);
        Map<String, String> enumerateNameMaping = CollUtil.reverse(MetaAggVOConvertToXmlUtil.NAMEMAPING_ENUMERATE);
        Map<String, String> enumeratePropertNameMaping =
                CollUtil.reverse(MetaAggVOConvertToXmlUtil.NAMEMAPING_ENUMERATE_PROPERT);
        Map<String, String> bizmapNameMaping = CollUtil.reverse(MetaAggVOConvertToXmlUtil.NAMEMAPING_BIZMAP);
        Map<String, String> accessorPropertiesPropertyMaping =
                CollUtil.reverse(MetaAggVOConvertToXmlUtil.NAMEMAPPER_ACCESSOR_PROPERTIES_PROPERTY);

        ArrayList<ClassDTO> cs = new ArrayList<>();
        ComponentDTO d = new ComponentDTO();
        d.setClassDTOS(cs);

        ComponentAggVO.attr2VO(d, root, componentNameMaping);
        Element celllist = XmlUtil.getElement(root, "celllist");
        if (celllist == null) {
            return d;
        }

        List<Element> entitys = XmlUtil.getElements(celllist, "entity");
        //读取实体列表
        for (Element entity : entitys) {
            ClassDTO c = new ClassDTO();
            c.setClassType(ClassDTO.CLASSTYPE_ENTITY);
            c.setPerperties(new LinkedList<>());
            c.setEnumValues(new LinkedList<>());
            c.setBizItfMaps(new LinkedList<>());
            c.setAccessorParameters(new LinkedList<>());

            cs.add(c);
            ComponentAggVO.attr2VO(c, entity, entityNameMaping);
            //attributelist
            Element attributelist = XmlUtil.getElement(entity, "attributelist");
            if (attributelist != null) {
                List<Element> attributes = XmlUtil.getElements(attributelist, "attribute");
                for (Element attribute : attributes) {
                    PropertyDTO p = new PropertyDTO();
                    ComponentAggVO.attr2VO(p, attribute, attributeNameMaping);
                    c.getPerperties().add(p);

                    if (p.getAttrsequence() == null) {
                        p.setAttrsequence(1000000);
                    }
                }
                c.getPerperties().sort((p1, p2) -> p1.getAttrsequence().compareTo(p2.getAttrsequence()));
            }

            //busimap
            Element busimapsEle = XmlUtil.getElement(entity, "busimaps");
            if (busimapsEle != null) {
                List<Element> busimaps = XmlUtil.getElements(busimapsEle, "busimap");
                for (Element busimap : busimaps) {
                    BizItfMapDTO p = new BizItfMapDTO();
                    ComponentAggVO.attr2VO(p, busimap, bizmapNameMaping);
                    c.getBizItfMaps().add(p);
                }
            }

            //accessor > properties > property
            Element accessor = XmlUtil.getElement(entity, "accessor");
            if (accessor != null) {
                Element properties = XmlUtil.getElement(accessor, "properties");
                if (properties != null) {
                    List<Element> propertys = XmlUtil.getElements(properties, "property");
                    for (Element property : propertys) {
                        AccessorParameterDTO p = new AccessorParameterDTO();
                        ComponentAggVO.attr2VO(p, property, accessorPropertiesPropertyMaping);
                        c.getAccessorParameters().add(p);
                    }
                }
            }
        }
        //读取枚举类型实体的列表
        List<Element> enumerates = XmlUtil.getElements(celllist, "Enumerate");
        for (Element enumerate : enumerates) {
            ClassDTO c = new ClassDTO();
            c.setClassType(ClassDTO.CLASSTYPE_ENUMERATE);
            c.setPerperties(new LinkedList<>());
            c.setEnumValues(new LinkedList<>());
            c.setBizItfMaps(new LinkedList<>());
            c.setAccessorParameters(new LinkedList<>());

            cs.add(c);
            ComponentAggVO.attr2VO(c, enumerate, enumerateNameMaping);

            //enumitem
            Element enumitemlist = XmlUtil.getElement(enumerate, "enumitemlist");
            if (enumitemlist != null) {
                List<Element> enumitems = XmlUtil.getElements(enumitemlist, "enumitem");
                for (Element enumitem : enumitems) {
                    EnumValueDTO p = new EnumValueDTO();
                    c.getEnumValues().add(p);
                    ComponentAggVO.attr2VO(p, enumitem, enumeratePropertNameMaping);
                }
            }

            //busimap
            Element busimapsEle = XmlUtil.getElement(enumerate, "busimaps");
            if (busimapsEle != null) {
                List<Element> busimaps = XmlUtil.getElements(busimapsEle, "busimap");
                for (Element busimap : busimaps) {
                    BizItfMapDTO p = new BizItfMapDTO();
                    ComponentAggVO.attr2VO(p, busimap, bizmapNameMaping);
                    c.getBizItfMaps().add(p);
                }
            }

            //accessor > properties > property
            Element accessor = XmlUtil.getElement(enumerate, "accessor");
            if (accessor != null) {
                Element properties = XmlUtil.getElement(accessor, "properties");
                if (properties != null) {
                    List<Element> propertys = XmlUtil.getElements(properties, "property");
                    for (Element property : propertys) {
                        AccessorParameterDTO p = new AccessorParameterDTO();
                        ComponentAggVO.attr2VO(p, property, accessorPropertiesPropertyMaping);
                        c.getAccessorParameters().add(p);
                    }
                }
            }
        }

        return d;
    }


}
