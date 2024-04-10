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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * bmf元数据组件aggvo转换成xml文本 ,NC6+系列                       <br>
 * yonbuilder 工具双击打开 bmf处理逻辑： org.eclipse.gef.ui.parts.GraphicalEditor#init              <br>
 * -> ncmdp.serialize.XMLSerialize#paserXmlToMDP                 <br>
 * yonbuilder 元数据 点击实体或者元素后监听： org.eclipse.ui.ISelectionListener                 <br>
 * -> ncmdp.views.NCMDPViewSheet#selectionChanged           <br>
 * -> ncmdp.views.NCMDPViewPage#selectionChanged               <br>
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
public class MetaAggVOConvertToXmlUtil {
    ///////数据库字段名和bmf文件的标签属性的 对照表， key=数据库， value=xml里
    public static Map<String, String> NAMEMAPING_ENTITY = new ConcurrentHashMap();
    public static Map<String, String> NAMEMAPING_ENTITY_ATTRIBUTE = new ConcurrentHashMap();
    public static Map<String, String> NAMEMAPING_BIZMAP = new ConcurrentHashMap();
    public static Map<String, String> NAMEMAPING_ENUMERATE = new ConcurrentHashMap();
    public static Map<String, String> NAMEMAPING_ENUMERATE_PROPERT = new ConcurrentHashMap();
    public static Map<String, String> NAMEMAPPER_BUSIITFCONNECTION = new ConcurrentHashMap();
    public static Map<String, String> NAMEMAPPER_AGGREGATIONRELATION = new ConcurrentHashMap();
    public static Map<String, String> NAMEMAPPER_ACCESSOR_PROPERTIES_PROPERTY = new ConcurrentHashMap();
    ///////////////////


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

