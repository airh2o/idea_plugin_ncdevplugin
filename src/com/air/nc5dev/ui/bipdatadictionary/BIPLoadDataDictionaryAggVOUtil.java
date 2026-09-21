package com.air.nc5dev.ui.bipdatadictionary;

import cn.hutool.core.util.StrUtil;
import com.air.nc5dev.util.*;
import com.air.nc5dev.util.idea.LogUtil;
import com.air.nc5dev.util.jdbc.ConnectionUtil;
import com.air.nc5dev.util.jdbc.resulthandel.ArrayListMapLowerResultSetExtractor;
import com.air.nc5dev.util.jdbc.resulthandel.VOArrayListResultSetExtractor;
import com.air.nc5dev.util.meta.consts.PropertyDataTypeEnum;
import com.air.nc5dev.vo.DataDictionaryAggVO;
import com.air.nc5dev.vo.NCDataSourceVO;
import com.air.nc5dev.vo.meta.*;
import com.alibaba.fastjson.JSON;
import com.google.common.cache.Cache;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import lombok.Data;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <br>
 * <br>
 * <br>
 *
 * @author 唐粟 Email:209308343@qq.com 微信yongyourj
 * @date 2023/12/1 0001 16:15
 * @project
 * @Version
 */
@Data
public class BIPLoadDataDictionaryAggVOUtil {
    Project project;
    NCDataSourceVO ncDataSourceVO;
    DataDictionaryAggVO agg;
    ProgressIndicator indicator;
    List<SearchComponentVO2> md_componentList;
    List<ClassExtInfoDTO> fieldList;
    ArrayListMapLowerResultSetExtractor arrayListMapLowerResultSetExtractor = new ArrayListMapLowerResultSetExtractor();
    List<Map<String, Object>> billTypes;
    List<Map<String, Object>> webinfos;
    List<Map<String, Object>> aggFullClasss;
    List<Map<String, Object>> pks;
    List<PropertyDTO> propertyDTOList;
    List<EnumValueDTO> enumValueDTOList;
    String compomentSql = "select c.id\n" +
            "     ,c.legacy_id\n" +
            "     ,c.domain as namespace\n" +
            "     ,c.uri\n" +
            "     ,c.display_name as displayName\n" +
            "     ,c.own_module as ownModule\n" +
            "     ,c.name\n" +
            "     ,c.description\n" +
            "     ,c.create_time as createTime\n" +
            "     ,c.version\n" +
            "from iuap_metadata_base.md_meta_component c\n" +
            "where c.ytenant_id='0' ";
    static Cache<Object, Object> cache;

    static {
        //    cache = CacheBuilder.newBuilder().expireAfterWrite(20, TimeUnit.MINUTES).build();
    }

    public BIPLoadDataDictionaryAggVOUtil(Project project, NCDataSourceVO ncDataSourceVO) {
        this.project = project;
        this.ncDataSourceVO = ncDataSourceVO;
    }

