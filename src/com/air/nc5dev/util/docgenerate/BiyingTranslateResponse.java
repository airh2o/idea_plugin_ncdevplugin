package com.air.nc5dev.util.docgenerate;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class BiyingTranslateResponse {

    /**
     * detectedLanguage
     */ /**
     * detectedLanguage : {"language":"en","score":1}
     * translations : [{"text":"房子","transliteration":{"text":"fáng zi","script":"Latn"},"to":"zh-Hans","sentLen":{"srcSentLen":[5],"transSentLen":[2]}}]
     */

    @JsonProperty("detectedLanguage")
    private DetectedLanguage detectedLanguage;
    /**
     * translations
     */
    @JsonProperty("translations")
    private List<Translations> translations;

    public DetectedLanguage getDetectedLanguage() {
        return detectedLanguage;
    }

    public List<Translations> getTranslations() {
        return translations;
    }

    public void setDetectedLanguage(DetectedLanguage detectedLanguage) {
        this.detectedLanguage = detectedLanguage;
    }

    public void setTranslations(List<Translations> translations) {
        this.translations = translations;
    }
}
