package io.coinpeeker.bot_hotssan.utils;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class HttpUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpUtils.class);

    /**
     * HttpGet
     *
     * @param url
     * @return
     * @throws IOException
     */
    public CloseableHttpResponse get(String url) throws IOException {
        RequestConfig globalConfig = RequestConfig.custom()
                .setCookieSpec(CookieSpecs.DEFAULT)
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(globalConfig)
                .build();

        RequestConfig localConfig = RequestConfig.copy(globalConfig)
                .setCookieSpec(CookieSpecs.STANDARD)
                .build();

        HttpGet httpGet = new HttpGet(url);
        httpGet.setConfig(localConfig);

        CloseableHttpResponse response = httpClient.execute(httpGet);
        return response;
    }

    /**
     * HttpPost
     *
     * @param url
     * @param params
     * @return
     * @throws IOException
     */
    public CloseableHttpResponse post(String url, List<NameValuePair> params) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);

        httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        CloseableHttpResponse response = httpClient.execute(httpPost);

        return response;
    }

    /**
     * API 호출결과를 JSONObject 로 리턴받는 메소드, apiResponse 의 최상단이 Array 인 경우
     *
     * @param url
     * @return
     * @throws IOException
     */
    public JSONObject getResponseByArray(String url) throws IOException {
        CloseableHttpResponse httpResponse = get(url);
        JSONArray jsonArray = new JSONArray(EntityUtils.toString(httpResponse.getEntity(), "UTF-8"));
        JSONObject jsonObject = new JSONObject(jsonArray.get(0).toString());

        return jsonObject;
    }

    /**
     * API 호출결과를 JSONObject 로 리턴받는 메소드, apiResponse 의 최상단이 Object 인 경우
     *
     * @param url
     * @return
     * @throws IOException
     */
    public JSONObject getResponseByObject(String url) throws IOException {
        CloseableHttpResponse httpResponse = get(url);
        JSONObject jsonObject = new JSONObject(EntityUtils.toString(httpResponse.getEntity(), "UTF-8"));

        return jsonObject;
    }
}
