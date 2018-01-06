package io.coinpeeker.bot_hotssan.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class HotssanService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HotssanService.class);

    public String getWebhook() {
        LOGGER.info("@#$@#$@#$ telegram call");
        return "telegram_bot web_hook";
    }
}
