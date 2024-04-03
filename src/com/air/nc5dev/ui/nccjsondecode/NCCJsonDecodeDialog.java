package com.air.nc5dev.ui.nccjsondecode;

import com.air.nc5dev.util.CollUtil;
import com.air.nc5dev.util.ExceptionUtil;
import com.air.nc5dev.util.ProjectNCConfigUtil;
import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.util.XmlUtil;
import com.air.nc5dev.util.idea.LogUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.components.JBTextField;
import lombok.Data;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/***
 *     弹框UI        <br>
 *           <br>
 *           <br>
 *           <br>
 * @author air Email: 209308343@qq.com
 * @date 2019/12/25 0025 15:29
 * @Param
 * @return
 */
@Data
public class NCCJsonDecodeDialog extends DialogWrapper {
    JComponent contentPane;
    Project project;
    JTextArea textArea_requst;
    JTextArea textArea_result;
    JBCheckBox checkBox_aes;
    JBCheckBox checkBox_mark;
    JBCheckBox checkBox_gzip;
    JBCheckBox checkBox_format;
    JButton button_decode;
    JButton button_encode;
    JBTextField textField_aesKey;
    JBTextField textField_cowboy;
    JBTextField textField_cowboyAesKey;
    JBTextField textField_crux;

    public static volatile String TEMP_AESKEY = null;
    public static volatile String TEMP_CRUX = null;
    public static volatile String TEMP_COWBODY = null;
    public static volatile String TEMP_COWBODYAESKEY = "4fa8959db7b4423a99f056e299914128";

    public NCCJsonDecodeDialog(Project project) {
        super(project);
        this.project = project;
        init();
        setTitle("NCC(BIP)请求json解码");
    }

    public JComponent getContentPane() {
        return createCenterPanel();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        if (contentPane == null) {
            contentPaneInit();
        }

        return this.contentPane;
    }

