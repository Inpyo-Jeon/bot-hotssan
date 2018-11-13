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

    @Value("${hotssan_id}")
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
    public void updateHandler(Update update) throws IOException {

        // validation check
        if (update == null) {
            LOGGER.error("#$#$#$ update object is null");
            return;
        }

        String url = CommonConstant.URL_TELEGRAM_BASE + apiKey + CommonConstant.METHOD_TELEGRAM_SENDMESSAGE;

        long chatId;
        String instruction;

        if (update.getEditedMessage() == null) {
            chatId = update.getMessage().getChatId();
            instruction = update.getMessage().getText();
        } else {
            chatId = update.getEditedMessage().getChatId();
            instruction = update.getEditedMessage().getText();
        }

        StringBuilder message = new StringBuilder();

        if (instruction != null) {
            if (!authUtils.isAuthenticated(chatId)) {
                message.append("등록되지 않은 사용자입니다.\n사용자 아이디 등록을 요청하세요 : ");
                message.append(chatId);
            } else {
                if ("/auto_start".equals(instruction) || "/auto_stop".equals(instruction) || "/auto_status".equals(instruction) || "/auto_j_start".equals(instruction) || "/auto_j_stop".equals(instruction)) {
                    if ("/auto_status".equals(instruction)) {
                        message.append("모임 자동 매수기능 : " + CommonConstant.autoTrade);
                        message.append("\n");
                        message.append("개인 자동 매수기능 : " + CommonConstant.inpyoTrade);
                    } else {
                        if (!authUtils.isAutoAuthenticated(chatId)) {
                            message.append("- 권한이 없으니 관리자 헬프 -");
                        } else {
                            if ("/auto_start".equals(instruction)) {
                                CommonConstant.autoTrade = true;
                                message.append("모임 자동 매수기능 ON(" + CommonConstant.autoTrade + ")");
                            } else if("/auto_stop".equals(instruction)){
                                CommonConstant.autoTrade = false;
                                message.append("모임 자동 매수기능 OFF(" + CommonConstant.autoTrade + ")");
                            } else if("/auto_j_start".equals(instruction)){
                                CommonConstant.inpyoTrade = true;
                                message.append("개인 자동 매수기능 ON(" + CommonConstant.inpyoTrade + ")");
                            } else if("/auto_j_stop".equals(instruction)){
                                CommonConstant.inpyoTrade = false;
                                message.append("개인 자동 매수기능 OFF(" + CommonConstant.inpyoTrade + ")");
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