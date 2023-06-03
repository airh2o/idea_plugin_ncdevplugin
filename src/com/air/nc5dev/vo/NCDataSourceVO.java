package com.air.nc5dev.vo;

import com.air.nc5dev.enums.NcVersionEnum;
import com.air.nc5dev.util.NCPropXmlUtil;
import com.air.nc5dev.util.ProjectNCConfigUtil;
import com.air.nc5dev.util.StringUtil;
import com.air.nc5dev.util.ncutils.AESEncode;
import com.air.nc5dev.util.ncutils.NC5xEncode;
import com.air.nc5dev.util.ncutils.NC6xEncode;
import lombok.Data;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Objects;

/**
 * NC数据源
 *
 * @Author air Email: 209308343@qq.com
 * @Description
 * @Date 2021/8/3 0003 10:18
 **/
@Data
public class NCDataSourceVO {
    private transient Element element;
    private String dataSourceName;
    private String oidMark;
    private String databaseUrl;
    private String user;
    private String password;
    String passwordOrgin;
    private String driverClassName;
    private String databaseType;
    private String maxCon;
    private String minCon;
    private String dataSourceClassName;
    private String xaDataSourceClassName;
    private String conIncrement;
    private String conInUse;
    private String conIdle;
    private String isBase;

    public NCDataSourceVO(Element e, Element root) {
        this.element = e;
        read(this.element, root);
    }

    private void read(Element element, Element root) {
        dataSourceName = getSonElementTextContent(element, "dataSourceName", 0);
        oidMark = getSonElementTextContent(element, "oidMark", 0);
        databaseUrl = getSonElementTextContent(element, "databaseUrl", 0);
        user = getSonElementTextContent(element, "user", 0);
        password = getSonElementTextContent(element, "password", 0);
        passwordOrgin = password;
        password = new NC5xEncode().decode(password);
        if (NcVersionEnum.NCC.equals(ProjectNCConfigUtil.getNCVerSIon())) {
            if (root.getElementsByTagName("isEncode").getLength() > 0) {
                String isEncode = root.getElementsByTagName("isEncode").item(0).getTextContent();
                if ("true".equalsIgnoreCase(StringUtil.trim(isEncode))) {
                    password = AESEncode.decrypt(passwordOrgin);
                    if (password.equals("ufnull")) {
                        password = "";
                    }
                }
            }

        }
        driverClassName = getSonElementTextContent(element, "driverClassName", 0);
        databaseType = getSonElementTextContent(element, "databaseType", 0);
        maxCon = getSonElementTextContent(element, "maxCon", 0);
        minCon = getSonElementTextContent(element, "minCon", 0);
        dataSourceClassName = getSonElementTextContent(element, "dataSourceClassName", 0);
        xaDataSourceClassName = getSonElementTextContent(element, "xaDataSourceClassName", 0);
        conIncrement = getSonElementTextContent(element, "conIncrement", 0);
        conInUse = getSonElementTextContent(element, "conInUse", 0);
        conIdle = getSonElementTextContent(element, "conIdle", 0);
        isBase = getSonElementTextContent(element, "isBase", 0);
    }

    private String getSonElementTextContent(Element father, String tagName, int itemIndex) {
        NodeList elementsByTagName = element.getElementsByTagName(tagName);
        if (null == elementsByTagName || elementsByTagName.getLength() < itemIndex) {
            return "";
        }

        Node item = elementsByTagName.item(itemIndex);
        if (null == item) {
            return "";
        }

        return item.getTextContent() == null ? "" : item.getTextContent();
    }

    public NCDataSourceVO clone() throws CloneNotSupportedException {
        return (NCDataSourceVO) super.clone();
    }


    @Override
    public String toString() {
        return dataSourceName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NCDataSourceVO that = (NCDataSourceVO) o;
        return Objects.equals(element, that.element) &&
                Objects.equals(dataSourceName, that.dataSourceName) &&
                Objects.equals(oidMark, that.oidMark) &&
                Objects.equals(databaseUrl, that.databaseUrl) &&
                Objects.equals(user, that.user) &&
                Objects.equals(password, that.password) &&
                Objects.equals(driverClassName, that.driverClassName) &&
                Objects.equals(databaseType, that.databaseType) &&
                Objects.equals(maxCon, that.maxCon) &&
                Objects.equals(minCon, that.minCon) &&
                Objects.equals(dataSourceClassName, that.dataSourceClassName) &&
                Objects.equals(xaDataSourceClassName, that.xaDataSourceClassName) &&
                Objects.equals(conIncrement, that.conIncrement) &&
                Objects.equals(conInUse, that.conInUse) &&
                Objects.equals(conIdle, that.conIdle) &&
                Objects.equals(isBase, that.isBase);
    }

    @Override
    public int hashCode() {

        return Objects.hash(element, dataSourceName, oidMark, databaseUrl, user, password, driverClassName
                , databaseType, maxCon, minCon, dataSourceClassName, xaDataSourceClassName, conIncrement, conInUse,
                conIdle, isBase);
    }
}
