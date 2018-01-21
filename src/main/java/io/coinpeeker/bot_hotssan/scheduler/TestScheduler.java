package io.coinpeeker.bot_hotssan.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TestScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestScheduler.class);

    /**
     * Application 시작후 10초 딜레이 후에 시작, 딜레이는 60초 기준
     */
    @Scheduled(initialDelay = 10000, fixedDelay = 60000)
    public void test() {
        LOGGER.info("#$#$#$ scheduled test !!!");
    }
}
