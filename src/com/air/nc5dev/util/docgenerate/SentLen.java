package com.air.nc5dev.util.docgenerate;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class SentLen {
    /**
     * srcSentLen
     */
    @JsonProperty("srcSentLen")
    private List<Integer> srcSentLen;
    /**
     * transSentLen
     */
    @JsonProperty("transSentLen")
    private List<Integer> transSentLen;

    public List<Integer> getSrcSentLen() {
        return srcSentLen;
    }

    public List<Integer> getTransSentLen() {
        return transSentLen;
    }

    public void setSrcSentLen(List<Integer> srcSentLen) {
        this.srcSentLen = srcSentLen;
    }

    public void setTransSentLen(List<Integer> transSentLen) {
        this.transSentLen = transSentLen;
    }
}
