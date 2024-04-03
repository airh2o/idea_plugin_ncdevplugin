package com.air.nc5dev.util.meta.xml;

import com.air.nc5dev.util.CollUtil;
import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.util.V;
import com.air.nc5dev.util.XmlUtil;
import com.air.nc5dev.vo.meta.AccessorParameterDTO;
import com.air.nc5dev.vo.meta.AssociationDTO;
import com.air.nc5dev.vo.meta.BizItfMapDTO;
import com.air.nc5dev.vo.meta.BusiitfconnectionDTO;
import com.air.nc5dev.vo.meta.ClassDTO;
import com.air.nc5dev.vo.meta.ComponentAggVO;
import com.air.nc5dev.vo.meta.ComponentDTO;
import com.air.nc5dev.vo.meta.EnumValueDTO;
import com.air.nc5dev.vo.meta.PropertyDTO;
import com.air.nc5dev.vo.meta.ReferenceDTO;
import com.google.common.collect.Sets;
import lombok.Data;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * NC6系列 <br>
 * yonbuilder 工具双击打开 bmf处理逻辑： org.eclipse.gef.ui.parts.GraphicalEditor#init -> ncmdp.serialize
 * .XMLSerialize#paserXmlToMDP <br>
 * yonbuilder 元数据 点击实体或者元素后监听： org.eclipse.ui.ISelectionListener ->
 * ncmdp.views.NCMDPViewSheet#selectionChanged -> ncmdp.views.NCMDPViewPage#selectionChanged <br>
 * <br>
 * <br>
 * <br>
 * <br>
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
        v.setMetaType(V.get(v.getMetaType(), ".bmf"));
        v.setConRouterType(V.get(v.getConRouterType(), "手动"));
        v.setCreateIndustry(V.get(v.getCreateIndustry(), "0"));
        v.setGencodestyle(V.get(v.getGencodestyle(), "NC传统样式"));
        v.setIndustryChanged(V.get(v.getIndustryChanged(), "bmf"));
        v.setIndustryIncrease(V.get(v.getIndustryIncrease(), false));
        v.setIndustryName(V.get(v.getIndustryName(), "false"));
        v.setIsSource(V.get(v.getIsSource(), false));
        v.setModifyIndustry(V.get(v.getModifyIndustry(), "0"));
        v.setPreload(V.get(v.getPreload(), false));
        v.setProgramcode(V.get(v.getProgramcode(), "0"));

        if (agg.getClassVOlist() != null && StringUtil.isBlank(v.getMainEntity())) {
            for (ClassDTO c : agg.getClassVOlist()) {
                if (c.isPrimary) {
                    v.setMainEntity(c.getId());
                    break;
                }
            }
        }
        XmlUtil.addAttr(doc, component, v, map);

        return component;
    }

    private Element celllist() {
        Element celllist = component.addElement("celllist");

        //<component> <celllist>
        int x = 1;
        int y = 1;
        agg.setClassVOlist(V.get(agg.getClassVOlist(), new ArrayList<>()));
        agg.setItfEntityId2PropertyIdsMap(V.get(agg.getItfEntityId2PropertyIdsMap(), new HashMap<>()));

        //Key =当前元数据实体id  , value = 被引用接口的属性id等信息
        Map<String, Set<BizItfMapDTO>> entityId2ItfPropertyIdsMap = new HashMap<>();
        for (Collection<BizItfMapDTO> bizItfMapDTOS : agg.getItfEntityId2PropertyIdsMap().values()) {
            for (BizItfMapDTO bizItfMapDTO : bizItfMapDTOS) {
                entityId2ItfPropertyIdsMap.putIfAbsent(bizItfMapDTO.getClassID(), new HashSet<>());
                entityId2ItfPropertyIdsMap.get(bizItfMapDTO.getClassID()).add(bizItfMapDTO);
            }
        }

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

                Element operationlist = entity.addElement("operationlist");

                //每个实体， 会有实现的接口或者参照配置， 这里做处理
                Element busiitfs = entity.addElement("busiitfs");
                Set<String> itfs = V.get(agg.getEntityId2ItfEntityIdMap().get(c.getId()), new HashSet<>());
                for (String itf : itfs) {
                    Element itfid = busiitfs.addElement("itfid");
                    itfid.setText(itf);
                }
                Set<BizItfMapDTO> bizItfMapDTOS = entityId2ItfPropertyIdsMap.get(c.getId());
                if (CollUtil.isNotEmpty(bizItfMapDTOS)) {
                    Element busimaps = entity.addElement("busimaps");
                    for (BizItfMapDTO itfAttr : bizItfMapDTOS) {
                        Element busimap = busimaps.addElement("busimap");
                        XmlUtil.addAttr(doc, busimap, itfAttr, bizMapNameMaping);
                    }
                }

                Element canzhaolist = entity.addElement("canzhaolist");

                Element accessor = entity.addElement("accessor");
                Element properties = accessor.addElement("properties");
                accessor.addAttribute("classFullname", c.getAccessorClassName());
                accessor.addAttribute("displayName", CollUtil.asMap(
                                        "nc.md.model.access.javamap.POJOStyle", "POJO"
                                        , "nc.md.model.access.javamap.NCBeanStyle", "NCVO"
                                        , "nc.md.model.access.javamap.AggVOStyle", "AggVO"
                                )
                                .get(c.getAccessorClassName())
                );
                accessor.addAttribute("name", CollUtil.asMap(
                                "nc.md.model.access.javamap.POJOStyle", "pojo"
                                , "nc.md.model.access.javamap.NCBeanStyle", "NCVO"
                                , "nc.md.model.access.javamap.AggVOStyle", "AggVO"
                        )
                        .get(c.getAccessorClassName()));

                List<AccessorParameterDTO> accessorParameterDTOS = agg.getAccessorParaVOList().get(c.getId());
                if (CollUtil.isNotEmpty(accessorParameterDTOS)) {
                    for (AccessorParameterDTO accessorParameterDTO : accessorParameterDTOS) {
                        Element property = properties.addElement("property");
                        //包装类名
                        property.addAttribute("classid", accessorParameterDTO.getId());
                        property.addAttribute("displayName", "包装类名");
                        property.addAttribute("name", "wrapclsname");
                        property.addAttribute("sequence", accessorParameterDTO.getAssosequence() == null ? "0" :
                                accessorParameterDTO.getAssosequence() + "");
                        property.addAttribute("value", accessorParameterDTO.getParaValue());
                    }
                }
            } else if (ClassDTO.CLASSTYPE_ENUMERATE.equals(c.getClassType())) {
                Element enumerate = celllist.addElement("Enumerate");
                XmlUtil.addAttr(doc, enumerate, c, enumerateNameMaping);

                Element enumitemlist = enumerate.addElement("enumitemlist");
                List<EnumValueDTO> enumValueDTOS = V.get(agg.getEnumValueVOList().get(c.getId()), new ArrayList<>());
                for (EnumValueDTO enumValueDTO : enumValueDTOS) {
                    Element enumitem = enumitemlist.addElement("enumitem");
                    if (StringUtil.isBlank(enumValueDTO.getItemId())) {
                        enumValueDTO.setItemId(StringUtil.uuid().toLowerCase());
                    }
                    XmlUtil.addAttr(doc, enumitem, enumValueDTO, enumeratePropersNameMaping);
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
        enumeratePropersNameMaping.put("itemid", "id");
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
