package io.coinpeeker.bot_hotssan;

import io.coinpeeker.bot_hotssan.service.HotssanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class ApplicationStartup implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationStartup.class);

    @Autowired
    private HotssanService hotssanService;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        LOGGER.info("#$#$#$ onApplicationEvent : {}", event);
        hotssanService.sendBootMessage();

        return ;
    }
}
