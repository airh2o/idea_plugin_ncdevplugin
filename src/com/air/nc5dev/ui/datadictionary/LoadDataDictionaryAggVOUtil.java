package com.air.nc5dev.ui.datadictionary;

import cn.hutool.core.util.StrUtil;
import com.air.nc5dev.util.*;
import com.air.nc5dev.util.jdbc.ConnectionUtil;
import com.air.nc5dev.util.jdbc.resulthandel.VOArrayListResultSetExtractor;
import com.air.nc5dev.util.meta.consts.PropertyDataTypeEnum;
import com.air.nc5dev.vo.DataDictionaryAggVO;
import com.air.nc5dev.vo.NCDataSourceVO;
import com.air.nc5dev.vo.meta.ClassDTO;
import com.air.nc5dev.vo.meta.EnumValueDTO;
import com.air.nc5dev.vo.meta.PropertyDTO;
import com.air.nc5dev.vo.meta.SearchComponentVO;
import com.alibaba.fastjson.JSON;
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

    public LoadDataDictionaryAggVOUtil(Project project, NCDataSourceVO ncDataSourceVO) {
        this.project = project;
        this.ncDataSourceVO = ncDataSourceVO;
    }

    public DataDictionaryAggVO read() throws SQLException, ClassNotFoundException {
        agg = new DataDictionaryAggVO();
        agg.setClassId2EnumValuesMap(new HashMap<>());
        agg.setCompomentIdMap(new HashMap<>());

        Connection conn = ConnectionUtil.getConn(ncDataSourceVO);
        Statement st = conn.createStatement();
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

        rs = st.executeQuery("select id, name ,displayname, parentmoduleid from md_module");
        ArrayList<DataDictionaryAggVO.Module> allModules = new VOArrayListResultSetExtractor<DataDictionaryAggVO.Module>
                (DataDictionaryAggVO.Module.class).extractData(rs);
        IoUtil.close(rs);
        List<DataDictionaryAggVO.Module> modules = V.toTree(allModules, "id", "parentmoduleid", "childs");

        //读取元数据了
        rs = st.executeQuery(compomentSql);
        ArrayList<SearchComponentVO> coms = new VOArrayListResultSetExtractor<SearchComponentVO>(SearchComponentVO.class).extractData(rs);
        IoUtil.close(rs);

        //读取他们的实体列表和字段列表
        LinkedList<ClassDTO> allClasss = new LinkedList<>();
        Map<String, DataDictionaryAggVO.Module> id2ModuleMap = allModules.stream()
                .collect(Collectors.toMap(DataDictionaryAggVO.Module::getId, m -> m, (m1, m2) -> m2));
        for (SearchComponentVO com : coms) {
            rs = st.executeQuery("select id,name,displayName,fullClassName,componentID,classType,parentClassID from md_class where componentid='" + com.getId() + "' ");
            ArrayList<ClassDTO> cs = new VOArrayListResultSetExtractor<ClassDTO>(ClassDTO.class).extractData(rs);
            IoUtil.close(rs);

            com.setClassDTOS(cs);
            allClasss.addAll(cs);

            DataDictionaryAggVO.Module m = id2ModuleMap.get(com.getOwnModule());
            if (m == null) {
                m = new DataDictionaryAggVO.Module();
                m.setId(com.getOwnModule());
                m.setName(com.getOwnModule());
                m.setDisplayname(com.getOwnModule());
                m.setMetas(new ArrayList<>());
                id2ModuleMap.put(m.getId(), m);
                modules.add(m);
            }

            m.getMetas().add(com);

            for (ClassDTO c : cs) {
                if (ClassDTO.CLASSTYPE_ENTITY.equals(c.getClassType())) {
                    rs = st.executeQuery("select * from md_property where classid='" + c.getId() + "' ");
                    ArrayList<PropertyDTO> ps = new VOArrayListResultSetExtractor<PropertyDTO>(PropertyDTO.class).extractData(rs);
                    IoUtil.close(rs);

                    c.setPerperties(ps);

                    rs = st.executeQuery("select paravalue from md_accessorpara where id='" + c.getId() + "' ");
                    if (rs.next()) {
                        c.setAggFullClassName(rs.getString(1));
                    }
                    IoUtil.close(rs);
                } else if (ClassDTO.CLASSTYPE_ENUMERATE.equals(c.getClassType())) {
                    rs = st.executeQuery("select * from md_enumvalue where id='" + c.getId() + "' ");
                    ArrayList<EnumValueDTO> ps = new VOArrayListResultSetExtractor<EnumValueDTO>(EnumValueDTO.class).extractData(rs);
                    IoUtil.close(rs);

                    agg.getClassId2EnumValuesMap().put(c.getId(), ps);
                }

                if (m.getChilds() == null) {
                    m.setChilds(new ArrayList<>());
                }
                m.getChilds().add(DataDictionaryAggVO.Module.builder()
                        .id(c.getId())
                        .type(V.get(c.getClassType(), ClassDTO.CLASSTYPE_ENTITY))
                        .name(c.getName() + " " + simpleClassName(c.getFullClassName()))
                        .displayname(c.getDisplayName())
                        .build());
            }
        }

        agg.setProjectName(getProject().getName());
        agg.setNcHome(ProjectNCConfigUtil.getNCHomePath(getProject()));
        agg.setModules(modules);
        agg.setClassMap(allClasss.stream().collect(Collectors.toMap(ClassDTO::getId, c -> c, (c1, c2) -> c2)));
        for (ClassDTO c : allClasss) {
            if (CollUtil.isEmpty(c.getPerperties())) {
                continue;
            }

            for (PropertyDTO p : c.getPerperties()) {
                p.setFileTypeDesc(p.getTypeName());
                p.setRefModelDesc(p.getTypeDisplayName() + " (" + p.getFieldType() + ')');

                if (PropertyDataTypeEnum.BS000010000100001051.is(p.getDataType())) {
                    p.setRefModelDesc("当前表主键:字符串 (String)");
                    p.setRefModelName(null);
                    continue;
                }

                if (StrUtil.isBlank(p.getRefModelName())) {
                    if (p.getAttrLength() != null && p.getAttrLength() != 0) {
                        p.setFileTypeDesc(p.getTypeName() + " (" + p.getAttrLength() + ')');
                    }
                    p.setRefModelName(null);
                    continue;
                }

                //引用的其他元数据！！！
                ClassDTO refc = agg.getClassMap().get(p.getDataType());
                if (refc == null) {
                    p.setRefModelDesc(p.getRefModelDesc() + " (引用的其他实体 但是找不到此实体信息!) " + p.getRefModelName());
                    p.setRefModelName(null);
                    continue;
                }

                if (ClassDTO.CLASSTYPE_ENUMERATE.equals(refc.getClassType())) {
                    //枚举!
                    List<EnumValueDTO> vs = agg.getClassId2EnumValuesMap().get(refc.getId());
                    if (vs == null) {
                        p.setRefModelDesc(String.format(
                                "%s(%s %s) 这是枚举实体 但是找不到注册的值"
                                , refc.getDisplayName()
                                , refc.getName()
                                , simpleClassName(refc.getFullClassName())
                        ));
                        continue;
                    }

                    String s = "";
                    for (EnumValueDTO v : vs) {
                        s += v.getValue() + "=" + v.getName() + ";<br/>";
                    }
                    p.setDescription(s);
                }

                p.setRefModelDesc(String.format(
                        "%s(%s %s)"
                        , refc.getDisplayName()
                        , refc.getName()
                        , simpleClassName(refc.getFullClassName())
                ));
            }
        }

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
    }

    public String simpleClassName(String c) {
        if (c == null) {
            return "";
        }

        return c.substring(c.lastIndexOf('.') + 1);
    }
}
