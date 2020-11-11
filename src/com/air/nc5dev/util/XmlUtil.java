package com.air.nc5dev.util;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

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
     * @param xml
     * @return 如果异常返回null
     */
    public static final org.w3c.dom.Document xmlFile2Document2(File xml){
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
}
