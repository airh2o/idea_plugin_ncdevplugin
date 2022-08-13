package com.air.nc5dev.util.docgenerate;

/**
 * @author xiaobiao
 * @version 2020/9/26
 */
public class Template {

    private String setter = "/**\n" +
            " * set field ${FIELD_COMMENT} \n" +
            " *\n" +
            " * @param ${FIELD_NAME} ${FIELD_COMMENT}\n" +
            " */";

    private String getter = "/**\n" +
            " * get field ${FIELD_COMMENT}\n" +
            " *  \n" +
            " * @return ${FIELD_NAME} ${FIELD_COMMENT}\n" +
            " */";

    public String getSetter() {
        return setter;
    }

    public void setSetter(String setter) {
        this.setter = setter;
    }

    public String getGetter() {
        return getter;
    }

    public void setGetter(String getter) {
        this.getter = getter;
    }
}
