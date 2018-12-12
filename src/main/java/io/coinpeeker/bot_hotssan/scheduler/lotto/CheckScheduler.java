package io.coinpeeker.bot_hotssan.scheduler.lotto;

import io.coinpeeker.bot_hotssan.service.lotto.PriceProcess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @author : Jeon
 * @version : 1.0
 * @date : 2018-11-09
 * @description :
 */

@Service
public class CheckScheduler {

    @Autowired
    PriceProcess priceProcess;

    @Scheduled(cron = "0 0 0/1 * * ?")
    public void run() throws IOException {
        priceProcess.executePrice();
        System.out.println("완료");
    }
}