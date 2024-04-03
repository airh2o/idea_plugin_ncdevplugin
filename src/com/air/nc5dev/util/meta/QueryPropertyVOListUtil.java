package com.air.nc5dev.util.meta;

import com.air.nc5dev.util.IoUtil;
import com.air.nc5dev.util.jdbc.resulthandel.VOArrayListResultSetExtractor;
import com.air.nc5dev.vo.meta.PropertyDTO;
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
public class QueryPropertyVOListUtil {
    Connection con = null;

    public QueryPropertyVOListUtil(Connection con) {
        this.con = con;
    }

    /**
     * @param componentid md_class 的 id
     * @return
     * @throws SQLException
     */
    public List<PropertyDTO> queryVOs(String classid) throws SQLException {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = con.prepareStatement("select * from md_property where classid=?");

            st.setObject(1, classid);

            rs = st.executeQuery();

            ArrayList<PropertyDTO> vs = new VOArrayListResultSetExtractor<PropertyDTO>(PropertyDTO.class).extractData(rs);

            if (vs == null) {
                vs = new ArrayList<>();
            }

            for (PropertyDTO v : vs) {
                v.fixDisplays();
            }

            return vs;
        } finally {
            IoUtil.close(rs);
            IoUtil.close(st);
        }
    }
}
