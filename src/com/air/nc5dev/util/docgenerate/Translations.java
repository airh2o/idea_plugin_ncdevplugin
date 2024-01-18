package com.air.nc5dev.util.docgenerate;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Translations {
    /**
     * sentLen
     */
    @JsonProperty("sentLen")
    private SentLen sentLen;
    /**
     * text
     */
    @JsonProperty("text")
    private String text;
    /**
     * to
     */
    @JsonProperty("to")
    private String to;
    /**
     * transliteration
     */
    @JsonProperty("transliteration")
    private Transliteration transliteration;

    public SentLen getSentLen() {
        return sentLen;
    }

    public String getText() {
        return text;
    }

    public String getTo() {
        return to;
    }

    public Transliteration getTransliteration() {
        return transliteration;
    }

    public void setSentLen(SentLen sentLen) {
        this.sentLen = sentLen;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public void setTransliteration(Transliteration transliteration) {
        this.transliteration = transliteration;
    }
}
