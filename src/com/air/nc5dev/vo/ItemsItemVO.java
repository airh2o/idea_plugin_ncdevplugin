package com.air.nc5dev.vo;

import cn.hutool.core.util.StrUtil;
import com.air.nc5dev.util.ExceptionUtil;
import com.air.nc5dev.util.IoUtil;
import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.util.V;
import com.air.nc5dev.util.XmlUtil;
import com.air.nc5dev.util.idea.LogUtil;
import com.google.common.collect.Maps;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import lombok.Builder;
import lombok.Data;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * items.xml 里的一个表
 * <br>
 * <br>
 * <br>
 *
 * @author 唐粟 Email:209308343@qq.com
 * @date 2021/11/16 0016 11:35
 * @project
 * @Version
 */
@Data
@Builder
public class ItemsItemVO {
    public String itemKey;
    public String itemName;
    public String itemRule;
    public String sysField;
    public String corpField;
    public String grpField;
    public String fixedWhere;
    public String sql;
    public String schema;

    public static List<ItemsItemVO> read(Connection connection, File xml, Project project, Module module) {
        List<ItemsItemVO> vs = read0(connection, xml, project, module);
        // vs.addAll(read0(new File(xml.getParentFile(), "items_idea.xml"), project, module));
        return vs;
    }

