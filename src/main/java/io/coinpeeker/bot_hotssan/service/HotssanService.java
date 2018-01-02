package io.coinpeeker.bot_hotssan.service;

import org.springframework.stereotype.Service;

@Service
public class HotssanService {

    public String getWebhook() {
        return "telegram_bot web_hook";
    }
}
