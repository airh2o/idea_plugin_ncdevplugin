package com.air.nc5dev.ui.datadictionary;

import cn.hutool.core.util.StrUtil;
import com.air.nc5dev.enums.NcVersionEnum;
import com.air.nc5dev.util.CollUtil;
import com.air.nc5dev.util.EmptyProgressIndicatorImpl;
import com.air.nc5dev.util.IoUtil;
import com.air.nc5dev.util.ProjectNCConfigUtil;
import com.air.nc5dev.util.ReflectUtil;
import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.util.V;
import com.air.nc5dev.util.idea.LogUtil;
import com.air.nc5dev.util.jdbc.ConnectionUtil;
import com.air.nc5dev.util.jdbc.resulthandel.ArrayListMapLowerResultSetExtractor;
import com.air.nc5dev.util.jdbc.resulthandel.VOArrayListResultSetExtractor;
import com.air.nc5dev.util.meta.consts.PropertyDataTypeEnum;
import com.air.nc5dev.vo.DataDictionaryAggVO;
import com.air.nc5dev.vo.NCDataSourceVO;
import com.air.nc5dev.vo.meta.ClassDTO;
import com.air.nc5dev.vo.meta.ClassExtInfoDTO;
import com.air.nc5dev.vo.meta.EnumValueDTO;
import com.air.nc5dev.vo.meta.PropertyDTO;
import com.air.nc5dev.vo.meta.SearchComponentVO;
import com.air.nc5dev.vo.meta.SearchComponentVO2;
import com.alibaba.fastjson.JSON;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import lombok.Data;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
public class LoadDataDictionaryAggVOUtil {
    Project project;
    NCDataSourceVO ncDataSourceVO;
    DataDictionaryAggVO agg;
    ProgressIndicator indicator;
    NcVersionEnum ncVersion;
    List<SearchComponentVO2> md_componentList;
    List<ClassExtInfoDTO> fieldList;
    ArrayListMapLowerResultSetExtractor arrayListMapLowerResultSetExtractor = new ArrayListMapLowerResultSetExtractor();
    List<Map<String, Object>> billTypes;
    String defaulttablename = "defaulttablename";
    String attrlength = "attrlength";
    String dynamic = "dynamicattr";
    String classOrderBy = " order by attrsequence ";
    List<Map<String, Object>> webinfos;
    List<Map<String, Object>> aggFullClasss;
    List<Map<String, Object>> pks;
    List<PropertyDTO> propertyDTOList;
    List<EnumValueDTO> enumValueDTOList;
    String compomentSql = "select * from md_component";
    static Cache<Object, Object> cache;

    static {
    //    cache = CacheBuilder.newBuilder().expireAfterWrite(20, TimeUnit.MINUTES).build();
    }

    public LoadDataDictionaryAggVOUtil(Project project, NCDataSourceVO ncDataSourceVO) {
        this.project = project;
        this.ncDataSourceVO = ncDataSourceVO;
    }

