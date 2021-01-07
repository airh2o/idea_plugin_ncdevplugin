package com.air.nc5dev.vo;

import com.air.nc5dev.util.ncutils.NC5xEncode;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Objects;

public class NCDataSourceVO {
    private Element element;
    private String dataSourceName;
    private String oidMark;
    private String databaseUrl;
    private String user;
    private String password;
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

    public NCDataSourceVO() {
    }
    public NCDataSourceVO(Element e) {
        this.element = e;
        read(this.element);
    }

    private void read(Element element) {
        dataSourceName = getSonElementTextContent(element,"dataSourceName", 0);
        oidMark =  getSonElementTextContent(element,"oidMark", 0);
        databaseUrl = getSonElementTextContent(element,"databaseUrl", 0);
        user = getSonElementTextContent(element,"user", 0);
        password = getSonElementTextContent(element,"password", 0);
        password = new NC5xEncode().decode(password);
        driverClassName = getSonElementTextContent(element,"driverClassName", 0);
        databaseType = getSonElementTextContent(element,"databaseType", 0);
        maxCon = getSonElementTextContent(element,"maxCon", 0);
        minCon = getSonElementTextContent(element,"minCon", 0);
        dataSourceClassName = getSonElementTextContent(element,"dataSourceClassName", 0);
        xaDataSourceClassName = getSonElementTextContent(element,"xaDataSourceClassName", 0);
        conIncrement = getSonElementTextContent(element,"conIncrement", 0);
        conInUse = getSonElementTextContent(element,"conInUse", 0);
        conIdle = getSonElementTextContent(element,"conIdle", 0);
        isBase = getSonElementTextContent(element,"isBase", 0);
    }
    private String getSonElementTextContent(Element father, String tagName, int itemIndex){
        NodeList elementsByTagName = element.getElementsByTagName(tagName);
        if(null == elementsByTagName || elementsByTagName.getLength() < itemIndex){
            return "";
        }

        Node item = elementsByTagName.item(itemIndex);
        if(null == item){
            return "";
        }

        return item.getTextContent() == null ? "" : item.getTextContent();
    }
    public NCDataSourceVO clone() throws CloneNotSupportedException {
        return (NCDataSourceVO)super.clone();
    }

    public Element getElement() {
        return element;
    }

    public void setElement(Element element) {
        this.element = element;
    }

    public String getDataSourceName() {
        return dataSourceName;
    }

    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }

    public String getOidMark() {
        return oidMark;
    }

    public void setOidMark(String oidMark) {
        this.oidMark = oidMark;
    }

    public String getDatabaseUrl() {
        return databaseUrl;
    }

    public void setDatabaseUrl(String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public String getDatabaseType() {
        return databaseType;
    }

    public void setDatabaseType(String databaseType) {
        this.databaseType = databaseType;
    }

    public String getMaxCon() {
        return maxCon;
    }

    public void setMaxCon(String maxCon) {
        this.maxCon = maxCon;
    }

    public String getMinCon() {
        return minCon;
    }

    public void setMinCon(String minCon) {
        this.minCon = minCon;
    }

    public String getDataSourceClassName() {
        return dataSourceClassName;
    }

    public void setDataSourceClassName(String dataSourceClassName) {
        this.dataSourceClassName = dataSourceClassName;
    }

    public String getXaDataSourceClassName() {
        return xaDataSourceClassName;
    }

    public void setXaDataSourceClassName(String xaDataSourceClassName) {
        this.xaDataSourceClassName = xaDataSourceClassName;
    }

    public String getConIncrement() {
        return conIncrement;
    }

    public void setConIncrement(String conIncrement) {
        this.conIncrement = conIncrement;
    }

    public String getConInUse() {
        return conInUse;
    }

    public void setConInUse(String conInUse) {
        this.conInUse = conInUse;
    }

    public String getConIdle() {
        return conIdle;
    }

    public void setConIdle(String conIdle) {
        this.conIdle = conIdle;
    }

    public String getIsBase() {
        return isBase;
    }

    public void setIsBase(String isBase) {
        this.isBase = isBase;
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

        return Objects.hash(element, dataSourceName, oidMark, databaseUrl, user, password, driverClassName, databaseType, maxCon, minCon, dataSourceClassName, xaDataSourceClassName, conIncrement, conInUse, conIdle, isBase);
    }
}
