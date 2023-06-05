package org.tangsu.mstsc.entity;

import lombok.Data;

import java.util.Set;

@Data
public class MstscEntity implements IEntity {
    String pk;
    String ip;
    String user;
    String pass;
    int order1;
    String code;
    String title;
    String memo;
    String port;
    String class_id;
    String desktopwidth;
    String desktopheight;
    String screen_mode_id;
    String session_bpp;
    String winposstr;
    String compression;
    String displayconnectionbar;
    String disable_wallpaper;
    String disable_themes;
    String redirectclipboard;
    String autoreconnection;
    int dr = 0;

    @Override
    public String getTableName() {
        return "t_mstsc";
    }

    @Override
    public String getIdName() {
        return "pk";
    }

    @Override
    public Set<String> getFieadNames() {
        return IEntity.getFieadNames(MstscEntity.class);
    }
}
