package com.air.nc5dev.acion.meta;

/**
 *
 */
public class MakeBmf41bBillAction extends AbstractMakeBmfAction {
    @Override
    public String getBmfTemplateName() {
        return "singleBdemo.bmf";
    }

    @Override
    public int getType() {
        return 3;
    }
}
