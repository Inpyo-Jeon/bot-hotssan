package io.coinpeeker.bot_hotssan.service;

import io.coinpeeker.bot_hotssan.common.CommonConstant;
import io.coinpeeker.bot_hotssan.module.HotssanUpdateHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.api.objects.Update;

import java.io.IOException;


@Service
public class HotssanService implements HotssanUpdateHandler{

    private static final Logger LOGGER = LoggerFactory.getLogger(HotssanService.class);

    @Value("${property.hotssan_id}")
    private String apiKey;

    @Autowired
    private ExchangeService exchangeService;

    @Override
    public void deleteWebhook() {
        String deleteWebhookUrl = CommonConstant.URL_TELEGRAM_BASE + apiKey + "/deleteWebhook";
        LOGGER.info("@#$@#$@#$ delete : {}", deleteWebhookUrl);
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(deleteWebhookUrl);

        try {
            CloseableHttpResponse response = httpClient.execute(httpGet);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    @Override
    public boolean setWebhook(String url) {

        deleteWebhook();

        String setWebhookUrl = CommonConstant.URL_TELEGRAM_BASE + apiKey
                + "/setWebhook?url="
                + url
                + "/webhook";

        LOGGER.info("@#$@#$@#$ set : {}", setWebhookUrl);
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(setWebhookUrl);

        try {
            CloseableHttpResponse response = httpClient.execute(httpGet);
        } catch (IOException ioException) {
            ioException.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void updateHandler(Update update) {

        // validation check
        if (update == null) {
            LOGGER.error("#$#$#$ update object is null");
            return ;
        }

        long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText();

        String sendMessage = CommonConstant.URL_TELEGRAM_BASE + apiKey
                + "/sendmessage?chat_id="
                + String.valueOf(chatId)
                + "&text=대답:"
                + text
                + "환율:"
                + exchangeService.getUSDExchangeRate();

        LOGGER.info("@#$@#$@#$ {}", sendMessage);

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(sendMessage);

        try {
            CloseableHttpResponse response = httpClient.execute(httpGet);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }



}
