package com.air.nc5dev.util;

import cn.hutool.core.io.FileUtil;
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
import java.util.List;
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
        Document document = str2Dom4jXml(str);
        Element root = document.getRootElement();
        if (root == null) {
            return document;//连root都没有,拿我穷开心哦
        }
        String CID = root.attributeValue("id");
        UUID uuid = UUID.randomUUID();
        str = str.replace(CID, uuid.toString());//替换BMF 文件ID
        Element cell = root.element("celllist");
        if (cell != null) {
            List<Element> list = cell.elements("entity");
            if (list != null && list.size() > 0) {
                for (Element en : list) {
                    String ID = en.attributeValue("id");
                    uuid = UUID.randomUUID();
                    str = str.replace(ID, uuid.toString());//替换实体ID
                    Element attlist = en.element("attributelist");
                    List<Element> atts = attlist.elements("attribute");
                    if (atts != null && atts.size() > 0) {
                        for (Element element : atts) {
                            ID = element.attributeValue("id");
                            uuid = UUID.randomUUID();
                            str = str.replace(ID, uuid.toString());//替换实体字段ID
                        }
                    }
                }
            }
            List<Element> Reference = cell.elements("Reference");//实体与接口之间的映射关系
            if (Reference != null && Reference.size() > 0) {
                for (Element element : Reference) {
                    String ID = element.attributeValue("id");
                    uuid = UUID.randomUUID();
                    str = str.replace(ID, uuid.toString());//替换实体与接口之间的映射关系ID
                }
            }
            List<Element> Enumerate = cell.elements("Enumerate");//枚举
            if (Enumerate != null && Enumerate.size() > 0) {
                for (Element en : Enumerate) {
                    String ID = en.attributeValue("id");
                    uuid = UUID.randomUUID();
                    str = str.replace(ID, uuid.toString());//替换枚举ID
                    Element attlist = en.element("enumitemlist");
                    List<Element> atts = attlist.elements("enumitem");
                    if (atts != null && atts.size() > 0) {
                        for (Element element : atts) {
                            ID = element.attributeValue("id");
                            uuid = UUID.randomUUID();
                            str = str.replace(ID, uuid.toString());//替换枚举字段ID
                        }
                    }
                }
            }

            List<Element> busiIterface = cell.elements("busiIterface");//自定义接口
            if (busiIterface != null && busiIterface.size() > 0) {
                for (Element en : busiIterface) {
                    String ID = en.attributeValue("id");
                    uuid = UUID.randomUUID();
                    str = str.replace(ID, uuid.toString());//替换接口ID
                    Element attlist = en.element("busiItfAttrs");
                    List<Element> atts = attlist.elements("busiItAttr");
                    if (atts != null && atts.size() > 0) {
                        for (Element element : atts) {
                            ID = element.attributeValue("id");
                            uuid = UUID.randomUUID();
                            str = str.replace(ID, uuid.toString());//替换接口字段ID
                        }
                    }
                }
            }

            List<Element> notecell = cell.elements("notecell");//注释
            if (notecell != null && notecell.size() > 0) {
                for (Element en : notecell) {
                    String ID = en.attributeValue("id");
                    uuid = UUID.randomUUID();
                    str = str.replace(ID, uuid.toString());//替换注释ID
                }
            }
        }
        return document;
    }
}
