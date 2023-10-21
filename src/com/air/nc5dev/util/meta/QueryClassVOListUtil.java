package com.air.nc5dev.util.meta;

import com.air.nc5dev.util.CollUtil;
import com.air.nc5dev.util.IoUtil;
import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.util.jdbc.resulthandel.VOArrayListResultSetExtractor;
import com.air.nc5dev.vo.meta.ClassDTO;
import lombok.Data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * </br>
 * </br>
 * </br>
 *
 * @author 唐粟 Email:209308343@qq.com 微信yongyourj
 * @date 2023/10/19 0019 12:13
 * @project
 * @Version
 */
@Data
public class QueryClassVOListUtil {
    Connection con = null;

    public QueryClassVOListUtil(Connection con) {
        this.con = con;
    }

    /**
     * @param componentid md_component的id
     * @return
     * @throws SQLException
     */
    public List<ClassDTO> queryVOs(String componentid) throws SQLException {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = con.prepareStatement("select * from md_class where componentid=?");

            st.setObject(1, componentid);

            rs = st.executeQuery();

            ArrayList<ClassDTO> vs = new VOArrayListResultSetExtractor<ClassDTO>(ClassDTO.class).extractData(rs);

            if (vs == null) {
                vs = new ArrayList<>();
            }

            for (ClassDTO v : vs) {
                if (ClassDTO.CLASSTYPE_ENTITY.equals(v.getClassType())) {
                    v.setDataType(v.getReturnType());
                    v.setIsPrimary(Boolean.FALSE);
                    v.setIsSource(false);
                    v.setDbtype(CollUtil.asMap("BS000010000100001004", "int").get(v.getDataType()));
                    if (StringUtil.isBlank(v.getDbtype())) {
                        v.setDbtype("varchar");
                        v.setTypeDisplayName("String");
                    } else {
                        v.setTypeDisplayName("Integer");
                    }
                    v.setTypeName(v.getTypeDisplayName());
                    v.setIsCreateSQL(null);
                }
            }

            return vs;
        } finally {
            IoUtil.close(rs);
            IoUtil.close(st);
        }
    }
}
