package com.air.nc5dev.ui.nccactionsearch;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xiaobiao
 * @version 2020/5/2
 */
public class Annotations {
    private static final List<Mapping> clasz = new ArrayList<Mapping>() {
        {
            add(new Mapping("request", "RequestMapping", "org.springframework.web.bind.annotation.RequestMapping"));
        }
    };

    private static final List<Mapping> methods = new ArrayList<Mapping>() {
        {
            add(new Mapping("request", "RequestMapping", "org.springframework.web.bind.annotation.RequestMapping"));
            add(new Mapping("delete", "DeleteMapping", "org.springframework.web.bind.annotation.DeleteMapping"));
            add(new Mapping("get", "GetMapping", "org.springframework.web.bind.annotation.GetMapping"));
            add(new Mapping("post", "PostMapping", "org.springframework.web.bind.annotation.PostMapping"));
            add(new Mapping("put", "PutMapping", "org.springframework.web.bind.annotation.PutMapping"));
            add(new Mapping("patch", "PatchMapping", "org.springframework.web.bind.annotation.PatchMapping"));
        }
    };

    public static List<Mapping> getClasz() {
        return clasz;
    }

    public static List<Mapping> getMethods() {
        return methods;
    }
}
