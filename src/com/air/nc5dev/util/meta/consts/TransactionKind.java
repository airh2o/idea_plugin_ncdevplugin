package com.air.nc5dev.util.meta.consts;

public enum TransactionKind {
    PROPAGATION_REQUIRED,
    PROPAGATION_SUPPORTS,
    PROPAGATION_MANDATORY,
    PROPAGATION_REQUIRES_NEW,
    PROPAGATION_NOT_SUPPORTED,
    PROPAGATION_NEVER;

    private TransactionKind() {
    }

    public static final TransactionKind getInstance(int index) {
        switch (index) {
            case 0:
                return PROPAGATION_REQUIRED;
            case 1:
                return PROPAGATION_SUPPORTS;
            case 2:
                return PROPAGATION_MANDATORY;
            case 3:
                return PROPAGATION_REQUIRES_NEW;
            case 4:
                return PROPAGATION_NOT_SUPPORTED;
            case 5:
                return PROPAGATION_NEVER;
            default:
                return null;
        }
    }
}
