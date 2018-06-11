package io.coinpeeker.bot_hotssan.scheduler.listed;

import com.google.common.collect.Maps;
import com.neovisionaries.ws.client.WebSocketException;
import com.nimbusds.jose.JOSEException;
import io.coinpeeker.bot_hotssan.common.CommonConstant;
import io.coinpeeker.bot_hotssan.feature.MarketInfo;
import io.coinpeeker.bot_hotssan.scheduler.Listing;
import io.coinpeeker.bot_hotssan.trade.TradeAgency;
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
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

@Component
public class BittrexListedScheduler implements Listing {
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

    private static final Logger LOGGER = LoggerFactory.getLogger(BittrexListedScheduler.class);

    @Override
    @Scheduled(initialDelay = 1000 * 10, fixedDelay = 1500 * 1)
    public void inspectListedCoin() throws IOException, InvalidKeyException, NoSuchAlgorithmException, ParseException, JOSEException, WebSocketException {
        /** env validation check.**/
        if (!StringUtils.equals("real", env)) {
            return;
        }

        int listingCount = 0;
        String endPoint = "https://bittrex.com/api/v2.0/pub/markets/GetMarketSummaries";
        JSONObject jsonObject = httpUtils.getResponseByObject(endPoint);
        JSONArray list = jsonObject.getJSONArray("result");
        Map<String, Integer> deDuplicationMap = Maps.newHashMap();

        for (int i = 0; i < list.length(); i++) {
            deDuplicationMap.put(list.getJSONObject(i).getJSONObject("Market").getString("MarketCurrency"), 1);
        }

        synchronized (jedis) {
            listingCount = Math.toIntExact(jedis.hlen("L-Bittrex"));
        }

        if (deDuplicationMap.size() != listingCount) {
            for (String item : deDuplicationMap.keySet()) {
                boolean isExist = true;

                synchronized (jedis) {
                    if (!jedis.hexists("L-Bittrex", item)) {
                        isExist = false;
                    }
                }

                if (!isExist) {
                    Map<String, Map<String, String>> marketList = marketInfo.availableMarketList(item);
                    String orderResult = tradeAgency.list("Bittrex", item.toUpperCase(), marketList);

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

                    synchronized (jedis) {
                        messageContent.append(jedis.hget("I-CoinMarketCap", item));
                    }

                    messageContent.append(" (");
                    messageContent.append(item);
                    messageContent.append(")");
                    messageContent.append("\n구매가능 거래소 : ");
                    messageContent.append(marketInfo.marketInfo(marketList));
                    messageContent.append(orderResult);

                    String url = CommonConstant.URL_TELEGRAM_BASE + apiKey + CommonConstant.METHOD_TELEGRAM_SENDMESSAGE;
                    messageUtils.sendMessage(url, -300048567L, messageContent.toString());
                    messageUtils.sendMessage(url, -319177275L, messageContent.toString());

                    LOGGER.info(messageContent.toString());

                    synchronized (jedis) {
                        jedis.hset("L-Bittrex", item, "1");
                    }
                }
            }
        }
    }

//    @Scheduled(initialDelay = 1000 * 10, fixedDelay = 1000 * 3)
    public void getWalletHealth() throws IOException, InvalidKeyException, NoSuchAlgorithmException {
        /** env validation check.**/
        if (!StringUtils.equals("local", env)) {
            return;
        }

        int listingCount = 0;
        String endPoint = "https://bittrex.com/api/v2.0/pub/currencies/GetWalletHealth";
        JSONObject jsonObject = httpUtils.getResponseByObject(endPoint);
        JSONArray list = jsonObject.getJSONArray("result");

        synchronized (jedis) {
            listingCount = Math.toIntExact(jedis.hlen("L-Bittrex-Health"));
        }

        if (listingCount != list.length()) {
            for (int i = 0; i < list.length(); i++) {

                boolean isExist = true;

                synchronized (jedis) {
                    if (!jedis.hexists("L-Bittrex-Health", list.getJSONObject(i).getJSONObject("Currency").getString("Currency"))) {
                        isExist = false;
                    }
                }

                if (!isExist) {
                    String item = list.getJSONObject(i).getJSONObject("Currency").getString("Currency");
                    Map<String, Map<String, String>> marketList = marketInfo.availableMarketList(item);
//                    tradeAgency.list("Bittrex", item.toUpperCase(), marketList);

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
                    messageContent.append("\n확인방법 : List(Health)");
                    messageContent.append("\n코인정보 : ");

                    synchronized (jedis) {
                        messageContent.append(jedis.hget("I-CoinMarketCap", item));
                    }

                    messageContent.append(" (");
                    messageContent.append(item);
                    messageContent.append(")");
                    messageContent.append("\n구매가능 거래소 : ");
                    messageContent.append(marketInfo.marketInfo(marketList));

                    String url = CommonConstant.URL_TELEGRAM_BASE + apiKey + CommonConstant.METHOD_TELEGRAM_SENDMESSAGE;
//                    messageUtils.sendMessage(url, -300048567L, messageContent.toString());
//                    messageUtils.sendMessage(url, -319177275L, messageContent.toString());

                    LOGGER.info(messageContent.toString());

                    synchronized (jedis) {
                        jedis.hset("L-Bittrex-Health", item, "1");
                    }
                }
            }
        }
    }
}