    public DataDictionaryAggVO read() throws SQLException, ClassNotFoundException {
        agg = new DataDictionaryAggVO();
        agg.setClassId2EnumValuesMap(new HashMap<>());
        agg.setCompomentIdMap(new HashMap<>());
        agg.setClassMap(new HashMap<>());
        if (indicator == null) {
            indicator = new EmptyProgressIndicatorImpl();
        }

        Connection conn = null;
        Statement st = null;
        try {
            ncVersion = ProjectNCConfigUtil.getNCVersion(getProject());
            conn = ConnectionUtil.getConn(ncDataSourceVO);
            st = conn.createStatement();
            ResultSet rs = null;
            try {
                rs = st.executeQuery("select version from sm_product_version where version is not null");
                if (rs.next()) {
                    agg.setNcVersion(rs.getString(1));
                }
                IoUtil.close(rs);
            } catch (SQLException e) {
                //????
            }

            try {
                rs = st.executeQuery("select name from org_group");
                if (rs.next()) {
                    agg.setGroupName(rs.getString(1));
                }
                IoUtil.close(rs);
                if (NcVersionEnum.U8Cloud.equals(ncVersion)
                        || NcVersionEnum.NC5.equals(ncVersion)) {
                    ncVersion = NcVersionEnum.NCC;
                }
            } catch (Throwable e) {
                //U8C or NC5x
                try {
                    agg.setNcVersion(ProjectNCConfigUtil.getNCVersion(getProject()).name() + " " + StringUtil.get(agg.getNcVersion()));
                    rs = st.executeQuery("select unitname from bd_corp order by ts");
                    if (rs.next()) {
                        agg.setGroupName(rs.getString(1));
                    }
                    IoUtil.close(rs);
                    if (NcVersionEnum.NC6.equals(ncVersion)
                            || NcVersionEnum.NCC.equals(ncVersion)
                            || NcVersionEnum.BIP.equals(ncVersion)) {
                        ncVersion = NcVersionEnum.NC5;
                    }
                } catch (Throwable ex) {
                    //??????
                }
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

            sql = "select id, name ,displayname, parentmoduleid from md_module where id in(select ownmodule "
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
            List<DataDictionaryAggVO.Module> modules = V.toTree(allModules, "id", "parentmoduleid", "childs");
            Map<String, DataDictionaryAggVO.Module> id2ModuleMap = allModules.stream()
                    .collect(Collectors.toMap(DataDictionaryAggVO.Module::getId, m -> m, (m1, m2) -> m2));
            agg.setId2ModuleMap(id2ModuleMap);
            agg.setAllModules(allModules);

            //读取元数据了
            indicatorShow(String.format("正在一次性查询元数据组件列表(2/11)...%s", compomentSql));
            rs = st.executeQuery(compomentSql);
            md_componentList = new VOArrayListResultSetExtractor<SearchComponentVO2>(SearchComponentVO2.class).extractData(rs);
            IoUtil.close(rs);

            if (NcVersionEnum.U8Cloud.equals(ncVersion)
                    || NcVersionEnum.NC5.equals(ncVersion)) {
                defaulttablename = "(select name from md_table where id=md_class.id)";
                attrlength = "length";
                dynamic = " 'false' ";
                classOrderBy = " order by sequence ";
            }
            sql = String.format(
                    "select id,name,displayname,fullclassname,componentid,classtype,parentclassid,%s defaulttablename" +
                            ",componentid from md_class "
                    , defaulttablename
            );
            indicatorShow("正在一次性查询元数据字段列表(3/11)..." + sql);
            rs = st.executeQuery(sql);
            fieldList = new VOArrayListResultSetExtractor<ClassExtInfoDTO>(ClassExtInfoDTO.class).extractData(rs);
            IoUtil.close(rs);

            try {
                sql = "select pk_billtypecode,billtypename,nodecode, component from " +
                        "bd_billtype" +
                        " where istransaction='N'  " +
                        " union all\n" +
                        " select pk_billtypecode,billtypename,nodecode,component from bd_billtype  ";
                if (NcVersionEnum.isNCCOrBIP(ncVersion)
                        || NcVersionEnum.NC6.equals(ncVersion)) {
                    sql = "select b.pk_billtypecode,b.billtypename,b.nodecode,n.fun_name,np.paramvalue,component " +
                            "from bd_billtype b\n" +
                            "left join sm_funcregister n on n.funcode=b.nodecode\n" +
                            "left join sm_paramregister np on np.parentid=n.cfunid and np .paramname='BeanConfigFilePath'\n" +
                            "where istransaction='N'  " +
                            "union all\n" +
                            "select b.pk_billtypecode,b.billtypename,b.nodecode,n.fun_name,np.paramvalue,component from" +
                            " bd_billtype b\n" +
                            "left join sm_funcregister n on n.funcode=b.nodecode\n" +
                            "left join sm_paramregister np on np.parentid=n.cfunid and np.paramname='BeanConfigFilePath'\n" +
                            "where 1=1 ";
                }
                indicatorShow("正在一次性查询单据类型列表(4/11)..." + sql);
                rs = st.executeQuery(sql);
                billTypes = arrayListMapLowerResultSetExtractor.extractData(rs);
                IoUtil.close(rs);
            } catch (Exception e) {
                if (billTypes == null) {
                    billTypes = new ArrayList<>();
                }
            }

            try {
                sql = "select p.pagecode,p.pageurl,c.id from md_class c\n" +
                        "join sm_appregister a on c.id=a.mdid\n" +
                        "join sm_apppage p on p.parent_id=a.pk_appregister and p.isdefault='Y'  " +
                        "union all " +
                        "select p.pagecode,p.pageurl,c.id from md_class c " +
                        "join sm_appregister a on c.id=a.mdid\n" +
                        "join sm_apppage p on p.parent_id=a.pk_appregister  ";
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
                sql = String.format(
                        "select name,displayname,nullable,refmodelname,defaultvalue,description " +
                                ",datatype,calculation,%s dynamic" +
                                ",%s attrlength , classid " +
                                " from md_property where 1=1 %s  "
                        , dynamic
                        , attrlength
                        , classOrderBy
                );
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
                sql = "select  value,name,id  from md_enumvalue";
                indicatorShow("正在一次性查询元数据枚举列表(7/11)..." + sql);
                rs = st.executeQuery(sql);
                enumValueDTOList = new VOArrayListResultSetExtractor<EnumValueDTO>(EnumValueDTO.class).extractData(rs);
                IoUtil.close(rs);
            } catch (Exception e) {
                if (enumValueDTOList == null) {
                    enumValueDTOList = new ArrayList<>();
                }
            }

            try {
                if (NcVersionEnum.U8Cloud.equals(ncVersion)
                        || NcVersionEnum.NC5.equals(ncVersion)) {
                    sql = "select name,tableid from md_column where  pkey='Y'";
                } else {
                    sql = "select name,tableid from md_column where  pkey='Y'";
                }
                indicatorShow("正在一次性查询元数据主键列表(8/11)..." + sql);
                rs = st.executeQuery(sql);
                pks = arrayListMapLowerResultSetExtractor.extractData(rs);
                IoUtil.close(rs);
            } catch (Exception e) {
                if (pks == null) {
                    pks = new ArrayList<>();
                }
            }

            try {
                sql = "select paravalue,id from md_accessorpara ";
                indicatorShow("正在一次性查询元数据java类列表(9/11)..." + sql);
                rs = st.executeQuery(sql);
                aggFullClasss = arrayListMapLowerResultSetExtractor.extractData(rs);
                IoUtil.close(rs);
            } catch (Exception e) {
                if (aggFullClasss == null) {
                    aggFullClasss = new ArrayList<>();
                }
            }

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

                if (ClassDTO.CLASSTYPE_ENTITY.equals(cla.getClassType())
                        && NcVersionEnum.isNCCOrBIP(ncVersion)) {
                    Map<String, Object> webinfo = webinfos.stream()
                            .filter(pk -> cla.getId().equals(pk.get("id")))
                            .findAny()
                            .orElse(null);
                    if (CollUtil.isNotEmpty(webinfo)) {
                        ReflectUtil.copy2VO(webinfo, cla);
                    }
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