    public DataDictionaryAggVO read() throws SQLException, ClassNotFoundException {
        agg = new DataDictionaryAggVO();
        agg.setClassId2EnumValuesMap(new HashMap<>());
        agg.setCompomentIdMap(new HashMap<>());
        agg.setClassMap(new HashMap<>());
        agg.setNcVersion("BIP旗舰版");
        if (indicator == null) {
            indicator = new EmptyProgressIndicatorImpl();
        }

        Connection conn = null;
        Statement st = null;
        try {
            conn = ConnectionUtil.getConn(ncDataSourceVO);
            DatabaseMetaData metaData = conn.getMetaData();
            String productName = metaData.getDatabaseProductName().toLowerCase();

            st = conn.createStatement();
            ResultSet rs = null;
            try {
                rs = st.executeQuery("select show_version,evn from iuap_installer.product_version" +
                        " where product_code!='aPaaS' and show_version is not null");
                if (rs.next()) {
                    agg.setNcVersion(rs.getString(1) + ":" + rs.getString(2));
                }
                IoUtil.close(rs);
            } catch (SQLException e) {
                //????
            }

            try {
                rs = st.executeQuery("select tenant_name from iuap_uuas_usercenter.pub_tenant " +
                        " where tenant_group_code='default-tg' order by createtime ");
                if (rs.next()) {
                    agg.setGroupName(rs.getString(1));
                }
                IoUtil.close(rs);
            } catch (Throwable e) {
                agg.setGroupName("");
            }

            String sql = compomentSql
                    .toLowerCase()
                    .replace("\n", " ")
                    .replace("\r", " ")
                    .replace("\t", " ");
            String[] ss = StringUtil.split(sql, " ");
            sql = "";
            for (String s : ss) {
                if (StringUtil.isBlank(s)) {
                    continue;
                }

                sql += " " + s;
            }

            // select application_code,application_name,label_type,label_domain from iuap_apcom_benchservice
            // .wb_application
            sql = "select application_id as id, application_code as name ,application_name as displayname, label_type" +
                    " as parentmoduleid" +
                    " from iuap_apcom_benchservice.wb_application where application_code in(select app_code "
                    + sql.substring(
                    sql.indexOf(" from ")
                    , sql.lastIndexOf(" order by ")
            ) + ')';
            indicatorShow(String.format("正在查询模块列表(1/11)...%s", sql));
            rs = st.executeQuery(sql);
            ArrayList<DataDictionaryAggVO.Module> allModules =
                    new VOArrayListResultSetExtractor<DataDictionaryAggVO.Module>
                            (DataDictionaryAggVO.Module.class).extractData(rs);
            IoUtil.close(rs);

            sql = "select label_code as id, label_code as name ,label_name as displayname, label_type as " +
                    "parentmoduleid" +
                    " from iuap_apcom_benchservice.wb_label where label_code in(select parentmoduleid from ("
                    + sql + ") ) ";
            rs = st.executeQuery(sql);
            ArrayList<DataDictionaryAggVO.Module> allModules2 =
                    new VOArrayListResultSetExtractor<DataDictionaryAggVO.Module>
                            (DataDictionaryAggVO.Module.class).extractData(rs);
            IoUtil.close(rs);

            allModules.addAll(allModules2);

            List<DataDictionaryAggVO.Module> modules = V.toTree(allModules, "id", "parentmoduleid", "childs");
            Map<String, DataDictionaryAggVO.Module> id2ModuleMap = allModules.stream()
                    .collect(Collectors.toMap(DataDictionaryAggVO.Module::getId, m -> m, (m1, m2) -> m2));
            agg.setId2ModuleMap(id2ModuleMap);
            agg.setAllModules(allModules);

            //读取元数据了
            indicatorShow(String.format("正在一次性查询元数据组件列表(2/11)...%s", compomentSql));
            rs = st.executeQuery(compomentSql);
            md_componentList =
                    new VOArrayListResultSetExtractor<SearchComponentVO2>(SearchComponentVO2.class).extractData(rs);
            IoUtil.close(rs);

            sql = "select mmc.meta_component_uri as id " +
                    ",mmc.name " +
                    ",mmc.display_name as displayname" +
                    ",cp.id as componentid" +
                    ",201 as classtype" +
                    ",null as parentclassid" +
                    ",mmc.table_name as defaulttablename" +
                    ",lower(aoj.owner)||'.'||lower(aoj.object_name) as fullclassname" +
                    " from iuap_metadata_base.md_meta_class mmc " +
                    "join iuap_metadata_base.md_meta_component cp on mmc.meta_component_uri=cp.uri " +
                    "left join all_objects aoj on aoj.object_type='TABLE' and lower(aoj.object_name)=lower(mmc" +
                    ".table_name) ";
            if (productName.contains("mysql")
                    || productName.contains("tidb")
                    || productName.contains("mariadb")) {
                // MySQL
                sql = "select mmc.meta_component_uri as id " +
                        ",mmc.name " +
                        ",mmc.display_name as displayname" +
                        ",cp.id as componentid" +
                        ",201 as classtype" +
                        ",null as parentclassid" +
                        ",mmc.table_name as defaulttablename" +
                        ",LOWER(CONCAT(aoj.table_schema, '.', mmc.table_name)) as fullclassname" +
                        " from iuap_metadata_base.md_meta_class mmc " +
                        "join iuap_metadata_base.md_meta_component cp on mmc.meta_component_uri=cp.uri " +
                        "left join information_schema.tables aoj on aoj.TABLE_TYPE ='BASE TABLE' " +
                        "  and lower(aoj.table_name )=lower(mmc.table_name) ";
            } else if (productName.contains("oracle")) {
                // Oracle
            } else if (productName.contains("postgresql")
                    || productName.contains("kingbase")
                    || productName.contains("kdbms")
                    || productName.contains("gaussdb")) {
                // PostgreSQL (PG)   人大金仓 (Kingbase)   华为GaussDB
                sql = "select mmc.meta_component_uri as id " +
                        ",mmc.name " +
                        ",mmc.display_name as displayname" +
                        ",cp.id as componentid" +
                        ",201 as classtype" +
                        ",null as parentclassid" +
                        ",mmc.table_name as defaulttablename" +
                        ",LOWER(CONCAT(aoj.schemaname , '.', mmc.table_name)) as fullclassname" +
                        " from iuap_metadata_base.md_meta_class mmc " +
                        "join iuap_metadata_base.md_meta_component cp on mmc.meta_component_uri=cp.uri " +
                        "left join pg_tables aoj on 1=1 " +
                        "  and lower(aoj.tablename  )=lower(mmc.table_name) ";
            } else if (productName.contains("microsoft sql server")) {
                // SQL Server
                sql = "select mmc.meta_component_uri as id " +
                        ",mmc.name " +
                        ",mmc.display_name as displayname" +
                        ",cp.id as componentid" +
                        ",201 as classtype" +
                        ",null as parentclassid" +
                        ",mmc.table_name as defaulttablename" +
                        ",LOWER(CONCAT(SCHEMA_NAME(aoj.schema_id) , '.', mmc.table_name)) as fullclassname" +
                        " from iuap_metadata_base.md_meta_class mmc " +
                        "join iuap_metadata_base.md_meta_component cp on mmc.meta_component_uri=cp.uri " +
                        "left join sys.objects aoj on aoj.type = 'U' " +
                        "  and lower(aoj.name   )=lower(mmc.table_name) ";
            } else if (productName.contains("dm dbms") || productName.contains("dameng")) {
                // 达梦 (DM)
                sql = "select mmc.meta_component_uri as id " +
                        ",mmc.name " +
                        ",mmc.display_name as displayname" +
                        ",cp.id as componentid" +
                        ",201 as classtype" +
                        ",null as parentclassid" +
                        ",mmc.table_name as defaulttablename" +
                        ",LOWER(CONCAT(aoj.owner , '.', mmc.table_name)) as fullclassname" +
                        " from iuap_metadata_base.md_meta_class mmc " +
                        "join iuap_metadata_base.md_meta_component cp on mmc.meta_component_uri=cp.uri " +
                        "left join all_tables aoj on aoj.TABLE_TYPE ='BASE TABLE' " +
                        "  and lower(aoj.table_name  )=lower(mmc.table_name) ";
            }

            indicatorShow("正在一次性查询元数据字段列表(3/11)..." + sql);
            rs = st.executeQuery(sql);
            fieldList = new VOArrayListResultSetExtractor<ClassExtInfoDTO>(ClassExtInfoDTO.class).extractData(rs);
            IoUtil.close(rs);

            billTypes = new ArrayList<>();
//            try {
//                sql = "select pk_billtypecode,billtypename,nodecode, component from " +
//                        "bd_billtype" +
//                        " where istransaction='N'  " +
//                        " union all\n" +
//                        " select pk_billtypecode,billtypename,nodecode,component from bd_billtype  ";
//                indicatorShow("正在一次性查询单据类型列表(4/11)..." + sql);
//                rs = st.executeQuery(sql);
//                billTypes = arrayListMapLowerResultSetExtractor.extractData(rs);
//                IoUtil.close(rs);
//            } catch (Exception e) {
//                if (billTypes == null) {
//                    billTypes = new ArrayList<>();
//                }
//            }

            try {
                sql = "select b.bill_no as pagecode,b.bill_name as pageurl,mmc.id\n" +
                        "from  iuap_apcom_benchservice.wb_service s\n" +
                        "join iuap_metadata_service.uimeta_bill b on s.bu_code=b.biz_object\n" +
                        "join iuap_metadata_service.uimeta_bill_entity be on b.serial_code=be.bill_serial_code and be" +
                        ".ytenant_id=b.ytenant_id\n" +
                        "join iuap_metadata_base.md_meta_class mmc on mmc.uri=be.datasource_name\n" +
                        "where 1=1 and b.bill_type in('Voucher','VoucherList') ";
                indicatorShow("正在一次性查询节点信息(5/11)..." + sql);
                rs = st.executeQuery(sql);
                webinfos = arrayListMapLowerResultSetExtractor.extractData(rs);
                IoUtil.close(rs);
            } catch (Exception e) {
                if (webinfos == null) {
                    webinfos = new ArrayList<>();
                }
            }

            try {
                sql =
                        "select name" +
                                ",display_name as displayname" +
                                ",is_nullable asnullable" +
                                ",COALESCE(ref_meta_class_uri, ref_enum_uri) as refmodelname" +
                                ",default_value as defaultvalue" +
                                ",display_name as description " +
                                ",biz_type as datatype" +
                                ",biz_type as dbtype" +
                                ",biz_type as typeDisplayName" +
                                ",biz_type as fieldType" +
                                ",is_calculated as calculation" +
                                ",false dynamic" +
                                ",length as attrlength " +
                                ",precise as precise " +
                                ",object_uri as classid " +
                                " from iuap_metadata_base.md_attribute" +
                                " where 1=1   " +
                                " order by   "
                ;
                indicatorShow("正在一次性查询元数据属性列表,此步骤耗时很长(6/11)..." + sql);
                rs = st.executeQuery(sql);
                propertyDTOList = new VOArrayListResultSetExtractor<PropertyDTO>(PropertyDTO.class).extractData(rs);
                IoUtil.close(rs);
            } catch (Exception e) {
                if (propertyDTOList == null) {
                    propertyDTOList = new ArrayList<>();
                }
            }

            try {
                sql = "select  code as value, coalesce(display_name, name) as name, enumeration_uri as id" +
                        "  from iuap_metadata_base.md_enumeration_literal ";
                indicatorShow("正在一次性查询元数据枚举列表(7/11)..." + sql);
                rs = st.executeQuery(sql);
                enumValueDTOList = new VOArrayListResultSetExtractor<EnumValueDTO>(EnumValueDTO.class).extractData(rs);
                IoUtil.close(rs);
            } catch (Exception e) {
                if (enumValueDTOList == null) {
                    enumValueDTOList = new ArrayList<>();
                }
            }

            pks = new ArrayList<>();
//            try {
//                sql = "select name,tableid from md_column where  pkey='Y'";
//                indicatorShow("正在一次性查询元数据主键列表(8/11)..." + sql);
//                rs = st.executeQuery(sql);
//                pks = arrayListMapLowerResultSetExtractor.extractData(rs);
//                IoUtil.close(rs);
//            } catch (Exception e) {
//                if (pks == null) {
//                    pks = new ArrayList<>();
//                }
//            }

            aggFullClasss = new ArrayList<>();
//            try {
//                sql = "select paravalue,id from md_accessorpara ";
//                indicatorShow("正在一次性查询元数据java类列表(9/11)..." + sql);
//                rs = st.executeQuery(sql);
//                aggFullClasss = arrayListMapLowerResultSetExtractor.extractData(rs);
//                IoUtil.close(rs);
//            } catch (Exception e) {
//                if (aggFullClasss == null) {
//                    aggFullClasss = new ArrayList<>();
//                }
//            }

            //读取他们的实体列表和字段列表
            for (SearchComponentVO2 com : md_componentList) {
                if (indicator.isCanceled()) {
                    return agg;
                }

                indicatorShow(String.format("正在加载元数据组件(10/11)...%s - %s", com.getName(), com.getDisplayName()));

                loadSearchComponentVO(agg, com, st);
            }

            agg.setProjectName(getProject().getName());
            agg.setNcHome(ProjectNCConfigUtil.getNCHomePath(getProject()));
            agg.setModules(modules);
            agg.getCompomentIdMap().clear();
            for (SearchComponentVO c : md_componentList) {
                if (c.getClassDTOS() != null) {
                    ArrayList<ClassDTO> ncas = new ArrayList<>();
                    for (ClassDTO ca : c.getClassDTOS()) {
                        ClassExtInfoDTO nca = new ClassExtInfoDTO();
                        nca.setId(ca.getId());
                        nca.setName(ca.getName());
                        nca.setDisplayName(ca.getDisplayName());
                        nca.setFullClassName(ca.getFullClassName());
                        nca.setAggFullClassName(ca.getAggFullClassName());
                        nca.setClassType(ca.getClassType());
                        nca.setParentClassID(ca.getParentClassID());
                        nca.setComponentID(ca.getComponentID());
                        nca.setRefModelName(ca.getRefModelName());
                        nca.setDefaultTableName(ca.getDefaultTableName());
                        ncas.add(nca);
                    }
                    c.setClassDTOS(ncas);
                }
                agg.getCompomentIdMap().put(c.getId(), c);
            }

            return agg;
        } finally {
            IoUtil.close(st);
            IoUtil.close(conn);
        }
    }

