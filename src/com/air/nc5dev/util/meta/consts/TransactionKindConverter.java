
package com.air.nc5dev.util.meta.consts;


public class TransactionKindConverter {
    public TransactionKindConverter() {
    }

    public Object getConvertResult(String value) {
        Integer i = null;
        if ("REQUIRED".equalsIgnoreCase(value)) {
            i = TransactionKind.PROPAGATION_REQUIRED.ordinal();
        } else if ("SUPPORTS".equalsIgnoreCase(value)) {
            i = TransactionKind.PROPAGATION_SUPPORTS.ordinal();
        } else if ("MANDATORY".equalsIgnoreCase(value)) {
            i = TransactionKind.PROPAGATION_MANDATORY.ordinal();
        } else if ("REQUIRES_NEW".equalsIgnoreCase(value)) {
            i = TransactionKind.PROPAGATION_REQUIRES_NEW.ordinal();
        } else if ("NOT_SUPPORTED".equalsIgnoreCase(value)) {
            i = TransactionKind.PROPAGATION_NOT_SUPPORTED.ordinal();
        } else if ("NEVER".equalsIgnoreCase(value)) {
            i = TransactionKind.PROPAGATION_NEVER.ordinal();
        }

        return i;
    }
}
