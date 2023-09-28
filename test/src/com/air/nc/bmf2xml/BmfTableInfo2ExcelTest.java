package com.air.nc.bmf2xml;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.XmlUtil;
import cn.hutool.poi.excel.ExcelWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;

/**
 * 元数据里实体信息和字段 转换成 Excel表信息 </br>
 * </br>
 * </br>
 *
 * @author 唐粟 Email:209308343@qq.com
 * @date 2023/8/30 0030 17:36
 * @project
 * @Version
 */
public class BmfTableInfo2ExcelTest {
    public static void main(String[] args) throws Exception {

        File excel = new File(new File("G:\\temp\\7\\3\\P5.bmf").getParent(), "1.xlsx");

        FileUtil.del(excel);

        t2("G:\\temp\\7\\3\\P5.bmf");
        t2("G:\\temp\\7\\3\\P6.bmf");
        t2("G:\\temp\\7\\3\\P7.bmf");
        t2("G:\\temp\\7\\3\\P8.bmf");
        t2("G:\\temp\\7\\3\\P9.bmf");
        t2("G:\\temp\\7\\3\\P10.bmf");
    }

    private static void t2(String f) throws Exception {
        File excel = new File(new File(f).getParent(), "1.xlsx");

        ExcelWriter ew = cn.hutool.poi.excel.ExcelUtil.getWriter(excel);

        Document document = XmlUtil.readXML(new File(f));

        NodeList entitys = document.getElementsByTagName("entity");

        for (int i = 0; i < entitys.getLength(); i++) {
            System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n");
            Element e = (Element) entitys.item(i);
            NodeList attributes = e.getElementsByTagName("attribute");

            String name = e.getAttribute("displayName") + " (" + e.getAttribute("tableName") + " / " +
                    e.getAttribute("fullClassName") + " )";
            System.out.println(name);

            ew.setSheet(ew.getSheetCount());
            ew.renameSheet(ew.getSheetCount() - 1, e.getAttribute("tableName"));
            ew.getCell(0, 0, true).setCellValue(name);

            System.out.println("字段编码\t字段名称\t字段类型\t字段长度");

            int r = 2;
            int c = 3;
            ew.getCell(c + 0, r + 0, true).setCellValue("字段编码");
            ew.getCell(c + 1, r + 0, true).setCellValue("字段名称");
            ew.getCell(c + 2, r + 0, true).setCellValue("字段类型");
            ew.getCell(c + 3, r++ + 0, true).setCellValue("字段长度");

            for (int x = 0; x < attributes.getLength(); x++) {
                Element a = (Element) attributes.item(x);

                String row = String.format(
                        "%s\t%s\t%s\t%s"
                        , a.getAttribute("fieldName")
                        , a.getAttribute("displayName")
                        , a.getAttribute("fieldType")
                        , a.getAttribute("length")
                );

                ew.getCell(c + 0, r + x, true).setCellValue(a.getAttribute("fieldName"));
                ew.getCell(c + 1, r + x, true).setCellValue(a.getAttribute("displayName"));
                ew.getCell(c + 2, r + x, true).setCellValue(a.getAttribute("fieldType"));
                ew.getCell(c + 3, r + x, true).setCellValue(a.getAttribute("length"));

                System.out.println(row);
            }
        }

        ew.close();

        System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
    }


}
