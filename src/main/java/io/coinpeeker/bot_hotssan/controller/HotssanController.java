package io.coinpeeker.bot_hotssan.controller;

import io.coinpeeker.bot_hotssan.external.ApiClient;
import io.coinpeeker.bot_hotssan.external.UpbitApiClientImpl;
import io.coinpeeker.bot_hotssan.service.HotssanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.api.objects.Update;

import java.net.URISyntaxException;

@RestController
public class HotssanController {

    @Autowired
    private HotssanService hotssanService;

    @PostMapping("/webhook")
    public void webHook(@RequestBody Update update) {
        hotssanService.updateHandler(update);
    }

    @GetMapping("/setWebhook")
    public boolean setWebhook(@RequestParam String url) {
        return hotssanService.setWebhook(url);
    }

}
