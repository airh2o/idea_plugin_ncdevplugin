package com.air.nc5dev.ui.datadictionary;

import cn.hutool.core.util.StrUtil;
import com.air.nc5dev.util.*;
import com.air.nc5dev.util.jdbc.ConnectionUtil;
import com.air.nc5dev.util.jdbc.resulthandel.VOArrayListResultSetExtractor;
import com.air.nc5dev.util.meta.consts.PropertyDataTypeEnum;
import com.air.nc5dev.vo.DataDictionaryAggVO;
import com.air.nc5dev.vo.NCDataSourceVO;
import com.air.nc5dev.vo.meta.*;
import com.alibaba.fastjson.JSON;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

/**
 * </br>
 * </br>
 * </br>
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
    String compomentSql = "select * from md_component";
    ProgressIndicator indicator;

    public LoadDataDictionaryAggVOUtil(Project project, NCDataSourceVO ncDataSourceVO) {
        this.project = project;
        this.ncDataSourceVO = ncDataSourceVO;
    }

    public DataDictionaryAggVO read() throws SQLException, ClassNotFoundException {
        agg = new DataDictionaryAggVO();
        agg.setClassId2EnumValuesMap(new HashMap<>());
        agg.setCompomentIdMap(new HashMap<>());
        agg.setClassMap(new HashMap<>());

        Connection conn = null;
        Statement st = null;
        try {
            conn = ConnectionUtil.getConn(ncDataSourceVO);
            st = conn.createStatement();
            ResultSet rs = st.executeQuery("select version from sm_product_version where version is not null");
            if (rs.next()) {
                agg.setNcVersion(rs.getString(1));
            }
            IoUtil.close(rs);

            rs = st.executeQuery("select name from org_group");
            if (rs.next()) {
                agg.setGroupName(rs.getString(1));
            }
            IoUtil.close(rs);

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
            if (indicator != null) {
                indicator.setText2(String.format("正在查询模块列表...%s", sql));
            }
            rs = st.executeQuery(sql);
            ArrayList<DataDictionaryAggVO.Module> allModules = new VOArrayListResultSetExtractor<DataDictionaryAggVO.Module>
                    (DataDictionaryAggVO.Module.class).extractData(rs);
            IoUtil.close(rs);
            List<DataDictionaryAggVO.Module> modules = V.toTree(allModules, "id", "parentmoduleid", "childs");
            Map<String, DataDictionaryAggVO.Module> id2ModuleMap = allModules.stream()
                    .collect(Collectors.toMap(DataDictionaryAggVO.Module::getId, m -> m, (m1, m2) -> m2));
            agg.setId2ModuleMap(id2ModuleMap);
            agg.setAllModules(allModules);

            //读取元数据了
            if (indicator != null) {
                indicator.setText2(String.format("正在查询元数据组件列表...%s", compomentSql));
            }
            rs = st.executeQuery(compomentSql);
            ArrayList<SearchComponentVO2> coms = new VOArrayListResultSetExtractor<SearchComponentVO2>(SearchComponentVO2.class).extractData(rs);
            IoUtil.close(rs);

            //读取他们的实体列表和字段列表
            for (SearchComponentVO com : coms) {
                if (indicator != null && indicator.isCanceled()) {
                    return agg;
                }

                if (indicator != null) {
                    indicator.setText2(String.format("正在加载元数据组件...%s - %s", com.getName(), com.getDisplayName()));
                }

                loadSearchComponentVO(agg, com, st);
            }

            agg.setProjectName(getProject().getName());
            agg.setNcHome(ProjectNCConfigUtil.getNCHomePath(getProject()));
            agg.setModules(modules);

            agg.getCompomentIdMap().clear();
            for (SearchComponentVO c : coms) {
                if (c.getClassDTOS() != null) {
                    ArrayList<ClassDTO> ncas = new ArrayList<>();
                    for (ClassDTO ca : c.getClassDTOS()) {
                        ClassDTO nca = new ClassDTO();
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

    public void loadSearchComponentVO(DataDictionaryAggVO agg, SearchComponentVO c, Statement st) throws SQLException {
        try {
            if (agg.getCompomentIdMap().get(c.getId()) != null) {
                return;
            }

            String sql = compomentSql
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

            SearchComponentVO com = null;
            ResultSet rs = null;

            if (StringUtil.isBlank(c.getName())) {
                rs = st.executeQuery(sql.substring(0, sql.indexOf(" where "))
                        + " where id='" + c.getId() + "' "
                );
                com = CollUtil.getFirst(new VOArrayListResultSetExtractor<SearchComponentVO2>(SearchComponentVO2.class).extractData(rs));
                IoUtil.close(rs);
            } else {
                com = c;
            }

            if (com == null) {
                return;
            }

            agg.getCompomentIdMap().put(com.getId(), com);

            //读取他们的实体列表和字段列表
            if (indicator != null) {
                indicator.setText2(String.format("正在查询元数据组件实体列表...%s - %s", com.getName(), com.getDisplayName()));
            }
            LinkedList<ClassDTO> allClasss = new LinkedList<>();
            sql = "select id,name,displayname,fullclassname,componentid,classtype,parentclassid,defaulttablename from md_class where componentid='"
                    + com.getId() + "' and classtype=201 ";
            rs = st.executeQuery(sql);
            ArrayList<ClassDTO2> cs = new VOArrayListResultSetExtractor<ClassDTO2>(ClassDTO2.class).extractData(rs);
            IoUtil.close(rs);

            com.setClassDTOS(cs);
            allClasss.addAll(cs);

            DataDictionaryAggVO.Module m = agg.getId2ModuleMap().get(com.getOwnModule());
            if (m == null) {
                m = new DataDictionaryAggVO.Module();
                m.setId(com.getOwnModule());
                m.setName(com.getOwnModule());
                m.setDisplayname(com.getOwnModule());
                // m.setMetas(new ArrayList<>());
                agg.getId2ModuleMap().put(m.getId(), m);
            }

            // m.getMetas().add(com);
            //立即放入map中，防止下个元数据 又依赖他的实体！
            for (ClassDTO2 cla : cs) {
                agg.getClassMap().put(cla.getId(), cla);
            }

            for (ClassDTO2 cla : cs) {
                if (indicator != null && indicator.isCanceled()) {
                    return;
                }

                if (indicator != null) {
                    indicator.setText2(String.format("正在查询元数据组件实体的属性等信息...%s - %s - %s - %s "
                            , com.getName()
                            , com.getDisplayName()
                            , cla.getName()
                            , cla.getDisplayName()
                    ));
                }

                if (ClassDTO.CLASSTYPE_ENTITY.equals(cla.getClassType())) {
                    rs = st.executeQuery("select name,displayname,nullable,refmodelname,defaultvalue,description ,attrlength,datatype" +
                            " from md_property where classid='" + cla.getId() + "' ");
                    ArrayList<PropertyDTO> ps = new VOArrayListResultSetExtractor<PropertyDTO>(PropertyDTO.class).extractData(rs);
                    IoUtil.close(rs);

                    cla.setPerperties(ps);

                    rs = st.executeQuery("select paravalue from md_accessorpara where id='" + cla.getId() + "' ");
                    if (rs.next()) {
                        cla.setAggFullClassName(rs.getString(1));
                    }
                    IoUtil.close(rs);

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
                    rs = st.executeQuery("select  value,name  from md_enumvalue where id='" + cla.getId() + "' ");
                    ArrayList<EnumValueDTO> ps = new VOArrayListResultSetExtractor<EnumValueDTO>(EnumValueDTO.class).extractData(rs);
                    IoUtil.close(rs);

                    agg.getClassId2EnumValuesMap().put(cla.getId(), ps);
                }

                if (CollUtil.isEmpty(cla.getPerperties())) {
                    continue;
                }

                HashSet<String> idFields = new HashSet<>();
                rs = st.executeQuery(String.format("select name from md_column where tableid='%s' and pkey='Y'", cla.getDefaultTableName()));
                while (rs.next()) {
                    idFields.add(rs.getString(1));
                }
                IoUtil.close(rs);
                for (PropertyDTO p : cla.getPerperties()) {
                    if (indicator != null && indicator.isCanceled()) {
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
                            rs = st.executeQuery("select value,name from md_enumvalue where id='" + p.getDataType() + "' ");
                            vs = new VOArrayListResultSetExtractor<EnumValueDTO>(EnumValueDTO.class).extractData(rs);
                            IoUtil.close(rs);

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
                        String refccid = null;
                        rs = st.executeQuery("select componentid  from md_class where id ='" + p.getDataType() + "'");
                        if (rs.next()) {
                            refccid = rs.getString(1);
                        }
                        IoUtil.close(rs);

                        if (refccid != null) {
                            SearchComponentVO refcc = new SearchComponentVO();
                            refcc.setId(refccid);
                            loadSearchComponentVO(agg, refcc, st);
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

    public String simpleClassName(String c) {
        if (c == null) {
            return "";
        }

        return c.substring(c.lastIndexOf('.') + 1);
    }
}