package com.air.nc5dev.ui.exportapifoxdoc;

import cn.hutool.core.util.StrUtil;
import com.air.nc5dev.util.CollUtil;
import com.air.nc5dev.util.IoUtil;
import com.air.nc5dev.util.V;
import com.air.nc5dev.util.idea.LogUtil;
import com.air.nc5dev.util.jdbc.ConnectionUtil;
import com.air.nc5dev.util.meta.consts.PropertyDataTypeEnum;
import com.air.nc5dev.vo.meta.ClassDTO;
import com.air.nc5dev.vo.meta.ComponentAggVO;
import com.air.nc5dev.vo.meta.PropertyDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 生成apifox的接口文档json
 */
public class MakeApifoxJsonUtil {
    public static ApifoxObjVO meta(Connection con, ComponentAggVO agg) throws Throwable {
        ApifoxObjVO root = new ApifoxObjVO();

        HashMap<Object, Object> rootps = new HashMap<>();
        root.setProperties(rootps);

        rootps.put("pk_group", FieldVO.builder().title("集团ID(单据所属集团档案)").type("string").description("档案: 集团").build());
        rootps.put("pk_org", FieldVO.builder().title("组织id或编码(单据所属组织档案)").type("string").description("档案: 组织").build());
        rootps.put("cuserid", FieldVO.builder().title("NC用户id或编码").type("string").description("档案: 用户档案").build());
        rootps.put("billCode", FieldVO.builder().title("第三方单据号").type("string").build());
        rootps.put("pk_bill", FieldVO.builder().title("第三方单据id").type("string").description(
                "如果传了值，会根据这个检查单据的唯一性，如果已经有相同的单据，会报错").build());

        ClassDTO head = agg.getClassVOlist().stream()
                .filter(c -> ClassDTO.CLASSTYPE_ENTITY.equals(c.getClassType()) && c.getIsPrimary())
                .findAny().orElse(null);
        if (head != null && CollUtil.isNotEmpty(agg.getPropertyVOlist().get(head.getId()))) {
            ApifoxObjVO jh = new ApifoxObjVO();
            jh.setTitle("单据主表:" + head.getDisplayName());
            HashMap<Object, Object> jhps = new HashMap<>();
            jh.setProperties(jhps);
            rootps.put("head", jh);

            fill(con, root, rootps, agg, head, jh, jhps);
        }

        ApifoxObjVO bodys = new ApifoxObjVO();
        bodys.setTitle("所有类型的子表数据");
        HashMap<Object, Object> bodysps = new HashMap<>();
        bodys.setProperties(bodysps);
        bodys.setRequired(new ArrayList<String>());
        rootps.put("bodys", bodys);

        List<ClassDTO> bodymetas = agg.getClassVOlist().stream()
                .filter(c -> ClassDTO.CLASSTYPE_ENTITY.equals(c.getClassType()) && !c.getIsPrimary())
                .collect(Collectors.toList());

        if (bodymetas != null) {
            for (ClassDTO bodymeta : bodymetas) {
                if (CollUtil.isEmpty(agg.getPropertyVOlist().get(bodymeta.getId()))) {
                    continue;
                }

                ApifoxObjVO BODY = new ApifoxObjVO();
                BODY.setTitle("单据子表行数组: " + bodymeta.getDisplayName());
                BODY.setType("array");
                BODY.setRequired(new ArrayList<String>());
                BODY.getRequired().add(bodymeta.getName());
                bodysps.put(bodymeta.getName(), BODY);

                ApifoxObjVO items = new ApifoxObjVO();
                items.setTitle("子表行数组: " + bodymeta.getDisplayName());
                HashMap<Object, Object> bodyps = new HashMap<>();
                items.setProperties(bodyps);
                items.setRequired(new ArrayList<String>());
                BODY.setItems(items);

                fill(con, root, rootps, agg, bodymeta, items, bodyps);
            }
        }

        return root;
    }

