//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.air.nc5dev.util.meta.consts;

public class DataTypeStyleConverter {
    public DataTypeStyleConverter() {
    }

    public Object getConvertResult(String value) {
        Integer i = 300;
        if ("SINGLE".equalsIgnoreCase(value)) {
            i = 300;
        } else if ("ARRAY".equalsIgnoreCase(value)) {
            i = 301;
        } else if ("LIST".equalsIgnoreCase(value)) {
            i = 302;
        } else if ("VECTOR".equalsIgnoreCase(value)) {
            i = 303;
        } else if ("SET".equalsIgnoreCase(value)) {
            i = 304;
        } else if ("REF".equalsIgnoreCase(value)) {
            i = 305;
        }

        return i;
    }

    public String getConvertBefor(Integer i) {
        if (Integer.valueOf(300).equals(i)) {
            return "SINGLE";
        }
        if (Integer.valueOf(301).equals(i)) {
            return "ARRAY";
        }
        if (Integer.valueOf(302).equals(i)) {
            return "LIST";
        }
        if (Integer.valueOf(303).equals(i)) {
            return "VECTOR";
        }
        if (Integer.valueOf(304).equals(i)) {
            return "SET";
        }
        if (Integer.valueOf(305).equals(i)) {
            return "REF";
        }

        return "SINGLE";
    }
}