    private void contentPaneInit() {
        JBTabbedPane jtab = new JBTabbedPane();
        contentPane = jtab;
        JLabel label;
        {
            int x = 1;
            int y = 1;
            int height = 30;
            int width = 600;
            int w = 170;

            JBPanel jbxxp = new JBPanel();
            jbxxp.setLayout(null);
            JBScrollPane jbxx = new JBScrollPane(jbxxp);
            jbxx.setAutoscrolls(true);
            jtab.addTab("前端请求解密&加密", jbxx);

            checkBox_format = new JBCheckBox("是否格式化JSON");
            checkBox_format.setBounds(x = 1, y, w, height);
            jbxxp.add(checkBox_format);

            checkBox_gzip = new JBCheckBox("是否启用流量压缩gzip");
            checkBox_gzip.setBounds(checkBox_format.getX() + checkBox_format.getWidth() + 3, y, w, height);
            jbxxp.add(checkBox_gzip);

            checkBox_mark = new JBCheckBox("是否启用数据加签");
            checkBox_mark.setBounds(checkBox_gzip.getX() + checkBox_gzip.getWidth() + 3, y, w, height);
            jbxxp.add(checkBox_mark);

            checkBox_aes = new JBCheckBox("是否启用aes加解密");
            checkBox_aes.setBounds(checkBox_mark.getX() + checkBox_mark.getWidth() + 3, y, w, height);
            jbxxp.add(checkBox_aes);

            label = new JBLabel("前端F12窗口localStore里的cowboy(输入我了可以不用输入楼下的楼下↓↓这个):");
            label.setBounds(x = 1, y += height + 5, 500, height);
            jbxxp.add(label);
            textField_cowboy = new JBTextField(TEMP_COWBODY);
            textField_cowboy.setBounds(x + 500 + 5, y, 500, height);
            jbxxp.add(textField_cowboy);

            label = new JBLabel("cowboy解码的AESKEY(我和楼上的是为了帮你计算楼下这个):");
            label.setBounds(x = 1, y += height + 5, 500, height);
            jbxxp.add(label);
            textField_cowboyAesKey = new JBTextField(TEMP_COWBODYAESKEY);
            textField_cowboyAesKey.setBounds(x + 500 + 5, y, 500, height);
            jbxxp.add(textField_cowboyAesKey);

            label = new JBLabel("Session中的Aeskey(输入我了可以不用输入楼上↑这2个):");
            label.setBounds(x = 1, y += height + 5, 500, height);
            jbxxp.add(label);
            textField_aesKey = new JBTextField(TEMP_AESKEY);
            textField_aesKey.setBounds(x + 500 + 5, y, 500, height);
            jbxxp.add(textField_aesKey);

            label = new JBLabel("Header中的crux:");
            label.setBounds(x = 1, y += height + 5, w, height);
            jbxxp.add(label);
            textField_crux = new JBTextField(TEMP_CRUX);
            textField_crux.setBounds(x + w + 5, y, 500, height);
            jbxxp.add(textField_crux);

            button_decode = new JButton("执行解码");
            button_decode.setToolTipText("把密文解密成明文");
            button_decode.addActionListener(e -> SwingUtilities.invokeLater(() -> decode()));
            button_decode.setBounds(x + w + 500 + 20, y, 100, height + 15);
            jbxxp.add(button_decode);

            button_encode = new JButton("执行加码");
            button_encode.setToolTipText("把明文加密成密文");
            button_encode.addActionListener(e -> SwingUtilities.invokeLater(() -> encode()));
            button_encode.setBounds(x + w + 500 + 20 + 100 + 20, y, 100, height + 15);
            jbxxp.add(button_encode);

            label = new JBLabel("原始文本:");
            label.setBounds(x = 1, y += height + 5 + 15, w, height);
            jbxxp.add(label);
            textArea_requst = new JBTextArea();
            JBScrollPane jbScrollPane = new JBScrollPane(textArea_requst);
            jbScrollPane.setAutoscrolls(true);
            jbScrollPane.setBounds(x, y += height + 5, 600, 200);
            textArea_requst.setEnabled(true);
            textArea_requst.setEditable(true);
            textArea_requst.setLineWrap(true);
            jbxxp.add(jbScrollPane);

            label = new JBLabel("结果:");
            label.setBounds(x = 1, y += 200 + 5, 180, height);
            jbxxp.add(label);
            textArea_result = new JBTextArea();
            jbScrollPane = new JBScrollPane(textArea_result);
            jbScrollPane.setAutoscrolls(true);
            jbScrollPane.setBounds(x, y += height + 5, 600, 400);
            textArea_result.setEnabled(true);
            textArea_result.setEditable(true);
            // textArea_result.setLineWrap(true);
            jbxxp.add(jbScrollPane);
        }

        //设置点默认值
        initDefualtValues();
    }

    public void encode() {
        String txt = StringUtil.trim(textArea_requst.getText());

        if (StringUtil.isBlank(txt) || StringUtil.isNotBlank(txt)) {
            textArea_result.setText("这个功能还没有实现哦!");
            return;
        }

        if (StringUtil.isBlank(txt)) {
            textArea_result.setText("请输入请求内容!");
            return;
        }
        textArea_result.setText("");
       /* if (checkBox_mark.isSelected()) {
            textArea_result.setText("mark 是否启用数据加签 未勾选!应该不需要解密!");
            return;
        }*/
        String aeskey = StringUtil.get(StringUtil.trim(textField_aesKey.getText()), "");
        String crux = StringUtil.get(StringUtil.trim(textField_crux.getText()), "");
        String cowboy = StringUtil.get(StringUtil.trim(textField_cowboy.getText()), "");
        String cowboyAeskey = StringUtil.get(StringUtil.trim(textField_cowboyAesKey.getText()), "");
        TEMP_AESKEY = aeskey;
        TEMP_CRUX = crux;
        TEMP_COWBODY = cowboy;
        TEMP_COWBODYAESKEY = cowboyAeskey;

        if (checkBox_aes.isSelected()) {
            try {
                if (StringUtil.isBlank(aeskey) && StringUtil.isNotBlank(cowboy)) {
                    cowboy = unAes(cowboy, cowboyAeskey, null);
                    aeskey = unAes(cowboy, aeskey, crux);

                    apendResult(String.format("根据cowboy解密aeskey结果: %s", aeskey));
                    //textField_aesKey.setText("9b645baa78cf41e180e4751abc46d6c2");
                }

                txt = unAes(txt, aeskey, crux);
            } catch (Throwable e) {
                apendResult("AES解码失败:" + ExceptionUtil.toStringLines(e, 40));
            }
        }

        if (checkBox_gzip.isSelected()) {
            try {
                txt = unGZip(txt);
            } catch (Throwable e) {
                apendResult("GZIP解压缩失败:" + ExceptionUtil.toStringLines(e, 40));
            }
        }

        if (checkBox_format.isSelected()) {
            txt = JSON.toJSONString(JSON.parse(txt), SerializerFeature.PrettyFormat,
                    SerializerFeature.WriteMapNullValue);
        }

        textArea_result.setText(txt);
    }

