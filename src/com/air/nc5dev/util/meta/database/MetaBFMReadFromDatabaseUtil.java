package com.air.nc5dev.util.meta.database;

import com.air.nc5dev.util.CollUtil;
import com.air.nc5dev.util.IoUtil;
import com.air.nc5dev.util.jdbc.resulthandel.VOArrayListResultSetExtractor;
import com.air.nc5dev.util.meta.IMetaBMFReader;
import com.air.nc5dev.vo.meta.ComponentAggVO;
import com.air.nc5dev.vo.meta.ComponentDTO;
import com.intellij.openapi.project.Project;
import lombok.Data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

/**
 * 从数据库 查询 元数据组件信息 <br>
 * <br>
 * <br>
 *
 * @author 唐粟 Email:209308343@qq.com 微信yongyourj
 * @date 2023/10/19 0019 12:13
 * @project
 * @Version
 */
@Data
public class MetaBFMReadFromDatabaseUtil implements IMetaBMFReader<String> {
    Connection con = null;

    public MetaBFMReadFromDatabaseUtil(Connection con) {
        this.con = con;
    }

    /**
     * @param source  bmf组件的 componentId
     * @param project
     * @return
     * @throws Exception
     */
    @Override
    public ComponentAggVO readAggVO(String source, Project project) throws Exception {
        ComponentDTO componentVO = queryVO(source);
        return queryVOAgg(componentVO, project);
    }

    /**
     * @param componentid md_component的id
     * @return
     * @throws SQLException
     */
    public ComponentAggVO queryVOAgg(ComponentDTO componentVO, Project project) throws SQLException {
        if (componentVO == null) {
            return null;
        }
        String componentid = componentVO.getId();
        ComponentAggVO agg = ComponentAggVO.builder()
                .componentVO(componentVO)
                .classVOlist(new QueryClassVOListUtil(con).queryVOs(componentid))
                .enumValueVOList(new HashMap<>())
                .propertyVOlist(new HashMap<>())
                .bizItfMapVOlist(new HashMap<>())
                .bizItfImplMapVoList(new HashMap<>())
                .accessorParaVOList(new HashMap<>())
                .build();

        //计算出 需要逻辑计算出来的标签们
        agg.buildAll(con, project);

        return agg;
    }

    /**
     * @param componentid md_component的id
     * @return
     * @throws SQLException
     */
    public ComponentDTO queryVO(String componentid) throws SQLException {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = con.prepareStatement("select * from md_component where id=?");

            st.setObject(1, componentid);

            rs = st.executeQuery();

            return CollUtil.getFirst(new VOArrayListResultSetExtractor<ComponentDTO>(ComponentDTO.class).extractData(rs));
        } finally {
            IoUtil.close(rs);
            IoUtil.close(st);
        }
    }
}
