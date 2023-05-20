package com.air.nc5dev.util;

import cn.hutool.core.io.FileUtil;
import com.air.nc5dev.util.idea.LogUtil;
import com.air.nc5dev.util.idea.ProjectUtil;
import com.air.nc5dev.vo.ExportContentVO;
import com.google.common.collect.Lists;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 自动生成补丁xml等云管家信息  <br>
 * <br>
 * <br>
 * <br>
 *
 * @author 唐粟 Email:209308343@qq.com
 * @date 2022/10/10 0010 15:58
 * @project
 * @Version
 */
public class AutoGenerPathcherInfo4YunGuanJiaUtil {
    String fullname;
    String no;
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String date = simpleDateFormat.format(new Date());
    String v = "V1";

    public static void main(String[] args) throws Throwable {
       /* new AutoGenerPathcherInfo4YunGuanJiaUtil().
                run("G:\\projects\\iuap\\NCC2111_701_UI\\patchers\\NCC2111-审批中心-增加金额提示"
                        , "NCC2111-审批中心-增加金额提示"
                        , true
                        , null);*/

        File f = new File("I:\\projects\\idea_plugin_ncdevplugin\\resources\\templates\\packmetadata.xml");
        List<String> fs = FileUtil.readUtf8Lines(f);
        for (String s : fs) {
            System.out.println("lines.add(\"" + (StringUtil.isBlank(s) ? " " : s) + "\\n\");");
        }
    }

    /**
     * @param dir       补丁根目录文件夹路径
     * @param name      补丁名称
     * @param ncc       是否ncc
     * @param contentVO
     * @throws Throwable
     */
    public void run(String dir, String name, boolean ncc, ExportContentVO contentVO) throws Throwable {
        File root = new File(dir);
        if (!root.isDirectory()) {
            LogUtil.error("生成云管家描述文件错误: not dir , exit." + root.getPath());
            return;
        }

        try {
            readme(dir, name, root, contentVO);
        } catch (Throwable e) {
            e.printStackTrace();
            LogUtil.error(e.getMessage(), e);
        }

        try {
            packmetadata(dir, name, root, contentVO);
        } catch (Throwable e) {
            e.printStackTrace();
            LogUtil.error(e.getMessage(), e);
        }

        try {
            installpatch(dir, name, root, contentVO);
        } catch (Throwable e) {
            e.printStackTrace();
            LogUtil.error(e.getMessage(), e);
        }
    }

    public void installpatch(String dir, String name, File root, ExportContentVO contentVO) throws IOException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<installpatch>\n";
        File replacement = new File(dir, "replacement");
        File[] fs = replacement.listFiles();
        for (File f : fs) {
            if (f.isDirectory()) {
                xml += "    <copy>\n" +
                        "        <from>/replacement/" + f.getName() + "/</from>\n" +
                        "        <to>/" + f.getName() + "/</to>\n" +
                        "    </copy>\n";
            }
        }
        xml += "\n</installpatch>";

