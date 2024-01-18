package com.air.nc5dev.util.meta.consts;

public enum DataTypeStyle {
    SINGLE("SINGLE"),
    REF("REF"),
    LIST("LIST"),
    ARRAY("ARRAY");

    private String name;

    private DataTypeStyle(String name) {
        this.name = name;
    }

    public String value() {
        return this.name;
    }
}
