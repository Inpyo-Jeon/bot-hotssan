package io.coinpeeker.bot_hotssan.module;

import org.telegram.telegrambots.api.objects.Update;

import java.io.IOException;

public interface HotssanUpdateHandler {
    void updateHandler(Update update) throws IOException;
    boolean setWebhook(String url);
    void deleteWebhook();
}
