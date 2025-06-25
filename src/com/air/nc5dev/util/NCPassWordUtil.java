package com.air.nc5dev.util;

import cn.hutool.core.util.StrUtil;
import com.air.nc5dev.enums.NcVersionEnum;
import com.air.nc5dev.util.idea.ProjectUtil;
import com.air.nc5dev.util.ncutils.AESEncode;
import com.air.nc5dev.util.ncutils.NC5xEncode;
import com.air.nc5dev.util.ncutils.NC6xEncode;
import com.intellij.openapi.project.Project;
import org.apache.commons.crypto.cipher.CryptoCipher;
import org.apache.commons.crypto.utils.Utils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jetbrains.annotations.Nullable;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <br/>
 * <br/>
 * <br/>
 *
 * @author 唐粟 Email:209308343@qq.com
 * @date 2020/11/9 11:51
 * @project
 * @Version
 */
public class NCPassWordUtil {
    /**
     * 解码密码明文,支持NC5系列和部分特殊用户
     *
     * @param pass
     * @param expars
     * @return
     */
    @Deprecated
    private static String decode(String pass, Object... expars) {
        if (NcVersionEnum.NC5.equals(ProjectNCConfigUtil.getNCVersion())
                || NcVersionEnum.U8Cloud.equals(ProjectNCConfigUtil.getNCVersion())) {
            return new NC5xEncode().decode(pass);
        }

        if (NcVersionEnum.NC6.equals(ProjectNCConfigUtil.getNCVersion())
                || NcVersionEnum.isNCCOrBIP(ProjectNCConfigUtil.getNCVersion())) {
            return new NC6xEncode().decode(pass, expars);
        }

        return pass;
    }

    /**
     * 加密 密码
     *
     * @param text
     * @return
     */
    public static String encode(String pass, Object... expars) {
        if (NcVersionEnum.NC5.equals(ProjectNCConfigUtil.getNCVersion())
                || NcVersionEnum.U8Cloud.equals(ProjectNCConfigUtil.getNCVersion())) {
            return new NC5xEncode().encode(pass);
        }

        if (NcVersionEnum.NC6.equals(ProjectNCConfigUtil.getNCVersion())
                || NcVersionEnum.isNCCOrBIP(ProjectNCConfigUtil.getNCVersion())) {
            return new NC6xEncode().encode(pass, expars);
        }

        return pass;
    }

    public static String decode(String dsname, @Nullable Project project, String str, @Nullable String isEncode) {
        if (project == null) {
            project = ProjectUtil.getDefaultProject();
        }
        String password = null;
        boolean nccOrBIP = NcVersionEnum.isNCCOrBIP(ProjectNCConfigUtil.getNCVersion());

        if (!nccOrBIP) {
            try {
                password = new NC5xEncode().decode(str);
                if (StrUtil.isNotBlank(password)) {
                    return password;
                }
            } catch (Throwable e) {
            }
        }

        // nc.bs.framework.core.conf.DataSourceConf#setPassword
        // nc.bs.framework.aes.AESEncode#decrypt
        try {
            if (nccOrBIP) {
                if ("true".equalsIgnoreCase(StringUtil.trim(isEncode))) {
                    password = StrUtil.blankToDefault(AESEncode.decrypt(str), "");
                    if (password.equals("ufnull")) {
                        password = null;
                    }
                }
            }
        } catch (Throwable e) {
        }
        String nchome = ProjectNCConfigUtil.getNCHomePath(project);
        if (StrUtil.isBlank(password)) {
            //BIP 其他加密算法？
            if (str.substring(0, 1).equals("#")) {
                str = str.substring(1);
            }

            //拿一下加密key
            String key = query(nchome);
            try {
                password = aesDecode(key, str);
            } catch (Throwable e) {
               // e.printStackTrace();
            }

            if (StrUtil.isBlank(password)) {
                try {
                    password = new NC6xEncode().setNcHome(nchome).decodeError(str);
                } catch (Throwable e) {
                }
            }
        }

        //读取下 临时配置
        if (StrUtil.isBlank(password)) {
            password = TEMP_PASS.get(nchome + ":" + dsname);
        }

        if (StrUtil.isBlank(password)) {
            password = "无法解密 暂不支持";
        }

        return password;
    }

    public static Map<Object, String> TEMP_PASS = new ConcurrentHashMap<>();

    public static String query(String ncHome) {
        Properties properties = new Properties();
        File f = new File(ncHome, "ierp");
        f = new File(f, "bin");
        File propFile = new File(f, "key.properties");
        FileInputStream fileStream = null;
        if (propFile.exists()) {
            try {
                fileStream = new FileInputStream(propFile);
                properties.load(fileStream);
                return properties.get("secret_key").toString();
            } catch (Throwable var13) {
                throw new RuntimeException("Query the secret_key properties error!", var13);
            } finally {
                try {
                    if (fileStream != null) {
                        fileStream.close();
                    }
                } catch (Throwable var12) {
                }

            }
        }
        return null;
    }

    static final IvParameterSpec iv = new IvParameterSpec(getUTF8Bytes("1234567890123456"));

    public static String aesDecode(String secrekey, String encodedString) throws Throwable {
        Security.addProvider(new BouncyCastleProvider());
        Properties properties = new Properties();
        ByteBuffer decoded = ByteBuffer.allocateDirect(1024);

        CryptoCipher decipher = Utils.getCipherInstance("AES/CBC/PKCS5Padding", properties);
        try {
            SecretKeySpec keySpec = new SecretKeySpec(parseHexStr2Byte(secrekey), "AES");
            decipher.init(2, keySpec, iv);
            ByteBuffer outBuffer = ByteBuffer.allocateDirect(1024);
            outBuffer.put(parseHexStr2Byte(encodedString));
            outBuffer.flip();
            decipher.update(outBuffer, decoded);
            decipher.doFinal(outBuffer, decoded);
            decoded.flip();
        } finally {
            if (decipher != null) {
                try {
                    decipher.close();
                } catch (Throwable var17) {
                }
            }
        }

        return asString(decoded);
    }

    private static String asString(ByteBuffer buffer) {
        ByteBuffer copy = buffer.duplicate();
        byte[] bytes = new byte[copy.remaining()];
        copy.get(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private static byte[] getUTF8Bytes(String input) {
        return input.getBytes(StandardCharsets.UTF_8);
    }

    public static String parseByte2HexStr(byte[] buf) {
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

    public static byte[] parseHexStr2Byte(String hexStr) {
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
}
