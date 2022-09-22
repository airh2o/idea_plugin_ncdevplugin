package com.air.nc5dev.util.ncutils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class AESGeneratorKey {
    private static final byte[] KEY_END = new byte[]{43, 65, 23, 6, -54, -24, -16, 26, 7, 34, -29, -52, -14, 27, 38, 41};
    private static final int AESKEY_LENGTH = 256;
    private static final int TRANS_KEY_LENGTH = 32;
    private static final byte[] DEFAULT_TRANS_KEY = new byte[]{34, 25, 64, 23, 54, 65, 76, 34, -3, -54, -13, -35, 34, 54, 23};
    public static final String DEFAULT_PROVIDER_NAME = "BC";
    private static final String DEFAUL_WAY = "SHA1PRNG";

    public AESGeneratorKey() {
    }

    public static byte[] genKey() throws IOException {
        try {
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
            SecretKey secretKey = null;
            KeyGenerator kgen = null;
            if (Security.getProvider("BC") == null) {
                kgen = KeyGenerator.getInstance("AES");
            } else {
                kgen = KeyGenerator.getInstance("AES", "BC");
            }

            kgen.init(256, secureRandom);
            secretKey = kgen.generateKey();
            return secretKey.getEncoded();
        } catch (NoSuchProviderException var3) {
            throw new IOException("gen key error:" + var3.getMessage());
        } catch (NoSuchAlgorithmException var4) {
            throw new IOException(var4.getMessage());
        }
    }

    public static byte[] genKey(byte[] transKey) throws IOException {
        byte[] key = new byte[32];
        System.arraycopy(transKey, 8, key, 0, 24);
        System.arraycopy(KEY_END, 0, key, 24, 8);

        try {
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
            KeyGenerator kgen = null;
            if (Security.getProvider("BC") == null) {
                kgen = KeyGenerator.getInstance("AES");
            } else {
                kgen = KeyGenerator.getInstance("AES", "BC");
            }

            secureRandom.setSeed(key);
            kgen.init(256, secureRandom);
            kgen.generateKey();
            return key;
        } catch (NoSuchProviderException var4) {
            throw new IOException("gen key error:" + var4.getMessage());
        } catch (NoSuchAlgorithmException var5) {
            throw new IOException(var5.getMessage());
        }
    }

    public static byte[] genTransKey() {
        String ClientIP = null;
        byte[] transKey = new byte[32];

        try {
            ClientIP = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException var4) {
        }

        if (ClientIP == null || ClientIP.equals("")) {
            ClientIP = "uap:localHost";
        }

        try {
            byte[] srcKey = ClientIP.getBytes("unicode");
            if (srcKey.length > 32) {
                System.arraycopy(srcKey, 0, transKey, 0, 32);
            } else {
                System.arraycopy(srcKey, 0, transKey, 0, srcKey.length);

                for(int i = srcKey.length; i < 32; ++i) {
                    transKey[i] = 0;
                }
            }

            return transKey;
        } catch (UnsupportedEncodingException var5) {
            return DEFAULT_TRANS_KEY;
        }
    }

    public static String parseByte2HexStr(byte[] buf) {
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < buf.length; ++i) {
            String hex = Integer.toHexString(buf[i] & 255);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }

            sb.append(hex.toUpperCase());
        }

        return sb.toString();
    }

    public static String generHexStrKey() {
        try {
            return parseByte2HexStr(genKey());
        } catch (IOException var1) {
            var1.printStackTrace();
            return null;
        }
    }

    public static byte[] genBindIpKey() throws IOException {
        return genKey(genTransKey());
    }

    static {
        if (Security.getProvider("BC") == null) {
            try {
                String clsName = "org.bouncycastle.jce.provider.BouncyCastleProvider";
                Object o = Class.forName(clsName).newInstance();
                Security.addProvider((Provider)o);
            } catch (Exception var2) {
            }
        }

    }
}
