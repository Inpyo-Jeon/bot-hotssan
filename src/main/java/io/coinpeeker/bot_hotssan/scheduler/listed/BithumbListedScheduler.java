package io.coinpeeker.bot_hotssan.scheduler.listed;

import io.coinpeeker.bot_hotssan.common.CommonConstant;
import io.coinpeeker.bot_hotssan.feature.MarketInfo;
import io.coinpeeker.bot_hotssan.scheduler.Listing;
import io.coinpeeker.bot_hotssan.utils.HttpUtils;
import io.coinpeeker.bot_hotssan.utils.MessageUtils;
import io.coinpeeker.bot_hotssan.utils.bithumb.Api_Client;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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
public class BithumbListedScheduler implements Listing {

    @Autowired
    HttpUtils httpUtils;

    @Autowired
    MessageUtils messageUtils;

    @Autowired
    MarketInfo marketInfo;

    @Autowired
    Jedis jedis;

    @Value("${property.hotssan_id}")
    private String apiKey;

    @Value("${property.env}")
    private String env;

    private static final Logger LOGGER = LoggerFactory.getLogger(BithumbListedScheduler.class);

    @Override
    @Scheduled(initialDelay = 1000 * 5, fixedDelay = 1000 * 2)
    public void inspectListedCoin() throws IOException {
        /** env validation check.**/
        if (!StringUtils.equals("dev", env)) {
            return;
        }

        CloseableHttpResponse httpResponse = httpUtils.get("https://bithumb.cafe/feed");
        String convertData = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");

        LOGGER.info(convertData);
        
        Elements items = Jsoup.parseBodyFragment(convertData).body().getElementsByTag("item");

        for (Element item : items) {
            if (item.getElementsByTag("title").get(0).text().contains("상장")) {
                String text = item.getElementsByTag("title").get(0).text();
                String link = item.getElementsByTag("guid").get(0).text();
                String pubDate = item.getElementsByTag("pubDate").get(0).text();
                boolean check = false;

                synchronized (jedis) {
                    if (!jedis.hexists("L-Bithumb-Private", text)) {
                        check = true;
                    }
                }

                if (check) {
                    StringBuilder messageContent = new StringBuilder();
                    Date nowDate = new Date();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss (z Z)");
                    simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

                    messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
                    messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
                    messageContent.append(" [ Bithumb ] 상장 정보 - Test");
                    messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
                    messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
                    messageContent.append("\n");
                    messageContent.append(simpleDateFormat.format(nowDate));
                    messageContent.append("\n확인방법 : Blog-Internal api");
                    messageContent.append("\n내용 : ");
                    messageContent.append(text);
                    messageContent.append("\n링크 : ");
                    messageContent.append(link);
                    messageContent.append("\n등록시간 : ");
                    messageContent.append(pubDate);

                    String url = CommonConstant.URL_TELEGRAM_BASE + apiKey + CommonConstant.METHOD_TELEGRAM_SENDMESSAGE;
                    messageUtils.sendMessage(url, -294606763L, messageContent.toString());

                    LOGGER.info(text + " | " + link + " | " + pubDate);

                    synchronized (jedis) {
                        jedis.hset("L-Bithumb-Private", text, "0");
                    }

//                    messageContent.append("\n코인정보 : ");
//
//                    synchronized (jedis) {
//                        messageContent.append(jedis.hget("I-CoinMarketCap", currency));
//                    }
//
//                    messageContent.append(" (");
//                    messageContent.append(currency);
//                    messageContent.append(")");
//                    messageContent.append("\n구매가능 거래소 : ");
//                    messageContent.append(marketInfo.marketInfo(marketList));
//
//                    String url = CommonConstant.URL_TELEGRAM_BASE + apiKey + CommonConstant.METHOD_TELEGRAM_SENDMESSAGE;
//                    messageUtils.sendMessage(url, -294606763L, messageContent.toString());
//
//                    synchronized (jedis) {
//                        jedis.hset("L-Bithumb", currency, "1");
//                    }
//
//                    LOGGER.info("Bithumb 상장 : " + currency + " (" + simpleDateFormat.format(nowDate).toString() + ")");
                }
            }
        }
    }
//    @Override
//    @Scheduled(initialDelay = 1000 * 30, fixedDelay = 1000)
//    public void inspectListedCoin() throws IOException, NoSuchAlgorithmException, InvalidKeyException {
//        /** env validation check.**/
//        if (!StringUtils.equals("real", env)) {
//            return;
//        }
//        LOGGER.info("Bithumb 시작");
//
//        List<String> noListedCoinList = new ArrayList<>();
//        List<String> capList = new ArrayList<>();
//        capList.addAll(CommonConstant.getCapList());
//
//        capList.stream()
//                .forEach((key) -> {
//                    synchronized (jedis) {
//                        if (!jedis.hexists("L-Bithumb", key)) {
//                            noListedCoinList.add(key);
//                        }
//                    }
//                });
//
//        for (String item : noListedCoinList) {
//            Api_Client api = new Api_Client("1dc81ffb56788858690afb7b72bef9dd", "5ed812f9bfdf8fc23fd422742706947a");
//
//            HashMap<String, String> rgParams = new HashMap<>();
//            rgParams.put("currency", item);
//
//            String result = "";
//            JSONObject jsonObject = null;
//
//            try {
//                result = api.callApi("/info/wallet_address", rgParams);
//                jsonObject = new JSONObject(result);
//            } catch (Exception e) {
//                continue;
//            }
//
//            if (jsonObject.has("data")) {
//                String status = jsonObject.getString("status");
//                String currency = jsonObject.getJSONObject("data").getString("currency");
//                Map<String, List<String>> marketList = marketInfo.availableMarketList(currency);
//
//                StringBuilder messageContent = new StringBuilder();
//                Date nowDate = new Date();
//                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss (z Z)");
//                simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
//
//                messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
//                messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
//                messageContent.append(" [ Bithumb ] 상장 정보 ");
//                messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
//                messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
//                messageContent.append("\n");
//                messageContent.append(simpleDateFormat.format(nowDate));
//                messageContent.append("\n확인방법 : Address");
//                messageContent.append("\n코인정보 : ");
//
//                synchronized (jedis) {
//                    messageContent.append(jedis.hget("I-CoinMarketCap", currency));
//                }
//
//                messageContent.append(" (");
//                messageContent.append(currency);
//                messageContent.append(")");
//                messageContent.append("\n구매가능 거래소 : ");
//                messageContent.append(marketInfo.marketInfo(marketList));
//
//                String url = CommonConstant.URL_TELEGRAM_BASE + apiKey + CommonConstant.METHOD_TELEGRAM_SENDMESSAGE;
//                messageUtils.sendMessage(url, -294606763L, messageContent.toString());
//
//                synchronized (jedis) {
//                    jedis.hset("L-Bithumb", currency, "1");
//                }
//
//                LOGGER.info("Bithumb 상장 : " + currency + " (" + simpleDateFormat.format(nowDate).toString() + ")");
//            }
//
//            try {
//                Thread.sleep(500);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//
//        }
//    }
}
