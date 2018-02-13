package io.coinpeeker.bot_hotssan.scheduler;

import io.coinpeeker.bot_hotssan.common.CommonConstant;
import io.coinpeeker.bot_hotssan.common.NoListedCoin;
import io.coinpeeker.bot_hotssan.utils.HttpUtils;
import io.coinpeeker.bot_hotssan.utils.MessageUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

@Component
public class BinanceListedScheduler {

    @Autowired
    NoListedCoin noListedCoin;

    @Autowired
    HttpUtils httpUtils;

    @Value("${property.hotssan_id}")
    private String apiKey;

    @Autowired
    MessageUtils messageUtils;

    private static final Logger LOGGER = LoggerFactory.getLogger(BinanceListedScheduler.class);
    private int count = 1;
    private static final String SECRET_KEY = "7pKYaqrMkI2o0sJRatGjRuaFwolPw4gfxhhZprcu9dqECZYFE0dBSdo2LgQY2cGp";
    private static final String HEADER_KEY = "X-MBX-APIKEY";
    private static final String HEADER_VALUE = "jPgNEo8XGAyDPuqpJJV3gnzNROGbV3F2jzWhVP7lpYAOcuBTex0OBZlfRApoiY2D";


    @Scheduled(initialDelay = 1000, fixedDelay = 5000)
    public void checkListedCoin() {
        List<NameValuePair> header = new ArrayList<>();
        header.add(new BasicNameValuePair(HEADER_KEY, HEADER_VALUE));

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        LOGGER.info(count + "회차 뺑뺑이");
        Future<?> future = null;


        for (int i = 0; i < noListedCoin.getCoinList().size(); i++) {
            String currentCoin = noListedCoin.getCoinList().get(i);

            Runnable task = new Runnable() {
                @Override
                public void run() {
                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                    StringBuilder sb = new StringBuilder();
                    sb.append("https://api.binance.com/wapi/v3/depositAddress.html?");
                    String queryString = "asset=" + currentCoin + "&recvWindow=5000&timestamp=" + timestamp.getTime();
                    sb.append(queryString);

                    try {
                        sb.append("&signature=");
                        Mac mac = Mac.getInstance("HMACSHA256");
                        mac.init(new SecretKeySpec(SECRET_KEY.getBytes(), "HMACSHA256"));

                        for (byte byteItem : mac.doFinal(queryString.getBytes())) {
                            sb.append(Integer.toString((byteItem & 0xFF) + 0x100, 16).substring(1));
                        }

                        JSONObject jsonObject = httpUtils.getResponseByObject(sb.toString(), header);

                        LOGGER.info(jsonObject.toString() + " : " + currentCoin);


                        if (jsonObject.getBoolean("success")) {
                            StringBuilder content = new StringBuilder();
                            Date today = new Date();
                            content.append("!! 바이낸스 상장 정보 !!");
                            content.append("\n상장 예정 코인 : ");
                            content.append(jsonObject.getString("asset"));
                            content.append("\n주소 : ");
                            content.append(jsonObject.getString("address"));
                            content.append("\n딱 걸린 시간 : ");
                            content.append(today);
                            LOGGER.info(jsonObject.getBoolean("success") + " : " + currentCoin);
                            content.append(jsonObject.toString());
                            String url = CommonConstant.URL_TELEGRAM_BASE + apiKey + CommonConstant.METHOD_TELEGRAM_SENDMESSAGE;
                            messageUtils.sendMessage(url, -259666461L, content.toString());

                        }
                    } catch (InvalidKeyException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (NoSuchAlgorithmException e) {
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