    public void decode() {
        String txt = StringUtil.trim(textArea_requst.getText());
        if (StringUtil.isBlank(txt)) {
            textArea_result.setText("请输入请求内容!");
            return;
        }
        textArea_result.setText("");
       /* if (checkBox_mark.isSelected()) {
            textArea_result.setText("mark 是否启用数据加签 未勾选!应该不需要解密!");
            return;
        }*/
        String aeskey = StringUtil.get(StringUtil.trim(textField_aesKey.getText()), "");
        String crux = StringUtil.get(StringUtil.trim(textField_crux.getText()), "");
        String cowboy = StringUtil.get(StringUtil.trim(textField_cowboy.getText()), "");
        String cowboyAeskey = StringUtil.get(StringUtil.trim(textField_cowboyAesKey.getText()), "");
        TEMP_AESKEY = aeskey;
        TEMP_CRUX = crux;
        TEMP_COWBODY = cowboy;
        TEMP_COWBODYAESKEY = cowboyAeskey;

        if (checkBox_aes.isSelected()) {
            try {
                if (StringUtil.isBlank(aeskey) && StringUtil.isNotBlank(cowboy)) {
                    cowboy = unAes(cowboy, cowboyAeskey, null);
                    aeskey = unAes(cowboy, aeskey, crux);

                    apendResult(String.format("根据cowboy解密aeskey结果: %s", aeskey));
                    //textField_aesKey.setText("9b645baa78cf41e180e4751abc46d6c2");
                }

                txt = unAes(txt, aeskey, crux);
            } catch (Throwable e) {
                apendResult("AES解码失败:" + ExceptionUtil.toStringLines(e, 40));
            }
        }

        if (checkBox_gzip.isSelected()) {
            try {
                txt = unGZip(txt);
            } catch (Throwable e) {
                apendResult("GZIP解压缩失败:" + ExceptionUtil.toStringLines(e, 40));
            }
        }

        if (checkBox_format.isSelected()) {
            txt = JSON.toJSONString(JSON.parse(txt), SerializerFeature.PrettyFormat,
                    SerializerFeature.WriteMapNullValue);
        }

        textArea_result.setText(txt);
    }

    public void apendResult(String s) {
        textArea_result.setText(textArea_requst.getText() + "\n" + s);
    }

    public static String unAes(String txt, String aeskey, String crux) throws Exception {
        if (aeskey != null && !aeskey.trim().equals("") && crux != null) {
            aeskey = (crux + aeskey).substring(0, 32);
        }

        if (aeskey != null && !aeskey.trim().equals("")) {
            txt = decrypt(txt, aeskey);
        }

        return txt;
    }

    public static String decrypt(String crypto, String key) throws Exception {
        if (crypto == null) {
            return null;
        }

        byte[] base64Key = Base64.decodeBase64(key.getBytes("UTF-8"));
        byte[] data = Hex.decodeHex((new String(Base64.decodeBase64(crypto.getBytes("UTF-8")))).toCharArray());
        Cipher cipher = getCipher(2, base64Key, "db2139561c9fe068".getBytes("UTF-8"));
        String string = new String(cipher.doFinal(data), "UTF-8");
        return string;
    }

