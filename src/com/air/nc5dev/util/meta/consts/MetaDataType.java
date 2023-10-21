package com.air.nc5dev.util.meta.consts;

public enum MetaDataType {
    BMF(".bmf"),
    BPF(".bpf"),
    MBE(".mbe"),
    MBP(".mbp"),
    DEVBMF(".bmf"),
    DEVBPF(".bpf");

    private String value;

    private MetaDataType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public String getName() {
        return this.value.substring(1, this.value.length());
    }
}
