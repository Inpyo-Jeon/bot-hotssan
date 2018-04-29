package io.coinpeeker.bot_hotssan.scheduler.listed;

import io.coinpeeker.bot_hotssan.common.CommonConstant;
import io.coinpeeker.bot_hotssan.common.SecretKey;
import io.coinpeeker.bot_hotssan.feature.MarketInfo;
import io.coinpeeker.bot_hotssan.scheduler.Listing;
import io.coinpeeker.bot_hotssan.trade.TradeAgency;
import io.coinpeeker.bot_hotssan.utils.HttpUtils;
import io.coinpeeker.bot_hotssan.utils.MessageUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class OkexListedScheduler implements Listing {
    @Autowired
    MarketInfo marketInfo;

    @Autowired
    HttpUtils httpUtils;

    @Autowired
    MessageUtils messageUtils;

    @Autowired
    Jedis jedis;

    @Value("${property.hotssan_id}")
    private String apiKey;

    @Value("${property.env}")
    private String env;

    @Autowired
    private TradeAgency tradeAgency;

    private static final Logger LOGGER = LoggerFactory.getLogger(OkexListedScheduler.class);

    @Override
    @Scheduled(initialDelay = 1000 * 60, fixedDelay = 1000 * 2)
    public void inspectListedCoin() throws IOException, InvalidKeyException, NoSuchAlgorithmException {
        /** env validation check.**/
        if (!StringUtils.equals("real", env)) {
            return;
        }

        int listingCount = 0;
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("api_key", SecretKey.getApiKeyOkex()));
        params.add(new BasicNameValuePair("sign", SecretKey.getSignOkex()));

        JSONObject jsonObject = httpUtils.getPostResponseByObject(SecretKey.getUrlOkex(), params, "");
        JSONObject list = jsonObject.getJSONObject("info").getJSONObject("funds").getJSONObject("free");

        synchronized (jedis) {
            listingCount = Math.toIntExact(jedis.hlen("L-OKEx"));
        }

        if (listingCount != list.length()) {

            Date nowDate = new Date();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss (z Z)");
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

            for (Object item : list.keySet()) {
                String toStringItem = item.toString().toUpperCase();

                boolean isExist = true;

                synchronized (jedis) {
                    if (!jedis.hexists("L-OKEx", toStringItem)) {
                        isExist = false;
                    }
                }

                if (!isExist) {
                    Map<String, List<String>> marketList = marketInfo.availableMarketList(toStringItem.toUpperCase());
//                    tradeAgency.list("OKEx", toStringItem.toUpperCase(), marketList);

                    StringBuilder messageContent = new StringBuilder();
                    messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
                    messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
                    messageContent.append(" [ OKEx ] 상장 정보 ");
                    messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
                    messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
                    messageContent.append("\n");
                    messageContent.append(simpleDateFormat.format(nowDate));
                    messageContent.append("\n확인방법 : List");
                    messageContent.append("\n코인정보 : ");

                    synchronized (jedis) {
                        messageContent.append(jedis.hget("I-CoinMarketCap", toStringItem.toUpperCase()));
                    }

                    messageContent.append(" (");
                    messageContent.append(toStringItem.toUpperCase());
                    messageContent.append(")");
                    messageContent.append("\n구매가능 거래소 : ");
                    messageContent.append(marketInfo.marketInfo(marketList));

                    String url = CommonConstant.URL_TELEGRAM_BASE + apiKey + CommonConstant.METHOD_TELEGRAM_SENDMESSAGE;
                    messageUtils.sendMessage(url, -300048567L, messageContent.toString());
                    messageUtils.sendMessage(url, -319177275L, messageContent.toString());

                    LOGGER.info(messageContent.toString());

                    synchronized (jedis) {
                        jedis.hset("L-OKEx", toStringItem, "1");
                    }
                }
            }
        }
    }
}
