package com.air.nc5dev.util.docgenerate;

import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RequestUtil {

    public static String post(String url, String body) throws IOException {
        /* 创建HttpClient */
        CloseableHttpClient httpClient = HttpClients.createDefault();

        /* httpPost */
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(new StringEntity(body));
        CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
        try {
            HttpEntity httpEntity = httpResponse.getEntity();
            String json = EntityUtils.toString(httpEntity, "UTF-8");
            EntityUtils.consume(httpEntity);
            return json;
        } finally {
            if (httpResponse != null) {
                httpResponse.close();
            }
        }
    }
}
