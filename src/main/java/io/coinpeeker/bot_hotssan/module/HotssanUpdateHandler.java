package io.coinpeeker.bot_hotssan.module;

import org.telegram.telegrambots.api.objects.Update;

public interface HotssanUpdateHandler {
    void updateHandler(Update update);
    boolean setWebhook(String url);
    void deleteWebhook();
}
