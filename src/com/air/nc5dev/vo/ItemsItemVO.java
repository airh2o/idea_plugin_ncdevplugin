package com.air.nc5dev.vo;

import com.air.nc5dev.util.XmlUtil;
import lombok.Builder;
import lombok.Data;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
    String itemKey;
    String itemName;
    String itemRule;
    String sysField;
    String corpField;
    String grpField;
    String fixedWhere;

    public static List<ItemsItemVO> read(File xml) {
        List<ItemsItemVO> vs = read0(xml);
        vs.addAll(read0(new File(xml.getParentFile(), "items_idea.xml")));
        return vs;
    }

    private static List<ItemsItemVO> read0(File xml) {
        if (!xml.isFile()) {
            return new ArrayList<>();
        }
        Document doc = XmlUtil.xmlFile2Document2(xml);
        NodeList is = doc.getElementsByTagName("item");
        if (is == null || is.getLength() < 1) {
            return new ArrayList<>();
        }

        List<ItemsItemVO> vs = new ArrayList<>(is.getLength() << 1);

        for (int i = 0; i < is.getLength(); i++) {
            Node n = is.item(i);
            vs.add(ItemsItemVO.builder()
                    .itemKey(getValue(n, "itemKey"))
                    .itemName(getValue(n, "itemName"))
                    .itemRule(getValue(n, "itemRule"))
                    .sysField(getValue(n, "sysField"))
                    .corpField(getValue(n, "corpField"))
                    .grpField(getValue(n, "grpField"))
                    .fixedWhere(getValue(n, "fixedWhere"))
                    .build());
        }

        return vs;
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
