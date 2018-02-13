package io.coinpeeker.bot_hotssan.service;

import com.google.common.collect.Maps;
import io.coinpeeker.bot_hotssan.common.CommonConstant;
import io.coinpeeker.bot_hotssan.module.HotssanUpdateHandler;
import io.coinpeeker.bot_hotssan.utils.AuthUtils;
import io.coinpeeker.bot_hotssan.utils.Commander;
import io.coinpeeker.bot_hotssan.utils.HttpUtils;
import io.coinpeeker.bot_hotssan.utils.MessageUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.api.objects.Update;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;


@Service
public class HotssanService implements HotssanUpdateHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(HotssanService.class);
    private static Map<String, LocalDateTime> temp = Maps.newHashMap();

    @Value("${property.hotssan_id}")
    private String apiKey;

    @Autowired
    private AuthUtils authUtils;

    @Autowired
    private HttpUtils httpUtils;

    @Autowired
    private MessageUtils messageUtils;

    @Autowired
    private Commander commander;

    @Override
    public void deleteWebhook() {
        String deleteWebhookUrl = CommonConstant.URL_TELEGRAM_BASE + apiKey + CommonConstant.METHOD_TELEGRAM_DELETE_WEBHOOK;
        try {
            httpUtils.get(deleteWebhookUrl);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    @Override
    public boolean setWebhook(String url) {
        deleteWebhook();

        String setWebhookUrl = CommonConstant.URL_TELEGRAM_BASE + apiKey + CommonConstant.METHOD_TELEGRAM_SET_WEBHOOK;

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("url", url + "/webhook"));

        try {
            httpUtils.post(setWebhookUrl, params);
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
            return;
        }

        String url = CommonConstant.URL_TELEGRAM_BASE + apiKey + CommonConstant.METHOD_TELEGRAM_SENDMESSAGE;
        long chatId = update.getMessage().getChatId();
        String instruction = update.getMessage().getText();
        StringBuilder message = new StringBuilder();

        Timer m_timer = new Timer();
        TimerTask m_task = new TimerTask() {
            @Override
            public void run() {
                temp.remove(instruction);
            }
        };

        if (temp.containsKey(instruction) && Duration.between(temp.get(instruction), LocalDateTime.now()).toMillis() < 3000) {
            m_timer.schedule(m_task, 3000);
        }


        if (temp.isEmpty() || !temp.containsKey(instruction)) {
            temp.put(instruction, LocalDateTime.now());
            if (!authUtils.isAuthenticated(chatId)) {
                message.append("등록되지 않은 사용자입니다.\n사용자 아이디 등록을 요청하세요 : ");
                message.append(chatId);
            } else {
                temp.put(instruction, LocalDateTime.now());
                message.append(commander.execute(instruction));
                m_timer.schedule(m_task, 3000);
                messageUtils.sendMessage(url, chatId, message.toString());
            }
        }














    }

    public void removedTemp(){

    }
}
