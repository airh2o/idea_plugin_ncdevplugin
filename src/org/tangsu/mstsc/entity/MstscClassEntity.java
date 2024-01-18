package org.tangsu.mstsc.entity;

import lombok.Data;

import java.util.Set;

@Data
public class MstscClassEntity implements IEntity {
    int order1;
    String code;
    String title;
    String memo;
    String class_id;
    String father_class_id;


    @Override
    public String getTableName() {
        return "t_mstsc_class";
    }

    @Override
    public String getIdName() {
        return "class_id";
    }

    @Override
    public Set<String> getFieadNames() {
        return IEntity.getFieadNames(MstscClassEntity.class);
    }
}
