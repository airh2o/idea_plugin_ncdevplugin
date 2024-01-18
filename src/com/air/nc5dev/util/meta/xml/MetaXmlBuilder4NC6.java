package com.air.nc5dev.util.meta.xml;

import com.air.nc5dev.util.CollUtil;
import com.air.nc5dev.util.V;
import com.air.nc5dev.util.XmlUtil;
import com.air.nc5dev.vo.meta.AssociationDTO;
import com.air.nc5dev.vo.meta.BizItfMapDTO;
import com.air.nc5dev.vo.meta.BusiitfconnectionDTO;
import com.air.nc5dev.vo.meta.ClassDTO;
import com.air.nc5dev.vo.meta.ComponentAggVO;
import com.air.nc5dev.vo.meta.ComponentDTO;
import com.air.nc5dev.vo.meta.PropertyDTO;
import com.air.nc5dev.vo.meta.ReferenceDTO;
import com.google.common.collect.Sets;
import lombok.Data;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * NC6系列 </br>
 * </br>
 * </br>
 * </br>
 * </br>
 * </br>
 * </br>
 *
 * @author 唐粟 Email:209308343@qq.com 微信yongyourj
 * @date 2023/10/19 0019 16:53
 * @project
 * @Version
 */
@Data
public class MetaXmlBuilder4NC6 {
    Connection con;
    //数据库里的元数据信息对象
    ComponentAggVO agg;
    Document doc;
    Element component;
    Element celllist;
    Element connectlist;
    Element refdepends;
    Element refdependLoseIDs;
    Element cellRemoveLog;
    Element rulers;

    public MetaXmlBuilder4NC6(Connection con, ComponentAggVO agg) {
        this.con = con;
        this.agg = agg;
        init();
    }

    /**
     * 转换成xml文本
     *
     * @return
     */
    public String toBmfStr() throws SQLException {
        return toBmf().asXML();
    }

    public Document toBmf() throws SQLException {
        doc = DocumentHelper.createDocument();

        component = component();

        celllist = celllist();

        connectlist = connectlist();

        refdepends = refdepends();

        //这个目前应该没毛用
        rulers = rulers();
        //这个目前应该没毛用
        refdependLoseIDs = refdependLoseIDs();
        //这个目前应该没毛用
        cellRemoveLog = cellRemoveLog();

        return doc;
    }

    private Element component() {
        Element component = doc.addElement("component");
        HashMap<String, String> map = new HashMap();
        map.put("resmodule", "resModuleName");
        map.put("isbmf", "isbizmodel");

        ComponentDTO v = agg.getComponentVO();

        XmlUtil.addAttr(doc, component, v, map);

        return component;
    }

    private Element celllist() {
        Element celllist = component.addElement("celllist");

        //<component> <celllist>
        int x = 1;
        int y = 1;
        agg.setClassVOlist(V.get(agg.getClassVOlist(), new ArrayList<>()));
        for (ClassDTO c : agg.getClassVOlist()) {
            c.setX(x += 40);
            c.setY(y += 40);
            if (ClassDTO.CLASSTYPE_ENTITY.equals(c.getClassType())) {
                Element entity = celllist.addElement("entity");
                XmlUtil.addAttr(doc, entity, c, entityNameMaping);

                Element attributelist = entity.addElement("attributelist");
                List<PropertyDTO> propertyDTOs = V.get(agg.getPropertyVOlist().get(c.getId()), new ArrayList<>());
                for (PropertyDTO p : propertyDTOs) {
                    Element attribute = attributelist.addElement("attribute");
                    XmlUtil.addAttr(doc, attribute, p, attributeNameMaping);
                    attribute.addAttribute("dataTypeStyle", p.getDataTypeStyleName());
                    attribute.addAttribute("visibility", p.getVisibilityName());
                }

                Element operationlist = celllist.addElement("operationlist");

                Element busiitfs = celllist.addElement("busiitfs");
                Element busimaps = celllist.addElement("busimaps");
                Set<String> itfs = V.get(agg.getEntityId2ItfEntityIdMap().get(c.getId()), new HashSet<>());
                for (String itf : itfs) {
                    Element itfid = busiitfs.addElement("itfid");
                    itfid.setText(itf);

                    Set<BizItfMapDTO> itfAttrids = V.get(agg.getItfEntityId2PropertyIdsMap().get(itf), new HashSet<>());
                    for (BizItfMapDTO itfAttr : itfAttrids) {
                        Element busimap = busimaps.addElement("busimap");
                        XmlUtil.addAttr(doc, busimap, itfAttr, bizMapNameMaping);
                    }
                }

                Element canzhaolist = celllist.addElement("canzhaolist");

                Element accessor = celllist.addElement("accessor");
                Element properties = accessor.addElement("properties");
                accessor.attributeValue("classFullname", c.getAccessorClassName());
                accessor.attributeValue("displayName", CollUtil.asMap(
                                "nc.md.model.access.javamap.POJOStyle", "POJO"
                                , "nc.md.model.access.javamap.NCBeanStyle", "NCVO"
                                , "nc.md.model.access.javamap.AggVOStyle", "AggVO"
                        )
                        .get(c.getAccessorClassName()));
                accessor.attributeValue("name", CollUtil.asMap(
                                "nc.md.model.access.javamap.POJOStyle", "pojo"
                                , "nc.md.model.access.javamap.NCBeanStyle", "NCVO"
                                , "nc.md.model.access.javamap.AggVOStyle", "AggVO"
                        )
                        .get(c.getAccessorClassName()));
            } else if (ClassDTO.CLASSTYPE_ENUMERATE.equals(c.getClassType())) {
                Element enumerate = celllist.addElement("Enumerate");
                XmlUtil.addAttr(doc, enumerate, c, enumerateNameMaping);

                Element enumitemlist = enumerate.addElement("enumitemlist");
                List<PropertyDTO> propertyDTOs = V.get(agg.getPropertyVOlist().get(c.getId()), new ArrayList<>());
                for (PropertyDTO p : propertyDTOs) {
                    Element enumitem = enumitemlist.addElement("enumitem");
                    XmlUtil.addAttr(doc, enumitem, p, enumeratePropersNameMaping);
                }
            }
        }

        return celllist;
    }

