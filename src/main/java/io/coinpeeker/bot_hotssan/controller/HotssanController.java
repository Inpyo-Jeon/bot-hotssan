package io.coinpeeker.bot_hotssan.controller;

import io.coinpeeker.bot_hotssan.service.HotssanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HotssanController {

    @Autowired
    private HotssanService hotssanService;

    @GetMapping("/webhook")
    public String webHook() {
        return hotssanService.getWebhook();
    }
}
