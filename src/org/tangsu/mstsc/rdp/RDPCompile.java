package org.tangsu.mstsc.rdp;

import cn.hutool.core.io.FileUtil;
import com.sun.jna.platform.win32.Crypt32;
import com.sun.jna.platform.win32.Crypt32Util;
import com.sun.jna.platform.win32.WinCrypt;
import org.tangsu.mstsc.MstscApplication;
import org.tangsu.mstsc.dao.BaseDao;
import org.tangsu.mstsc.entity.MstscEntity;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Formatter;

public class RDPCompile {
    public static void main(String[] args) {

        String password = "Player";

        /*加密*/
        String compilePassword = cryptRdpPassword(password);
        System.out.println("加密:" + compilePassword);


        /*解密*/
        String originalPassword = decodeRdpPassword(compilePassword);
        System.out.println("解密:" + originalPassword);


    }


    /*执行加密*/
    private static String cryptRdpPassword(String password) {
        WinCrypt.DATA_BLOB pDataIn = new WinCrypt.DATA_BLOB(password.getBytes(Charset.forName("UTF-16LE")));
        WinCrypt.DATA_BLOB pDataEncrypted = new WinCrypt.DATA_BLOB();
        Crypt32.INSTANCE.CryptProtectData(pDataIn, "psw", null, null, null, 1, pDataEncrypted);
        StringBuffer epwsb = new StringBuffer();
        byte[] pwdBytes;
        pwdBytes = pDataEncrypted.getData();
        Formatter formatter = new Formatter(epwsb);
        for (final byte b : pwdBytes) {
            formatter.format("%02X", b);
        }
        return epwsb.toString();
    }


    /*执行解密*/
    private static String decodeRdpPassword(String password) {
        try {
            return new String(Crypt32Util.cryptUnprotectData(toBytes(password))
                    , StandardCharsets.UTF_16LE);
        } catch (Exception e1) {
            e1.printStackTrace();
            return "ERROR";
        }
    }

    /*去掉0x以后,转整数再转型成字节*/
    private static byte[] toBytes(String str) {
        if (str == null || str.trim().equals("")) {
            return new byte[0];
        }
        byte[] bytes = new byte[str.length() / 2];
        for (int i = 0; i < str.length() / 2; i++) {
            String subStr = str.substring(i * 2, i * 2 + 2);
            bytes[i] = (byte) Integer.parseInt(subStr, 16);
        }
        return bytes;
    }

    public static void link(MstscEntity e, Component com) {
        File rdp = new File(BaseDao.getDefualtBaseDir(), "temp.rdp");
        rdp.deleteOnExit();

        StringBuilder s = new StringBuilder(1000);
        s.append("screen mode id:i:").append(e.getScreen_mode_id()).append('\n');
        s.append("desktopwidth:i:").append(e.getDesktopwidth()).append('\n');
        s.append("desktopheight:i:").append(e.getDesktopheight()).append('\n');
        s.append("session bpp:i:").append(e.getSession_bpp()).append('\n');
        s.append("winposstr:s:").append(e.getWinposstr()).append('\n');
        s.append("compression:i:").append(e.getCompression()).append('\n');
        s.append("displayconnectionbar:i:").append(e.getDisplayconnectionbar()).append('\n');
        s.append("disable wallpaper:i:").append(e.getDisable_wallpaper()).append('\n');
        s.append("disable themes:i:").append(e.getDisable_themes()).append('\n');
        s.append("full address:s:").append(e.getIp()).append(':').append(e.getPort()).append('\n');
        s.append("autoreconnection enabled:i:").append(e.getAutoreconnection()).append('\n');
        s.append("username:s:").append(e.getUser()).append('\n');
        s.append("password 51:b:").append(RDPCompile.cryptRdpPassword(e.getPass())).append('\n');
        s.append("redirectclipboard:i:").append(e.getRedirectclipboard()).append('\n');

        try {
            FileUtil.writeUtf8String(s.toString(), rdp);

            Runtime.getRuntime().exec(
                    String.format("mstsc %s /console"
                            , rdp.getPath()));
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    com
                    , "连接失败:" + ex.getMessage()
                    , "错误"
                    , JOptionPane.ERROR_MESSAGE);
        }
    }
}

