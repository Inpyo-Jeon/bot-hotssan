package io.coinpeeker.bot_hotssan.scheduler.listed;

import com.google.common.collect.Maps;
import io.coinpeeker.bot_hotssan.common.CommonConstant;
import io.coinpeeker.bot_hotssan.feature.MarketInfo;
import io.coinpeeker.bot_hotssan.scheduler.Listing;
import io.coinpeeker.bot_hotssan.utils.HttpUtils;
import io.coinpeeker.bot_hotssan.utils.MessageUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
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
import java.util.Date;
import java.util.Map;

@Component
public class BittrexListedScheduler implements Listing {
    @Autowired
    MarketInfo marketInfo;

    @Autowired
    HttpUtils httpUtils;

    @Autowired
    Jedis jedis;

    @Autowired
    MessageUtils messageUtils;

    @Value("${property.hotssan_id}")
    private String apiKey;

    @Value("${property.env}")
    private String env;

    private static final Logger LOGGER = LoggerFactory.getLogger(BittrexListedScheduler.class);

    @Override
    @Scheduled(initialDelay = 1000 * 60, fixedDelay = 1000 * 3)
    public void inspectListedCoin() throws IOException {
        /** env validation check.**/
        if (!StringUtils.equals("real", env)) {
            return;
        }

        int listingCount = 0;
        String endPoint = "https://bittrex.com/api/v2.0/pub/markets/GetMarketSummaries";
        JSONObject jsonObject = httpUtils.getResponseByObject(endPoint);
        JSONArray list = jsonObject.getJSONArray("result");
        Map<String, Integer> deDuplicationMap = Maps.newHashMap();

        for(int i = 0; i < list.length(); i++){
            deDuplicationMap.put(list.getJSONObject(i).getJSONObject("Market").getString("MarketCurrency"), 1);
        }

        synchronized (jedis) {
            listingCount = Math.toIntExact(jedis.hlen("Listing-Bittrex"));
        }

        LOGGER.info(String.valueOf(deDuplicationMap.size()) + " // " + String.valueOf(listingCount) + " : Bittrex");

        if (deDuplicationMap.size() != listingCount) {
            for (String item : deDuplicationMap.keySet()){
                boolean isExist = true;

                synchronized (jedis) {
                    if (!jedis.hexists("Listing-Bittrex", item)) {
                        isExist = false;
                    }
                }

                if (!isExist) {
                    Date date = new Date();
                    StringBuilder messageContent = new StringBuilder();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd, hh:mm:ss a");

                    messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
                    messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
                    messageContent.append(" [ Bittrex ] 상장 정보 ");
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

                    String url = CommonConstant.URL_TELEGRAM_BASE + apiKey + CommonConstant.METHOD_TELEGRAM_SENDMESSAGE;
                    messageUtils.sendMessage(url, -300048567L, messageContent.toString());
                    messageUtils.sendMessage(url, -277619118L, messageContent.toString());

                    synchronized (jedis) {
                        jedis.hset("Listing-Bittrex", item, "1");
                    }

                    LOGGER.info("Bittrex 상장 : " + item + " (" + simpleDateFormat.format(date).toString() + ")");
                }
            }
        }
    }
}