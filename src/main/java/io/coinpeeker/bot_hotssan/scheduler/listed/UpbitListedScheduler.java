package io.coinpeeker.bot_hotssan.scheduler.listed;

import io.coinpeeker.bot_hotssan.common.CommonConstant;
import io.coinpeeker.bot_hotssan.feature.MarketInfo;
import io.coinpeeker.bot_hotssan.scheduler.Listing;
import io.coinpeeker.bot_hotssan.utils.HttpUtils;
import io.coinpeeker.bot_hotssan.utils.MessageUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static io.coinpeeker.bot_hotssan.common.CommonConstant.capList;

@Component
public class UpbitListedScheduler implements Listing {
    @Autowired
    MarketInfo marketInfo;

    @Autowired
    MessageUtils messageUtils;

    @Autowired
    HttpUtils httpUtils;

    @Value("${property.hotssan_id}")
    private String apiKey;

    @Value("${property.env}")
    private String env;

    @Autowired
    private Jedis jedis;

    private static final Logger LOGGER = LoggerFactory.getLogger(UpbitListedScheduler.class);

    @Override
    @Scheduled(initialDelay = 1000 * 60, fixedDelay = 1000 * 10)
    public void inspectListedCoin() throws IOException {
        /** env validation check.**/
        if (!StringUtils.equals("real", env)) {
            return;
        }

        List<String> noListedCoinList = new ArrayList<>();

        for (String item : capList) {
            synchronized (jedis) {
                if (!jedis.hexists("UpbitListing", item)) {
                    noListedCoinList.add(item);
                }
            }
        }

        for (String item : noListedCoinList) {

            String tempURL = "https://ccx.upbit.com/api/v1/market_status?market=BTC-" + item;

            JSONObject jsonObject = httpUtils.getResponseByObject(tempURL);

            // 일단 market value 체크가 된 애들만!
            if (jsonObject.has("id") || !(jsonObject.getJSONObject("error").getString("message").equals("market does not have a valid value"))) {

                StringBuilder messageContent = new StringBuilder();
                Date today = new Date();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd, hh:mm:ss a");

                messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
                messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
                messageContent.append(" [ Upbit ] 상장 정보 ");
                messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
                messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
                messageContent.append("\n상장 리스트 탐지되었습니다.");
                messageContent.append("\n코인 정보 : ");
                synchronized (jedis) {
                    messageContent.append(jedis.hget("CoinMarketCap", item));
                }
                messageContent.append(" (");
                messageContent.append(item);
                messageContent.append(")");
                messageContent.append("\n구매 가능 거래소");
                messageContent.append(marketInfo.availableMarketList(item));

                // 정확한 정보 완료일 경우 / 아닐 경우
                if (!jsonObject.has("id")) {
                    messageContent.append("\n!! 관리자의 확인이 필요한 코인 !!");
                    messageContent.append(jsonObject.toString());
                }

                String url = CommonConstant.URL_TELEGRAM_BASE + apiKey + CommonConstant.METHOD_TELEGRAM_SENDMESSAGE;
                messageUtils.sendMessage(url, -300048567L, messageContent.toString());
                messageUtils.sendMessage(url, -277619118L, messageContent.toString());

                synchronized (jedis) {
                    jedis.hset("UpbitListing", item, "0");
                }
                LOGGER.info("Upbit 상장 : " + item + " (" + today + ")");

            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
