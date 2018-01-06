package io.coinpeeker.bot_hotssan.service;

import org.apache.http.HttpClientConnection;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;


@Service
public class HotssanService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HotssanService.class);


    private static final String url = "https://api.telegram.org/bot541098487:AAGLR6VXrTIUoKcxn8R8axIcvvBlruI9kuc/sendmessage?chat_id=226524024&text=알았어임마";
    public String getWebhook() {
        LOGGER.info("@#$@#$@#$ telegram call");

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);

        try {
            CloseableHttpResponse response = httpClient.execute(httpGet);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "telegram_bot web_hook";
    }
}
