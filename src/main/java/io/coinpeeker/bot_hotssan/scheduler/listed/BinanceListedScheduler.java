package io.coinpeeker.bot_hotssan.scheduler.listed;

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

    private int articleCount = 0;

    private static final Logger LOGGER = LoggerFactory.getLogger(BinanceListedScheduler.class);


    @Override
    @Scheduled(initialDelay = 1000 * 30, fixedDelay = 1)
    public void inspectListedCoin() throws IOException {
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
                    messageContent.append(marketInfo.availableMarketList(asset));


                    String url = CommonConstant.URL_TELEGRAM_BASE + apiKey + CommonConstant.METHOD_TELEGRAM_SENDMESSAGE;
                    messageUtils.sendMessage(url, -300048567L, messageContent.toString());
                    messageUtils.sendMessage(url, -277619118L, messageContent.toString());

                    synchronized (jedis) {
                        jedis.hset("L-Binance", asset, pic);
                    }

                    LOGGER.info("Binance 상장(Image) : " + asset + " (" + simpleDateFormat.format(nowDate) + ")");
                    LOGGER.info("이미지상 상장시간 : " + asset + " (" + simpleDateFormat.format(imageTimeStamp) + ")");
                }
            }
        }

        try {
            Random random = new Random();
            int randomNum = random.nextInt(10 - 3 + 1) + 3;
            Thread.sleep(randomNum * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Scheduled(initialDelay = 1000 * 20, fixedDelay = 1000 * 2)
    public void articleCheck() throws IOException {
        /** env validation check.**/
        if (!StringUtils.equals("real", env)) {
            return;
        }

        String endPoint = "https://support.binance.com/hc/api/internal/recent_activities?locale=en-us&page=1&per_page=1&locale=en-us";

        JSONObject jsonObject = httpUtils.getResponseByObject(endPoint);

        int lastCount = jsonObject.getInt("count");

        if (articleCount == 0) {
            LOGGER.info("@#@#@# articleCount is null");
            articleCount = lastCount;
        }

        if (articleCount != lastCount) {
            String type = jsonObject.getJSONArray("activities").getJSONObject(0).getJSONArray("breadcrumbs").getJSONObject(0).getString("name");
            String title = jsonObject.getJSONArray("activities").getJSONObject(0).getString("title");

            if ("New Listings".equals(type) && title.contains("Binance Lists")) {
                String asset = "";
                int begin = title.lastIndexOf("(");
                int end = title.indexOf(")");

                for (int idx = begin + 1; idx < end; idx++) {
                    asset += title.charAt(idx);
                }

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
                messageContent.append("\n확인방법 : InternalAPI");
                messageContent.append("\n코인정보 : ");

                synchronized (jedis) {
                    messageContent.append(jedis.hget("I-CoinMarketCap", asset));
                }

                messageContent.append(" (");
                messageContent.append(asset);
                messageContent.append(")");
                messageContent.append("\n구매가능 거래소 : ");
                messageContent.append(marketInfo.availableMarketList(asset));


                String url = CommonConstant.URL_TELEGRAM_BASE + apiKey + CommonConstant.METHOD_TELEGRAM_SENDMESSAGE;
                messageUtils.sendMessage(url, -300048567L, messageContent.toString());
                messageUtils.sendMessage(url, -277619118L, messageContent.toString());

                synchronized (jedis) {
                    jedis.hset("L-Binance-A", asset, "0");
                }

                LOGGER.info("Binance 상장(Article) : " + asset + " (" + simpleDateFormat.format(nowDate) + ")");
                LOGGER.info(messageContent.toString());
            }
            articleCount = lastCount;
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
//                    messageUtils.sendMessage(url, -277619118L, messageContent.toString());
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
