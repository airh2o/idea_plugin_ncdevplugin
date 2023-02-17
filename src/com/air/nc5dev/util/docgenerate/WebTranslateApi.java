package com.air.nc5dev.util.docgenerate;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class WebTranslateApi {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final TypeReference typeReference = new TypeReference<List<BiyingTranslateResponse>>() {
    };

    public static String translate(String text) {
        JsonResult json = new JsonResult();
        json.type = "EN2ZH_CN";
        json.elapsedTime = 1;
        json.translateResult = new ArrayList<>();
        json.translateResult.add(new LinkedList<>());
        json.translateResult.get(0).add(TranslateResult.builder()
                .src(text)
                .build());
        String result = text;
        String newTxt = null;
        try {
            String t2 = StrUtil.toUnderlineCase(text);
            t2 = StrUtil.replaceChars(t2, "_", " ");
            t2 = StrUtil.replaceChars(t2, "-", " ");
            json = JSON.parseObject(RequestUtil
                            .post("http://fanyi.youdao.com/translate?doctype=json&type=AUTO&i="
                                            + URLUtil.encode(t2)
                                    , JSON.toJSONString(json))
                    , JsonResult.class);
            newTxt = json.getFirst();
        } catch (Exception e) {
            result = text;
        }

        return StrUtil.isBlank(newTxt) ? text : newTxt;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class JsonResult {
        private String type;
        private int errorCode;
        private int elapsedTime;
        private List<List<TranslateResult>> translateResult;

        public String getFirst() {
            if (translateResult == null || translateResult.isEmpty()) {
                return null;
            }

            List<TranslateResult> ts = translateResult.get(0);
            if (ts == null || ts.isEmpty()) {
                return null;
            }

            TranslateResult t = ts.get(0);
            return t == null ? null : t.getTgt();
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class TranslateResult {
        private String src;
        private String tgt;
    }
}
