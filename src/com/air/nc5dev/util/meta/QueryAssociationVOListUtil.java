package com.air.nc5dev.util.meta;

import com.air.nc5dev.util.IoUtil;
import com.air.nc5dev.util.jdbc.resulthandel.VOArrayListResultSetExtractor;
import com.air.nc5dev.vo.meta.AssociationDTO;
import lombok.Data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * <br>
 * <br>
 * <br>
 *
 * @author 唐粟 Email:209308343@qq.com 微信yongyourj
 * @date 2023/10/19 0019 12:13
 * @project
 * @Version
 */
@Data
public class QueryAssociationVOListUtil {
    Connection con = null;

    public QueryAssociationVOListUtil(Connection con) {
        this.con = con;
    }

    /**
     * @param componentid md_component 的 id
     * @return
     * @throws SQLException
     */
    public List<AssociationDTO> queryVOs(String componentid) throws SQLException {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = con.prepareStatement("select * from md_association where startbeanid in(select id from md_class where componentid=? ) and type='1'");

            st.setObject(1, componentid);

            rs = st.executeQuery();

            ArrayList<AssociationDTO> vs = new VOArrayListResultSetExtractor<AssociationDTO>(AssociationDTO.class).extractData(rs);

            if (vs != null) {
                for (AssociationDTO v : vs) {
                    v.setTarget(v.getEndElementID());
                    v.setSource(v.getStartBeanID());
                    v.setSrcAttributeid(v.getStartElementID());
                }
            }

            return vs;
        } finally {
            IoUtil.close(rs);
            IoUtil.close(st);
        }
    }
}
