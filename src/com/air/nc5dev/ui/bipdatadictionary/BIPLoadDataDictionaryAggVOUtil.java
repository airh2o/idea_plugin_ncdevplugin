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
import com.google.common.cache.CacheBuilder;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import lombok.Data;

import java.sql.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
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
    List<ClassExtInfoDTO> entityList;
    ArrayListMapLowerResultSetExtractor arrayListMapLowerResultSetExtractor = new ArrayListMapLowerResultSetExtractor();
    List<Map<String, Object>> billTypes;
    List<Map<String, Object>> webinfos;
    List<Map<String, Object>> aggFullClasss;
    List<Map<String, Object>> pks;
    List<PropertyDTO> propertyDTOList;
    List<EnumValueDTO> enumValueDTOList;
    String compomentSql = "select c.uri as id " +
            "     ,c.legacy_id " +
            "     ,c.domain as namespace " +
            "     ,c.uri " +
            "     ,c.display_name as displayName " +
            "     ,c.own_module as ownModule " +
            "     ,c.name " +
            "     ,c.description " +
            //   "     ,c.create_time as createTime " +
            "     ,c.version " +
            "from iuap_metadata_base.md_meta_component c " +
            "where c.ytenant_id='0' ";
    public static Cache<Object, Object> cache;
    SearchComponentVO2 unknowModel;
    String entitySql;

    static {
        cache = CacheBuilder.newBuilder().expireAfterWrite(60, TimeUnit.DAYS).build();
    }

    public BIPLoadDataDictionaryAggVOUtil(Project project, NCDataSourceVO ncDataSourceVO) {
        this.project = project;
        this.ncDataSourceVO = ncDataSourceVO;
    }

    public DataDictionaryAggVO read() throws SQLException, ClassNotFoundException {
        unknowModel = new SearchComponentVO2();
        unknowModel.setId("unknowModel");
        unknowModel.setName("unknowModel");
        unknowModel.setDisplayName("未知模块");
        unknowModel.setNamespace("");
        unknowModel.setFilePath("");

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
                rs = st.executeQuery("select show_version from iuap_installer.product_version " +
                        " where app_code like 'yonbip-%' and product_code!='aPaaS' and show_version is not null ");
                if (rs.next()) {
                    agg.setNcVersion("BIP旗舰版_" + rs.getString(1));
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
                agg.setGroupName("未知集团");
            }

            String sql = compomentSql
                    .toLowerCase()
                    .replace(" ", " ")
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
            sql = "select application_id as id" +
                    ", application_code as name " +
                    ",application_name as displayname" +
                    ", label_type as parentmoduleid" +
                    " from iuap_apcom_benchservice.wb_application" +
                    " where application_code in(   " +
                    " select app_code "
                    + sql.substring(sql.indexOf(" from "), sql.lastIndexOf(" order by "))
                    + " )  "
            ;
            indicatorShow(String.format("正在查询模块列表(1/11)...%s", sql));
            ArrayList<DataDictionaryAggVO.Module> allModules =
                    (ArrayList<DataDictionaryAggVO.Module>) cache.getIfPresent(sql);
            if (allModules == null) {
                rs = st.executeQuery(sql);
                allModules =
                        new VOArrayListResultSetExtractor<DataDictionaryAggVO.Module>(DataDictionaryAggVO.Module.class)
                                .extractData(rs);
                IoUtil.close(rs);
                cache.put(sql, allModules);
            }

            sql = "select label_code as id" +
                    ", label_code as name " +
                    ",label_name as displayname" +
                    ", label_type as parentmoduleid" +
                    " from iuap_apcom_benchservice.wb_label" +
                    " where label_code in(select parentmoduleid from (" + sql + ") ) ";
            ArrayList<DataDictionaryAggVO.Module> allModules2 =
                    (ArrayList<DataDictionaryAggVO.Module>) cache.getIfPresent(sql);

            if (allModules2 == null) {
                rs = st.executeQuery(sql);
                allModules2 = new VOArrayListResultSetExtractor<DataDictionaryAggVO.Module>
                        (DataDictionaryAggVO.Module.class).extractData(rs);
                IoUtil.close(rs);
                cache.put(sql, allModules2);
            }

            allModules.addAll(allModules2);

            Map<String, DataDictionaryAggVO.Module> id2ModuleMap = allModules.stream()
                    .collect(Collectors.toMap(DataDictionaryAggVO.Module::getId, m -> m, (m1, m2) -> m2));
            agg.setId2ModuleMap(id2ModuleMap);
            agg.setAllModules(allModules);

            //读取元数据了
            indicatorShow(String.format("正在一次性查询元数据组件列表(2/11)...%s", compomentSql));
            md_componentList = (List<SearchComponentVO2>) cache.getIfPresent(compomentSql);
            if (md_componentList == null) {
                rs = st.executeQuery(compomentSql);
                md_componentList = new VOArrayListResultSetExtractor<SearchComponentVO2>(SearchComponentVO2.class)
                        .extractData(rs);
                md_componentList = md_componentList.stream()
                        .filter(c -> c.getId() != null)
                        .collect(Collectors.collectingAndThen(
                                        Collectors.toMap(SearchComponentVO::getId
                                                , c -> c
                                                , (c1, c2) -> c1
                                                , LinkedHashMap::new)
                                        , m -> new ArrayList<>(m.values())
                                )
                        );
                IoUtil.close(rs);
                cache.put(compomentSql, md_componentList);
            }

            agg.getCompomentIdMap().clear();
            for (SearchComponentVO c : md_componentList) {
                agg.getCompomentIdMap().put(c.getId(), c);
            }

            sql = "select mmc.uri as id " +
                    "     , mmc.uri as name " +
                    "     , mmc.display_name       as displayname " +
                    "     , mmc.meta_component_uri            as componentid " +
                    "     , 201                    as classtype " +
                    "     , null                   as parentclassid " +
                    "     , mmc.table_name         as defaulttablename " +
                    "     , mmc.name          as fullclassname " +
                    "     , {aggFullClassName}          as aggfullclassname  " +
                    "     , e.code as resid " +
                    "from iuap_metadata_base.md_meta_class mmc " +
                    "  left join iuap_metadata_base.md_biz_obj e on mmc.uri = e.main_entity " +
                    "            and e.ytenant_id = mmc.ytenant_id " +
                    "   {join2} " +
                    " where 1=1  "
            ;

            if (productName.contains("mysql")
                    || productName.contains("tidb")
                    || productName.contains("mariadb")) {
                // MySQL
                sql = StringUtil.replace(sql, "{aggFullClassName}"
                        , "lower(concat(aoj.table_schema, '.', mmc.table_name))");

                sql = StringUtil.replace(sql, "{join2}"
                        , "left join information_schema.tables aoj on aoj.table_type ='BASE TABLE' " +
                                "  and lower(aoj.table_name )=lower(mmc.table_name) ");
            } else if (productName.contains("oracle")) {
                // Oracle
                sql = StringUtil.replace(sql, "{aggFullClassName}"
                        , "lower(aoj.owner)||'.'||lower(aoj.object_name)");

                sql = StringUtil.replace(sql, "{join2}"
                        , "left join all_objects aoj on aoj.object_type='TABLE' " +
                                " and lower(aoj.object_name)=lower(mmc.table_name) ");
            } else if (productName.contains("postgresql")
                    || productName.contains("kingbase")
                    || productName.contains("kdbms")
                    || productName.contains("gaussdb")) {
                // PostgreSQL (PG)   人大金仓 (Kingbase)   华为GaussDB
                sql = StringUtil.replace(sql, "{aggFullClassName}"
                        , "lower(concat(aoj.schemaname , '.', mmc.table_name))");

                sql = StringUtil.replace(sql, "{join2}"
                        , "left join pg_tables aoj on lower(aoj.tablename)=lower(mmc.table_name) ");
            } else if (productName.contains("microsoft sql server")) {
                // SQL Server
                sql = StringUtil.replace(sql, "{aggFullClassName}"
                        , "lower(concat(schema_name(aoj.schema_id) , '.', mmc.table_name))");

                sql = StringUtil.replace(sql, "{join2}"
                        , "left join sys.objects aoj on aoj.type = 'U' " +
                                "  and lower(aoj.name   )=lower(mmc.table_name) ");
            } else if (productName.contains("dm dbms") || productName.contains("dameng")) {
                // 达梦 (DM)
                sql = StringUtil.replace(sql, "{aggFullClassName}"
                        , "lower(concat(aoj.owner , '.', mmc.table_name))");

                sql = StringUtil.replace(sql, "{join2}"
                        , "left join all_tables aoj on aoj.TABLE_TYPE ='BASE TABLE' " +
                                "  and lower(aoj.table_name  )=lower(mmc.table_name) ");
            }

            entitySql = sql + " and mmc.ytenant_id='0' and mmc.meta_component_uri is not null  union all "
                    + sql + " and mmc.ytenant_id!='0' and mmc.meta_component_uri is not null ";  // mmc
            // .ytenant_id = '0'
            indicatorShow("正在一次性查询实体列表(3/11)..." + entitySql);
            entityList = (List<ClassExtInfoDTO>) cache.getIfPresent(entitySql);
            if (entityList == null) {
                rs = st.executeQuery(entitySql);
                entityList = new VOArrayListResultSetExtractor<ClassExtInfoDTO>(ClassExtInfoDTO.class).extractData(rs);
                IoUtil.close(rs);
                entityList = entityList.stream()
                        .filter(c -> c.getId() != null)
                        .collect(Collectors.collectingAndThen(
                                Collectors.toMap(ClassExtInfoDTO::getId
                                        , c -> c
                                        , (c1, c2) -> c1
                                        , LinkedHashMap::new)
                                ,
                                m -> new ArrayList<>(m.values()))
                        );

                cache.put(entitySql, entityList);
            }
            entitySql = sql;

            for (ClassExtInfoDTO entity : entityList) {
                agg.getClassMap().put(entity.getId(), entity);
            }

            try {
                sql =
                        "select concat(concat(object_uri,'.'), name) as id ,name " +
                                "     , display_name                               as displayname " +
                                "     , is_nullable                                   asnullable " +
                                "     , COALESCE(ref_meta_class_uri, ref_enum_uri) as refmodelname " +
                                "     , default_value                              as defaultvalue " +
                                "     , display_name                               as description " +
                                "     , biz_type                                   as datatype " +
                                "     , biz_type                                   as dbtype " +
                                "     , biz_type                                   as typeDisplayName " +
                                "     , biz_type                                   as fieldType " +
                                "     , is_calculated                              as calculation " +
                                "     , length                                     as attrlength " +
                                "     , precise                                    as precise " +
                                "     , object_uri                                 as classid " +
                                "     , field_name as field_name " +
                                "     , 1000086 as ordernum " +
                                "from iuap_metadata_base.md_attribute " +
                                " where 1=1 and object_uri in(select ccc.id from (" + entitySql + ") ccc) " +
                                "union all " +
                                "select uri as id, name " +
                                "     , display_name                               as displayname " +
                                "     , null                                   asnullable " +
                                "     , ref_type as refmodelname " +
                                "     , default_value                              as defaultvalue " +
                                "     , display_name                               as description " +
                                "     , 'text'                                   as datatype " +
                                "     , 'text'                                   as dbtype " +
                                "     , 'text'                                   as typeDisplayName " +
                                "     , 'text'                                   as fieldType " +
                                "     , null                              as calculation " +
                                "     , 100                                     as attrlength " +
                                "     , 28                                    as precise " +
                                "     , meta_class_uri                                 as classid " +
                                "     , name as field_name " +
                                "     , attribute_order as ordernum " +
                                "from iuap_metadata_base.md_biz_attribute " +
                                "where 1 = 1 "
                ;

                indicatorShow("正在一次性查询实体字段列表,此步骤耗时很长(6/11)..." + sql);
                propertyDTOList = (List<PropertyDTO>) cache.getIfPresent(sql);
                if (propertyDTOList == null) {
                    rs = st.executeQuery(sql);
                    propertyDTOList = new VOArrayListResultSetExtractor<PropertyDTO>(PropertyDTO.class).extractData(rs);
                    IoUtil.close(rs);

                    propertyDTOList = propertyDTOList.stream()
                            .filter(c -> c.getId() != null)
                            .collect(Collectors.collectingAndThen(
                                    Collectors.toMap(PropertyDTO::getId
                                            , c -> c
                                            , (c1, c2) -> c1
                                            , LinkedHashMap::new)
                                    ,
                                    m -> new ArrayList<>(m.values()))
                            );

                    cache.put(sql, propertyDTOList);
                }
            } catch (Exception e) {
                if (propertyDTOList == null) {
                    propertyDTOList = new ArrayList<>();
                }
                e.printStackTrace();
            }

            billTypes = new ArrayList<>();
            try {
                sql = "select code as pk_billtypecode " +
                        "     ,name as billtypename " +
                        "     ,busiobj_code as component " +
                        "from iuap_apdoc_basedoc.bd_billtype " +
                        "where dr=0 and busiobj_code in(select ccc.resid from (" + entitySql + ") ccc) "
                ;
                indicatorShow("正在一次性查询单据类型列表(4/11)..." + sql);
                billTypes = (List<Map<String, Object>>) cache.getIfPresent(sql);
                if (billTypes == null) {
                    rs = st.executeQuery(sql);
                    billTypes = arrayListMapLowerResultSetExtractor.extractData(rs);
                    IoUtil.close(rs);
                    cache.put(sql, billTypes);
                }
            } catch (Exception e) {
                if (billTypes == null) {
                    billTypes = new ArrayList<>();
                }
                e.printStackTrace();
            }

            try {
                sql = "select distinct ub.bill_no as pagecode " +
                        "    , ub.bill_type as pageurl " +
                        "     , im.entity_uri as id " +
                        "    ,  im.name as nodecode " +
                        "from iuap_yonbuilder_service.ide_module im " +
                        " join iuap_metadata_service.uimeta_bill ub   " +
                        "   on im.business_json like '%\"' || ub.def_tpl_serial_code || '\"%' " +
                        "where 1=1 and im.entity_uri in(select ccc.id from (" + entitySql + ") ccc) "
                ;
                indicatorShow("正在一次性查询节点信息(5/11)..." + sql);
                webinfos = (List<Map<String, Object>>) cache.getIfPresent(sql);
                if (webinfos == null) {
                    rs = st.executeQuery(sql);
                    webinfos = arrayListMapLowerResultSetExtractor.extractData(rs);
                    IoUtil.close(rs);
                    cache.put(sql, webinfos);
                }
            } catch (Exception e) {
                if (webinfos == null) {
                    webinfos = new ArrayList<>();
                }
                e.printStackTrace();
            }

            try {
                sql = "select tt.* " +
                        "from ( " +
                        "        select distinct CONCAT(COALESCE(em.uri,''), COALESCE(emv.code,'')) as id " +
                        "                        ,em.uri as resid " +
                        "                        ,emv.code as value " +
                        "                        ,emv.name as name " +
                        "          from iuap_metadata_base.md_enumeration em " +
                        "          join iuap_apdoc_basedoc.bd_cust_enum emv on emv.enumdefcode=em.name " +
                        ") tt " +
                        "order by tt.resid,tt.name ";
                indicatorShow("正在一次性查询元数据枚举列表(7/11)..." + sql);
                enumValueDTOList = (List<EnumValueDTO>) cache.getIfPresent(sql);
                if (enumValueDTOList == null) {
                    rs = st.executeQuery(sql);
                    enumValueDTOList =
                            new VOArrayListResultSetExtractor<EnumValueDTO>(EnumValueDTO.class).extractData(rs);
                    cache.put(sql, enumValueDTOList);
                }
                IoUtil.close(rs);
            } catch (Exception e) {
                if (enumValueDTOList == null) {
                    enumValueDTOList = new ArrayList<>();
                }
                e.printStackTrace();
            }

            pks = new ArrayList<>();
            aggFullClasss = new ArrayList<>();

            //读取他们的实体列表和字段列表
            for (int i = 0; i < entityList.size(); i++) {
                ClassExtInfoDTO e = entityList.get(i);
                indicatorShow(String.format("正在吃奶玩命的渲染实体(第%s个/共计%s个/剩余%s个):%s %s "
                        , i + 1
                        , entityList.size()
                        , entityList.size() - i - 1
                        , e.getId()
                        , e.getDisplayName()));
                if (indicator.isCanceled()) {
                    return agg;
                }

                //indicatorShow(String.format("正在加载元数据组件(10/11)...%s - %s", e.getName(), e.getDisplayName()));

                loadSearchComponentVO(agg, e, st);
            }

            agg.setProjectName(getProject().getName());
            agg.setNcHome(ProjectNCConfigUtil.getNCHomePath(getProject()));

            //注意！！！ 这里 必须 拷贝一份！！！ 因为 下面 会 clear() 掉 compomentIdMap，
            //comps 如果 直接拿 map.values() 是个视图， clear 后 也 就 空了， 会导致 后面 模块树 为 空！
            List<SearchComponentVO> comps = new ArrayList<>(agg.getCompomentIdMap().values());
            Iterator<SearchComponentVO> componentVOIterator = comps.iterator();
            while (componentVOIterator.hasNext()) {
                SearchComponentVO c = componentVOIterator.next();
                if (CollUtil.isEmpty(c.getClassDTOS())) {
                    componentVOIterator.remove();
                    continue;
                }

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

            agg.getCompomentIdMap().clear();
            for (SearchComponentVO c : comps) {
                agg.getCompomentIdMap().put(c.getId(), c);
            }

            //modules 只支持 1层！！！ 完全 来自 实际 选中 的 实体 所 直属 的 组件模块，
            //与 comps 数量 无关， 且 每个 模块 只 出现 一次！！！
            agg.getAllModules().clear();
            Map<String, DataDictionaryAggVO.Module> id2Module = new LinkedHashMap<>();
            for (ClassDTO ca : agg.getClassMap().values()) {
                String componentId = ca.getComponentID();
                if (StrUtil.isBlank(componentId) || id2Module.containsKey(componentId)) {
                    continue;
                }

                DataDictionaryAggVO.Module module = agg.getId2ModuleMap().get(componentId);
                if (module == null) {
                    //兜底： 理论上 loadSearchComponentVO 里 已经建过了
                    SearchComponentVO c = agg.getCompomentIdMap().get(componentId);
                    module = new DataDictionaryAggVO.Module();
                    module.setId(componentId);
                    module.setName(componentId);
                    module.setDisplayname(c == null || StrUtil.isBlank(c.getDisplayName())
                            ? componentId : c.getDisplayName());
                    agg.getId2ModuleMap().put(module.getId(), module);
                }

                //只支持 1层， 所以 他 必须 是 根节点！！！
                module.setParentmoduleid(null);
                id2Module.put(componentId, module);
                agg.getAllModules().add(module);
            }
            agg.setModules(new ArrayList<>(id2Module.values()));

            return agg;
        } finally {
            IoUtil.close(st);
            IoUtil.close(conn);
        }
    }

    public void loadSearchComponentVO(DataDictionaryAggVO agg, ClassExtInfoDTO entity, Statement st) throws SQLException {
        try {
            if (CollUtil.isNotEmpty(entity.getPerperties())) {
                // 已经加载过
                return;
            }

            if (indicator == null) {
                indicator = new EmptyProgressIndicatorImpl();
            }

            SearchComponentVO2 comp = (SearchComponentVO2) agg.getCompomentIdMap().get(entity.getComponentID());
            if (comp == null) {
                comp = md_componentList.stream()
                        .filter(m -> entity.getComponentID().startsWith(m.getId()))
                        .findAny()
                        .orElse(null);
            }

            if (comp == null) {
                comp = unknowModel;
                agg.getCompomentIdMap().put(unknowModel.getId(), comp);
            }

            entity.setComponentID(comp.getId());
            entity.setParamvalue(StrUtil.format(
                    "领域:{},微服务:{},{}"
                    , StringUtil.get(comp.getNamespace())
                    , StringUtil.get(comp.getDisplayName())
                    , StringUtil.get(comp.getFilePath())
            ));
            entity.setFun_name(entity.getId());

            ResultSet rs = null;
            SearchComponentVO2 com = comp;
            if (com.getClassDTOS() == null) {
                com.setClassDTOS(new LinkedList<>());
            }
            if (!com.getClassDTOS().contains(entity)) {
                com.getClassDTOS().add(entity);
            }

            //模块 只支持 1层！！！ 直接 用 组件id 作为 模块id， 也就是 modules 里 存的是 实体 直属 的 上一层 组件模块， 不再 往 上 module 归属！！！
            DataDictionaryAggVO.Module m = agg.getId2ModuleMap().get(com.getId());
            if (m == null) {
                m = new DataDictionaryAggVO.Module();
                m.setId(com.getId());
                m.setName(com.getId());
                m.setDisplayname(StrUtil.blankToDefault(com.getDisplayName(), com.getId()));
                // m.setMetas(new ArrayList<>());
                agg.getId2ModuleMap().put(m.getId(), m);
            }

            //只支持 1层， 所以 他 必须 是 根节点！！！ 否则 toTree 会 把他 当 某模块 的 子节点，
            //导致 modules 里 拿不到 他！
            m.setParentmoduleid(null);
            entity.setResid(StringUtil.get(entity.getResid()));
            //单据类型 一个实体 可能对应 多个， 所以 这里 要全部过滤出来 然后 同名字段 英文逗号拼接！
            List<Map<String, Object>> billTypeList = billTypes.stream()
                    .filter(pk -> entity.getResid().equals(pk.get("component")))
                    .collect(Collectors.toList());

            // m.getMetas().add(com);
            if (CollUtil.isNotEmpty(billTypeList)) {
                copy2VOWithJoin(billTypeList, entity);
            }

            //节点信息 一个实体 可能对应 多个， 所以 这里 要全部过滤出来 然后 同名字段 英文逗号拼接！
            List<Map<String, Object>> webinfoList = webinfos.stream()
                    .filter(pk -> entity.getId().equals(pk.get("id")))
                    .collect(Collectors.toList());
            if (CollUtil.isNotEmpty(webinfoList)) {
                copy2VOWithJoin(webinfoList, entity);
            }

            if (indicator.isCanceled()) {
                return;
            }

            //indicatorShow(String.format("正在填充元数据组件实体的属性等信息(11/11)...%s - %s - %s - %s "
            //        , com.getName()
            //        , com.getDisplayName()
            //        , entity.getName()
            //        , entity.getDisplayName()
            //));

            if (ClassDTO.CLASSTYPE_ENTITY.equals(entity.getClassType())) {
                List<PropertyDTO> ps = propertyDTOList.stream()
                        .filter(p -> entity.getId().equals(p.getClassID()))
                        .collect(Collectors.toList());
                entity.setPerperties(ps);

                //entity.setAggFullClassName((String) aggFullClasss.stream()
                //        .filter(fc -> entity.getId().equals(fc.get("id")))
                //        .findAny()
                //        .orElse(new HashMap<>())
                //        .get("paravalue")
                //);

                if (m.getChilds() == null) {
                    m.setChilds(new ArrayList<>());
                }
                m.getChilds().add(DataDictionaryAggVO.Module.builder()
                        .id(entity.getId())
                        .type(V.get(entity.getClassType(), ClassDTO.CLASSTYPE_ENTITY))
                        .name(entity.getName())
                        .defaultTableName(entity.getDefaultTableName())
                        .displayname(entity.getDisplayName())
                        .fullClassName(entity.getFullClassName())
                        .aggFullClassName(entity.getAggFullClassName())
                        .build());
            } else if (ClassDTO.CLASSTYPE_ENUMERATE.equals(entity.getClassType())) {
                List<EnumValueDTO> ps = enumValueDTOList.stream()
                        .filter(e -> entity.getId().equals(e.getId()))
                        .collect(Collectors.toList());
                agg.getClassId2EnumValuesMap().put(entity.getId(), ps);
            }

            if (CollUtil.isEmpty(entity.getPerperties())) {
                return;
            }

            HashSet<String> idFields = new HashSet<>();
            Map<String, Object> pkMap = pks.stream()
                    .filter(pk -> entity.getDefaultTableName().equals(pk.get("tableid")) || entity.getId().equals(pk.get(
                            "tableid")))
                    .findAny()
                    .orElse(null);
            if (pkMap != null) {
                idFields.add((String) pkMap.get("name"));
            } else {
                idFields.add("id");
            }

            for (PropertyDTO p : entity.getPerperties()) {
                if (indicator.isCanceled()) {
                    return;
                }

                p.setTypeName(PropertyDataTypeEnum.ofTypeDefualt(p.getDataType()).getTypeName());
                p.setFieldType(PropertyDataTypeEnum.ofTypeDefualt(p.getDataType()).getFieldType());
                p.setFileTypeDesc(p.getTypeName());
                p.setTypeDisplayName(PropertyDataTypeEnum.ofTypeDefualt(p.getDataType()).getTypeDisplayName());
                p.setRefModelDesc(p.getTypeDisplayName() + " (" + p.getFieldType() + ')');
                // p.setFieldName(p.getName());

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
                ClassDTO refc = agg.getClassMap().get(p.getRefModelName());
                p.setRefModelName(StringUtil.get(p.getRefModelName(), ""));
                if (refc == null) {//也许是枚举！
                    //枚举!
                    List<EnumValueDTO> vs = agg.getClassId2EnumValuesMap().get(p.getRefModelName());
                    if (vs == null) {
                        vs = enumValueDTOList.stream()
                                .filter(e -> p.getRefModelName().equals(e.getResid()))
                                .collect(Collectors.toList());
                        agg.getClassId2EnumValuesMap().put(p.getRefModelName(), vs);
                    }

                    if (CollUtil.isNotEmpty(vs)) {
                        for (EnumValueDTO v : vs) {
                            v.setIndustry(null);
                        }
                        vs.sort((a, b) -> StringUtil.get(a.getValue()).compareTo(StringUtil.get(b.getValue())));

                        p.setDescription(JSON.toJSONString(vs));
                        p.setRefModelDesc("枚举");
                        p.setRefModelName(null);
                        continue;
                    }
                }

                if (false && refc == null) {
                    SearchComponentVO2 cmt = md_componentList.stream()
                            .filter(e -> e.getId().equals(p.getRefModelName()))
                            .findAny()
                            .orElse(null);
                    if (cmt != null) {
                        // loadSearchComponentVO(agg, refc, st);
                    }
                }

                refc = agg.getClassMap().get(p.getRefModelName());
                if (false && refc == null && StringUtil.isNotBlank(p.getRefModelName())) {
                    String key = "补充查询实体:BIPQJB:" + ncDataSourceVO.getDatabaseUrl() + ":" + p.getRefModelName();
                    Optional d = (Optional) cache.getIfPresent(key);
                    if (d == null) {
                        //查一次数据库看看情况
                        String sql = entitySql + " and mmc.uri = '" + p.getRefModelName() + "' ";
                        indicatorShow("正在补充查询实体..." + sql);
                        rs = st.executeQuery(sql);
                        ArrayList<ClassExtInfoDTO> nclss =
                                new VOArrayListResultSetExtractor<ClassExtInfoDTO>(ClassExtInfoDTO.class).extractData(rs);
                        IoUtil.close(rs);
                        if (CollUtil.notEmpty(nclss)) {
                            refc = nclss.get(0);
                        }

                        cache.put(key, Optional.ofNullable(refc));
                    } else {
                        refc = (ClassDTO) d.orElse(null);
                    }

                    if (refc != null) {
                        agg.getClassMap().put(p.getRefModelName(), refc);
                        loadSearchComponentVO(agg, (ClassExtInfoDTO) refc, st);
                    }
                }

                if (refc == null) {
                    p.setRefModelDesc(p.getRefModelName() + " (引用的其他实体 但是找不到此实体信息!) " + p.getDataType());
                    p.setRefModelName(null);
                    continue;
                } else {
                    p.setDataType(refc.getId());
                }

                p.setRefModelDesc(String.format(
                        "%s(%s %s)"
                        , refc.getDisplayName()
                        , refc.getName()
                        , simpleClassName(refc.getFullClassName())
                ));
            }

            entity.getPerperties().sort((a, b) -> {
                //注意！！！ 比较器 必须 满足 自反/反对称/传递 三个 契约！！！
                //原来 的 写法 在 a 是默认排序号 而 b 不是时 返回 1， 但反向 比较 却 返回 0，
                //违反 反对称性， TimSort 会 抛 Comparison method violates its general contract!
                boolean aDefaultOrdnum = a.getOrdernum() == 1000086;
                boolean bDefaultOrdnum = b.getOrdernum() == 1000086;

                //非 默认排序号 的 排 前面
                if (aDefaultOrdnum != bDefaultOrdnum) {
                    return aDefaultOrdnum ? 1 : -1;
                }

                //都 不是 默认排序号， 按 排序号 升序
                if (!aDefaultOrdnum) {
                    return Integer.compare(a.getOrdernum(), b.getOrdernum());
                }

                //都 是 默认排序号， 按 字段名 升序（做 null 保护， 保证 一致性）
                String nameA = a.getName();
                String nameB = b.getName();
                if (nameA == null && nameB == null) {
                    return 0;
                }
                if (nameA == null) {
                    return 1;
                }
                if (nameB == null) {
                    return -1;
                }

                return nameA.compareTo(nameB);
            });
        } finally {
        }
    }

    /**
     * 把 多个 map 里的 同名字段 的值 全部取出来， 去重后 用 英文逗号 拼接， 再设置到 vo 里！ <br>
     * 比如 一个实体 对应 多个单据类型、多个节点 的时候！ <br>
     * <br>
     *
     * @param maps 符合条件的所有 map
     * @param tovo 目标 vo， 字段名 忽略大小写 且忽略下划线
     */
    public static void copy2VOWithJoin(List<Map<String, Object>> maps, Object tovo) {
        if (CollUtil.isEmpty(maps) || tovo == null) {
            return;
        }

        //字段名(小写) -> 该字段所有不重复的值， 按出现顺序
        Map<String, LinkedHashSet<String>> field2Values = new LinkedHashMap<>();

        for (Map<String, Object> map : maps) {
            if (CollUtil.isEmpty(map)) {
                continue;
            }

            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (entry.getKey() == null || entry.getValue() == null) {
                    continue;
                }

                String value = String.valueOf(entry.getValue()).trim();
                if (value.isEmpty()) {
                    continue;
                }

                field2Values.computeIfAbsent(entry.getKey().toLowerCase()
                        , k -> new LinkedHashSet<>()
                ).add(value);
            }
        }

        for (Map.Entry<String, LinkedHashSet<String>> entry : field2Values.entrySet()) {
            LinkedHashSet<String> values = entry.getValue();
            if (CollUtil.isEmpty(values)) {
                continue;
            }

            try {
                ReflectUtil.setFieldValueAutoConvertIgnoreNotHasField(tovo
                        , entry.getKey()
                        , String.join(",", values)
                );
            } catch (Throwable e) {
                //字段不存在 或者 类型不匹配 就忽略他！
            }
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
