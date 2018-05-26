package io.coinpeeker.bot_hotssan.scheduler.listed;

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
import java.text.ParseException;
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

    @Autowired
    private TradeAgency tradeAgency;

    private static final Logger LOGGER = LoggerFactory.getLogger(UpbitListedScheduler.class);

    @Override
//    @Scheduled(initialDelay = 1000 * 10, fixedDelay = 1000 * 10)
    public void inspectListedCoin() throws IOException, InvalidKeyException, NoSuchAlgorithmException {
        /** env validation check.**/
        if (!StringUtils.equals("dev", env)) {
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

//            String tempURL = "https://ccx.upbit.com/api/v1/market_status?market=BTC-" + item;
//            String tempURL = "https://ccx.upbit.com/api/v1/deposits/coin_address?currency=" + item;
            String tempURL = "https://ccx.upbit.com/api/v1/deposits/generate_coin_address?currency=" + item;
            List<NameValuePair> header = new ArrayList<>();
            header.add(new BasicNameValuePair("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhY2Nlc3Nfa2V5IjoiOUc3SGFWb0JrdVpRMElQcUs1OFJNeXl3RXVxZU5senhiODgwaVdSdCIsIm5vbmNlIjoxNTI1Nzg5MTA3ODM5fQ.m0R2l9pwMjix6cWT9QdyRlXOYEfa_OHH4IVWDBn1bmI"));


//            String tempURL = "https://static.upbit.com/marketing/" + item.toLowerCase() + "_listing.png";
//            String tempURL = "https://api-manager.upbit.com/api/v1/notices?page=1&per_page=1";
//            List<NameValuePair> header = new ArrayList<>();
//            header.add(new BasicNameValuePair("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8"));
//            header.add(new BasicNameValuePair("Accept-Encoding", "gzip, deflate, br"));
//            header.add(new BasicNameValuePair("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7"));
//            header.add(new BasicNameValuePair("Cache-Control", "max-age=0"));
//            header.add(new BasicNameValuePair("Connection", "keep-alive"));
//            header.add(new BasicNameValuePair("Host", "static.upbit.com"));
//            header.add(new BasicNameValuePair("Upgrade-Insecure-Requests", "1"));
//            header.add(new BasicNameValuePair("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.186 Safari/537.36"));


//            System.out.println(item + " | " + EntityUtils.toString(httpUtils.get(tempURL, header).getEntity(), "UTF-8"));

//            System.out.println(EntityUtils.toString(httpUtils.get(tempURL, header).getEntity(), "UTF-8") + "| " + item);
            System.out.println(EntityUtils.toString(httpUtils.post(tempURL, header, "kucoinTrade").getEntity(), "UTF-8") + "| " + item);

            // 일단 market value 체크가 된 애들만!
//            if (jsonObject.has("id") || !(jsonObject.getJSONObject("error").getString("message").equals("market does not have a valid value"))) {
//
//                Map<String, List<String>> marketList = marketInfo.availableMarketList(item);
//                StringBuilder messageContent = new StringBuilder();
//                Date nowDate = new Date();
//                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss (z Z)");
//                simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
//
//                messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
//                messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
//                messageContent.append(" [ Upbit ] 상장 정보 ");
//                messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
//                messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
//                messageContent.append("\n");
//                messageContent.append(simpleDateFormat.format(nowDate));
//                messageContent.append("\n코인정보 : ");
//
//                synchronized (jedis) {
//                    messageContent.append(jedis.hget("I-CoinMarketCap", item));
//                }
//
//                messageContent.append(" (");
//                messageContent.append(item);
//                messageContent.append(")");
//                messageContent.append("\n구매가능 거래소 : ");
//                messageContent.append(marketInfo.marketInfo(marketList));
//
//                // 정확한 정보 완료일 경우 / 아닐 경우
//                if (!jsonObject.has("id")) {
//                    messageContent.append("\n!! 관리자의 확인이 필요한 코인 !!\n");
//                    messageContent.append(jsonObject.toString());
//                }
//
//                String url = CommonConstant.URL_TELEGRAM_BASE + apiKey + CommonConstant.METHOD_TELEGRAM_SENDMESSAGE;
//                messageUtils.sendMessage(url, -300048567L, messageContent.toString());
//                messageUtils.sendMessage(url, -319177275L, messageContent.toString());
//
//
//                synchronized (jedis) {
//                    jedis.hset("L-Upbit", item, "0");
//                }
//            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    //    @Scheduled(initialDelay = 1000 * 20, fixedDelay = 1000 * 60 * 1)
    public void checkListedFromBtc() throws IOException {
        /** env validation check.**/
        if (!StringUtils.equals("dev", env)) {
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

    @Scheduled(initialDelay = 1000 * 10, fixedDelay = 1)
    public void checkNotice() throws IOException, InvalidKeyException, NoSuchAlgorithmException, ParseException, JOSEException, WebSocketException {
        /** env validation check.**/
        if (!StringUtils.equals("real", env)) {
            return;
        }

        String tempURL = "https://api-manager.upbit.com/api/v1/notices?page=1&per_page=1";

        List<NameValuePair> header = new ArrayList<>();
        header.add(new BasicNameValuePair("Accept", "*.*"));
        header.add(new BasicNameValuePair("Accept-Encoding", "gzip, deflate, br"));
        header.add(new BasicNameValuePair("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7"));
        header.add(new BasicNameValuePair("Connection", "keep-alive"));
        header.add(new BasicNameValuePair("Host", "api-manager.upbit.com"));
//        header.add(new BasicNameValuePair("If-None-Match", "W/\"985eb02ff1db12398359cc595dd25056\""));
        header.add(new BasicNameValuePair("Origin", "https://upbit.com"));
        header.add(new BasicNameValuePair("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.186 Safari/537.36"));
        int preNoticeCount = 0;

        synchronized (jedis) {
            preNoticeCount = Integer.valueOf(jedis.hget("L-Upbit-Notice", "totalCount"));
        }


        JSONObject jsonObject = httpUtils.getResponseByObject(tempURL, header);

        int currentNoticeCount = jsonObject.getJSONObject("data").getInt("total_count");
        if (preNoticeCount != currentNoticeCount) {
            LOGGER.info(jsonObject.toString());
            String title = jsonObject.getJSONObject("data").getJSONArray("list").getJSONObject(0).getString("title");
            boolean autoTrade = false;

            if ((title.contains("[이벤트]") && title.contains("상장")) || (title.contains("[거래]") && title.contains("원화") && ((title.contains("추가")) || title.contains("상장")))) {
                int bracketCount = StringUtils.countMatches(title, "(");
                if (bracketCount == 1) {
                    boolean isExist = true;
                    String symbol = title.replaceAll("(\\W)", "").toUpperCase();

                    synchronized (jedis) {
                        if (!jedis.hexists("L-Upbit-Notice", symbol)) {
                            jedis.hset("L-Upbit-Notice", symbol, "0");
                            isExist = false;
                        }
                    }

                    if (!isExist) {
                        // 실제 매수 봇
                        Map<String, List<String>> marketList = marketInfo.availableMarketList(symbol);
                        tradeAgency.list("Upbit", symbol, marketList);
                        autoTrade = true;
                    }
                }
            }

            StringBuilder messageContent = new StringBuilder();
            Date nowDate = new Date();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss (z Z)");
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

            messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\udce3"));
            messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\udce3"));
            messageContent.append(" [ Upbit ] 공지사항 ");
            messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\udce3"));
            messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\udce3"));
            messageContent.append("\n");
            messageContent.append(simpleDateFormat.format(nowDate));
            messageContent.append("\n");
            messageContent.append(title);

            if (autoTrade) {
                messageContent.append("\n-- 봇 자동매수 완료 --");
            }

            LOGGER.info(messageContent.toString());

            String url = CommonConstant.URL_TELEGRAM_BASE + apiKey + CommonConstant.METHOD_TELEGRAM_SENDMESSAGE;
            messageUtils.sendMessage(url, -300048567L, messageContent.toString());
            messageUtils.sendMessage(url, -319177275L, messageContent.toString());

            synchronized (jedis) {
                jedis.hset("L-Upbit-Notice", "totalCount", String.valueOf(currentNoticeCount));
            }
        }

        try {
            Random random = new Random();
            int randomDelayTime = random.nextInt(2) + 1;
            Thread.sleep(1000 * randomDelayTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}