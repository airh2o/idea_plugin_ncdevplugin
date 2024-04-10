package com.air.nc5dev.util.meta.database;

import com.air.nc5dev.util.IoUtil;
import com.air.nc5dev.util.jdbc.resulthandel.VOArrayListResultSetExtractor;
import com.air.nc5dev.vo.meta.AccessorParameterDTO;
import lombok.Data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
public class QueryAccessorParameterVOListUtil {
    Connection con = null;

    public QueryAccessorParameterVOListUtil(Connection con) {
        this.con = con;
    }

    /**
     * @param componentid md_class 的 id
     * @return
     * @throws SQLException
     */
    public List<AccessorParameterDTO> queryVOs(String classid) throws SQLException {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = con.prepareStatement("select * from md_accessorpara where id=?");

            st.setObject(1, classid);

            rs = st.executeQuery();

            return new VOArrayListResultSetExtractor<AccessorParameterDTO>(AccessorParameterDTO.class).extractData(rs);
        } finally {
            IoUtil.close(rs);
            IoUtil.close(st);
        }
    }
}
