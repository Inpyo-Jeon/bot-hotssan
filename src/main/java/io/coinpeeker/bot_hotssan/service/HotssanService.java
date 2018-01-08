package io.coinpeeker.bot_hotssan.service;

import io.coinpeeker.bot_hotssan.common.CommonConstant;
import org.apache.http.HttpClientConnection;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;


@Service
public class HotssanService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HotssanService.class);

    @Value("${property.hotssan_id}")
    private String apiKey;

    public String getWebhook() {
        LOGGER.info("@#$@#$@#$ telegram call");

        String sendMessage = getBaseUrl() + "/sendmessage?chat_id=226524024&text=시끄러워임마";

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(sendMessage);

        try {
            CloseableHttpResponse response = httpClient.execute(httpGet);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "telegram_bot web_hook";
    }

    private String getBaseUrl() {
        return CommonConstant.URL_TELEGRAM_BASE + apiKey;
    }
}
