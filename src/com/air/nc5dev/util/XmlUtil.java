package com.air.nc5dev.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * XML tool
 */
public final class XmlUtil extends cn.hutool.core.util.XmlUtil {
    /**
     * XML 文件 解析成 Document
     *
     * @param xml
     * @return 如果异常返回null
     */
    public static final Document xmlFile2DocumentSax(File xml) {
        try {
            SAXReader saxReader = new SAXReader();
            Document document = saxReader.read(xml);
            return document;
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * XML 文件 解析成 Document
     *
     * @param xml
     * @return 如果异常返回null
     */
    public static final org.w3c.dom.Document xmlFile2Document2(InputStream xml) {
        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            org.w3c.dom.Document parse = documentBuilder.parse(xml);
            return parse;
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * XML 文件 解析成 Document
     *
     * @param xml
     * @return 如果异常返回null
     */
    public static final org.w3c.dom.Document xmlFile2Document2(File xml) {
        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            org.w3c.dom.Document parse = documentBuilder.parse(xml);
            return parse;
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private XmlUtil() {
    }

    /**
     * String 转 XML
     *
     * @param xmlStr
     * @return thx
     * 2013-3-4
     */
    public static org.dom4j.Document str2Dom4jXml(String xmlStr) {// Str是传入的一段XML内容的字符串
        org.dom4j.Document document = null;
        try {
            byte[] b = xmlStr.getBytes("UTF-8");
            if (b != null && b.length > 0 && b[0] < 0) {
                //>>>>>>>>>>>>>>该文件保存格式带BOM信息,所以截取掉前3个头信息<<<<<<<<<<<<<<<<
                xmlStr = new String(b, 3, b.length - 3, "UTF-8");
            }

        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }

        try {
            document = org.dom4j.DocumentHelper.parseText(xmlStr);// DocumentHelper.parseText(str)
            // 这个方法将传入的XML字符串转换处理后返回一个Document对象
            document.setXMLEncoding("utf-8");
        } catch (DocumentException e) {
            //System.out.println("可能是因为被复制的bmf文件,保存格式是UTF-8+BOM, 用EditPlus,另存为 UTF-8.");
            e.printStackTrace();
        }
        return document;
    }

    /**
     * 重置 元数据的各种id
     *
     * @param old
     * @return
     */
    public static Document resetMeateIds(File old) {
        String str = FileUtil.readUtf8String(old);
        org.dom4j.Document doc = BmfUtil.str2Xml(str);
        if (doc == null) {
            //System.out.println("读取XML后创建dom对象失败!");
            return null;
        }
        Element root = doc.getRootElement();
        if (root == null) {
            return null;//连root都没有,拿我穷开心哦
        }

        HashMap<String, String> replateMap = new HashMap<>(100);
        UUID uuid = UUID.randomUUID();
        String rootID = uuid.toString();
        replateMap.put(root.attributeValue("id"), rootID);  //替换BMF 文件ID

        String mainEntityIdOld = root.attributeValue("mainEntity");
        Element celllist = root.element("celllist");
        if (celllist != null) {
            List<Element> entityNodeList = celllist.elements("entity");
            if (CollUtil.isNotEmpty(entityNodeList)) {
                for (int i = 0; i < entityNodeList.size(); i++) {
                    Element entity = entityNodeList.get(i);
                    String entityIdOld = entity.attributeValue("id");
                    uuid = UUID.randomUUID();
                    replateMap.put(entityIdOld, uuid.toString());//替换实体ID

                    Element attributelist = entity.element("attributelist");
                    List<Element> attributeNodeList = attributelist.elements("attribute");
                    if (attributeNodeList != null && attributeNodeList.size() > 0) {
                        for (Element attribute : attributeNodeList) {
                            String attributeId = attribute.attributeValue("id");
                            uuid = UUID.randomUUID();
                            replateMap.put(attributeId, uuid.toString());//替换实体字段ID
                        }
                    }
                }
            }

            List<Element> Reference = celllist.elements("Reference");//实体与接口之间的映射关系
            if (Reference != null && Reference.size() > 0) {
                for (Element element : Reference) {
                    String ID = element.attributeValue("id");
                    uuid = UUID.randomUUID();
                    replateMap.put(ID, uuid.toString());//替换实体与接口之间的映射关系ID
                }
            }

            List<Element> Enumerate = celllist.elements("Enumerate");//枚举
            if (Enumerate != null && Enumerate.size() > 0) {
                for (Element en : Enumerate) {
                    String ID = en.attributeValue("id");
                    uuid = UUID.randomUUID();
                    replateMap.put(ID, uuid.toString());//替换枚举ID
                    Element attlist = en.element("enumitemlist");
                    List<Element> atts = attlist.elements("enumitem");
                    if (atts != null && atts.size() > 0) {
                        for (Element element : atts) {
                            ID = element.attributeValue("id");
                            uuid = UUID.randomUUID();
                            replateMap.put(ID, uuid.toString());//替换枚举字段ID
                        }
                    }
                }
            }

            List<Element> busiIterface = celllist.elements("busiIterface");//自定义接口
            if (busiIterface != null && busiIterface.size() > 0) {
                for (Element en : busiIterface) {
                    String ID = en.attributeValue("id");
                    uuid = UUID.randomUUID();
                    replateMap.put(ID, uuid.toString());//替换接口ID
                    Element attlist = en.element("busiItfAttrs");
                    List<Element> atts = attlist.elements("busiItAttr");
                    if (atts != null && atts.size() > 0) {
                        for (Element element : atts) {
                            ID = element.attributeValue("id");
                            uuid = UUID.randomUUID();
                            replateMap.put(ID, uuid.toString());//替换接口字段ID
                        }
                    }
                }
            }

            List<Element> notecell = celllist.elements("notecell");//注释
            if (notecell != null && notecell.size() > 0) {
                for (Element en : notecell) {
                    String ID = en.attributeValue("id");
                    uuid = UUID.randomUUID();
                    replateMap.put(ID, uuid.toString());//替换注释ID
                }
            }
        }

        //>>>>>>>>>>>>>>>>分割线,下面是root 下的另外一个节点集合<<<<<<<<<<<<<<<<<<
        Element connectlist = root.element("connectlist");//实体与其他接口,实体的关系集合
        if (connectlist != null) {
            List<Element> bus = connectlist.elements("busiitfconnection");//实体与接口的连接线
            if (bus != null && bus.size() > 0) {
                for (Element element : bus) {
                    String ID = element.attributeValue("id");
                    uuid = UUID.randomUUID();
                    replateMap.put(ID, uuid.toString());//替换实体与接口的连接线ID
                }
            }
            List<Element> Agg = connectlist.elements("AggregationRelation");//实体与实体的连接线
            if (Agg != null && Agg.size() > 0) {
                for (Element element : Agg) {
                    String ID = element.attributeValue("id");
                    uuid = UUID.randomUUID();
                    replateMap.put(ID, uuid.toString());//替换实体与实体的连接线ID
                }
            }

            List<Element> note = connectlist.elements("noteConnection");//实体与注释的连接线
            if (note != null && note.size() > 0) {
                for (Element element : note) {
                    String id = element.attributeValue("id");
                    uuid = UUID.randomUUID();
                    replateMap.put(id, uuid.toString());//替换实体与注释的连接线ID
                }
            }
        }

        str = BmfUtil.writ2Str(doc);

        Set<String> keys = replateMap.keySet();
        for (String key : keys) {
            str = StrUtil.replace(str, key, replateMap.get(key));
        }

        doc = BmfUtil.str2Xml(str);//根据替换后的XML内容重新生成DOM对象

        return doc;
    }
}
