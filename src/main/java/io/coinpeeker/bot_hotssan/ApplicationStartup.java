package io.coinpeeker.bot_hotssan;

import io.coinpeeker.bot_hotssan.service.HotssanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class ApplicationStartup implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private HotssanService hotssanService;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        hotssanService.sendBootMessage();

        return ;
    }
}
