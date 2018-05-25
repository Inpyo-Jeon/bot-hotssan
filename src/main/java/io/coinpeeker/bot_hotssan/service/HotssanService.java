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
            httpUtils.post(setWebhookUrl, params, "");
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

        if (instruction != null) {
            if (!authUtils.isAuthenticated(chatId)) {
                message.append("등록되지 않은 사용자입니다.\n사용자 아이디 등록을 요청하세요 : ");
                message.append(chatId);
            } else {
                if ("Auto_Start".equals(instruction) || "Auto_Stop".equals(instruction) || "Auto_Status".equals(instruction)) {
                    if ("Auto_Status".equals(instruction)) {
                        message.append("-- 현재 자동 매수기능 : " + CommonConstant.autoTrade);
                    } else {
                        if (!authUtils.isAutoAuthenticated(chatId)) {
                            message.append("- 권한이 없으니 관리자 헬프 -");
                        } else {
                            if ("Auto_Start".equals(instruction)) {
                                CommonConstant.autoTrade = true;
                                message.append("자동 매수기능 ON(" + CommonConstant.autoTrade + ")");
                            } else {
                                CommonConstant.autoTrade = false;
                                message.append("자동 매수기능 OFF(" + CommonConstant.autoTrade + ")");
                            }
                            LOGGER.info("-- 자동 매수기능 : " + CommonConstant.autoTrade + " --");
                        }
                    }
                } else {
                    message.append(commander.execute(instruction));
                }
            }
            messageUtils.sendMessage(url, chatId, message.toString());
        }
    }
}