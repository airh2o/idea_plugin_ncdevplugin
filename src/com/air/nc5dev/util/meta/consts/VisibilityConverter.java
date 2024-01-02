package com.air.nc5dev.util.meta.consts;

public class VisibilityConverter {
    public VisibilityConverter() {
    }

    public Object getConvertResult(String value) {
        Integer i = 0;
        if ("public".equalsIgnoreCase(value)) {
            i = 0;
        } else if ("protected".equalsIgnoreCase(value)) {
            i = 1;
        } else if ("private".equalsIgnoreCase(value)) {
            i = 2;
        } else if ("friendly".equalsIgnoreCase(value)) {
            i = 3;
        }

        return i;
    }

    public static String getNameOfVisibility(Integer i) {
        if (i != null) {
            if (i == 0) {
                return "public";
            } else if (i == 1) {
                return "protected";
            } else if (2 == i) {
                return "private";
            } else if (3 == i) {
                return "friendly";
            }
        }

        return "public";
    }
}
