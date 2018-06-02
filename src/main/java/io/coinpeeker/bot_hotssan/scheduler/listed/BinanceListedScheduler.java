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
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class BinanceListedScheduler implements Listing {
    @Autowired
    MarketInfo marketInfo;

    @Autowired
    HttpUtils httpUtils;

    @Autowired
    MessageUtils messageUtils;

    @Value("${property.hotssan_id}")
    private String apiKey;

    @Value("${property.env}")
    private String env;

    @Autowired
    Jedis jedis;

    @Autowired
    private TradeAgency tradeAgency;

    private static final Logger LOGGER = LoggerFactory.getLogger(BinanceListedScheduler.class);


    @Override
    @Scheduled(initialDelay = 1000 * 30, fixedDelay = 1)
    public void inspectListedCoin() throws IOException, InvalidKeyException, NoSuchAlgorithmException, ParseException, JOSEException, WebSocketException {
        /** env validation check.**/
        if (!StringUtils.equals("real", env)) {
            return;
        }

        int listingCount = 0;
        String endPoint = "https://www.binance.com/dictionary/getAssetPic.html";

        JSONObject jsonObject = httpUtils.getPostResponseByObject(endPoint);
        JSONArray jsonArray = jsonObject.getJSONArray("data");

        synchronized (jedis) {
            listingCount = Math.toIntExact(jedis.hlen("L-Binance"));
        }

        if (listingCount != jsonArray.length()) {
            for (int i = 0; i < jsonArray.length(); i++) {
                boolean isExist = true;

                synchronized (jedis) {
                    if (!jedis.hexists("L-Binance", jsonArray.getJSONObject(i).getString("asset"))) {
                        isExist = false;
                    }
                }

                if (!isExist) {
                    String asset = jsonArray.getJSONObject(i).getString("asset");
                    String pic = "-";

                    if (jsonArray.getJSONObject(i).has("pic")) {
                        pic = jsonArray.getJSONObject(i).getString("pic");
                    }

                    int fileNameIndex = pic.lastIndexOf("/");
                    StringBuilder fileName = new StringBuilder();

                    for (int index = fileNameIndex + 1; index < pic.length(); index++) {
                        fileName.append(pic.charAt(index));
                    }

                    synchronized (jedis) {
                        jedis.hset("L-Binance", asset, pic);
                    }

                    Map<String, Map<String, String>> marketList = marketInfo.availableMarketList(asset);
                    tradeAgency.list("Binance", asset, marketList);

                    Date nowDate = new Date();
                    Date imageTimeStamp = new Date(Long.valueOf(fileName.toString().replaceAll("\\D", "")));
                    StringBuilder messageContent = new StringBuilder();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss (z Z)");
                    simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

                    messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
                    messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
                    messageContent.append(" [ Binance ] 상장 정보 ");
                    messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
                    messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
                    messageContent.append("\n");
                    messageContent.append(simpleDateFormat.format(nowDate));
                    messageContent.append("\n확인방법 : Image");
                    messageContent.append("\n코인정보 : ");

                    synchronized (jedis) {
                        messageContent.append(jedis.hget("I-CoinMarketCap", asset));
                    }

                    messageContent.append(" (");
                    messageContent.append(asset);
                    messageContent.append(")");
                    messageContent.append("\n이미지주소 : ");
                    messageContent.append(pic);
                    messageContent.append("\n변환시간 : ");
                    messageContent.append(simpleDateFormat.format(imageTimeStamp));
                    messageContent.append("\n구매가능 거래소 : ");
                    messageContent.append(marketInfo.marketInfo(marketList));


                    String url = CommonConstant.URL_TELEGRAM_BASE + apiKey + CommonConstant.METHOD_TELEGRAM_SENDMESSAGE;
                    messageUtils.sendMessage(url, -300048567L, messageContent.toString());
                    messageUtils.sendMessage(url, -319177275L, messageContent.toString());

                    LOGGER.info("Binance 상장(Image) : " + asset + " (" + simpleDateFormat.format(nowDate) + ")");
                    LOGGER.info("이미지상 상장시간 : " + asset + " (" + simpleDateFormat.format(imageTimeStamp) + ")");
                }
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

    @Scheduled(initialDelay = 1000 * 20, fixedDelay = 1000 * 1)
    public void articleCheck() throws IOException, InvalidKeyException, NoSuchAlgorithmException, ParseException, JOSEException, WebSocketException {
        /** env validation check.**/
        if (!StringUtils.equals("real", env)) {
            return;
        }

        String endPoint = "https://support.binance.com/hc/api/internal/recent_activities?locale=en-us&page=1&per_page=1&locale=en-us";

        JSONObject jsonObject = httpUtils.getResponseByObject(endPoint);

        int lastCount = jsonObject.getInt("count");
        int activitiesCount = 0;

        synchronized (jedis) {
            activitiesCount = Integer.valueOf(jedis.hget("L-Binance-InternalAPI-Activities", "count"));
        }

        if (activitiesCount != lastCount) {
            String type = jsonObject.getJSONArray("activities").getJSONObject(0).getJSONArray("breadcrumbs").getJSONObject(0).getString("name");
            String title = jsonObject.getJSONArray("activities").getJSONObject(0).getString("title");

            if ("New Listings".equals(type) && (title.contains("Binance Lists") || (title.contains("Binance Will List")))) {
                String asset = "";
                int begin = title.lastIndexOf("(");
                int end = title.indexOf(")");

                for (int idx = begin + 1; idx < end; idx++) {
                    asset += title.charAt(idx);
                }

                boolean isExist = true;

                synchronized (jedis) {
                    if (!jedis.hexists("L-Binance", asset)) {
                        isExist = false;
                    }
                }

                if (!isExist) {

                    synchronized (jedis) {
                        jedis.hset("L-Binance", asset, "Activities");
                    }

                    Map<String, Map<String, String>> marketList = marketInfo.availableMarketList(asset);
                    tradeAgency.list("Binance", asset, marketList);

                    Date nowDate = new Date();
                    StringBuilder messageContent = new StringBuilder();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss (z Z)");
                    simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

                    messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
                    messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
                    messageContent.append(" [ Binance ] 상장 정보 ");
                    messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
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


                    String url = CommonConstant.URL_TELEGRAM_BASE + apiKey + CommonConstant.METHOD_TELEGRAM_SENDMESSAGE;
                    messageUtils.sendMessage(url, -300048567L, messageContent.toString());
                    messageUtils.sendMessage(url, -319177275L, messageContent.toString());

                    LOGGER.info("Binance 상장(activities) : " + asset + " (" + simpleDateFormat.format(nowDate) + ")");
                    LOGGER.info(messageContent.toString());
                }
            }

            synchronized (jedis) {
                jedis.hset("L-Binance-InternalAPI-Activities", "count", String.valueOf(lastCount));
            }
        }
    }

    @Scheduled(initialDelay = 1000 * 5, fixedDelay = 1000 * 60)
    public void articleCheckVer2() throws IOException, InvalidKeyException, NoSuchAlgorithmException, ParseException, JOSEException, WebSocketException {
        /** env validation check.**/
        if (!StringUtils.equals("real", env)) {
            return;
        }

        String endPoint = "https://support.binance.com/api/v2/help_center/en-us/sections/115000106672/articles.json?page=1&per_page=1";

        CloseableHttpResponse response = httpUtils.get(endPoint);

        if (response.getStatusLine().getStatusCode() == 200) {
            JSONObject jsonObject = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));
            String title = jsonObject.getJSONArray("articles").getJSONObject(0).getString("title");
            int count = jsonObject.getInt("count");
            int articlesCount = 0;

            synchronized (jedis) {
                articlesCount = Integer.valueOf(jedis.hget("L-Binance-InternalAPI-Articles", "count"));
            }

            if (articlesCount != count) {
                if (title.contains("Binance Lists") || title.contains("Binance Will List")) {
                    String asset = "";
                    int begin = title.lastIndexOf("(");
                    int end = title.indexOf(")");

                    for (int idx = begin + 1; idx < end; idx++) {
                        asset += title.charAt(idx);
                    }

                    boolean isExist = true;

                    synchronized (jedis) {
                        if (!jedis.hexists("L-Binance", asset)) {
                            isExist = false;
                        }
                    }

                    if (!isExist) {

                        synchronized (jedis) {
                            jedis.hset("L-Binance", asset, "Articles");
                        }

                        Map<String, Map<String, String>> marketList = marketInfo.availableMarketList(asset);
                        tradeAgency.list("Binance", asset, marketList);

                        Date nowDate = new Date();
                        StringBuilder messageContent = new StringBuilder();
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss (z Z)");
                        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

                        messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
                        messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
                        messageContent.append(" [ Binance ] 상장 정보 ");
                        messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
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


                        String url = CommonConstant.URL_TELEGRAM_BASE + apiKey + CommonConstant.METHOD_TELEGRAM_SENDMESSAGE;
                        messageUtils.sendMessage(url, -300048567L, messageContent.toString());
                        messageUtils.sendMessage(url, -319177275L, messageContent.toString());

                        LOGGER.info("Binance 상장(InternalAPI-articles) : " + asset + " (" + simpleDateFormat.format(nowDate) + ")");
                        LOGGER.info(messageContent.toString());
                    }
                }

                synchronized (jedis) {
                    jedis.hset("L-Binance-InternalAPI-Articles", "count", String.valueOf(count));
                }
            }
        }


        if (response.getStatusLine().getStatusCode() == 429) {
            LOGGER.info("-- Binance Article 429 ERROR --");
            int delay = 0;
            Header[] headers = response.getAllHeaders();
            for (Header header : headers) {
                if (header.getName().equals("Retry-After")) {
                    delay = Integer.valueOf(header.getValue());
                    LOGGER.info("-- delay time : " + delay + "--");
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
            LOGGER.info("-- Binance Article 418 ERROR --");
            try {
                Thread.sleep(1000 * 60 * 60 * 24);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}


//    // 지갑으로 찾기
//    @Override
//    @Scheduled(initialDelay = 1000 * 60, fixedDelay = 1000 * 10)
//    public void inspectListedCoin2() throws IOException {
//        /** env validation check.**/
//        if (!StringUtils.equals("real", env)) {
//            return;
//        }
//
//
//        List<NameValuePair> header = new ArrayList<>();
//        header.add(new BasicNameValuePair(SecretKey.getHeaderKeyBinance(), SecretKey.getHeaderValueBinance()));
//
//        List<String> noListedCoinList = new ArrayList<>();
//        List<String> capList = new ArrayList<>();
//        capList.addAll(CommonConstant.getCapList());
//
//        for (String item : capList) {
//            synchronized (jedis) {
//                if (!jedis.hexists("BinanceListing", item)) {
//                    noListedCoinList.add(item);
//                }
//            }
//        }
//
//        for (String item : noListedCoinList) {
//            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
//            StringBuilder sb = new StringBuilder();
//            sb.append("https://api.binance.com/wapi/v3/depositAddress.html?");
//            String queryString = "asset=" + item + "&recvWindow=5000&timestamp=" + timestamp.getTime();
//            sb.append(queryString);
//
//            try {
//                sb.append("&signature=");
//                Mac mac = Mac.getInstance("HMACSHA256");
//                mac.init(new SecretKeySpec(SecretKey.getSecretKeyBinance().getBytes(), "HMACSHA256"));
//
//                for (byte byteItem : mac.doFinal(queryString.getBytes())) {
//                    sb.append(Integer.toString((byteItem & 0xFF) + 0x100, 16).substring(1));
//                }
//
//                JSONObject jsonObject = httpUtils.getResponseByObject(sb.toString(), header);
//
//                if (jsonObject.has("ContentLengthZero")) {
//                    sb.setLength(0);
//                    continue;
//                }
//
//                if (jsonObject.getBoolean("success")) {
//                    Date date = new Date();
//                    StringBuilder messageContent = new StringBuilder();
//                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd, hh:mm:ss a");
//
//                    messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
//                    messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
//                    messageContent.append(" [ Binance ] 상장 정보 ");
//                    messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
//                    messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
//                    messageContent.append("\n상장 리스트 탐지되었습니다(지갑주소)");
//                    messageContent.append("\n코인 정보 : ");
//                    synchronized (jedis) {
//                        messageContent.append(jedis.hget("CoinMarketCap", item));
//                    }
//                    messageContent.append(" (");
//                    messageContent.append(item);
//                    messageContent.append(")");
//                    messageContent.append("\n지갑주소 : ");
//                    messageContent.append(jsonObject.getString("address"));
//                    messageContent.append("\n구매 가능 거래소");
//                    messageContent.append(marketInfo.availableMarketList(item));
//
//
//                    String url = CommonConstant.URL_TELEGRAM_BASE + apiKey + CommonConstant.METHOD_TELEGRAM_SENDMESSAGE;
//                    messageUtils.sendMessage(url, -300048567L, messageContent.toString());
//                    messageUtils.sendMessage(url, -319177275L, messageContent.toString());
//
//                    synchronized (jedis) {
//                        jedis.hset("BinanceListing", item, "0");
//                    }
//
//                    LOGGER.info("Binance 상장 : " + item + " (" + simpleDateFormat.format(date).toString() + ")");
//                } else if (!jsonObject.getBoolean("success")) {
//
//                } else {
//                    LOGGER.info(" [ Binance ] 상장 정보 이상발생");
//                    LOGGER.info("코인 정보 : " + item);
//                    LOGGER.info("에러내용 : " + jsonObject.toString());
//                }
//            } catch (Exception e) {
//                LOGGER.info(" [ Binance ] Exception");
//                LOGGER.info("코인 정보 : " + item);
//                e.printStackTrace();
//            }
//            sb.setLength(0);
//        }
//    }
