package io.coinpeeker.bot_hotssan.scheduler.listed;

import io.coinpeeker.bot_hotssan.common.CommonConstant;
import io.coinpeeker.bot_hotssan.scheduler.CoinMarketCapScheduler;
import io.coinpeeker.bot_hotssan.scheduler.Listing;
import io.coinpeeker.bot_hotssan.utils.HttpUtils;
import io.coinpeeker.bot_hotssan.utils.MessageUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

@Component
public class BinanceListedScheduler implements Listing {

    @Autowired
    HttpUtils httpUtils;

    @Autowired
    MessageUtils messageUtils;

    @Autowired
    CoinMarketCapScheduler coinMarketCapScheduler;

    @Resource(name = "redisTemplate")
    private HashOperations<String, String, String> hashOperations;

    @Value("${property.hotssan_id}")
    private String apiKey;

    private int count = 1;
    private static final Logger LOGGER = LoggerFactory.getLogger(BinanceListedScheduler.class);
    private static final String SECRET_KEY = "7pKYaqrMkI2o0sJRatGjRuaFwolPw4gfxhhZprcu9dqECZYFE0dBSdo2LgQY2cGp";
    private static final String HEADER_KEY = "X-MBX-APIKEY";
    private static final String HEADER_VALUE = "jPgNEo8XGAyDPuqpJJV3gnzNROGbV3F2jzWhVP7lpYAOcuBTex0OBZlfRApoiY2D";

    @Override
    public void init() throws IOException {

        // Redis에 coinMarketCap 존재 여부 체크
        if (hashOperations.keys("CoinMarketCap").isEmpty()) {
            LOGGER.info("@#@#@# CoinMarketCap Listing is null");
            coinMarketCapScheduler.refreshCoinData();
        }

        // Redis에 binance 상장목록 존재 여부 체크
        if (hashOperations.keys("BinanceListing").isEmpty()) {
            LOGGER.info("@#@#@# binance Listing is null");

            hashOperations.put("BinanceListing", "BTC", "0");

            JSONArray jsonArrayBinance = httpUtils.getResponseByArrays("https://api.binance.com/api/v3/ticker/price");
            for (int i = 0; i < jsonArrayBinance.length(); i++) {
                if (jsonArrayBinance.getJSONObject(i).getString("symbol").contains("BTC")) {
                    hashOperations.put("BinanceListing", jsonArrayBinance.getJSONObject(i).getString("symbol").replace("BTC", ""), "0");
                }
            }
        }
    }


    @Override
    @Scheduled(initialDelay = 1000 * 5, fixedDelay = 1000 * 5)
    public void inspectListedCoin() throws IOException {
        init();

        List<NameValuePair> header = new ArrayList<>();
        header.add(new BasicNameValuePair(HEADER_KEY, HEADER_VALUE));

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        List<String> noListedCoinList = new ArrayList<>();
        for (String item : hashOperations.keys("CoinMarketCap")) {
            if (!hashOperations.hasKey("BinanceListing", item)) {
                noListedCoinList.add(item);
            }
        }

        LOGGER.info("Binance " + count + "회차 뺑뺑이");

        Future<?> future = null;
        for (String item : noListedCoinList) {
            Runnable task = new Runnable() {
                @Override
                public void run() {
                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                    StringBuilder sb = new StringBuilder();
                    sb.append("https://api.binance.com/wapi/v3/depositAddress.html?");
                    String queryString = "asset=" + item + "&recvWindow=5000&timestamp=" + timestamp.getTime();
                    sb.append(queryString);

                    try {
                        sb.append("&signature=");
                        Mac mac = Mac.getInstance("HMACSHA256");
                        mac.init(new SecretKeySpec(SECRET_KEY.getBytes(), "HMACSHA256"));

                        for (byte byteItem : mac.doFinal(queryString.getBytes())) {
                            sb.append(Integer.toString((byteItem & 0xFF) + 0x100, 16).substring(1));
                        }

                        JSONObject jsonObject = httpUtils.getResponseByObject(sb.toString(), header);

                        LOGGER.info(jsonObject.toString());

                        if (jsonObject.getBoolean("success")) {
                            Date date = new Date();
                            StringBuilder messageContent = new StringBuilder();
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd, hh:mm:ss a");

                            messageContent.append("!! 바이낸스 상장 정보 !!");
                            messageContent.append("\n코인 정보 : ");
                            messageContent.append(hashOperations.get("CoinMarketCap", jsonObject.getString("asset")));
                            messageContent.append(" (");
                            messageContent.append(jsonObject.getString("asset"));
                            messageContent.append(")");
                            messageContent.append("\n주소 : ");
                            messageContent.append(jsonObject.getString("address"));
                            messageContent.append("\n확인시간 : ");
                            messageContent.append(simpleDateFormat.format(date).toString());

                            String url = CommonConstant.URL_TELEGRAM_BASE + apiKey + CommonConstant.METHOD_TELEGRAM_SENDMESSAGE;
                            messageUtils.sendMessage(url, -300048567L, messageContent.toString());

                            hashOperations.put("BinanceListing", item, "0");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    sb.setLength(0);
                }
            };

            future = executorService.submit(task);
        }

        try {
            future.get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        count++;
    }
}