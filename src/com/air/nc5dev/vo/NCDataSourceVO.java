package com.air.nc5dev.vo;

import com.air.nc5dev.ui.compoment.SimpleListColumn;
import com.air.nc5dev.util.NCPassWordUtil;
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
    @SimpleListColumn(id = true, value = "数据源名称")
    private String dataSourceName;
    @SimpleListColumn(value = "OID标记", sort = 20000)
    private String oidMark;
    @SimpleListColumn("URL")
    private String databaseUrl;
    @SimpleListColumn("用户")
    private String user;
    @SimpleListColumn("密码")
    private String password;
    String passwordOrgin;
    private String driverClassName;
    @SimpleListColumn("数据库类型")
    private String databaseType;
    private String maxCon;
    private String minCon;
    private String dataSourceClassName;
    private String xaDataSourceClassName;
    private String conIncrement;
    private String conInUse;
    private String conIdle;
    private String isBase;
    String ncHome;

    public NCDataSourceVO(String ncHome, Element e, Element root) {
        this.element = e;
        this.ncHome = ncHome;
        read(this.element, root);
    }

    private void read(Element element, Element root) {
        dataSourceName = getSonElementTextContent(element, "dataSourceName", 0);
        oidMark = getSonElementTextContent(element, "oidMark", 0);
        databaseUrl = getSonElementTextContent(element, "databaseUrl", 0);
        user = getSonElementTextContent(element, "user", 0);
        password = getSonElementTextContent(element, "password", 0);
        String passwordOrg = getSonElementTextContent(element, "password", 0);
        passwordOrgin = password;
        String isEncode = null;
        if (root.getElementsByTagName("isEncode").getLength() > 0) {
            isEncode = root.getElementsByTagName("isEncode").item(0).getTextContent();
        }
        password = NCPassWordUtil.decode(dataSourceName, null, passwordOrgin, isEncode);
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
