package com.air.nc5dev.util;

import cn.hutool.core.util.StrUtil;
import com.air.nc5dev.enums.NcVersionEnum;
import com.air.nc5dev.util.idea.ProjectUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * thx
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BmfUtil {
    String template;
    private String billtype;
    private String billname;
    private String modulecode;
    String langcode;
    private String outfilepath;
    @Builder.Default
    private int defnum = 50;
    // 0 一主一子， 1 树卡, 2 LFW单据, 3 单子表 ，  4 单表，
    @Builder.Default
    int type = 0;
    String defName;
    NcVersionEnum versionEnum;

    /**
     * String 转 XML
     *
     * @param xmlStr
     * @return thx
     * 2013-3-4
     */
    public static org.dom4j.Document str2Xml(String xmlStr) {// Str是传入的一段XML内容的字符串
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
     * XML 转 字符串
     *
     * @param document
     * @return thx
     * 2013-3-4
     */
    public String xml2Str(org.dom4j.Document document) {
        String XMLStr = document.asXML();// obj.asXML()则为Document对象转换为字符串方法
        return XMLStr;
    }

    /**
     * 生成文件
     *
     * @param document
     * @param outpath  2013-8-12
     * @author thx
     * @email t@holdhe.com
     */
    public static void writFile(org.dom4j.Document document, String outpath) {
        /*保存文档*/
        //System.out.println("保存文件到:"+outpath);
        outpath = outpath.replace("\\", "/");
        String dir = outpath.substring(0, outpath.lastIndexOf("/"));
        OutputFormat format = OutputFormat.createPrettyPrint();//设置美化格式
        format.setEncoding("UTF-8");//设置字符编码
        try {
            if (!deleteFile(outpath)) {
                creatdir(dir);
            }
            FileOutputStream out = new FileOutputStream(outpath);
            XMLWriter writer = new XMLWriter(out, format);
            writer.write(document);//输出XML文档
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String writ2Str(org.dom4j.Document document) {
        OutputFormat format = OutputFormat.createPrettyPrint();//设置美化格式
        format.setEncoding("UTF-8");//设置字符编码
        String xml = null;
        StringWriter stringWriter = null;
        XMLWriter writer = null;
        try {
            stringWriter = new StringWriter(5000);
            writer = new XMLWriter(stringWriter, format);
            writer.write(document);//输出XML文档
            writer.close();
            xml = stringWriter.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (Exception e) {
            }
        }

        return xml;
    }

    /**
     * 是否存在文件夹
     *
     * @param path 2013-8-12
     * @author thx
     * @email t@holdhe.com
     */
    public boolean isExist(String path) {
        //System.out.println("判断文件夹是否存在");
        File file = new File(path);
        // 判断文件夹是否存在,如果不存在则创建文件夹
        if (!file.exists()) {
            return false;
        }
        return true;
    }

    /**
     * 判断文件夹是否存在,没有则创建文件夹!
     *
     * @param path 2013-8-12
     * @author thx
     * @email t@holdhe.com
     */
    public static void creatdir(String path) {
        //System.out.println("判断文件夹是否存在,没有则创建文件夹!");
        File file = new File(path);
        // 判断文件夹是否存在,如果不存在则创建文件夹
        if (!file.exists()) {
            file.mkdir();
        }
    }

    /**
     * 删除文件,有文件且删除返回true,其他false
     *
     * @param sPath 2013-8-12
     * @author thx
     * @email t@holdhe.com
     */
    public static boolean deleteFile(String sPath) {
        //System.out.println("删除文件!");
        boolean flag = false;
        File file = new File(sPath);
        // 路径为文件且不为空则进行删除
        if (file.isFile() && file.exists()) {

            file.delete();
            flag = true;
            //System.out.println("删除已存在文件 "+sPath);
        }
        return flag;
    }

    /**
     * 完完整整复制文件,用于备份
     *
     * @param inpath
     * @param outpath 2013-8-11
     * @throws IOException
     * @author thx
     * @email t@holdhe.com
     */
    public static void copyfile(String inpath, String outpath) throws IOException {
        //System.out.println("复制文件"+inpath+"到"+outpath);
        BufferedInputStream inBuff = null;
        BufferedOutputStream outBuff = null;
        try {
            // 新建文件输入流并对它进行缓冲
            inBuff = new BufferedInputStream(new FileInputStream(inpath));
            // 新建文件输出流并对它进行缓冲
            outBuff = new BufferedOutputStream(new FileOutputStream(outpath));
            // 缓冲数组
            byte[] b = new byte[1024 * 5];
            int len;
            while ((len = inBuff.read(b)) != -1) {
                outBuff.write(b, 0, len);
            }
            // 刷新此缓冲的输出流
            outBuff.flush();
        } finally {
            // 关闭流
            if (inBuff != null)
                inBuff.close();
            if (outBuff != null)
                outBuff.close();
        }
    }

    /**
     * inpath 绝对路径下的bmf 生成新bmf文件到指定outpath绝对路径位置,
     *
     * @param inpath
     * @param outpath 2013-8-11
     * @throws IOException
     * @author thx
     * @email t@holdhe.com
     */
    public void newbmf() throws IOException {
        org.dom4j.Document doc = str2Xml(template);
        if (doc == null) {
            //System.out.println("读取XML后创建dom对象失败!");
            return;
        }
        Element root = doc.getRootElement();
        if (root == null) {
            return;//连root都没有,拿我穷开心哦
        }

        HashMap<String, String> replateMap = new HashMap<>(100);
        UUID uuid = UUID.randomUUID();
        String rootID = uuid.toString();
        replateMap.put(root.attributeValue("id"), rootID);  //替换BMF 文件ID

        String mainEntityIdOld = root.attributeValue("mainEntity");
        String[] defNames = StringUtil.split(defName, ",");
        if (CollUtil.isEmpty(defNames)) {
            defNames = new String[]{"vdef"};
        }

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

                    addDefFieds(defNames, 0, i, entityIdOld, attributelist, attributeNodeList.size(), defnum,
                            replateMap);
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
                    String ID = element.attributeValue("id");
                    uuid = UUID.randomUUID();
                    replateMap.put(ID, uuid.toString());//替换实体与注释的连接线ID
                }
            }

        }

        //>>>>>>>>>>>>>>>>>>>>>>>>分割线,开始处理插件的模块名等信息>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        replateMap.put("${djbm}", billtype);
        replateMap.put("${djbmupper}", billtype.toUpperCase());
        replateMap.put("${djbmlower}", billtype.toLowerCase());
        replateMap.put("${djbmvo}", billtype.toUpperCase().substring(0, 1)
                + billtype.toLowerCase().substring(1, billtype.length()));
        replateMap.put("${djmc}", billname);
        replateMap.put("${mkbm}", modulecode.toLowerCase());
        //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>分割线,已经替换完毕>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>


        String str = writ2Str(doc);

        Set<String> keys = replateMap.keySet();
        for (String key : keys) {
            str = StrUtil.replace(str, key, replateMap.get(key));
        }

        doc = str2Xml(str);//根据替换后的XML内容重新生成DOM对象
        writFile(doc, outfilepath + File.separatorChar + billtype + ".bmf");

        processSkIfneed(replateMap);
    }

    public static void addDefFieds(String[] defNames, int start, int entityIndex, String entityId,
                                   Element attributelist,
                                   int sequenceStart, int defnum, HashMap<String, String> replateMap) {
        String defPrefix = entityIndex >= defNames.length ? defNames[0] : defNames[entityIndex];
        //增加自定义项目！！！！！
        for (int x = 1; x <= defnum; x++) {
            Element def = attributelist.addElement("attribute");
            def.addAttribute("accessStrategy", "");
            def.addAttribute("accesspower", "false");
            def.addAttribute("accesspowergroup", "");
            def.addAttribute("calculation", "false");
            def.addAttribute("classID", entityId);
            def.addAttribute("createIndustry", "0");
            def.addAttribute("dataType", "BS000010000100001056");
            def.addAttribute("dataTypeStyle", "SINGLE");
            def.addAttribute("dbtype", "varchar");
            def.addAttribute("defaultValue", "");
            def.addAttribute("description", "");
            def.addAttribute("displayName", "自定义项" + (x + start));
            def.addAttribute("dynamic", "false");
            def.addAttribute("dynamicTable", "");
            def.addAttribute("fieldName", defPrefix + (x + start));
            def.addAttribute("fieldType", "varchar");
            def.addAttribute("fixedLength", "false");
            def.addAttribute("forLocale", "false");
            def.addAttribute("help", "");
            def.addAttribute("id", UUID.randomUUID().toString());
            def.addAttribute("industryChanged", "false");
            def.addAttribute("isActive", "true");
            def.addAttribute("isAuthorization", "true");
            def.addAttribute("isDefaultDimensionAttribute", "false");
            def.addAttribute("isDefaultMeasureAttribute", "false");
            def.addAttribute("isFeature", "false");
            def.addAttribute("isGlobalization", "false");
            def.addAttribute("isHide", "false");
            def.addAttribute("isKey", "false");
            def.addAttribute("isReadOnly", "false");
            def.addAttribute("isShare", "false");
            def.addAttribute("notSerialize", "false");
            def.addAttribute("isNullable", "true");
            def.addAttribute("isSource", "true");
            def.addAttribute("length", "101");
            def.addAttribute("maxValue", "");
            def.addAttribute("minValue", "");
            def.addAttribute("modifyIndustry", "0");
            def.addAttribute("name", defPrefix + (x + start));
            def.addAttribute("precise", "");
            def.addAttribute("refModelName", "");
            def.addAttribute("resid", "");
            def.addAttribute("sequence", "" + (sequenceStart + x + start));
            def.addAttribute("typeDisplayName", "自定义项");
            def.addAttribute("typeName", "CUSTOM");
            def.addAttribute("versionType", "0");
            def.addAttribute("visibility", "public");
        }
    }

    /**
     * 树卡处理
     */
    public void processSkIfneed(HashMap<String, String> replateMap) {
        if (1 != type) {
            return;
        }

        String str;
        //如果是树卡,需呀生成SQL语句
        File sqlpath = new File(new File(outfilepath).getParentFile()
                , new File(outfilepath).getName() + ".sql");
        File clientpath = new File(sqlpath.getParentFile()
                , billtype.toUpperCase().substring(0, 1)
                + billtype.toLowerCase().substring(1, billtype.length()) + "ClassRefModel.java");

        String sql = "insert into BD_REFINFO (CODE, DR, ISNEEDPARA, ISSPECIALREF, METADATATYPENAME" +
                ", MODULENAME, NAME, PARA1, PARA2, PARA3, PK_REFINFO, REFCLASS, REFSYSTEM," +
                " REFTYPE, RESERV1, RESERV2, RESERV3, RESID, RESIDPATH, TS, WHEREPART)" +
                "values ('${djbm}01', 0, null, null, null, '${mkbm}', '${djmc}', null, null" +
                ", null, '${id}', 'nc.ui.${mkbm}.ref.${djbmvo}ClassRefModel', null, 1, null, null, null" +
                ", '${djmc}', 'ref', '2013-10-05 22:57:13', null);";
        sql = sql.replace("${djbm}", billtype);
        sql = sql.replace("${djbmupper}", billtype.toUpperCase());
        sql = sql.replace("${djbmlower}", billtype.toLowerCase());
        sql = sql.replace("${djbmvo}", billtype.toUpperCase().substring(0, 1)
                + billtype.toLowerCase().substring(1, billtype.length()));
        sql = sql.replace("${djmc}", billname);
        sql = sql.replace("${mkbm}", modulecode.toLowerCase());
        sql = sql.replace("${id}", UUID.randomUUID().toString().replace("-"
                , "").substring(0, 20));
        //写SQL 到 sqlpath 目录,并设置到剪切板
        writeFile(sql, sqlpath.getPath());

        str = ProjectUtil.getResourceTemplatesUtf8Txt("treeCardRefModel.r");
        replateMap.put("${djbm}", billtype);
        replateMap.put("${djbmupper}", billtype.toUpperCase());
        replateMap.put("${djbmlower}", billtype.toLowerCase());
        replateMap.put("${djbmvo}", billtype.toUpperCase()
                .substring(0, 1) + billtype.toLowerCase().substring(1, billtype.length()));
        replateMap.put("${djmc}", billname);
        replateMap.put("${mkbm}", modulecode.toLowerCase());

        String def = "";
        for (int i = 0; i < 100; i++) {
            if (str.contains("")) {
                def = "";
                for (int x = 1; x <= defnum; x++) {
                    def += "\n" + "<attribute accessStrategy=\"\" accesspower=\"false\" " +
                            "accesspowergroup=\"\" calculation=\"false\" " +
                            "classID=\"c815d664-0039-4a8b-802e-6c8f99812232\" " +
                            "createIndustry=\"0\" dataType=\"BS000010000100001056\" " +
                            "dataTypeStyle=\"SINGLE\" dbtype=\"varchar\" defaultValue=\"\" description=\"\"" +
                            " displayName=\"自定义项" + x + "\" dynamic=\"true\" dynamicTable=\"\" " +
                            "fieldName=\"def" + x + "\" fieldType=\"varchar\" fixedLength=\"false\"" +
                            " forLocale=\"false\" help=\"\" id=\"" + UUID.randomUUID().toString() + "\" " +
                            "industryChanged=\"false\" isActive=\"true\" isAuthorization=\"true\" " +
                            "isDefaultDimensionAttribute=\"false\" isDefaultMeasureAttribute=\"false\" " +
                            "isFeature=\"false\" isGlobalization=\"false\" isHide=\"false\" isKey=\"false\" " +
                            "isNullable=\"true\" isReadOnly=\"false\" isShare=\"false\" isSource=\"true\"" +
                            " length=\"101\" maxValue=\"\" minValue=\"\" modifyIndustry=\"0\" name=\"def" + x +
                            "\" " +
                            "notSerialize=\"false\" precise=\"\" refModelName=\"\" resid=\"\" sequence=\"21\"" +
                            " typeDisplayName=\"自定义项\" typeName=\"CUSTOM\" versionType=\"0\" " +
                            "visibility=\"public\"/>";
                }
                replateMap.put("${mkbm}", modulecode.toLowerCase());
            }
        }

        //写类 str 到 目录 clientpath
        writeFile(str, clientpath.getPath());
    }

    public void writeFile(String str, String filepath) {
        FileWriter writer;
        try {
            if (!deleteFile(filepath)) {
                creatdir(filepath.substring(0, filepath.lastIndexOf("/")));
            }
            File f = new File(filepath);
            f.createNewFile();
            writer = new FileWriter(f);
            writer.write(str);

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
