package com.air.nc5dev.util.meta.database;

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
                    if (v.getIsPrimary() == null) {
                        v.setIsPrimary(Boolean.FALSE);
                    }

                    if (v.getIsSource() == null) {
                        v.setIsSource(false);
                    }

                    v.setDbtype(CollUtil.asMap("BS000010000100001004", "int").get(v.getDataType()));

                    if (StringUtil.isBlank(v.getDbtype())) {
                        v.setDbtype("varchar");
                        v.setTypeDisplayName("String");
                    } else {
                        v.setTypeDisplayName("Integer");
                    }

                    if (v.getTypeName() == null) {
                        v.setTypeName(v.getTypeDisplayName());
                    }

                    if (v.getIsCreateSQL() == null) {
                        v.setIsCreateSQL(null);
                    }
                }
            }

            return vs;
        } finally {
            IoUtil.close(rs);
            IoUtil.close(st);
        }
    }

    public String getCompomentIDByClassId(String classId) throws SQLException {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = con.prepareStatement("select componentid from md_class where id=?");

            st.setObject(1, classId);

            rs = st.executeQuery();

            if (rs.next()) {
                return rs.getString(1);
            }
        } finally {
            IoUtil.close(rs);
            IoUtil.close(st);
        }
        return null;
    }
}
