package com.air.nc5dev.util.meta.database;

import com.air.nc5dev.util.CollUtil;
import com.air.nc5dev.util.IoUtil;
import com.air.nc5dev.util.ReflectUtil;
import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.util.jdbc.resulthandel.VOArrayListResultSetExtractor;
import com.air.nc5dev.vo.meta.BizItfMapDTO;
import lombok.Data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * busiitfconnection   busimap 标签
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
public class QueryBizItfVOMapVOListUtil {
    Connection con = null;

    public QueryBizItfVOMapVOListUtil(Connection con) {
        this.con = con;
    }

    /**
     * @param componentid md_class 的 id
     * @return
     * @throws SQLException
     */
    public List<BizItfMapDTO> queryVOs(String classid) throws SQLException {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = con.prepareStatement("select * from md_bizitfmap where classid=?");

            st.setObject(1, classid);

            rs = st.executeQuery();

            ArrayList<BizItfMapDTO> bs = new VOArrayListResultSetExtractor<BizItfMapDTO>(BizItfMapDTO.class).extractData(rs);

            if (bs != null) {
                for (BizItfMapDTO b : bs) {
                    b.setSource(StringUtil.get(b.getSource(), b.getRealsource()));
                }
            }

            return bs;
        } finally {
            IoUtil.close(rs);
            IoUtil.close(st);
        }
    }


}
