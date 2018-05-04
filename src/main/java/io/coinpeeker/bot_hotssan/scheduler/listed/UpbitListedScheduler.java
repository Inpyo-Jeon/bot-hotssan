package io.coinpeeker.bot_hotssan.scheduler.listed;

import io.coinpeeker.bot_hotssan.common.CommonConstant;
import io.coinpeeker.bot_hotssan.feature.MarketInfo;
import io.coinpeeker.bot_hotssan.scheduler.Listing;
import io.coinpeeker.bot_hotssan.utils.HttpUtils;
import io.coinpeeker.bot_hotssan.utils.MessageUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
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
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class UpbitListedScheduler implements Listing {
    @Autowired
    MarketInfo marketInfo;

    @Autowired
    MessageUtils messageUtils;

    @Autowired
    HttpUtils httpUtils;

    @Autowired
    Jedis jedis;

    @Value("${property.hotssan_id}")
    private String apiKey;

    @Value("${property.env}")
    private String env;

    private static final Logger LOGGER = LoggerFactory.getLogger(UpbitListedScheduler.class);

    @Override
//    @Scheduled(initialDelay = 1000 * 70, fixedDelay = 1000 * 10)
    public void inspectListedCoin() throws IOException, InvalidKeyException, NoSuchAlgorithmException {
        /** env validation check.**/
        if (!StringUtils.equals("real", env)) {
            return;
        }
        LOGGER.info("Upbit 시작");

        List<String> noListedCoinList = new ArrayList<>();
        List<String> capList = new ArrayList<>();
        capList.addAll(CommonConstant.getCapList());

        for (String item : capList) {
            synchronized (jedis) {
                if (!jedis.hexists("L-Upbit", item)) {
                    noListedCoinList.add(item);
                }
            }
        }

        for (String item : noListedCoinList) {

            String tempURL = "https://ccx.upbit.com/api/v1/market_status?market=BTC-" + item;

            JSONObject jsonObject = httpUtils.getResponseByObject(tempURL);

            // 일단 market value 체크가 된 애들만!
            if (jsonObject.has("id") || !(jsonObject.getJSONObject("error").getString("message").equals("market does not have a valid value"))) {

                Map<String, List<String>> marketList = marketInfo.availableMarketList(item);
                StringBuilder messageContent = new StringBuilder();
                Date nowDate = new Date();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss (z Z)");
                simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

                messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
                messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
                messageContent.append(" [ Upbit ] 상장 정보 ");
                messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
                messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
                messageContent.append("\n");
                messageContent.append(simpleDateFormat.format(nowDate));
                messageContent.append("\n코인정보 : ");

                synchronized (jedis) {
                    messageContent.append(jedis.hget("I-CoinMarketCap", item));
                }

                messageContent.append(" (");
                messageContent.append(item);
                messageContent.append(")");
                messageContent.append("\n구매가능 거래소 : ");
                messageContent.append(marketInfo.marketInfo(marketList));

                // 정확한 정보 완료일 경우 / 아닐 경우
                if (!jsonObject.has("id")) {
                    messageContent.append("\n!! 관리자의 확인이 필요한 코인 !!\n");
                    messageContent.append(jsonObject.toString());
                }

                String url = CommonConstant.URL_TELEGRAM_BASE + apiKey + CommonConstant.METHOD_TELEGRAM_SENDMESSAGE;
                messageUtils.sendMessage(url, -300048567L, messageContent.toString());
                messageUtils.sendMessage(url, -319177275L, messageContent.toString());


                synchronized (jedis) {
                    jedis.hset("L-Upbit", item, "0");
                }
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    @Scheduled(initialDelay = 1000 * 20, fixedDelay = 1000 * 60 * 1)
    public void checkListedFromBtc() throws IOException {
        /** env validation check.**/
        if (!StringUtils.equals("real", env)) {
            return;
        }

        LOGGER.info("-- Upbit S3 시작 --");

        int redisCount = 0;
        List<NameValuePair> header = new ArrayList<>();
        header.add(new BasicNameValuePair("Accept", "*/*"));
        header.add(new BasicNameValuePair("Accept-Encoding", "gzip, deflate, br"));
        header.add(new BasicNameValuePair("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7"));
        header.add(new BasicNameValuePair("Connection", "keep-alive"));
        header.add(new BasicNameValuePair("Host", "s3.ap-northeast-2.amazonaws.com"));
        header.add(new BasicNameValuePair("Origin", "https://upbit.com"));
        header.add(new BasicNameValuePair("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36"));

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String callUrl = "https://s3.ap-northeast-2.amazonaws.com/crix-production/crix_master?nonce" + timestamp.getTime();

        JSONArray jsonArray = httpUtils.getResponseByArrays(callUrl, header);

        synchronized (jedis) {
            redisCount = Math.toIntExact(jedis.hlen("L-Upbit-S3"));
        }

        if (redisCount != jsonArray.length()) {
            for (int i = 0; i < jsonArray.length(); i++) {
                boolean isExist = true;

                synchronized (jedis) {
                    isExist = jedis.hexists("L-Upbit-S3", jsonArray.getJSONObject(i).getString("code"));
                }

                if (!isExist) {
                    LOGGER.info("-- Upbit S3 상장 감지 --");

                    String koreanName = jsonArray.getJSONObject(i).getString("koreanName");
                    String pair = jsonArray.getJSONObject(i).getString("pair");
                    String baseCurrencyCode = jsonArray.getJSONObject(i).getString("baseCurrencyCode");

                    Map<String, List<String>> marketList = marketInfo.availableMarketList(baseCurrencyCode);

                    StringBuilder messageContent = new StringBuilder();
                    Date nowDate = new Date();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss (z Z)");
                    simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

                    messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
                    messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
                    messageContent.append(" [ Upbit ] 상장 정보 ");
                    messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
                    messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
                    messageContent.append("\n");
                    messageContent.append(simpleDateFormat.format(nowDate));
                    messageContent.append("\n확인방법 : S3");
                    messageContent.append("\n상장마켓 : ");
                    messageContent.append(pair);
                    messageContent.append("\n코인정보 : ");
                    messageContent.append(koreanName);
                    messageContent.append(" (");
                    messageContent.append(baseCurrencyCode);
                    messageContent.append(")");
                    messageContent.append("\n구매가능 거래소 : ");
                    messageContent.append(marketInfo.marketInfo(marketList));

                    String url = CommonConstant.URL_TELEGRAM_BASE + apiKey + CommonConstant.METHOD_TELEGRAM_SENDMESSAGE;
                    messageUtils.sendMessage(url, -300048567L, messageContent.toString());
                    messageUtils.sendMessage(url, -319177275L, messageContent.toString());

                    LOGGER.info(jsonArray.getJSONObject(i).toString());
                    LOGGER.info(messageContent.toString());

                    synchronized (jedis) {
                        jedis.hset("L-Upbit-S3", jsonArray.getJSONObject(i).getString("code"), pair);
                    }
                }
            }
        }
    }
}