package com.air.nc5dev.vo.meta;

import com.air.nc5dev.ui.exportbmf.ExportbmfDialog;
import com.air.nc5dev.util.CollUtil;
import com.air.nc5dev.util.EmptyProgressIndicatorImpl;
import com.air.nc5dev.util.ExceptionUtil;
import com.air.nc5dev.util.ReflectUtil;
import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.util.V;
import com.air.nc5dev.util.XmlUtil;
import com.air.nc5dev.util.idea.LogUtil;
import com.air.nc5dev.util.meta.database.QueryAccessorParameterVOListUtil;
import com.air.nc5dev.util.meta.database.QueryAssociationVOListUtil;
import com.air.nc5dev.util.meta.database.QueryBizItfVOMapVOListUtil;
import com.air.nc5dev.util.meta.database.QueryClassVOListUtil;
import com.air.nc5dev.util.meta.database.QueryEnumValueVOListUtil;
import com.air.nc5dev.util.meta.database.QueryPropertyVOListUtil;
import com.air.nc5dev.util.meta.xml.MetaAggVOConvertToXmlUtil;
import com.google.common.collect.Sets;
import com.intellij.openapi.project.Project;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * 组件aggvo <br>
 * <br>
 * <br>
 *
 * @author 唐粟 Email:209308343@qq.com 微信yongyourj
 * @date 2023/10/19 0019 15:43
 * @project
 * @Version
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ComponentAggVO implements Serializable, Cloneable {
    ComponentDTO componentVO;
    List<? extends ClassDTO> classVOlist;
    Map<String, List<EnumValueDTO>> enumValueVOList;
    Map<String, List<PropertyDTO>> propertyVOlist;
    Map<String, List<BizItfMapDTO>> bizItfMapVOlist;
    Map<String, List<BizItfMapDTO>> bizItfImplMapVoList;
    /**
     * key=实体id
     */
    Map<String, List<AccessorParameterDTO>> accessorParaVOList;
    List<AssociationDTO> associationVOList;
    List<TableDTO> tableVOList;
    List<ColumnDTO> columnVOList;
    List<ORMapDTO> orMapVOList;
    List<DbRelationDTO> dbRelationList;

    /////////////////////////// 计算出来的 标签列表 START /////////////////////////////
    List<ReferenceDTO> referenceList;   // celllist > Reference
    //Key = 实体id， value=被实现的的引用的接口或实体id
    Map<String, Set<String>> entityId2ItfEntityIdMap;
    //Key =被实现的的引用的接口或实体id  , value = 被引用接口的属性id等信息
    Map<String, Set<BizItfMapDTO>> itfEntityId2PropertyIdsMap;  // celllist > entity > busimaps > busimap
    List<BusiitfconnectionDTO> busiitfconnectionList; //connectlist > busiitfconnection
    /////////////////////////// 计算出来的 标签列表 END  /////////////////////////////

    /**
     * 部分标签 丫儿的 是算出来的
     */
    public void buildAll(Connection con, Project project) throws SQLException {
        QueryAssociationVOListUtil queryAssociationVOListUtil = new QueryAssociationVOListUtil(con);
        setAssociationVOList(queryAssociationVOListUtil.queryVOs(getComponentVO().getId()));

        if (CollUtil.isNotEmpty(getClassVOlist())) {
            QueryEnumValueVOListUtil queryEnumValueVOListUtil = new QueryEnumValueVOListUtil(con);
            QueryPropertyVOListUtil queryPropertyVOListUtil = new QueryPropertyVOListUtil(con);
            QueryBizItfVOMapVOListUtil queryBizItfVOMapVOListUtil = new QueryBizItfVOMapVOListUtil(con);
            QueryAccessorParameterVOListUtil queryAccessorParameterVOListUtil =
                    new QueryAccessorParameterVOListUtil(con);

            for (ClassDTO classVO : getClassVOlist()) {
                getEnumValueVOList().put(classVO.getId(), queryEnumValueVOListUtil.queryVOs(classVO.getId()));
                getPropertyVOlist().put(classVO.getId(), queryPropertyVOListUtil.queryVOs(classVO.getId()));
                getBizItfMapVOlist().put(classVO.getId(), queryBizItfVOMapVOListUtil.queryVOs(classVO.getId()));
                getBizItfImplMapVoList().put(classVO.getId(),
                        BizItfMapDTO.toImpl(getBizItfMapVOlist().get(classVO.getId())));
                getAccessorParaVOList().put(classVO.getId(),
                        queryAccessorParameterVOListUtil.queryVOs(classVO.getId()));
            }
        }

        buildAll0(con, project, (classId, docOrConn) -> {
            Connection connection = (Connection) docOrConn;
            try {
                return new QueryClassVOListUtil(connection).getCompomentIDByClassId(classId);
            } catch (SQLException e) {
                LogUtil.error(e.toString(), e);
                ExceptionUtil.wrapRuntime(e);
            }
            return null;
        });
    }

    /**
     * 部分标签 丫儿的 是算出来的
     */
    public void buildAll(Document doc, Project project) throws SQLException {
        Element root = XmlUtil.getRootElement(doc);
        if (root == null || !root.getNodeName().equals("component")) {
            root = (Element) doc.getElementsByTagName("component").item(0);
        }

        //connectlist -> busiitfconnection 和 AggregationRelation
        setAssociationVOList(new ArrayList<>());
        setBusiitfconnectionList(new ArrayList<>());
        Element connectlistEle;
        List<Element> busiitfconnections;
        List<Element> aggregationRelations;
        AccessorParameterDTO accessorParameterDTO;
        AssociationDTO associationDTO;
        BusiitfconnectionDTO busiitfconnectionDTO;
        List<Element> connectlists = XmlUtil.getElements(root, "connectlist");
        Map<String, String> aggregationRelationNameMapper =
                CollUtil.reverse(MetaAggVOConvertToXmlUtil.NAMEMAPPER_AGGREGATIONRELATION);
        Map<String, String> busiitfconnectionNameMapper =
                CollUtil.reverse(MetaAggVOConvertToXmlUtil.NAMEMAPPER_BUSIITFCONNECTION);
        for (Element connectlist : connectlists) {
            busiitfconnections = XmlUtil.getElements(connectlist, "busiitfconnection");
            for (Element busiitfconnection : busiitfconnections) {
                busiitfconnectionDTO = new BusiitfconnectionDTO();
                ComponentAggVO.attr2VO(busiitfconnectionDTO, busiitfconnection, busiitfconnectionNameMapper);
                getBusiitfconnectionList().add(busiitfconnectionDTO);
            }

            aggregationRelations = XmlUtil.getElements(connectlist, "AggregationRelation");
            for (Element aggregationRelation : aggregationRelations) {
                associationDTO = new AssociationDTO();
                ComponentAggVO.attr2VO(associationDTO, aggregationRelation, aggregationRelationNameMapper);
                getAssociationVOList().add(associationDTO);
            }
        }

        buildAll0(doc, project, (classId, docOrConn) -> {
            ExportbmfDialog.searchHomeBmfFileIfNeed(new EmptyProgressIndicatorImpl(), project);
            ExportbmfDialog.searchProjectBmfFileIfNeed(new EmptyProgressIndicatorImpl(), project);

            Map<String, SearchComponentVO> entityId2SearchComponentVOMap =
                    ExportbmfDialog.CACHE_ENTITYID2COMVOMAP.get(project);
            if (entityId2SearchComponentVOMap != null) {
                SearchComponentVO c = entityId2SearchComponentVOMap.get(classId);
                if (c != null) {
                    return c.getId();
                }
            }

            return null;
        });
    }

    protected void buildAll0(Object docOrConn, Project project
            , BiFunction<String, Object, String> getCompomentIDByClassId) throws SQLException {
        BusiitfconnectionDTO busiitfconnectionDTO;
        if (CollUtil.isNotEmpty(getClassVOlist())) {//实体列表循环
            for (ClassDTO classVO : getClassVOlist()) {
                getEnumValueVOList().put(classVO.getId(), classVO.getEnumValues());
                getPropertyVOlist().put(classVO.getId(), classVO.getPerperties());
                getBizItfMapVOlist().put(classVO.getId(), classVO.getBizItfMaps());
                getBizItfImplMapVoList().put(classVO.getId(),
                        BizItfMapDTO.toImpl(getBizItfMapVOlist().get(classVO.getId())));
                getAccessorParaVOList().put(classVO.getId(), classVO.getAccessorParameters());
            }
        }

        referenceList = new ArrayList<>();
        entityId2ItfEntityIdMap = new HashMap<>();
        itfEntityId2PropertyIdsMap = new HashMap<>();
        busiitfconnectionList = new ArrayList<>();

        if (bizItfImplMapVoList == null) {
            bizItfImplMapVoList = new HashMap<>();
        }
        if (bizItfMapVOlist == null) {
            bizItfMapVOlist = new HashMap<>();
        }
        if (classVOlist == null) {
            classVOlist = new ArrayList<>();
        }

        HashSet<String> bmfSelfClassIds = Sets.newHashSet();
        HashSet<String> entityIds = Sets.newHashSet();
        HashSet<String> refids_bmf = Sets.newHashSet();
        HashSet<String> refids_enum = Sets.newHashSet();

        for (ClassDTO c : classVOlist) {
            bmfSelfClassIds.add(c.getId());
            if (ClassDTO.CLASSTYPE_ENTITY.equals(c.getClassType())) {
                entityIds.add(c.getId());
            } else if (ClassDTO.CLASSTYPE_ENUMERATE.equals(c.getClassType())) {
                refids_enum.add(c.getId());
            } else {
                refids_bmf.add(c.getId());
            }
        }

        List<BizItfMapDTO> allBizItfMapDTOs = new LinkedList<>();
        for (List<BizItfMapDTO> vs : bizItfMapVOlist.values()) {
            if (vs == null) {
                continue;
            }
            for (BizItfMapDTO v : vs) {
                //busiitfid 是引用的接口实体id
                allBizItfMapDTOs.add(v);
            }
        }

        HashSet<Object> distent = Sets.newHashSet();
        distent.clear();
        for (BizItfMapDTO bizItfMapDTO : allBizItfMapDTOs) {
            if (bmfSelfClassIds.contains(bizItfMapDTO.getBizInterfaceID())) {
                //不是引用的对象
                continue;
            }

            //是否处理过
            String key = bizItfMapDTO.getBizInterfaceID() + ":" + bizItfMapDTO.getClassID()
                    + '_' + bizItfMapDTO.getClassAttrPath() + ":" + bizItfMapDTO.getIntAttrID();
            if (distent.contains(key)) {
                continue;
            }

            if (StringUtil.isNotBlank(bizItfMapDTO.getClassAttrPath())) {
                bizItfMapDTO.setAttrpathid(StringUtil.uuid().toLowerCase());
            }

            distent.add(key);
            entityId2ItfEntityIdMap.putIfAbsent(bizItfMapDTO.getClassID(), new HashSet<>());
            Set<String> set = entityId2ItfEntityIdMap.get(bizItfMapDTO.getClassID());
            set.add(bizItfMapDTO.getBizInterfaceID());

            itfEntityId2PropertyIdsMap.putIfAbsent(bizItfMapDTO.getBizInterfaceID(), new HashSet<>());
            Set<BizItfMapDTO> set2 = itfEntityId2PropertyIdsMap.get(bizItfMapDTO.getBizInterfaceID());
            set2.add(bizItfMapDTO);

            //不是bmf文件本身的classid，说明是 引用的实体或接口对象！
            //计算 component > celllist > Reference
            ReferenceDTO referenceDTO = new ReferenceDTO();
            referenceDTO.setId(StringUtil.uuid());
            referenceDTO.setComponentID(componentVO.getId());
            referenceDTO.setRefId(bizItfMapDTO.getBizInterfaceID());
            SearchComponentVO c = V.get(ExportbmfDialog.CACHE_COMPONENTID2COMVOMAP.get(project), new HashMap<String,
                            SearchComponentVO>())
                    .get(getCompomentIDByClassId.apply(referenceDTO.getRefId(), docOrConn));
            if (c != null) {
                referenceDTO.setModuleName(c.getOwnModule());
                String name = c.getFilePath().substring(c.getFilePath().lastIndexOf('\\'));
                String pp = c.getFilePath().substring(0, c.getFilePath().lastIndexOf(name));
                if (pp.lastIndexOf('\\') == pp.length() - 1) {
                    pp = pp.substring(0, pp.length() - 1);
                }
                pp = pp.substring(pp.lastIndexOf('\\'));
                referenceDTO.setMdFilePath(pp + name);
                referenceDTO.setDisplayName("引用：" + c.getDisplayName());
            }
            referenceDTO.setName("reference" + referenceList.size());
            referenceDTO.setFullClassName("nc.vo." + componentVO.getNamespace() + "." + componentVO.getName() +
                    ".reference");
            referenceList.add(referenceDTO);

            //计算  connectlist > busiitfconnection
            //是否处理过
            key = bizItfMapDTO.getBizInterfaceID() + ":" + bizItfMapDTO.getClassID();
            if (!distent.contains(key)) {
                distent.add(key);
                busiitfconnectionDTO = new BusiitfconnectionDTO();
                busiitfconnectionDTO.setId(StringUtil.uuid());
                busiitfconnectionDTO.setComponentID(referenceDTO.getComponentID());
                busiitfconnectionDTO.setTarget(referenceDTO.getId());
                busiitfconnectionDTO.setRealsource(bizItfMapDTO.getClassID());
                busiitfconnectionDTO.setRealtarget(referenceDTO.getRefId());
                busiitfconnectionDTO.setSource(busiitfconnectionDTO.getRealsource());
                busiitfconnectionList.add(busiitfconnectionDTO);
            }
        }
    }

    public static void attr2VO(Object d, Element e, Map<String, String> nameMaping) {
        if (nameMaping == null) {
            nameMaping = new HashMap<>();
        }
        NamedNodeMap attrs = e.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Node item = attrs.item(i);
            try {
                String fieldName = item.getNodeName();

                if (nameMaping.get(fieldName) != null) {
                    fieldName = nameMaping.get(fieldName);
                }

                ReflectUtil.setFieldValueAutoConvert(d, fieldName, item.getTextContent());
            } catch (Throwable ex) {
                LogUtil.error(ex.getMessage(), ex);
            }
        }
    }
}