    private Element connectlist() {
        //<component> <connectlist>
        //<component> <connectlist> <busiitfconnection>
        Element connectlist = component.addElement("connectlist");
        for (BusiitfconnectionDTO b : agg.getBusiitfconnectionList()) {
            Element busiitfconnection = connectlist.addElement("busiitfconnection");
            XmlUtil.addAttr(doc, busiitfconnection, b, busiitfconnectionNameMapper);

            Element points = busiitfconnection.addElement("points");
        }

        //<component> <connectlist> <AggregationRelation>
        HashSet<Object> distent = Sets.newHashSet();
        distent.clear();
        for (AssociationDTO b : agg.getAssociationVOList()) {
            String key = b.getTarget() + ":" + b.getSource();
            if (distent.contains(key)) {
                continue;
            }
            distent.add(key);

            Element aggregationRelation = connectlist.addElement("AggregationRelation");
            XmlUtil.addAttr(doc, aggregationRelation, b, aggregationRelationNameMapper);

            Element points = aggregationRelation.addElement("points");
        }

        return connectlist;
    }

    private Element refdepends() {
        //<component> <refdepends>
        Element refdepends = component.addElement("refdepends");
        for (ReferenceDTO r : agg.getReferenceList()) {
            //<component> <celllist> <Reference>
            Element reference = celllist.addElement("Reference");
            XmlUtil.addAttr(doc, reference, r, null);

            Element dependfile = refdepends.addElement("dependfile");
            dependfile.addAttribute("entityid", r.getRefId());
        }

        return refdepends;
    }

    private Element rulers() {
        Element rulers = component.addElement("rulers");
        Element ruler = rulers.addElement("ruler");
        rulers.attributeValue("isHorizontal", "true");
        rulers.attributeValue("unit", "0");

        ruler = rulers.addElement("ruler");
        rulers.attributeValue("isHorizontal", "false");
        rulers.attributeValue("unit", "0");
        return rulers;
    }

    private Element refdependLoseIDs() {
        Element refdependLoseIDs = component.addElement("refdependLoseIDs");
        return refdependLoseIDs;
    }

    private Element cellRemoveLog() {
        Element cellRemoveLog = component.addElement("cellRemoveLog");
        return cellRemoveLog;
    }

    private void init() {
        entityNameMaping.put("keyattribute", "keyAttributeId");
        entityNameMaping.put("defaulttablename", "tableName");
        entityNameMaping.put("refmodelname", "czlist");
        entityNameMaping.put("isextendbean", "isExtend");

        attributeNameMaping.put("hided", "isHide");
        attributeNameMaping.put("nullable", "isNullable");
        attributeNameMaping.put("readonly", "isReadOnly");
        attributeNameMaping.put("accessorclassname", "accessStrategy");
        attributeNameMaping.put("attrmaxvalue", "maxValue");
        attributeNameMaping.put("attrminvalue", "minValue");
        attributeNameMaping.put("attrlength", "length");
        attributeNameMaping.put("attrsequence", "sequence");
        attributeNameMaping.put("dynamicattr", "dynamic");

        bizMapNameMaping.put("bizinterfaceid", "busiitfid");
        bizMapNameMaping.put("intattrid", "busiitfattrid");
        bizMapNameMaping.put("classid", "cellid");
        bizMapNameMaping.put("classattrid", "attrid");
        bizMapNameMaping.put("classattrpath", "attrpath");

        enumerateNameMaping.put("returntype", "dataType");

        enumeratePropersNameMaping.put("value", "enumValue");
        enumeratePropersNameMaping.put("name", "enumDisplay");
        enumeratePropersNameMaping.put("id", "enumID");
        enumeratePropersNameMaping.put("enumsequence", "sequence");

        busiitfconnectionNameMapper.put("startelementid", "realsource");
        busiitfconnectionNameMapper.put("endelementid", "realtarget");
        busiitfconnectionNameMapper.put("startcardinality", "sourceConstraint");
        busiitfconnectionNameMapper.put("endcardinality", "targetConstraint");
        busiitfconnectionNameMapper.put("startbeanid", "realsource");

        aggregationRelationNameMapper.put("startelementid", "realsource");
        aggregationRelationNameMapper.put("endelementid", "realtarget");
        aggregationRelationNameMapper.put("startcardinality", "sourceConstraint");
        aggregationRelationNameMapper.put("endcardinality", "targetConstraint");
        aggregationRelationNameMapper.put("startbeanid", "realsource");

    }

    HashMap<String, String> entityNameMaping = new HashMap();
    HashMap<String, String> attributeNameMaping = new HashMap();
    HashMap<String, String> bizMapNameMaping = new HashMap();
    HashMap<String, String> enumerateNameMaping = new HashMap();
    HashMap<String, String> enumeratePropersNameMaping = new HashMap();
    HashMap<String, String> busiitfconnectionNameMapper = new HashMap();
    HashMap<String, String> aggregationRelationNameMapper = new HashMap();
}
