package com.air.nc5dev.vo;

import cn.hutool.core.util.StrUtil;
import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.util.V;
import com.air.nc5dev.util.XmlUtil;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * items.xml 里的一个表
 * </br>
 * </br>
 * </br>
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

    public static List<ItemsItemVO> read(File xml, Project project, Module module) {
        List<ItemsItemVO> vs = read0(xml, project, module);
        vs.addAll(read0(new File(xml.getParentFile(), "items_idea.xml"), project, module));
        return vs;
    }

    private static List<ItemsItemVO> read0(File xml, Project project, Module module) {
        if (!xml.isFile()) {
            return new ArrayList<>();
        }

        HashMap<String, String> vars = Maps.newHashMap();
        vars.put("date", V.nowDate());
        vars.put("datetime", V.nowDateTime());
        vars.put("project", project.getName());
        vars.put("module", module.getName());

        Document doc = XmlUtil.xmlFile2Document2(xml);
        //读取自定义变量
        NodeList varsList = doc.getElementsByTagName("vars");
        if (varsList != null && varsList.getLength() > 0) {
            for (int i = 0; i < varsList.getLength(); i++) {
                Node varsNode = varsList.item(i);
                NodeList varList = ((Element) varsNode).getElementsByTagName("var");
                if (varList == null || varList.getLength() < 1) {
                    continue;
                }

                for (int y = 0; y < varList.getLength(); y++) {
                    Element varNode = (Element) varList.item(y);
                    if (StringUtil.isBlank(varNode.getAttribute("name"))) {
                        continue;
                    }

                    vars.put(varNode.getAttribute("name"), varNode.getTextContent());
                }
            }
        }

        //var可能本身也有变量
        HashMap<String, String> varsFinal = Maps.newHashMap();
        Set<String> keys = vars.keySet();
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
                    .itemName(getValueIfull2EmtpyStr(n, "itemName"))
                    .itemRule(getValueIfull2EmtpyStr(n, "itemRule"))
                    .sysField(getValueIfull2EmtpyStr(n, "sysField"))
                    .corpField(getValueIfull2EmtpyStr(n, "corpField"))
                    .grpField(getValueIfull2EmtpyStr(n, "grpField"))
                    .fixedWhere(getValueIfull2EmtpyStr(n, "fixedWhere"))
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
            }

            vs.add(e);
        }

        return vs;
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
