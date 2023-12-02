package com.air.nc5dev.ui.datadictionary;

import com.air.nc5dev.util.CollUtil;
import com.air.nc5dev.util.IoUtil;
import com.air.nc5dev.util.ProjectNCConfigUtil;
import com.air.nc5dev.util.V;
import com.air.nc5dev.util.jdbc.ConnectionUtil;
import com.air.nc5dev.util.jdbc.resulthandel.VOArrayListResultSetExtractor;
import com.air.nc5dev.vo.DataDictionaryAggVO;
import com.air.nc5dev.vo.NCDataSourceVO;
import com.air.nc5dev.vo.meta.ClassDTO;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
@AllArgsConstructor
public class LoadDataDictionaryAggVOUtil {
    Project project;
    NCDataSourceVO ncDataSourceVO;
    DataDictionaryAggVO agg;

    public String toHtml(DataDictionaryAggVO agg) {
        String template = "";


        return template
                .replace("{{'aggJsonString':'aggJsonString'}}", JSON.toJSONString(agg))
                .replace("{{TreeHtml}}", buildTreeHtml(agg))
                ;
    }

    private CharSequence buildTreeHtml(DataDictionaryAggVO agg) {
        StringBuilder tree = new StringBuilder(10000);
        for (DataDictionaryAggVO.Module m : agg.getModules()) {
            tree.append(buildTreeHtml(m));
        }
        return tree.toString();
    }

    private CharSequence buildTreeHtml(DataDictionaryAggVO.Module m) {
        StringBuilder tree = new StringBuilder(10000);
        StringBuilder childDir = new StringBuilder(10000);
        if (CollUtil.isNotEmpty(m.getChilds())) {
            for (DataDictionaryAggVO.Module c : m.getChilds()) {
                childDir.append(buildTreeHtml(c));
            }
        }

        tree.append(String.format(
                " <div id=\"%s\" class=\"card section fade scale slide-in-up-100 slide-in-right-50 show in choosed open\" >\n" +
                        "          <div class=\"card-heading\" title=\"%s\">\n" +
                        "              <i class=\"icon icon-tablet\"></i>\n" +
                        "              <h5><a class=\"name\" onclick=\"memu1click('%s')\">%s</a></h5>\n" +
                        "          </div>\n" +
                        "          <div class=\"card-content\">\n" +
                        "                  %s \n" +
                        "                  %s" +
                        "          </div>" +
                        "</div>"
                , m.getId()
                , m.getName()
                , m.getId()
                , m.getDisplayname()
                , childDir
                , buildTreeHtml0(m.getMetas())
        ));
        return tree.toString();
    }

    private CharSequence buildTreeHtml0(List<SearchComponentVO> metas) {
        if (CollUtil.isEmpty(metas)) {
            return "";
        }

        StringBuilder tree = new StringBuilder(10000);
        for (SearchComponentVO m : metas) {
            tree.append(String.format(
                    "<ul class=\"topics\">\n" +
                            "    <li><a onclick=\"memu2click('%s')\" id=\"%s\">%s</a></li>\n" +
                            "</ul>\n"
                    , m.getId()
                    , m.getName()
                    , m.getDisplayName()
            ));
        }
        return tree.toString();
    }

    public DataDictionaryAggVO read() throws SQLException, ClassNotFoundException {
        agg = new DataDictionaryAggVO();

        Connection conn = ConnectionUtil.getConn(ncDataSourceVO);
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery("select version from sm_product_version where versionn is not null");
        if (rs.next()) {
            agg.setNcVersion(rs.getString(1));
        }
        IoUtil.close(rs);

        rs = st.executeQuery("select id, name ,displayname, parentmoduleid from md_module");
        ArrayList<DataDictionaryAggVO.Module> allModules = new VOArrayListResultSetExtractor<DataDictionaryAggVO.Module>
                (DataDictionaryAggVO.Module.class).extractData(rs);
        IoUtil.close(rs);
        List<DataDictionaryAggVO.Module> modules = V.toTree(allModules, "id", "parentmoduleid", "childs");

        //读取元数据了
        rs = st.executeQuery("select * from md_component ");
        ArrayList<SearchComponentVO> coms = new VOArrayListResultSetExtractor<SearchComponentVO>(SearchComponentVO.class).extractData(rs);
        IoUtil.close(rs);

        //读取他们的实体列表和字段列表
        Map<String, DataDictionaryAggVO.Module> id2ModuleMap = allModules.stream().collect(Collectors.toMap(DataDictionaryAggVO.Module::getId, m -> m));
        for (SearchComponentVO com : coms) {
            rs = st.executeQuery("select * from md_class where componentid='" + com.getId() + "' ");
            ArrayList<ClassDTO> cs = new VOArrayListResultSetExtractor<ClassDTO>(ClassDTO.class).extractData(rs);
            IoUtil.close(rs);

            com.setClassDTOS(cs);

            for (ClassDTO c : cs) {
                rs = st.executeQuery("select * from md_property where classid='" + c.getId() + "' ");
                ArrayList<PropertyDTO> ps = new VOArrayListResultSetExtractor<PropertyDTO>(PropertyDTO.class).extractData(rs);
                IoUtil.close(rs);

                c.setPerperties(ps);
            }

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
        }

        agg.setProjectName(getProject().getName());
        agg.setNcHome(ProjectNCConfigUtil.getNCHomePath(getProject()));
        agg.setModules(modules);
        return agg;
    }

    public String toHtml() throws SQLException, ClassNotFoundException {
        return toHtml(read());
    }


}