    public static Cipher getCipher(int mode, byte[] key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CTR/NOPadding");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        cipher.init(mode, secretKeySpec, new IvParameterSpec(iv));
        return cipher;
    }

    public static String unGZip(String txt) throws IOException {
        Base64 bo = new Base64();
        Object decodes = bo.decode(txt.getBytes());
        byte[] ret = null;
        if (decodes != null) {
            ret = (byte[]) ((byte[]) decodes);
        } else {
            ret = "".getBytes();
        }

        return uncompress(ret, "UTF-8");
    }

    private static String uncompress(byte[] bytes, String encoding) throws IOException {
        String ret = null;
        InputStream in = new ByteArrayInputStream(bytes);
        Throwable ex = null;

        try {
            GZIPInputStream gi = new GZIPInputStream(in);
            Throwable ex2 = null;
            try {
                ret = IOUtils.toString(gi, encoding);
            } finally {
                if (gi != null) {
                    if (ex2 != null) {
                        try {
                            gi.close();
                        } catch (Throwable var31) {
                            ex2.addSuppressed(var31);
                        }
                    } else {
                        gi.close();
                    }
                }

            }
        } finally {
            if (in != null) {
                if (ex != null) {
                    try {
                        in.close();
                    } catch (Throwable var30) {
                        ex.addSuppressed(var30);
                    }
                } else {
                    in.close();
                }
            }

        }

        return ret;
    }

    public void initDefualtValues() {
        checkBox_format.setSelected(true);
        checkBox_aes.setSelected(true);
        checkBox_gzip.setSelected(true);
        checkBox_mark.setSelected(true);
        File home = ProjectNCConfigUtil.getNCHome();
        //D:\runtime\BIPV3_JETCOM\hotwebs\nccloud\WEB-INF
        File hotwebs = new File(home, "hotwebs");
        File nccloud = new File(hotwebs, "nccloud");
        File webinf = new File(nccloud, "WEB-INF");
        File config = new File(webinf, "config");
        File miscellaneous = new File(config, "miscellaneous.xml");
        if (miscellaneous.isFile()) {
            Document document = null;
            try {
                document = XmlUtil.xmlFile2Document2(miscellaneous);
                Element root = XmlUtil.getRootElement(document);
                Map<String, Object> mp = XmlUtil.xmlToMap(root);
                if (CollUtil.isNotEmpty(mp)) {
                    checkBox_gzip.setSelected("true".equalsIgnoreCase((String) mp.get("gzip")));
                    checkBox_aes.setSelected("true".equalsIgnoreCase((String) mp.get("aesKey")));
                    checkBox_mark.setSelected("true".equalsIgnoreCase((String) mp.get("mark")));
                }
            } catch (Throwable exception) {
                LogUtil.error(exception.getMessage(), exception);
                checkBox_format.setSelected(true);
                checkBox_aes.setSelected(true);
                checkBox_gzip.setSelected(true);
                checkBox_mark.setSelected(true);
            } finally {
            }
        }

        textField_aesKey.setText(TEMP_AESKEY);
        textField_crux.setText(TEMP_CRUX);
        textField_cowboy.setText(TEMP_COWBODY);
        textField_cowboyAesKey.setText(TEMP_COWBODYAESKEY);
        textArea_result.setText("说明：输入了session里的aesKey后 就可以不输入 cowboy\n" +
                "，如果没输入aesKey 输入了cowboy 会自动解码cowboy得到aeskey！\n" +
                "cowboy其实就是aeskey加密后的字符串！\ncowboyAeskey是加密aeskey成为cowboy的密匙key 一般用当前默认值就好。"
        );
    }

    @Nullable
    @Override
    protected String getDimensionServiceKey() {
        return this.getClass().getName();
    }

    private void onCancel() {
        dispose();
    }
}