    public void loadSearchComponentVO(DataDictionaryAggVO agg, SearchComponentVO2 c, Statement st) throws SQLException {
        try {
            if (indicator == null) {
                indicator = new EmptyProgressIndicatorImpl();
            }
            if (agg.getCompomentIdMap().get(c.getId()) != null) {
                return;
            }

            SearchComponentVO2 com = md_componentList.stream()
                    .filter(m -> m.getId().equals(c.getId()))
                    .findAny()
                    .orElse(c);
            ResultSet rs = null;

            if (com == null) {
                return;
            }

            agg.getCompomentIdMap().put(com.getId(), com);

            List cs = fieldList.stream()
                    .filter(f -> f.getComponentID().equals(com.getId()))
                    .collect(Collectors.toList());
            com.setClassDTOS(cs);

            DataDictionaryAggVO.Module m = agg.getId2ModuleMap().get(com.getOwnModule());
            if (m == null) {
                m = new DataDictionaryAggVO.Module();
                m.setId(com.getOwnModule());
                m.setName(com.getOwnModule());
                m.setDisplayname(com.getOwnModule());
                // m.setMetas(new ArrayList<>());
                agg.getId2ModuleMap().put(m.getId(), m);
            }

            Map<String, Object> billType = billTypes.stream()
                    .filter(pk -> com.getName().equals(pk.get("component")))
                    .findAny()
                    .orElse(null);

            // m.getMetas().add(com);
            //立即放入map中，防止下个元数据 又依赖他的实体！
            List<ClassExtInfoDTO> classExtInfoDTOs = cs;
            for (ClassExtInfoDTO cla : classExtInfoDTOs) {
                if (CollUtil.isNotEmpty(billType)) {
                    ReflectUtil.copy2VO(billType, cla);
                }

                Map<String, Object> webinfo = webinfos.stream()
                        .filter(pk -> cla.getId().equals(pk.get("id")))
                        .findAny()
                        .orElse(null);
                if (CollUtil.isNotEmpty(webinfo)) {
                    ReflectUtil.copy2VO(webinfo, cla);
                }

                agg.getClassMap().put(cla.getId(), cla);
            }

            for (ClassExtInfoDTO cla : classExtInfoDTOs) {
                if (indicator.isCanceled()) {
                    return;
                }

                indicatorShow(String.format("正在填充元数据组件实体的属性等信息(11/11)...%s - %s - %s - %s "
                        , com.getName()
                        , com.getDisplayName()
                        , cla.getName()
                        , cla.getDisplayName()
                ));

                if (ClassDTO.CLASSTYPE_ENTITY.equals(cla.getClassType())) {
                    List<PropertyDTO> ps = propertyDTOList.stream()
                            .filter(p -> cla.getId().equals(p.getClassID()))
                            .collect(Collectors.toList());
                    cla.setPerperties(ps);

                    cla.setAggFullClassName((String) aggFullClasss.stream()
                            .filter(fc -> cla.getId().equals(fc.get("id")))
                            .findAny()
                            .orElse(new HashMap<>())
                            .get("paravalue")
                    );

                    if (m.getChilds() == null) {
                        m.setChilds(new ArrayList<>());
                    }
                    m.getChilds().add(DataDictionaryAggVO.Module.builder()
                            .id(cla.getId())
                            .type(V.get(cla.getClassType(), ClassDTO.CLASSTYPE_ENTITY))
                            .name(cla.getName())
                            .defaultTableName(cla.getDefaultTableName())
                            .displayname(cla.getDisplayName())
                            .fullClassName(cla.getFullClassName())
                            .aggFullClassName(cla.getAggFullClassName())
                            .build());
                } else if (ClassDTO.CLASSTYPE_ENUMERATE.equals(cla.getClassType())) {
                    List<EnumValueDTO> ps = enumValueDTOList.stream()
                            .filter(e -> cla.getId().equals(e.getId()))
                            .collect(Collectors.toList());
                    agg.getClassId2EnumValuesMap().put(cla.getId(), ps);
                }

                if (CollUtil.isEmpty(cla.getPerperties())) {
                    continue;
                }

                HashSet<String> idFields = new HashSet<>();
                Map<String, Object> pkMap = pks.stream()
                        .filter(pk -> cla.getDefaultTableName().equals(pk.get("tableid")) || cla.getId().equals(pk.get("tableid")))
                        .findAny()
                        .orElse(null);
                if (pkMap != null) {
                    idFields.add((String) pkMap.get("name"));
                }

                for (PropertyDTO p : cla.getPerperties()) {
                    if (indicator.isCanceled()) {
                        return;
                    }

                    p.setTypeName(PropertyDataTypeEnum.ofTypeDefualt(p.getDataType()).getTypeName());
                    p.setFieldType(PropertyDataTypeEnum.ofTypeDefualt(p.getDataType()).getFieldType());
                    p.setFileTypeDesc(p.getTypeName());
                    p.setTypeDisplayName(PropertyDataTypeEnum.ofTypeDefualt(p.getDataType()).getTypeDisplayName());
                    p.setRefModelDesc(p.getTypeDisplayName() + " (" + p.getFieldType() + ')');
                    p.setFieldName(p.getName());

                    if (idFields.contains(p.getName())) {
                        p.setRefModelDesc("当前表主键:字符串 (String)");
                        p.setIsKey(true);
                        p.setRefModelName(null);
                        continue;
                    }

                    if (StrUtil.isBlank(p.getDataType()) || PropertyDataTypeEnum.ofType(p.getDataType()) != null) {
                        //基本类型！
                        if (p.getAttrLength() != null && p.getAttrLength() != 0) {
                            p.setFileTypeDesc(p.getTypeName() + " (" + p.getAttrLength() + ')');
                        }
                        p.setRefModelName(null);
                        continue;
                    }

                    //引用的其他元数据！！！
                    ClassDTO refc = agg.getClassMap().get(p.getDataType());

                    if (refc == null) {//也许是枚举！
                        //枚举!
                        List<EnumValueDTO> vs = agg.getClassId2EnumValuesMap().get(p.getDataType());
                        if (vs == null) {
                            vs = enumValueDTOList.stream()
                                    .filter(e -> p.getDataType().equals(e.getId()))
                                    .collect(Collectors.toList());
                            agg.getClassId2EnumValuesMap().put(p.getDataType(), vs);
                        }

                        if (CollUtil.isNotEmpty(vs)) {
                            for (EnumValueDTO v : vs) {
                                v.setIndustry(null);
                            }

                            p.setDescription(JSON.toJSONString(vs));
                            p.setRefModelDesc("枚举");
                            continue;
                        }
                    }

                    if (refc == null) {
                        SearchComponentVO2 cmt = md_componentList.stream()
                                .filter(e -> p.getDataType().equals(e.getId()))
                                .findAny()
                                .orElse(null);
                        if (cmt != null) {
                            loadSearchComponentVO(agg, cmt, st);
                        }
                    }
                    refc = agg.getClassMap().get(p.getDataType());

                    if (refc == null) {
                        p.setRefModelDesc(p.getRefModelDesc() + " (引用的其他实体 但是找不到此实体信息!) " + p.getDataType());
                        p.setRefModelName(null);
                        continue;
                    }

                    p.setRefModelDesc(String.format(
                            "%s(%s %s)"
                            , refc.getDisplayName()
                            , refc.getName()
                            , simpleClassName(refc.getFullClassName())
                    ));
                }
            }

            com.setClassDTOS(cs);
        } finally {
        }
    }

    public void indicatorShow(String msg) {
        indicator.setText2(msg);
        LogUtil.output(msg);
    }

    public String simpleClassName(String c) {
        if (c == null) {
            return "";
        }

        return c.substring(c.lastIndexOf('.') + 1);
    }
}
