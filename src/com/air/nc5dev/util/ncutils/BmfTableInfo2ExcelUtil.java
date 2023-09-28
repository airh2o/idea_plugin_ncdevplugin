package com.air.nc5dev.util.ncutils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.XmlUtil;
import cn.hutool.poi.excel.ExcelWriter;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.ss.usermodel.BorderExtent;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.util.CellRangeAddress;
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
public class BmfTableInfo2ExcelUtil {
    public static void main(String[] args) throws Exception {
        File excel = new File(new File("G:\\temp\\7\\3\\P5.bmf").getParent(), "1.xlsx");

        FileUtil.del(excel);

        toExcel(new File("G:\\temp\\7\\3\\P5.bmf"), new File("G:\\temp\\7\\3\\1.xlsx"));
    }

    public static void toExcel(File bmf, File excel) throws Exception {
        ExcelWriter ew = cn.hutool.poi.excel.ExcelUtil.getWriter(excel);

        Document document = XmlUtil.readXML(bmf);

        NodeList entitys = document.getElementsByTagName("entity");

        CellStyle setBorder = ew.createCellStyle();
        setBorder.setBorderBottom(BorderStyle.MEDIUM); //下边框
        setBorder.setBorderLeft(BorderStyle.MEDIUM);//左边框
        setBorder.setBorderTop(BorderStyle.MEDIUM);//上边框
        setBorder.setBorderRight(BorderStyle.MEDIUM);//右边框
        setBorder.setWrapText(true);//设置自动换行

        for (int i = 0; i < entitys.getLength(); i++) {
            System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n");
            Element e = (Element) entitys.item(i);
            NodeList attributes = e.getElementsByTagName("attribute");

            String name = e.getAttribute("displayName") + " (" + e.getAttribute("tableName") + " / " +
                    e.getAttribute("fullClassName") + " )";
            System.out.println(name);

            ew.setSheet(ew.getSheetCount());
            ew.renameSheet(ew.getSheetCount() - 1, e.getAttribute("displayName"));
            ew.getCell(0, 0, true).setCellValue(name);

            System.out.println("字段编码\t字段名称\t字段类型\t字段长度\t参照档案\t参照名称");

            int r = 2;
            int c = 3;
            ew.getCell(c + 0, r + 0, true).setCellValue("字段编码");
            ew.getCell(c + 1, r + 0, true).setCellValue("字段名称");
            ew.getCell(c + 2, r + 0, true).setCellValue("字段类型");
            ew.getCell(c + 3, r + 0, true).setCellValue("字段长度");
            ew.getCell(c + 4, r + 0, true).setCellValue("参照档案");
            ew.getCell(c + 5, r + 0, true).setCellValue("参照名称");

            ew.getCell(c + 0, r + 0, true).setCellStyle(setBorder);
            ew.getCell(c + 1, r + 0, true).setCellStyle(setBorder);
            ew.getCell(c + 2, r + 0, true).setCellStyle(setBorder);
            ew.getCell(c + 3, r + 0, true).setCellStyle(setBorder);
            ew.getCell(c + 4, r + 0, true).setCellStyle(setBorder);
            ew.getCell(c + 5, r + 0, true).setCellStyle(setBorder);

            // 设置列宽
            ew.setColumnWidth(c + 0, 10);
            ew.setColumnWidth(c + 1, 30);
            ew.setColumnWidth(c + 2, 10);
            ew.setColumnWidth(c + 3, 10);
            ew.setColumnWidth(c + 4, 30);
            ew.setColumnWidth(c + 5, 30);

            ++r;
            for (int x = 0; x < attributes.getLength(); x++) {
                Element a = (Element) attributes.item(x);

                String row = String.format(
                        "%s\t%s\t%s\t%s\t%s\t%s"
                        , a.getAttribute("fieldName")
                        , a.getAttribute("displayName")
                        , a.getAttribute("fieldType")
                        , a.getAttribute("length")
                        , a.getAttribute("refModelName")
                        , a.getAttribute("typeDisplayName")
                );

                ew.getCell(c + 0, r + x, true).setCellValue(a.getAttribute("fieldName"));
                ew.getCell(c + 1, r + x, true).setCellValue(a.getAttribute("displayName"));
                ew.getCell(c + 2, r + x, true).setCellValue(a.getAttribute("fieldType"));
                ew.getCell(c + 3, r + x, true).setCellValue(a.getAttribute("length"));
                ew.getCell(c + 4, r + x, true).setCellValue(a.getAttribute("refModelName"));
                ew.getCell(c + 5, r + x, true).setCellValue(StrUtil.isBlank(a.getAttribute("refModelName")) ? "" : a.getAttribute("typeDisplayName"));

                ew.getCell(c + 0, r + x, true).setCellStyle(setBorder);
                ew.getCell(c + 1, r + x, true).setCellStyle(setBorder);
                ew.getCell(c + 2, r + x, true).setCellStyle(setBorder);
                ew.getCell(c + 3, r + x, true).setCellStyle(setBorder);
                ew.getCell(c + 4, r + x, true).setCellStyle(setBorder);
                ew.getCell(c + 5, r + x, true).setCellStyle(setBorder);

                System.out.println(row);
            }
        }

        ew.getWorkbook().removeSheetAt(0);
        ew.close();

        System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
    }


}
