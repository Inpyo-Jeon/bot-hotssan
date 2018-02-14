package io.coinpeeker.bot_hotssan.scheduler.pos;

import io.coinpeeker.bot_hotssan.common.CommonConstant;
import io.coinpeeker.bot_hotssan.external.etc.XgoxApiClient;
import io.coinpeeker.bot_hotssan.utils.MessageUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class PosScheduler {

    @Autowired
    XgoxApiClient xgoxApiClient;

    @Autowired
    MessageUtils messageUtils;

    @Value("${property.hotssan_id}")
    private String apiKey;

    @Value("${property.env}")
    private String env;

    private static final Logger LOGGER = LoggerFactory.getLogger(PosScheduler.class);

    private static Double XGOX_LAST_BALANCE;

    /**
     * xgox staking monitoring job.
     * last balance check, delay = 10min.
     * only real env working.
     */
    @Scheduled(initialDelay = 5000, fixedDelay = 1000 * 60 * 10)
    public void checkLastBalanceForXgox() {
        /** env validation check.**/
        if (!StringUtils.equals("real", env)) {
            return;
        }

        try {

            Double lastBalance = xgoxApiClient.getLastBalance();

            if (XGOX_LAST_BALANCE == null) {
                LOGGER.info("@#@#@# XGOX_LAST_BALANCE is null");
                XGOX_LAST_BALANCE = lastBalance;
            }

            if (!XGOX_LAST_BALANCE.equals(lastBalance)) {

                StringBuilder sb = new StringBuilder();
                sb.append("### xgox staking... ###");
                sb.append("\nLastBalance => ");
                sb.append(XGOX_LAST_BALANCE);
                sb.append("\nCurrentBalance => ");
                sb.append(lastBalance);
                sb.append("\nGap => ");
                sb.append(lastBalance - XGOX_LAST_BALANCE);

                String url = CommonConstant.URL_TELEGRAM_BASE + apiKey + CommonConstant.METHOD_TELEGRAM_SENDMESSAGE;
                messageUtils.sendMessage(url, -300048567L, sb.toString());

                XGOX_LAST_BALANCE = lastBalance;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
