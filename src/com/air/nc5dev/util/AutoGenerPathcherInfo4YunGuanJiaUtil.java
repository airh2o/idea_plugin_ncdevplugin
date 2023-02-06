package com.air.nc5dev.util;

import com.air.nc5dev.util.idea.ProjectUtil;
import com.air.nc5dev.vo.ExportContentVO;

import java.io.*;
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
        new AutoGenerPathcherInfo4YunGuanJiaUtil().
                run("G:\\projects\\iuap\\NCC2111_701_UI\\patchers\\NCC2111-审批中心-增加金额提示"
                        , "NCC2111-审批中心-增加金额提示"
                        , true
                        , null);
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
            System.out.println("not dir , exit.");
            return;
        }

        try {
            readme(dir, name, root);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            packmetadata(dir, name, root);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            installpatch(dir, name, root);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        System.out.println("gener sucess!");
    }

    public void installpatch(String dir, String name, File root) throws IOException {
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

    public void packmetadata(String dir, String name, File root) throws IOException {
        List<String> lines = null;

        if (new File(dir, "packmetadata.xml").isFile()) {
            lines = read2Strings(new FileInputStream(new File(dir, "packmetadata.xml")));
        } else {
            lines = read2Strings(
                    new FileInputStream(ProjectUtil.getResourceTemplates("packmetadata.xml"))
            );
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

        lines.set(i_name, "    <patchName>" + fullname + "</patchName>");
        lines.set(i_id, "    <id>" + no + "</id>");
        lines.set(i_create, "    <time>" + date + "</time>");

        outputString2File(new File(dir, "packmetadata.xml"), builderString(lines));
    }

    public void readme(String dir, String name, File root) throws IOException {
        List<String> lines = null;

        if (new File(dir, "readme.txt").isFile()) {
            lines = read2Strings(new FileInputStream(new File(dir, "readme.txt")));
        } else {
            lines = read2Strings(
                    new FileInputStream(ProjectUtil.getResourceTemplates("readme.template"))
            );
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
        }
        fullname = name + "-" + v;
        lines.set(i_name, "\t补丁名称 - " + fullname);

        no = UUID.randomUUID().toString();
        lines.set(i_no, "\t补丁编号 - " + no);

        if (trim(lines.get(i_module)).equals("补丁修改模块 -")) {
            try {
                lines.set(i_module, "\t补丁修改模块 - "
                        + findFirstDir(new File(new File(root, "replacement"), "modules").listFiles()).getName()
                );
            } catch (Throwable e) {
            }
        }

        lines.set(i_create, "\t补丁创建时间 - " + date);

        outputString2File(new File(dir, "readme.txt"), builderString(lines));
    }

    public void outputString2File(File file, String s) throws IOException {
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "utf-8"));
        out.write(s);
        out.flush();
        out.close();
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
            sb.append(s).append('\n');
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