    public MetaAggVOConvertToXmlUtil(ComponentAggVO agg) {
        this.agg = agg;
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

    protected Element component() {
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

    protected Element celllist() {
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
                XmlUtil.addAttr(doc, entity, c, NAMEMAPING_ENTITY);

                Element attributelist = entity.addElement("attributelist");
                List<PropertyDTO> propertyDTOs = V.get(agg.getPropertyVOlist().get(c.getId()), new ArrayList<>());
                for (PropertyDTO p : propertyDTOs) {
                    Element attribute = attributelist.addElement("attribute");
                    XmlUtil.addAttr(doc, attribute, p, NAMEMAPING_ENTITY_ATTRIBUTE);
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
                        XmlUtil.addAttr(doc, busimap, itfAttr, NAMEMAPING_BIZMAP);
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
                XmlUtil.addAttr(doc, enumerate, c, NAMEMAPING_ENUMERATE);

                Element enumitemlist = enumerate.addElement("enumitemlist");
                List<EnumValueDTO> enumValueDTOS = V.get(agg.getEnumValueVOList().get(c.getId()), new ArrayList<>());
                for (EnumValueDTO enumValueDTO : enumValueDTOS) {
                    Element enumitem = enumitemlist.addElement("enumitem");
                    if (StringUtil.isBlank(enumValueDTO.getItemId())) {
                        enumValueDTO.setItemId(StringUtil.uuid().toLowerCase());
                    }
                    XmlUtil.addAttr(doc, enumitem, enumValueDTO, NAMEMAPING_ENUMERATE_PROPERT);
                }
            }
        }

        return celllist;
    }

    protected Element connectlist() {
        //<component> <connectlist>
        //<component> <connectlist> <busiitfconnection>
        Element connectlist = component.addElement("connectlist");
        for (BusiitfconnectionDTO b : agg.getBusiitfconnectionList()) {
            Element busiitfconnection = connectlist.addElement("busiitfconnection");
            XmlUtil.addAttr(doc, busiitfconnection, b, NAMEMAPPER_BUSIITFCONNECTION);

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
            XmlUtil.addAttr(doc, aggregationRelation, b, NAMEMAPPER_AGGREGATIONRELATION);

            Element points = aggregationRelation.addElement("points");
        }

        return connectlist;
    }

    protected Element refdepends() {
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

    protected Element rulers() {
        Element rulers = component.addElement("rulers");
        Element ruler = rulers.addElement("ruler");
        rulers.attributeValue("isHorizontal", "true");
        rulers.attributeValue("unit", "0");

        ruler = rulers.addElement("ruler");
        rulers.attributeValue("isHorizontal", "false");
        rulers.attributeValue("unit", "0");
        return rulers;
    }

    protected Element refdependLoseIDs() {
        Element refdependLoseIDs = component.addElement("refdependLoseIDs");
        return refdependLoseIDs;
    }

    protected Element cellRemoveLog() {
        Element cellRemoveLog = component.addElement("cellRemoveLog");
        return cellRemoveLog;
    }

    static {
        NAMEMAPING_ENTITY.put("keyattribute", "keyAttributeId");
        NAMEMAPING_ENTITY.put("defaulttablename", "tableName");
        NAMEMAPING_ENTITY.put("refmodelname", "czlist");
        NAMEMAPING_ENTITY.put("isextendbean", "isExtend");

        NAMEMAPING_ENTITY_ATTRIBUTE.put("hided", "isHide");
        NAMEMAPING_ENTITY_ATTRIBUTE.put("nullable", "isNullable");
        NAMEMAPING_ENTITY_ATTRIBUTE.put("readonly", "isReadOnly");
        NAMEMAPING_ENTITY_ATTRIBUTE.put("accessorclassname", "accessStrategy");
        NAMEMAPING_ENTITY_ATTRIBUTE.put("attrmaxvalue", "maxValue");
        NAMEMAPING_ENTITY_ATTRIBUTE.put("attrminvalue", "minValue");
        NAMEMAPING_ENTITY_ATTRIBUTE.put("attrlength", "length");
        NAMEMAPING_ENTITY_ATTRIBUTE.put("attrsequence", "sequence");
        NAMEMAPING_ENTITY_ATTRIBUTE.put("dynamicattr", "dynamic");

        NAMEMAPING_BIZMAP.put("bizinterfaceid", "busiitfid");
        NAMEMAPING_BIZMAP.put("intattrid", "busiitfattrid");
        NAMEMAPING_BIZMAP.put("classid", "cellid");
        NAMEMAPING_BIZMAP.put("classattrid", "attrid");
        NAMEMAPING_BIZMAP.put("classattrpath", "attrpath");

        NAMEMAPING_ENUMERATE.put("returntype", "dataType");

        NAMEMAPING_ENUMERATE_PROPERT.put("value", "enumValue");
        NAMEMAPING_ENUMERATE_PROPERT.put("name", "enumDisplay");
        NAMEMAPING_ENUMERATE_PROPERT.put("id", "enumID");
        NAMEMAPING_ENUMERATE_PROPERT.put("itemid", "id");
        NAMEMAPING_ENUMERATE_PROPERT.put("enumsequence", "sequence");

        NAMEMAPPER_BUSIITFCONNECTION.put("startelementid", "realsource");
        NAMEMAPPER_BUSIITFCONNECTION.put("endelementid", "realtarget");
        NAMEMAPPER_BUSIITFCONNECTION.put("startcardinality", "sourceConstraint");
        NAMEMAPPER_BUSIITFCONNECTION.put("endcardinality", "targetConstraint");
        NAMEMAPPER_BUSIITFCONNECTION.put("startbeanid", "realsource");

        NAMEMAPPER_AGGREGATIONRELATION.put("startelementid", "realsource");
        NAMEMAPPER_AGGREGATIONRELATION.put("endelementid", "realtarget");
        NAMEMAPPER_AGGREGATIONRELATION.put("startcardinality", "sourceConstraint");
        NAMEMAPPER_AGGREGATIONRELATION.put("endcardinality", "targetConstraint");
        NAMEMAPPER_AGGREGATIONRELATION.put("startbeanid", "realsource");

        NAMEMAPPER_ACCESSOR_PROPERTIES_PROPERTY.put("id", "classid");
        NAMEMAPPER_ACCESSOR_PROPERTIES_PROPERTY.put("name", "wrapclsname");
        NAMEMAPPER_ACCESSOR_PROPERTIES_PROPERTY.put("assosequence", "sequence");
        NAMEMAPPER_ACCESSOR_PROPERTIES_PROPERTY.put("paraValue", "value");
    }
}
