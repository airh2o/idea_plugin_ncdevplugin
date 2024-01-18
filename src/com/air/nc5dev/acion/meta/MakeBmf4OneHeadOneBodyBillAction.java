package com.air.nc5dev.acion.meta;

/**
 * NC主子表单据
 */
public class MakeBmf4OneHeadOneBodyBillAction extends AbstractMakeBmfAction {
    @Override
    public String getBmfTemplateName() {
      return "demo.bmf";
    }

    @Override
    public int getType() {
        return 0;
    }
}
