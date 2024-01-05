package com.air.nc5dev.acion.meta;

/**
 *
 */
public class MakeBmf4skBillAction extends AbstractMakeBmfAction {
    @Override
    public String getBmfTemplateName() {
      return "treeCard.bmf";
    }

    @Override
    public int getType() {
        return 1;
    }
}
