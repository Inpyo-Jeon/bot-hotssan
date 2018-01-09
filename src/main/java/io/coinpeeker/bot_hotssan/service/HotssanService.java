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
import org.telegram.telegrambots.api.objects.Update;

import java.io.IOException;


@Service
public class HotssanService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HotssanService.class);

    @Value("${property.hotssan_id}")
    private String apiKey;

    public void commandHandler(Update update) {

        // validation check
        if (update == null) {
            LOGGER.error("#$#$#$ update object is null");
            return ;
        }

        long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText();

        String sendMessage = getBaseUrl()
                + "/sendmessage?chat_id="
                + String.valueOf(chatId)
                + "&text=대답:"
                + text;

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(sendMessage);

        try {
            CloseableHttpResponse response = httpClient.execute(httpGet);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public boolean setNgrok(String url) {
        CloseableHttpClient httpClient = HttpClients.createDefault();

        String deleteUrl = getBaseUrl() + "/deleteWebhook";
        String setUrl = getBaseUrl() + "/setWebhook?url=" + url + "/webhook";

        HttpGet deleteGet = new HttpGet(deleteUrl);
        HttpGet setMethod = new HttpGet(setUrl);

        try {
            CloseableHttpResponse response = httpClient.execute(deleteGet);
            CloseableHttpResponse response2 = httpClient.execute(setMethod);
        } catch (IOException ioException) {
            ioException.printStackTrace();
            return false;
        }

        return true;
    }

    private String getBaseUrl() {
        return CommonConstant.URL_TELEGRAM_BASE + apiKey;
    }
}
