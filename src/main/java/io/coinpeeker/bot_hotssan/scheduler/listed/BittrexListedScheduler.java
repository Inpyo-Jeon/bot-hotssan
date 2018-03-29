package io.coinpeeker.bot_hotssan.scheduler.listed;

import com.google.common.collect.Maps;
import io.coinpeeker.bot_hotssan.common.CommonConstant;
import io.coinpeeker.bot_hotssan.common.CustomJedis;
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
import java.util.TimeZone;

@Component
public class BittrexListedScheduler implements Listing {
    @Autowired
    MarketInfo marketInfo;

    @Autowired
    HttpUtils httpUtils;

    @Autowired
    CustomJedis customJedis;

    @Autowired
    MessageUtils messageUtils;

    @Value("${property.hotssan_id}")
    private String apiKey;

    @Value("${property.env}")
    private String env;

    private static final Logger LOGGER = LoggerFactory.getLogger(BittrexListedScheduler.class);

    @Override
    @Scheduled(initialDelay = 1000 * 10, fixedDelay = 1000 * 3)
    public void inspectListedCoin() throws IOException {
        /** env validation check.**/
        if (!StringUtils.equals("real", env)) {
            return;
        }

        Jedis jedis;
        int listingCount = 0;
        String endPoint = "https://bittrex.com/api/v2.0/pub/markets/GetMarketSummaries";
        JSONObject jsonObject = httpUtils.getResponseByObject(endPoint);
        JSONArray list = jsonObject.getJSONArray("result");
        Map<String, Integer> deDuplicationMap = Maps.newHashMap();

        for (int i = 0; i < list.length(); i++) {
            deDuplicationMap.put(list.getJSONObject(i).getJSONObject("Market").getString("MarketCurrency"), 1);
        }

        jedis = customJedis.getResource();
        listingCount = Math.toIntExact(jedis.hlen("L-Bittrex"));
        jedis.close();

        System.out.println(listingCount);
        System.out.println(deDuplicationMap.size());

        if (deDuplicationMap.size() != listingCount) {
            for (String item : deDuplicationMap.keySet()) {
                boolean isExist = true;

                jedis = customJedis.getResource();
                if (!jedis.hexists("L-Bittrex", item)) {
                    isExist = false;
                }
                jedis.close();

                if (!isExist) {
                    StringBuilder messageContent = new StringBuilder();
                    Date nowDate = new Date();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss (z Z)");
                    simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

                    messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
                    messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
                    messageContent.append(" [ Bittrex ] 상장 정보 ");
                    messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
                    messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
                    messageContent.append("\n");
                    messageContent.append(simpleDateFormat.format(nowDate));
                    messageContent.append("\n확인방법 : List");
                    messageContent.append("\n코인정보 : ");

                    jedis = customJedis.getResource();
                    messageContent.append(jedis.hget("I-CoinMarketCap", item));
                    jedis.close();

                    messageContent.append(" (");
                    messageContent.append(item);
                    messageContent.append(")");
                    messageContent.append("\n구매가능 거래소 : ");
                    messageContent.append(marketInfo.availableMarketList(item));

                    String url = CommonConstant.URL_TELEGRAM_BASE + apiKey + CommonConstant.METHOD_TELEGRAM_SENDMESSAGE;
                    messageUtils.sendMessage(url, -300048567L, messageContent.toString());
                    messageUtils.sendMessage(url, -277619118L, messageContent.toString());

                    jedis = customJedis.getResource();
                    jedis.hset("L-Bittrex", item, "1");
                    jedis.close();

                    LOGGER.info("Bittrex 상장 : " + item + " (" + simpleDateFormat.format(nowDate).toString() + ")");
                }
            }
        }
    }
}