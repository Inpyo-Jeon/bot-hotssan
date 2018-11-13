package io.coinpeeker.bot_hotssan.scheduler.listed;

import com.neovisionaries.ws.client.WebSocketException;
import com.nimbusds.jose.JOSEException;
import io.coinpeeker.bot_hotssan.common.CommonConstant;
import io.coinpeeker.bot_hotssan.common.SecretKey;
import io.coinpeeker.bot_hotssan.feature.MarketInfo;
import io.coinpeeker.bot_hotssan.scheduler.Listing;
import io.coinpeeker.bot_hotssan.trade.TradeAgency;
import io.coinpeeker.bot_hotssan.utils.HttpUtils;
import io.coinpeeker.bot_hotssan.utils.MessageUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
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

    @Value("${hotssan_id}")
    private String apiKey;

    @Value("${spring.profiles.active}")
    private String env;

    @Autowired
    private TradeAgency tradeAgency;

    private static final Logger LOGGER = LoggerFactory.getLogger(OkexListedScheduler.class);

    @Override
//    @Scheduled(initialDelay = 1000 * 60, fixedDelay = 1000 * 2)
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
                    Map<String, Map<String, String>> marketList = marketInfo.availableMarketList(toStringItem.toUpperCase());
//                    tradeAgency.list("OKEx", toStringItem.toUpperCase(), marketList);

                    StringBuilder messageContent = new StringBuilder();
                    messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
                    messageContent.append(" [ OKEx ] 상장 정보 ");
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

    @Scheduled(initialDelay = 1000 * 20, fixedDelay = 1000 * 3)
    public void articleCheck() throws IOException, InvalidKeyException, NoSuchAlgorithmException, ParseException, JOSEException, WebSocketException {
        /** env validation check.**/
        if (!StringUtils.equals("real", env)) {
            return;
        }

        String endPoint = "https://support.okex.com/hc/api/internal/recent_activities?locale=en-us&page=1&per_page=1&locale=en-us";

        JSONObject jsonObject = httpUtils.getResponseByObject(endPoint);

        int lastCount = jsonObject.getInt("count");
        int activitiesCount = 0;

        synchronized (jedis) {
            activitiesCount = Integer.valueOf(jedis.hget("L-OKEx-InternalAPI-Activities", "count"));
        }

        if (activitiesCount != lastCount) {
            String type = jsonObject.getJSONArray("activities").getJSONObject(0).getJSONArray("breadcrumbs").getJSONObject(0).getString("name");
            String title = jsonObject.getJSONArray("activities").getJSONObject(0).getString("title");

            if ("New Token".equals(type) && (title.contains("Now Available"))) {
                int bracketCount = StringUtils.countMatches(title, "(");
                if (bracketCount == 1) {
                    String asset = "";
                    int begin = title.lastIndexOf("(");
                    int end = title.indexOf(")");

                    for (int idx = begin + 1; idx < end; idx++) {
                        asset += title.charAt(idx);
                    }

                    boolean isExist = true;
                    synchronized (jedis) {
                        if (!jedis.hexists("L-OKEx", asset)) {
                            isExist = false;
                        }
                    }

                    if (!isExist) {

                        synchronized (jedis) {
                            jedis.hset("L-OKEx", asset, "Activities");
                        }

                        Map<String, Map<String, String>> marketList = marketInfo.availableMarketList(asset);
                        String orderResult = tradeAgency.list("OKEx", asset, marketList);

                        Date nowDate = new Date();
                        StringBuilder messageContent = new StringBuilder();
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss (z Z)");
                        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

                        messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
                        messageContent.append(" [ OKEx ] 상장 정보 ");
                        messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
                        messageContent.append("\n");
                        messageContent.append(simpleDateFormat.format(nowDate));
                        messageContent.append("\n확인방법 : InternalAPI(activities)");
                        messageContent.append("\n코인정보 : ");

                        synchronized (jedis) {
                            messageContent.append(jedis.hget("I-CoinMarketCap", asset));
                        }

                        messageContent.append(" (");
                        messageContent.append(asset);
                        messageContent.append(")");
                        messageContent.append("\n구매가능 거래소 : ");
                        messageContent.append(marketInfo.marketInfo(marketList));
                        messageContent.append(orderResult);


                        String url = CommonConstant.URL_TELEGRAM_BASE + apiKey + CommonConstant.METHOD_TELEGRAM_SENDMESSAGE;
                        messageUtils.sendMessage(url, -300048567L, messageContent.toString());
                        messageUtils.sendMessage(url, -319177275L, messageContent.toString());

                        LOGGER.info("OKEx 상장(activities) : " + asset + " (" + simpleDateFormat.format(nowDate) + ")");
                        LOGGER.info(messageContent.toString());
                    }
                }
            } else if ("Cryptocurrency Intro".equals(type)) {
                int bracketCount = StringUtils.countMatches(title, "(");
                if (bracketCount == 1) {
                    String asset = "";
                    int begin = title.lastIndexOf("(");
                    int end = title.indexOf(")");

                    for (int idx = begin + 1; idx < end; idx++) {
                        asset += title.charAt(idx);
                    }

                    boolean isExist = true;
                    synchronized (jedis) {
                        if (!jedis.hexists("L-OKEx", asset)) {
                            isExist = false;
                        }
                    }

                    if (!isExist) {

                        synchronized (jedis) {
                            jedis.hset("L-OKEx", asset, "Activities");
                        }

                        Map<String, Map<String, String>> marketList = marketInfo.availableMarketList(asset);
                        String orderResult = tradeAgency.list("OKEx", asset, marketList);

                        Date nowDate = new Date();
                        StringBuilder messageContent = new StringBuilder();
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss (z Z)");
                        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

                        messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
                        messageContent.append(" [ OKEx ] 상장 정보 ");
                        messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
                        messageContent.append("\n");
                        messageContent.append(simpleDateFormat.format(nowDate));
                        messageContent.append("\n확인방법 : InternalAPI(Intro)");
                        messageContent.append("\n코인정보 : ");

                        synchronized (jedis) {
                            messageContent.append(jedis.hget("I-CoinMarketCap", asset));
                        }

                        messageContent.append(" (");
                        messageContent.append(asset);
                        messageContent.append(")");
                        messageContent.append("\n구매가능 거래소 : ");
                        messageContent.append(marketInfo.marketInfo(marketList));
                        messageContent.append(orderResult);


                        String url = CommonConstant.URL_TELEGRAM_BASE + apiKey + CommonConstant.METHOD_TELEGRAM_SENDMESSAGE;
                        messageUtils.sendMessage(url, -300048567L, messageContent.toString());
                        messageUtils.sendMessage(url, -319177275L, messageContent.toString());

                        LOGGER.info("OKEx 상장(Intro) : " + asset + " (" + simpleDateFormat.format(nowDate) + ")");
                        LOGGER.info(messageContent.toString());
                    }
                }


            }

            synchronized (jedis) {
                jedis.hset("L-OKEx-InternalAPI-Activities", "count", String.valueOf(lastCount));
            }
        }
    }

    @Scheduled(initialDelay = 1000 * 5, fixedDelay = 1000 * 15)
    public void articleCheckVer2() throws IOException, ParseException, WebSocketException, NoSuchAlgorithmException, InvalidKeyException, JOSEException {
        /** env validation check.**/
        if (!StringUtils.equals("real", env)) {
            return;
        }

        String endPoint = "https://support.okex.com/api/v2/help_center/en-us/sections/115000447632/articles.json?page=1&per_page=1";

        CloseableHttpResponse response = httpUtils.get(endPoint);

        if (response.getStatusLine().getStatusCode() == 200) {
            JSONObject jsonObject = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
            String title = jsonObject.getJSONArray("articles").getJSONObject(0).getString("title");
            int count = jsonObject.getInt("count");
            int articlesCount = 0;

            synchronized (jedis) {
                articlesCount = Integer.valueOf(jedis.hget("L-OKEx-InternalAPI-Articles", "count"));
            }

            if (articlesCount != count) {
                if (title.contains("Now Available")) {
                    int bracketCount = StringUtils.countMatches(title, "(");
                    if (bracketCount == 1) {
                        String asset = "";
                        int begin = title.lastIndexOf("(");
                        int end = title.indexOf(")");

                        for (int idx = begin + 1; idx < end; idx++) {
                            asset += title.charAt(idx);
                        }

                        boolean isExist = true;

                        synchronized (jedis) {
                            if (!jedis.hexists("L-OKEx", asset)) {
                                isExist = false;
                            }
                        }

                        if (!isExist) {

                            synchronized (jedis) {
                                jedis.hset("L-OKEx", asset, "Articles");
                            }

                            Map<String, Map<String, String>> marketList = marketInfo.availableMarketList(asset);
                            String orderResult = tradeAgency.list("OKEx", asset, marketList);


                            Date nowDate = new Date();
                            StringBuilder messageContent = new StringBuilder();
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss (z Z)");
                            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

                            messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
                            messageContent.append(" [ OKEx ] 상장 정보 ");
                            messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
                            messageContent.append("\n");
                            messageContent.append(simpleDateFormat.format(nowDate));
                            messageContent.append("\n확인방법 : InternalAPI(articles)");
                            messageContent.append("\n코인정보 : ");

                            synchronized (jedis) {
                                messageContent.append(jedis.hget("I-CoinMarketCap", asset));
                            }

                            messageContent.append(" (");
                            messageContent.append(asset);
                            messageContent.append(")");
                            messageContent.append("\n구매가능 거래소 : ");
                            messageContent.append(marketInfo.marketInfo(marketList));
                            messageContent.append(orderResult);

                            String url = CommonConstant.URL_TELEGRAM_BASE + apiKey + CommonConstant.METHOD_TELEGRAM_SENDMESSAGE;
                            messageUtils.sendMessage(url, -300048567L, messageContent.toString());
                            messageUtils.sendMessage(url, -319177275L, messageContent.toString());

                            LOGGER.info("OKEx 상장(InternalAPI-articles) : " + asset + " (" + simpleDateFormat.format(nowDate) + ")");
                            LOGGER.info(messageContent.toString());
                        }
                    }
                }

                synchronized (jedis) {
                    jedis.hset("L-OKEx-InternalAPI-Articles", "count", String.valueOf(count));
                }
            }
        }


        if (response.getStatusLine().getStatusCode() == 429) {
            LOGGER.info("-- OKEx Article 429 ERROR --");

            int delay = 0;
            Header[] headers = response.getAllHeaders();
            for (Header header : headers) {
                if (header.getName().equals("Retry-After")) {
                    delay = Integer.valueOf(header.getValue());
                    LOGGER.info(String.valueOf(delay));
                }
            }

            try {
                if (response.getStatusLine().getStatusCode() == 429) {
                    Thread.sleep((1000 * delay) + (1000 * 1));
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (response.getStatusLine().getStatusCode() == 418) {
            LOGGER.info("-- OKEx Article 418 ERROR --");
            try {
                Thread.sleep(1000 * 60 * 60 * 24);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