        outputString2File(new File(dir, "installpatch.xml"), xml);
    }

    public void packmetadata(String dir, String name, File root, ExportContentVO contentVO) throws IOException {
        List<String> lines = null;

/*        if (new File(dir, "packmetadata.xml").isFile()) {
            lines = read2Strings(new FileInputStream(new File(dir, "packmetadata.xml")));
        } else {*/
        try {
            File resourceTemplates = ProjectUtil.getResourceTemplates("packmetadata.xml");
            if (resourceTemplates != null) {
                lines = FileUtil.readUtf8Lines(resourceTemplates);
                LogUtil.info("云管家信息文件:packmetadata.xml,模板路径： " + resourceTemplates.getPath());
            }
        } catch (Throwable e) {
            e.printStackTrace();
            LogUtil.error(e.getMessage(), e);
        }

        /*  }*/

        if (CollUtil.isEmpty(lines)) {
            lines = Lists.newArrayListWithCapacity(40);
            lines.add("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            lines.add("<packmetadata>\n");
            lines.add("    <canAppliedMiddleware>Weblogic,Websphere 7.0,Yonyou Middleware V5,Yonyou Middleware V6</canAppliedMiddleware>\n");
            lines.add("    <canAppliedDB>DB2 V9.7,SQL Server 2008 R2,Oracle 10,Oracle 11</canAppliedDB>\n");
            lines.add("    <patchType>新增功能补丁</patchType>\n");
            lines.add("    <modifiedJavaClasses></modifiedJavaClasses>\n");
            lines.add("    <description/>\n");
            lines.add("    <modifiedModules/>\n");
            lines.add("    <needRecreatedLoginJar>true</needRecreatedLoginJar>\n");
            lines.add("    <applyVersion>5.0,5.01,5.011,5.02,5.3,5.5,5.6,5.7,5.75,6.0,6.1,6.3</applyVersion>\n");
            lines.add("    <patchName></patchName>\n");
            lines.add("    <bugs/>\n");
            lines.add("    <provider>209308343@qq.com</provider>\n");
            lines.add("    <patchPriority>高危补丁</patchPriority>\n");
            lines.add("    <patchVersion/>\n");
            lines.add("    <dependInfo/>\n");
            lines.add("    <canAppliedOS>Linux,Windows,AIX,Solaris</canAppliedOS>\n");
            lines.add("    <id></id>\n");
            lines.add("    <time></time>\n");
            lines.add("    <department>air QQ 209308343, 微信: yongyourj</department>\n");
            lines.add("    <needDeploy>false</needDeploy>\n");
            lines.add("    <searchKeys/>\n");
            lines.add("</packmetadata>\n");
        }

        int i_name = -1;
        int i_id = -1;
        int i_create = -1;
        for (int x = 0; x < lines.size(); x++) {
            if (trim(lines.get(x)).startsWith("<patchName>")) {
                i_name = x;
            } else if (trim(lines.get(x)).startsWith("<id>")) {
                i_id = x;
            } else if (trim(lines.get(x)).startsWith("<time>")) {
                i_create = x;
            }
        }

        lines.set(i_name, "    <patchName>" + fullname + "</patchName>" + "\n");
        lines.set(i_id, "    <id>" + no + "</id>" + "\n");
        lines.set(i_create, "    <time>" + date + "</time>" + "\n");

        File out = new File(dir, "packmetadata.xml");

        contentVO.indicator.setText("云管家信息文件:packmetadata.xml..." + out.getPath() + " , " + lines.size());
        LogUtil.info("云管家信息文件:packmetadata.xml,输出路径： " + out.getPath() + "   , " + lines.size());

        outputString2File(out, builderString(lines));
    }

    public void readme(String dir, String name, File root, ExportContentVO contentVO) throws IOException {
        List<String> lines = null;
/*
        if (new File(dir, "readme.txt").isFile()) {
            lines = read2Strings(new FileInputStream(new File(dir, "readme.txt")));
        } else {*/
        try {
            File resourceTemplates = ProjectUtil.getResourceTemplates("readme.template");
            if (resourceTemplates != null) {
                lines = FileUtil.readUtf8Lines(resourceTemplates);
                LogUtil.info("云管家信息文件:readme.txt,模板路径： " + resourceTemplates.getPath());
            }
        } catch (Throwable e) {
            e.printStackTrace();
            LogUtil.error(e.getMessage(), e);
        }

        /*  }
         */
        if (CollUtil.isEmpty(lines)) {
            lines = Lists.newArrayListWithCapacity(40);
            lines.add("\n");
            lines.add("==============================================================================\n");
            lines.add("1)补丁基本信息\n");
            lines.add("==============================================================================\n");
            lines.add("\n");
            lines.add("	补丁名称 -\n");
            lines.add("	补丁编号 -\n");
            lines.add("	产品版本 - \n");
            lines.add("	补丁修改模块 -\n");
            lines.add("	补丁依赖信息 - \n");
            lines.add("	适用的中间件平台 - Weblogic,Websphere 7.0,Yonyou Middleware V5,Yonyou Middleware V6\n");
            lines.add("	适用的操作系统平台 - Linux,Windows,AIX,Solaris\n");
            lines.add("	适用的数据库平台 - DB2 V9.7,SQL Server 2008 R2,Oracle 10,Oracle 11\n");
            lines.add("	补丁创建时间 - 2022-07-01 18:02:52\n");
            lines.add("	是否需要部署 - true\n");
            lines.add("	是否需要重新生成客户端Applet Jar包 - true\n");
            lines.add("\n");
            lines.add("==============================================================================\n");
            lines.add("2)补丁安装步骤说明\n");
            lines.add("==============================================================================\n");
            lines.add("\n");
            lines.add("\n");
            lines.add("	补丁安装前置准备工作(比如数据备份)\n");
            lines.add("	======================================================================\n");
            lines.add("\n");
            lines.add("\n");
            lines.add("\n");
            lines.add("	补丁安装\n");
            lines.add("	======================================================================\n");
            lines.add("\n");
            lines.add("\n");
            lines.add("\n");
            lines.add("	补丁安装后置工作\n");
            lines.add("	======================================================================\n");
            lines.add("\n");
            lines.add("\n");
            lines.add("\n");
            lines.add("	补丁安装成功的验证工作\n");
            lines.add("	======================================================================\n");
            lines.add("\n");
            lines.add("\n");
            lines.add("\n");
            lines.add("	其它信息\n");
            lines.add("	======================================================================\n");
            lines.add("\n");
            lines.add("\n");
            lines.add("==============================================================================\n");
            lines.add("3)补丁修复bug列表说明\n");
            lines.add("==============================================================================\n");
        }

        int i_name = -1;
        int i_no = -1;
        int i_module = -1;
        int i_create = -1;
        for (int x = 0; x < lines.size(); x++) {
            if (trim(lines.get(x)).startsWith("补丁名称 -")) {
                i_name = x;
            } else if (trim(lines.get(x)).startsWith("补丁编号 -")) {
                i_no = x;
            } else if (trim(lines.get(x)).startsWith("补丁修改模块 -")) {
                i_module = x;
            } else if (trim(lines.get(x)).startsWith("补丁创建时间 - ")) {
                i_create = x;
            }
        }

        try {
            String s = trim(lines.get(i_name));
            s = s.substring(s.lastIndexOf("V") + 1);
            int i = Integer.parseInt(s);
            v = "V" + (i + 1);
        } catch (Throwable e) {
            e.printStackTrace();
            LogUtil.error(e.getMessage(), e);
        }
        fullname = name + "-" + v;
        lines.set(i_name, "\t补丁名称 - " + fullname + "\n");

        no = UUID.randomUUID().toString();
        lines.set(i_no, "\t补丁编号 - " + no + "\n");

        if (trim(lines.get(i_module)).equals("补丁修改模块 -")) {
            try {
                lines.set(i_module, "\t补丁修改模块 - "
                        + findFirstDir(new File(new File(root, "replacement"), "modules").listFiles()).getName()
                        + "\n"
                );
            } catch (Throwable e) {
                e.printStackTrace();
                LogUtil.error(e.getMessage(), e);
            }
        }

        lines.set(i_create, "\t补丁创建时间 - " + date + "\n");

        File out = new File(dir, "readme.txt");

        contentVO.indicator.setText("云管家信息文件:raedme.txt..." + out.getPath() + " , " + lines.size());
        LogUtil.info("云管家信息文件:raedme.txt,输出路径： " + out.getPath() + "   , " + lines.size());

        outputString2File(out, builderString(lines));
    }

    public void outputString2File(File file, String s) throws IOException {
        FileUtil.writeUtf8String(s, file);
    }

    public static File findFirstDir(File[] ss) {
        for (File s : ss) {
            if (s.isDirectory()) {
                return s;
            }
        }
        return null;
    }

    public static String trim(String s) {
        return s == null ? "" : s.trim();
    }

    public static String read2String(InputStream in) {
        return read2String(in, "utf-8");
    }

    public static List<String> read2Strings(InputStream in) {
        return read2Strings(in, "utf-8");
    }

    public static String read2String(InputStream in, String charset) {
        List<String> ss = read2Strings(in, charset);
        return builderString(ss);
    }

    private static String builderString(List<String> ss) {
        StringBuilder sb = new StringBuilder(500);
        for (String s : ss) {
            sb.append(s);

            if (!s.endsWith("\n")) {
                sb.append('\n');
            }

        }
        return sb.toString();
    }

    public static List<String> read2Strings(InputStream in, String charset) {
        Scanner s = new Scanner(in, charset);
        ArrayList sb = new ArrayList(500);
        while (s.hasNextLine()) {
            sb.add(s.nextLine());
        }
        s.close();
        return sb;
    }
}
