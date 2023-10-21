package com.air.nc5dev.util;


import cn.hutool.core.util.StrUtil;
import nc.vo.pub.lang.UFDate;
import nc.vo.pub.lang.UFDateTime;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串 工具类 <br>
 * <br>
 *
 * @author air
 * @version 505
 * @date 2019年8月9日 下午9:14:26
 */
public final class StringUtil extends StrUtil {
    public static void setIntoClipboard(String data) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(data), null);
    }

    public static String getFromClipboard() {
        Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            try {
                return (String) transferable.getTransferData(DataFlavor.stringFlavor);
            } catch (Exception e) {
                return "";
            }
        }
        return "";
    }

    /**
     * 字符串 == null 或者 trim后是空 <br>
     * <br>
     *
     * @param s
     * @return
     * @author air
     * @date 2019年8月9日 下午9:15:29
     */
    public static boolean isEmpty(String s) {
        return null == s || s.trim().length() < 1;
    }

    /**
     * 字符串 != null && trim后 !=空 <br>
     * <br>
     *
     * @param s
     * @return
     * @author air
     * @date 2019年8月9日 下午9:15:29
     */
    public static boolean notEmpty(String s) {
        return null != s && !(s.trim().length() < 1);
    }

    /**
     * 任何一个 字符串 == null 或者 trim后是空 返回true <br>
     * <br>
     *
     * @param strs
     * @return
     * @author air
     * @date 2019年8月9日 下午9:15:29
     */
    public static boolean isEmptys(String... strs) {
        if (null == strs || strs.length < 1) {
            return true;
        }

        for (String s : strs) {
            if (isEmpty(s)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 字符串转换成数字,如果是空或null 返回0 <br>
     * <br>
     *
     * @param s
     * @return
     * @author air
     * @date 2019年8月9日 下午9:15:06
     */
    public static int toInt(String s) {
        if (isEmpty(s)) {
            return 0;
        }
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 如果 传入的字符串是空or null 就返回 第二个值 <br>
     * <br>
     *
     * @param s
     * @param ifEmptyReturn
     * @return
     * @author air
     * @date 2019年8月9日 下午9:14:40
     */
    public static String get(String s, String ifEmptyReturn) {
        return isEmpty(s) ? ifEmptyReturn : s;
    }

    /**
     * 如果 传入的字符串是空or null 就返回 "" <br>
     * <br>
     *
     * @param s
     * @return
     * @author air
     * @date 2019年8月9日 下午9:14:40
     */
    public static String get(String s) {
        return isEmpty(s) ? "" : s;
    }

    /**
     * 安全把一个对象转成 String, null 返回 "" 否则返回tostring <br>
     * <br>
     *
     * @param o
     * @return
     * @author air
     * @date 2019年8月9日 下午9:49:33
     */
    public static String of(Object o) {
        return o == null ? "" : o.toString();
    }

    /**
     * 把一个对象安全的变成字符串,如果是null返回"" </br> </br>
     *
     * @param o
     * @return
     * @author air Email:209308343@qq.com
     * @date 2019-8-15 下午8:33:49
     */
    public static String getSafeString(Object o) {
        if (null == o) {
            return "";
        }

        return o.toString().trim();
    }

    public static String obj4StrIfnullBlack(Object o) {
        if (null == o) {
            return "";
        }

        if (o instanceof Date) {
            return V.formatDateTime((Date) o);
        } else if (o instanceof LocalDateTime) {
            return V.formatDateTime((LocalDateTime) o);
        } else if (o instanceof LocalDate) {
            return V.formatDate((LocalDate) o);
        } else if (o instanceof UFDate) {
            return ((UFDate) o).toString();
        } else if (o instanceof UFDateTime) {
            return ((UFDateTime) o).toString();
        }

        return o.toString();
    }

    /**
     * 字符串编码转换的实现方法
     *
     * @param str        待转换编码的字符串
     * @param oldCharset 原编码
     * @param newCharset 目标编码
     * @return
     * @throws
     * @deprecated
     */
    @Deprecated
    public static String changeCharset(String str, String oldCharset,
                                       String newCharset) {
        try {
            if (str != null) {
                // 用旧的字符编码解码字符串。解码可能会出现异常。
                byte[] bs = str.getBytes(oldCharset);
                // 用新的字符编码生成字符串
                return new String(bs, newCharset);
            }
        } catch (Exception e) {
        }
        return str;
    }

    /**
     * 获得字符串编码 </br>
     * </br>
     *
     * @param str
     * @return
     * @author air Email:209308343@qq.com
     * @date 2019-8-17 下午12:18:03
     */
    public static String getEncoding(String str) {
        String encode;

        encode = "GBK";
        try {
            if (str.equals(new String(str.getBytes(), encode))) {
                return encode;
            }
        } catch (Exception ex) {
        }

        encode = "UTF-8";
        try {
            if (str.equals(new String(str.getBytes(), encode))) {
                return encode;
            }
        } catch (Exception ex) {
        }

        encode = "GB2312";
        try {
            if (str.equals(new String(str.getBytes(), encode))) {
                return encode;
            }
        } catch (Exception ex) {
        }

        encode = "UTF-16";
        try {
            if (str.equals(new String(str.getBytes(), encode))) {
                return encode;
            }
        } catch (Exception ex) {
        }

        encode = "ASCII";
        try {
            if (str.equals(new String(str.getBytes(), encode))) {
                // "字符串<< " + str + " >>中仅由数字和英文字母组成，无法识别其编码格式"
                return null;
            }
        } catch (Exception ex) {
        }

        encode = "ISO-8859-1";
        try {
            if (str.equals(new String(str.getBytes(), encode))) {
                return encode;
            }
        } catch (Exception ex) {
        }
        /*
         * ......待完善
         */

        return "UTF-8";
    }

    /**
     * 计算字符串 src 总 给定字符串 des 出现的次数 </br> </br>
     *
     * @param src
     * @param des
     * @return
     * @author air Email:209308343@qq.com
     * @date 2019-8-17 下午6:16:06
     */
    public static int findCount(String src, String des) {
        int index = 0;
        int count = 0;
        while ((index = src.indexOf(des, index)) != -1) {
            count++;
            index = index + des.length();
        }
        return count;
    }

    /**
     * 计算字符串 src 总 给定字符串 des 出现的次数 达到阀值over,也就是出现次数只要 >= over 返回true </br> </br>
     *
     * @param src
     * @param des
     * @return
     * @author air Email:209308343@qq.com
     * @date 2019-8-17 下午6:16:06
     */
    public static boolean isCountOver(String src, String des, int over) {
        int index = 0;
        int count = 0;
        while ((index = src.indexOf(des, index)) != -1) {
            if (count >= over) {
                return true;
            }
            count++;
            index = index + des.length();
        }
        return false;
    }

    /**
     * 把一个字符串从 老的编码 转换到 新的编码 </br> </br>
     *
     * @param s
     * @param oldCharEncode
     * @param newCharEncode
     * @return
     * @author air Email:209308343@qq.com
     * @date 2019-8-27 下午7:36:58
     */
    public static String converEncode(String s, String oldCharEncode,
                                      String newCharEncode) {
        if (isEmpty(oldCharEncode)
                || isEmpty(newCharEncode)) {
            return s;
        }

        InputStreamReader in = null;
        ByteArrayOutputStream bar = null;
        OutputStreamWriter out = null;
        try {
            in = new InputStreamReader(new ByteArrayInputStream(s.getBytes()),
                    oldCharEncode);
            bar = new ByteArrayOutputStream();
            out = new OutputStreamWriter(bar, newCharEncode);
            char[] bs = new char[1];
            while (in.read(bs) > 0) {
                out.write(bs);
            }
            out.flush();
            String ns = bar.toString();

            return ns;
        } catch (Exception e) {
            return s;
        } finally {
            try {
                if (null != in) {
                    in.close();
                }
                if (null != out) {
                    out.close();
                }
                if (null != bar) {
                    bar.close();
                }
            } catch (Exception e2) {
            }
        }
    }

    /**
     * 字符串格式化,把占位符替换成 参数
     * </br>
     * </br>
     *
     * @param s    母字符串 比如 select 1 from dual where 1 != %s;
     * @param pars 参数值 比如 2 就是  select 1 from dual where 1 != 2;
     * @return
     * @author air Email:209308343@qq.com
     * @date 2019-10-26 下午7:36:52
     */
    public static final String format(String s, String... pars) {
        if (isEmpty(s)) {
            return s;
        }

        return String.format(s, pars);
    }

    /**
     * 把一个字符串src 里的 所有 from 替换成 to
     * 不使用 正则！                   </br>
     * </br>
     *
     * @param src
     * @param from
     * @param to
     * @return
     * @author air Email:209308343@qq.com
     * @date 2019年12月6日 上午9:16:18
     */
    public static final String replaceAll(String src, String from, String to) {
        String re = src;
        while (re.contains(from)) {
            re = replace(re, from, to);
        }

        return re;
    }

    /**
     * 把一个字符串src 里的 第一次遇见的 from 替换成 to
     * 不使用 正则！                   </br>
     * </br>
     *
     * @param src
     * @param from
     * @param to
     * @return
     * @author air Email:209308343@qq.com
     * @date 2019年12月6日 上午9:16:18
     */
    public static final String replace(String src, String from, String to) {
        char[] cs = src.toCharArray();
        char[] cf = from.toCharArray();
        char[] ct = to.toCharArray();

        if (cs.length < cf.length) {
            return src;
        }

        int start = -1;
        int mark = 0;

        for (int i = 0; i < cs.length; i++) {
            if (mark == cf.length) {
                break;
            } else if (cs[i] == cf[mark]) {
                if (start == -1) {
                    start = i;
                }
                ++mark;
            } else if (start != -1) {
                start = -1;
                mark = 0;
            }
        }

        if (start > -1 && mark > 0) {
            StringBuilder sb = new StringBuilder(cs.length - cf.length + ct.length);
            for (int i = 0; i < start; i++) {
                sb.append(cs[i]);
            }
            for (int i = 0; i < ct.length; i++) {
                sb.append(ct[i]);
            }
            for (int i = start + cf.length; i < cs.length; i++) {
                sb.append(cs[i]);
            }

            return sb.toString();
        }

        return src;
    }

    /**
     * 判断字符串中是否包含中文
     *
     * @param str 待校验字符串
     * @return 是否为中文
     */
    public static boolean isContainChinese(String str) {
        Pattern p = Pattern.compile("[\u4e00-\u9fa5！#￥%……&*——|{}【】‘；：”“'。，、？]");
        Matcher m = p.matcher(str);
        if (m.find()) {
            return true;
        }
        return false;
    }

    public static List<String> split2ListAndTrim(String str, String sp) {
        String[] ss = split(str, sp);
        if (ss == null || ss.length < 1) {
            return new ArrayList<>();
        }

        ArrayList<String> l = new ArrayList<>();
        for (int i = 0; i < ss.length; i++) {
            if (ss[i] == null) {
                continue;
            }
            l.add(trim(ss[i]));
        }
        return l;
    }

    public static boolean startsWith(String str, String start) {
        return startWith(str, start);
    }

    public static String removeStart(String s, String st) {
        return removePrefix(s, st);
    }

    public static boolean endsWith(String s, String e) {
        return endWith(s, e);
    }

    public static String removeEnd(String str, String s) {
        return removeSuffix(str, s);
    }
}