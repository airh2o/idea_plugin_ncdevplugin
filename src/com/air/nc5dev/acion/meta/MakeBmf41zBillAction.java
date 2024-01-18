package com.air.nc5dev.acion.meta;

/**
 *
 */
public class MakeBmf41zBillAction extends AbstractMakeBmfAction {
    @Override
    public String getBmfTemplateName() {
        return "singleHdemo.bmf";
    }

    @Override
    public int getType() {
        return 4;
    }
}