    private static void fill(Connection con, ApifoxObjVO root, HashMap<Object, Object> rootps, ComponentAggVO agg
            , ClassDTO meta, ApifoxObjVO jvo, HashMap<Object, Object> jvops) throws Throwable {
        jvo.setRequired(new ArrayList<String>());
        List<PropertyDTO> as = agg.getPropertyVOlist().get(meta.getId());
        for (PropertyDTO a : as) {
            if (a.getName() == null
                    || a.getName().toLowerCase().startsWith("def")
                    || a.getName().toLowerCase().startsWith("vdef")
                    || a.getName().toLowerCase().startsWith("bvdef")
                    || a.getName().toLowerCase().startsWith("bdef")
            ) {
                continue;
            }

            FieldVO f = FieldVO.builder()
                    .title(a.getDisplayName())
                    .build();

            if (V.isTrue(a.getNullable())) {
                jvo.getRequired().add(a.getName());
            }

            if (PropertyDataTypeEnum.BS000010000100001031.dataType.equals(a.getDataType())
                    || PropertyDataTypeEnum.BS000010000100001052.dataType.equals(a.getDataType())
                    || PropertyDataTypeEnum.BS000010000100001040.dataType.equals(a.getDataType())
            ) {
                f.setType("number");
            } else if (PropertyDataTypeEnum.BS000010000100001004.dataType.equals(a.getDataType())
            ) {
                f.setType("integer");
            } else if (PropertyDataTypeEnum.BS000010000100001032.dataType.equals(a.getDataType())
            ) {
                f.setType("boolean");
                f.setDescription("可以用true false 也可以用字符串 Y  N 表示");
            } else if (PropertyDataTypeEnum.BS000010000100001033.dataType.equals(a.getDataType())
                    || PropertyDataTypeEnum.BS000010000100001039.dataType.equals(a.getDataType())
            ) {
                f.setType("string");
                f.setDescription("日期格式:2020-05-05");
            } else if (PropertyDataTypeEnum.BS000010000100001034.dataType.equals(a.getDataType())
                    || PropertyDataTypeEnum.BS000010000100001034.dataType.equals(a.getDataType())
            ) {
                f.setType("string");
                f.setDescription("日期时间格式:2020-05-06 07:05:05");
            } else if (PropertyDataTypeEnum.BS000010000100001036.dataType.equals(a.getDataType())
                    || PropertyDataTypeEnum.BS000010000100001036.dataType.equals(a.getDataType())
            ) {
                f.setType("string");
                f.setDescription("时间格式:07:05:05");
            } else {
                f.setType("string");
            }

            if (StrUtil.isNotBlank(a.getRefModelName())) {
                PreparedStatement st = null;
                ResultSet rs = null;
                try {
                    String sql = "select displayname||' '||defaulttablename from md_class where id=? ";
                    if (ConnectionUtil.isSqlServer(con)) {
                        sql = "select displayname+' '+defaulttablename from md_class where id=? ";
                    } else if (ConnectionUtil.isGaussDB(con)) {
                        sql = "select concat(displayname,' ',defaulttablename) from md_class where id=? ";
                    }

                    st = con.prepareStatement(sql);
                    st.setObject(1, a.getDataType());
                    rs = st.executeQuery();
                    rs.next();
                    f.setDescription(String.format("档案: %s ", rs.getString(1)));
                } catch (Throwable ex) {
                    LogUtil.error(ex.getMessage(), ex);
                    f.setDescription(String.format("档案: %s ", a.getRefModelDesc()));
                } finally {
                    IoUtil.close(st);
                    IoUtil.close(rs);
                }
            }

            if (a.getDefaultValue() != null) {
                f.setDescription((f.getDescription() == null ? "" : f.getDescription()) + " ,默认值:" + a.getDefaultValue());
            }

            jvops.put(a.getName(), f);
        }
    }

//    public static List<String> insertDocconvertDefdocSqls(Class<? extends SuperVO> head, Class<? extends SuperVO> body
//            , Map<Class, String> pk_defdoclistCode, Class<? extends SuperVO>... extbodys) throws Throwable {
//        ArrayList<String> sqls = new ArrayList<>();
//        JdbcUtil dao = JdbcUtil.me();
//        if (head != null) {
//            SuperVO h = head.newInstance();
//            IVOMeta meta = h.getMetaData();
//            insertDocconvertDefdocSqls(
//                    dao.doSingleResultQuery("select pk_defdoclist from bd_defdoclist where dr=0 and code='" +
//                    pk_defdoclistCode.get(head) + "'")
//                    , sqls, h, meta);
//        }
//
//        if (body != null) {
//            SuperVO b = body.newInstance();
//            IVOMeta meta = b.getMetaData();
//            insertDocconvertDefdocSqls(dao.doSingleResultQuery("select pk_defdoclist from bd_defdoclist where dr=0 " +
//                            "and code='" + pk_defdoclistCode.get(body) + "'")
//                    , sqls, b, meta);
//        }
//
//        if (extbodys != null) {
//            for (Class<? extends SuperVO> bc : extbodys) {
//                SuperVO b = bc.newInstance();
//                IVOMeta meta = b.getMetaData();
//                insertDocconvertDefdocSqls(dao.doSingleResultQuery("select pk_defdoclist from bd_defdoclist where " +
//                                "dr=0 and code='" + pk_defdoclistCode.get(bc) + "'")
//                        , sqls, b, meta);
//            }
//        }
//
//        return sqls;
//    }
//
//    private static void insertDocconvertDefdocSqls(String pk_defdoclist, List<String> sqls, SuperVO vo, IVOMeta
//    meta) throws Throwable {
//        JdbcUtil dao = JdbcUtil.me();
//        ColumnProcessor columnProcessor = new ColumnProcessor();
//        IAttributeMeta[] as = meta.getAttributes();
//        for (IAttributeMeta at : as) {
//            nc.vo.pubapp.pattern.model.meta.entity.vo.Attribute a =
//                    (nc.vo.pubapp.pattern.model.meta.entity.vo.Attribute) at;
//            if (StringUtil.isNotBlank(a.getReferenceDoc())) {
//                String json = StrUtil.format("{\"fieldName\":\"%s\",\"value\":\"%s\"," +
//                                "\"must\":false,\"enableDependon\":true,\"dependOn\":[],\"parmarMap" +
//                                "\":{},\"order\": 100}"
//                        , a.getName()
//                        , dao.doSingleResultQuery("select defaulttablename from md_class where " +
//                                "name='" + a.getReferenceDoc() + "'")
//                );
//
//                sqls.add(StrUtil.format("INSERT INTO BD_DEFDOC (CODE, CREATIONTIME, CREATOR, DATAORIGINFLAG, " +
//                                        "DATATYPE, DR, ENABLESTATE, INNERCODE, MEMO,\n" +
//                                        "                                MNECODE, MODIFIER, NAME,\n" +
//                                        "                                PK_DEFDOC, PK_DEFDOCLIST, PK_GROUP,
//                                        PK_ORG," +
//                                        "\n" +
//                                        "                                SHORTNAME, TS)\n" +
//                                        "VALUES ('%s', '2023-12-09 09:24:11', '1001A1100000000011N9', 0, 1, 0, 2, " +
//                                        "null, " +
//                                        "'%s', '备注输入配置值', '~',\n" +
//                                        "        '%s', '%s', '%s',\n" +
//                                        "        '0001A110000000000E5H', 'GLOBLE00000000000000', '编码名称等随便输入,
//                                        我只读取备注'," +
//                                        "\n" +
//                                        "        '2023-12-09 09:24:18')"
//                                , a.getName()
//                                , json
//                                , a.getLabel()
//                                , dao.newNcId()
//                                , pk_defdoclist
//                        )
//                );
//            }
//        }
//
//
//    }
//

}
