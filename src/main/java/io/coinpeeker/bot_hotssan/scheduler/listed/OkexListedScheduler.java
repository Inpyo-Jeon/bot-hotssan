package io.coinpeeker.bot_hotssan.scheduler.listed;

import io.coinpeeker.bot_hotssan.common.CommonConstant;
import io.coinpeeker.bot_hotssan.scheduler.CoinMarketCapScheduler;
import io.coinpeeker.bot_hotssan.scheduler.Listing;
import io.coinpeeker.bot_hotssan.utils.HttpUtils;
import io.coinpeeker.bot_hotssan.utils.MessageUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class OkexListedScheduler implements Listing {

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
    private static final Logger LOGGER = LoggerFactory.getLogger(OkexListedScheduler.class);
    private static final String URL = "https://www.okex.com/api/v1/userinfo.do";
    private static final String API_KEY = "4b47a99a-bc50-4bf2-9ae3-3bb53b681148";
    private static final String SIGN = "E03C5D7899A25793A9E173EC80FC1B81";

    @Override
    public void init() throws IOException {
        if (hashOperations.keys("CoinMarketCap").isEmpty()) {
            LOGGER.info("@#@#@# CoinMarketCap Listing is null");
            coinMarketCapScheduler.refreshCoinData();
        }

        if (hashOperations.keys("OKExListing").isEmpty()) {
            LOGGER.info("@#@#@# OKEx Listing is null");

            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("api_key", API_KEY));
            params.add(new BasicNameValuePair("sign", SIGN));

            JSONObject jsonObject = httpUtils.getPostResponseByObject(URL, params);
            JSONObject list = jsonObject.getJSONObject("info").getJSONObject("funds").getJSONObject("free");

            for (Object item : list.keySet()) {
                hashOperations.put("OKExListing", item.toString(), "0");
            }
        }
    }


    @Override
    @Scheduled(initialDelay = 1000 * 6, fixedDelay = 1000 * 10)
    public void inspectListedCoin() throws IOException {
        init();

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("api_key", API_KEY));
        params.add(new BasicNameValuePair("sign", SIGN));

        JSONObject jsonObject = httpUtils.getPostResponseByObject(URL, params);
        JSONObject list = jsonObject.getJSONObject("info").getJSONObject("funds").getJSONObject("free");

        LOGGER.info("OKEx " + count + "회차 뺑뺑이 : " + hashOperations.values("OKExListing").size() + " // " + list.length());

        if (hashOperations.values("OKExListing").size() != list.length()) {

            Date date = new Date();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd, hh:mm:ss a");

            for (Object item : list.keySet()) {
                if (!hashOperations.hasKey("OKExListing", item.toString())) {
                    StringBuilder messageContent = new StringBuilder();
                    messageContent.append("!! OKEx 상장 정보 !!");
                    messageContent.append("\n지갑이 생성 되었나 봅니다.");
                    messageContent.append("\n확인시간 : ");
                    messageContent.append(simpleDateFormat.format(date).toString());
                    messageContent.append("\n코인 Symbol : ");
                    messageContent.append(item.toString().toUpperCase());

                    String url = CommonConstant.URL_TELEGRAM_BASE + apiKey + CommonConstant.METHOD_TELEGRAM_SENDMESSAGE;
                    messageUtils.sendMessage(url, -294606763L, messageContent.toString());

                    hashOperations.put("OKExListing", item.toString(), "0");
                }
            }
        }
        count++;
    }
}
