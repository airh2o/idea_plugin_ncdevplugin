package com.air.nc5dev.acion;

import cn.hutool.core.io.FileUtil;
import com.air.nc5dev.acion.base.AbstractIdeaAction;
import com.air.nc5dev.util.NCPropXmlUtil;
import com.air.nc5dev.util.XmlUtil;
import com.air.nc5dev.util.idea.LogUtil;
import com.air.nc5dev.vo.NCDataSourceVO;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.jz.dialog.ExportDlg;
import org.w3c.dom.Document;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * 导出NC6X单据所有SQL脚本        </br>
 * </br>
 * </br>
 * </br>
 *
 * @author air Email: 209308343@qq.com
 * @date 2020/4/1 0001 22:03
 * @Param
 * @return
 */
public class ExportBillAllAction extends AbstractIdeaAction {

    @Override
    protected void doHandler(AnActionEvent e) {
        File propXml = new File(System.getProperty("user.dir") + File.separator + "sqldiffProp.xml");
        propXml.getParentFile().mkdirs();
        propXml.deleteOnExit();
        File outdir = new File(e.getProject().getBasePath() + File.separator
                + "patchers" + File.separator + "单据脚本导出");
        outdir.mkdirs();
        NCDataSourceVO dataConf = NCPropXmlUtil.getDataSourceVOS().get(0);
        String dbType = dataConf.getDatabaseType().toUpperCase()
                .startsWith
                        ("ORACLE")
                ? dataConf.getDatabaseType() : "SQLSERVER";
        FileUtil.writeString("<?xml version=\"1.0\" encoding=\"UTF-8\"?>                          \n" +
                "<sqldiffprops>                          \n" +
                "    <defprop>design</defprop>                          \n" +
                "    <sqldiffprop>                          \n" +
                "        <propname>design</propname>                          \n" +
                "        <ip>" + dataConf.getDatabaseUrl() + "</ip>                          \n" +
                "        <username>" + dataConf.getUser() + "</username>                          \n" +
                "        <psw>" + dataConf.getPassword() + "</psw>                          \n" +
                "        <dbtype>" + dbType + "</dbtype>                          \n" +
                "        <dbname>desigindb</dbname>                          \n" +
                "        <port>1521</port>                          \n" +
                "        <outputpath>" + outdir.getPath() + "</outputpath>                          \n" +
                "        <nctype>NC6</nctype>                          \n" +
                "    </sqldiffprop>                          \n" +
                "</sqldiffprops>", propXml, "UTF-8");

        try {
            ExportDlg exportDlg = new ExportDlg();
            exportDlg.setTitle("导出节点相关SQL文件");
            exportDlg.setVisible(true);
            exportDlg.setOnOk(dlg -> {
                dlg.dispose();
                LogUtil.info("脚本保存路径： " + outdir.getPath());
                Desktop desktop = Desktop.getDesktop();
                try {
                    desktop.open(outdir.getParentFile());
                } catch (IOException e1) {
                    e1.printStackTrace();
                    LogUtil.error(e1.toString() + outdir.getParentFile().getPath(), e1);
                }
            });
        } catch (Throwable iae) {
            iae.printStackTrace();
            LogUtil.error(iae.toString() + outdir.getParentFile().getPath(), iae);
        }
    }
}
