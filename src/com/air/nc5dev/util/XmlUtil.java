package com.air.nc5dev.util;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

/**
 * XML tool
 */
public final class XmlUtil extends cn.hutool.core.util.XmlUtil{
    /**
     * XML 文件 解析成 Document
     * @param xml
     * @return 如果异常返回null
     */
    public static final Document xmlFile2Document(File xml){
        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document parse = documentBuilder.parse(xml);
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
