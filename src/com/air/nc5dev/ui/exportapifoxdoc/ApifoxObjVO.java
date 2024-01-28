package com.air.nc5dev.ui.exportapifoxdoc;

import lombok.Data;

import java.util.List;

@Data
public class ApifoxObjVO {
    String type = "object";
    String title = "";
    Object properties;
    List<String> required;
    Object items;
}