    private static List<ItemsItemVO> read0(Connection connection, File xml, Project project, Module module) {
        if (!xml.isFile()) {
            return new ArrayList<>();
        }

        HashMap<String, String> vars = Maps.newHashMap();
        vars.put("date", V.nowDate());
        vars.put("datetime", V.nowDateTime());
        vars.put("project", project.getName());
        vars.put("module", module.getName());

        Document doc = XmlUtil.xmlFile2Document2(xml);

        String schema = null;
        NodeList env = doc.getElementsByTagName("env");
        if (env != null && env.getLength() > 0) {
            Element e = (Element) env.item(0);
            NodeList schemaNode = e.getElementsByTagName("schema");
            if (schemaNode != null && schemaNode.getLength() > 0) {
                Element schemaItem = (Element) schemaNode.item(0);
                schema = schemaItem.getTextContent();
            }
        }

        //读取自定义变量
        Map<String, String> varNodeMap = new HashMap<>();
        Map<String, Element> varSqlNodeMap = new HashMap<>();

        NodeList varsList = doc.getElementsByTagName("vars");
        if (varsList != null && varsList.getLength() > 0) {
            for (int i = 0; i < varsList.getLength(); i++) {
                Node varsNode = varsList.item(i);
                NodeList varList = ((Element) varsNode).getElementsByTagName("var");
                if (varList != null && varList.getLength() > 0) {
                    for (int y = 0; y < varList.getLength(); y++) {
                        Element node = (Element) varList.item(y);
                        if (StringUtil.isBlank(node.getAttribute("name"))) {
                            continue;
                        }
                        varNodeMap.put(node.getAttribute("name"), node.getTextContent());
                    }
                }

                NodeList varSqlList = ((Element) varsNode).getElementsByTagName("var");
                if (varSqlList != null && varSqlList.getLength() > 0) {
                    for (int y = 0; y < varSqlList.getLength(); y++) {
                        Element node = (Element) varSqlList.item(y);
                        if (StringUtil.isBlank(node.getAttribute("name"))) {
                            continue;
                        }
                        varSqlNodeMap.put(node.getAttribute("name"), node);
                    }
                }
            }
        }

        HashMap<String, String> varsFinal = Maps.newHashMap();
        Set<String> keys = vars.keySet();

        //执行varSql公式
        Statement st = null;
        ResultSet rs = null;
        try {
            st = connection.createStatement();

            for (String key : varSqlNodeMap.keySet()) {
                Element sqlele = varSqlNodeMap.get(key);
                String sql = sqlele.getTextContent();
                for (String key2 : keys) {
                    sql = StrUtil.replace(sql, "{" + key2 + "}", vars.get(key2));
                }

                try {
                    rs = st.executeQuery(sql);
                    ArrayList<String> vs = new ArrayList<>();
                    while (rs.next()) {
                        String v = StringUtil.of(rs.getObject(1));
                        vs.add(v);
                    }

                    StringBuilder fv = new StringBuilder(1000);
                    String join = sqlele.getAttribute("join");
                    String itemwarp = sqlele.getAttribute("itemwarp");
                    boolean first = true;
                    for (String v : vs) {
                        if (!first) {
                           fv.append(join);
                        }
                        fv.append(itemwarp).append(v).append(itemwarp);
                        first = false;
                    }

                    vars.put(key, fv.toString());
                    varsFinal.put(key, fv.toString());
                } finally {
                    IoUtil.close(rs);
                }
            }
        } catch (SQLException e) {
            LogUtil.error("解析varsql标签错误", e);
        } finally {
            IoUtil.close(st);
        }

        //执行剩余var标签
        for (String key : keys) {
            String t = vars.get(key);
            for (String key2 : keys) {
                t = StrUtil.replace(t, "{" + key2 + "}", vars.get(key2));
            }
            varsFinal.put(key, t);
        }

        vars = varsFinal;

        //
        NodeList is = doc.getElementsByTagName("item");
        if (is == null || is.getLength() < 1) {
            return new ArrayList<>();
        }

        List<ItemsItemVO> vs = new ArrayList<>(is.getLength() << 1);

        for (int i = 0; i < is.getLength(); i++) {
            Node n = is.item(i);
            ItemsItemVO e = ItemsItemVO.builder()
                    .itemKey(getValueIfull2EmtpyStr(n, "itemKey"))
                    .itemName(getValueTwo(n, "itemName", "itemKey"))
                    .itemRule(getValueTwo(n, "itemRule", "itemKey"))
                    .sysField(getValueIfull2EmtpyStr(n, "sysField"))
                    .corpField(getValueIfull2EmtpyStr(n, "corpField"))
                    .grpField(getValueIfull2EmtpyStr(n, "grpField"))
                    .fixedWhere(getValueIfull2EmtpyStr(n, "fixedWhere"))
                    .sql(getValueIfull2EmtpyStr(n, "sql"))
                    .schema(schema)
                    .build();

            //处理变量填充
            for (String key : keys) {
                e.setItemKey(StrUtil.replace(e.getItemKey(), "{" + key + "}", vars.get(key)));
                e.setItemName(StrUtil.replace(e.getItemName(), "{" + key + "}", vars.get(key)));
                e.setItemRule(StrUtil.replace(e.getItemRule(), "{" + key + "}", vars.get(key)));
                e.setSysField(StrUtil.replace(e.getSysField(), "{" + key + "}", vars.get(key)));
                e.setCorpField(StrUtil.replace(e.getCorpField(), "{" + key + "}", vars.get(key)));
                e.setGrpField(StrUtil.replace(e.getGrpField(), "{" + key + "}", vars.get(key)));
                e.setFixedWhere(StrUtil.replace(e.getFixedWhere(), "{" + key + "}", vars.get(key)));
                e.setSql(StrUtil.replace(e.getSql(), "{" + key + "}", vars.get(key)));
                e.setSchema(StrUtil.replace(e.getSchema(), "{" + key + "}", vars.get(key)));
            }

            vs.add(e);
        }

        return vs;
    }

    public static String getValueTwo(Node n, String nodeName, String ifblackKey) {
        String v = getValue(n, nodeName);
        if (StringUtil.isBlank(v)) {
            return getValueIfull2EmtpyStr(n, ifblackKey);
        }
        return v;
    }

    public static String getValue(Node n, String nodeName, String ifblack) {
        String v = getValue(n, nodeName);
        if (StringUtil.isBlank(v)) {
            return ifblack;
        }
        return v;
    }

    public static String getValueIfull2EmtpyStr(Node n, String nodeName) {
        String v = getValue(n, nodeName);
        if (v == null) {
            return "";
        }
        return v;
    }

    public static String getValue(Node n, String nodeName) {
        if (n == null) {
            return null;
        }

        NodeList ns = n.getChildNodes();
        if (ns == null || ns.getLength() < 1) {
            return null;
        }

        for (int i = 0; i < ns.getLength(); i++) {
            Node t = ns.item(i);
            if (t.getNodeName().equals(nodeName)) {
                return t.getTextContent();
            }
        }

        return null;
    }
}
