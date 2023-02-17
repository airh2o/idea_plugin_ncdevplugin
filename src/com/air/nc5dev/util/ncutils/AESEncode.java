package com.air.nc5dev.util.ncutils;

import com.air.nc5dev.util.ProjectNCConfigUtil;
import nc.vo.framework.rsa.Encode;
import org.apache.commons.crypto.cipher.CryptoCipher;
import org.apache.commons.crypto.utils.ReflectionUtils;
import org.apache.commons.crypto.utils.Utils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.Properties;

public class AESEncode {
    static final IvParameterSpec iv = new IvParameterSpec(getUTF8Bytes("1234567890123456"));
    static final String transform = "AES/CBC/PKCS5Padding";
    private static final String FLAY = "#";
    private static String KEY = null;

    public AESEncode() {
    }

    public static String encrypt(String data) {
        return "#" + aesEncode(data);
    }

    public static String decrypt(String data) {
        if (data.substring(0, 1).equals("#")) {
            data = data.substring(1);
            return aesDecode(data);
        } else {
            return (new Encode()).decode(data);
        }
    }

    public static String aesEncode(String text) {
        Properties properties = new Properties();
        String encodedString = null;
        Security.addProvider(new BouncyCastleProvider());

        try {
            CryptoCipher encipher =  Utils.getCipherInstance(transform, properties);
            Throwable var8 = null;

            try {
                ByteBuffer inBuffer = ByteBuffer.allocateDirect(1024);
                ByteBuffer outBuffer = ByteBuffer.allocateDirect(1024);
                inBuffer.put(getUTF8Bytes(text));
                inBuffer.flip();
                SecretKeySpec keySpec = null;
                byte[] encoded;
                if (query() != null) {
                    keySpec = new SecretKeySpec(parseHexStr2Byte(query()), "AES");
                } else {
                    encoded = AESGeneratorKey.genBindIpKey();
                    insert(parseByte2HexStr(encoded));
                    keySpec = new SecretKeySpec(encoded, "AES");
                }

                encipher.init(1, keySpec, iv);
                int updateBytes = encipher.update(inBuffer, outBuffer);
                int finalBytes = encipher.doFinal(inBuffer, outBuffer);
                outBuffer.flip();
                encoded = new byte[updateBytes + finalBytes];
                outBuffer.duplicate().get(encoded);
                encodedString = parseByte2HexStr(encoded);
            } catch (Throwable var20) {
                var8 = var20;
                throw var20;
            } finally {
                if (encipher != null) {
                    if (var8 != null) {
                        try {
                            encipher.close();
                        } catch (Throwable var19) {
                            var8.addSuppressed(var19);
                        }
                    } else {
                        encipher.close();
                    }
                }

            }
        } catch (Exception var22) {
        }

        return encodedString;
    }

    public static String aesDecode(String encodedString) {
        Security.addProvider(new BouncyCastleProvider());
        Properties properties = new Properties();
        ByteBuffer decoded = ByteBuffer.allocateDirect(1024);

        try {
            CryptoCipher decipher = // Utils.getCipherInstance("AES/CBC/PKCS5Padding", properties)
                    ReflectionUtils.newInstance(Class.forName("org.apache.commons.crypto.cipher.OpenSslCipher").asSubclass
                            (CryptoCipher.class), properties, transform)
                    ;
            Throwable var6 = null;

            try {
                SecretKeySpec keySpec = null;
                if (query() != null) {
                    keySpec = new SecretKeySpec(parseHexStr2Byte(query()), "AES");
                } else {
                    byte[] keysecByte = AESGeneratorKey.genBindIpKey();
                    insert(parseByte2HexStr(keysecByte));
                    keySpec = new SecretKeySpec(keysecByte, "AES");
                }

                decipher.init(2, keySpec, iv);
                ByteBuffer outBuffer = ByteBuffer.allocateDirect(1024);
                outBuffer.put(parseHexStr2Byte(encodedString));
                outBuffer.flip();
                decipher.update(outBuffer, decoded);
                decipher.doFinal(outBuffer, decoded);
                decoded.flip();
            } catch (Throwable var17) {
                var6 = var17;
                throw var17;
            } finally {
                if (decipher != null) {
                    if (var6 != null) {
                        try {
                            decipher.close();
                        } catch (Throwable var16) {
                            var6.addSuppressed(var16);
                        }
                    } else {
                        decipher.close();
                    }
                }

            }
        } catch (Throwable var19) {
            return null;
        }

        return asString(decoded);
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

    public static byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr.length() < 1) {
            return null;
        } else {
            byte[] result = new byte[hexStr.length() / 2];

            for(int i = 0; i < hexStr.length() / 2; ++i) {
                int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
                int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
                result[i] = (byte)(high * 16 + low);
            }

            return result;
        }
    }

    private static byte[] getUTF8Bytes(String input) {
        return input.getBytes(StandardCharsets.UTF_8);
    }

    private static String asString(ByteBuffer buffer) {
        ByteBuffer copy = buffer.duplicate();
        byte[] bytes = new byte[copy.remaining()];
        copy.get(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static String query() {
        if (KEY != null) {
            return KEY;
        } else {
            String ncHome = ProjectNCConfigUtil.getNCHomePath();
            if (ncHome.contains("bin")) {
                ncHome = ncHome.split("bin")[0];
                ncHome = ncHome.substring(0, ncHome.length() - 1);
            }

            Properties properties = new Properties();
            File propFile = new File(ncHome, "/ierp/bin/key.properties");
            FileInputStream fileStream = null;
            if (propFile.exists()) {
                try {
                    fileStream = new FileInputStream(propFile);
                    properties.load(fileStream);
                    KEY = properties.get("secret_key").toString();
                } catch (Exception var13) {
                } finally {
                    try {
                        if (fileStream != null) {
                            fileStream.close();
                        }
                    } catch (IOException var12) {
                    }

                }
            }

            return KEY;
        }
    }

    public static void insert(String secret_key) {
        String ncHome = ProjectNCConfigUtil.getNCHomePath();
        if (ncHome.contains("bin")) {
            ncHome = ncHome.split("bin")[0];
            ncHome = ncHome.substring(0, ncHome.length() - 1);
        }

        File propFile = new File(ncHome, "/ierp/bin/key.properties");
        Properties properties = new Properties();
        properties.setProperty("secret_key", secret_key);
        FileOutputStream outputStream = null;

        try {
            outputStream = new FileOutputStream(propFile);
            properties.store(outputStream, (String)null);
            KEY = secret_key;
        } catch (Exception var14) {
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException var13) {
                }
            }

        }

    }
}
