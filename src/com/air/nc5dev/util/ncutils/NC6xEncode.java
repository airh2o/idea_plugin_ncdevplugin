//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.air.nc5dev.util.ncutils;

import com.air.nc5dev.exception.BusinessException;
import com.air.nc5dev.vo.NcUserVo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import nc.bs.framework.common.RuntimeEnv;
import nc.uap.studio.pub.db.Logger;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.crypto.cipher.CryptoCipher;
import org.apache.commons.crypto.utils.Utils;
import org.apache.commons.lang3.StringUtils;
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

@Data
@Accessors(chain = true)
public class NC6xEncode {
    /**
     * 为了能区分用户密码是否被md5加密完，在被md5加密的串前面加前缀
     **/
    public final static String MD5PWD_PREFIX = "U_U++--V";
    //最初的密码前缀过于简单，但为了兼容先保持一段时间。 逐渐替换
    @Deprecated
    public final static String MD5PWD_PREFIX_Deprecated = "md5";
    String ncHome;
    String KEY;
    static final IvParameterSpec iv = new IvParameterSpec(getUTF8Bytes("1234567890123456"));

    public String decode(String encodedString, Object... expars) {
        Security.addProvider(new BouncyCastleProvider());
        Properties properties = new Properties();
        int bufferSize = 1024;
        ByteBuffer decoded = ByteBuffer.allocateDirect(1024);

        try (CryptoCipher decipher = Utils.getCipherInstance("AES/CBC/PKCS5Padding", properties)) {
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

            return asString(decoded);
        } catch (Throwable e) {
            return encodedString;
        }
    }

    public String decodeError(String encodedString, Object... expars) throws Throwable {
        Security.addProvider(new BouncyCastleProvider());
        Properties properties = new Properties();
        int bufferSize = 1024;
        ByteBuffer decoded = ByteBuffer.allocateDirect(1024);

        try (CryptoCipher decipher = Utils.getCipherInstance("AES/CBC/PKCS5Padding", properties)) {
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

            return asString(decoded);
        }
    }

    public String encode(String s, Object... expars) {
        try {
            return getEncodedPassword((NcUserVo) expars[0], s);
        } catch (Throwable e) {
            return s;
        }
    }

    public String query() {
        if (KEY != null) {
            return KEY;
        } else {
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
                } catch (Exception e) {
                    Logger.error("Query the secret_key properties error!", e);
                } finally {
                    try {
                        if (fileStream != null) {
                            fileStream.close();
                        }
                    } catch (IOException e) {
                        Logger.error("close io exception ", e);
                    }

                }
            }

            return KEY;
        }
    }

    /**
     * 由UserVO 和 明文密码获得 加密后的用户密码
     *
     * @param user
     * @param expresslyPWD 明文密码
     * @return 加密后的密码串
     * @throws BusinessException
     */
    public static String getEncodedPassword(NcUserVo user, String expresslyPWD) throws BusinessException {
        if (user == null || StringUtils.isBlank(user.getId()))
            throw new BusinessException("illegal arguments");
        if (StringUtils.isNotBlank(expresslyPWD) && expresslyPWD.startsWith(MD5PWD_PREFIX))
            return expresslyPWD;

        String codecPWD = DigestUtils.md5Hex(user.getId() + StringUtils.stripToEmpty(expresslyPWD));

        return MD5PWD_PREFIX + codecPWD;
    }

    public byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr.length() < 1) {
            return null;
        } else {
            byte[] result = new byte[hexStr.length() / 2];

            for (int i = 0; i < hexStr.length() / 2; ++i) {
                int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
                int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
                result[i] = (byte) (high * 16 + low);
            }

            return result;
        }
    }

    public String parseByte2HexStr(byte[] buf) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < buf.length; ++i) {
            String hex = Integer.toHexString(buf[i] & 255);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }

            sb.append(hex.toUpperCase());
        }

        return sb.toString();
    }

    public void insert(String secret_key) {
        String ncHome = RuntimeEnv.getInstance().getNCHome();
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
            properties.store(outputStream, (String) null);
            KEY = secret_key;
        } catch (Exception e) {
            Logger.error("Write the secret_key properties error!", e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    Logger.error("close io exception ", e);
                }
            }
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
}
