package io.coinpeeker.bot_hotssan.scheduler;

import com.google.common.collect.Maps;
import io.coinpeeker.bot_hotssan.common.CommonConstant;
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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class OkexListedScheduler {

    @Autowired
    HttpUtils httpUtils;

    @Value("${property.hotssan_id}")
    private String apiKey;

    @Autowired
    MessageUtils messageUtils;

    private static final Logger LOGGER = LoggerFactory.getLogger(OkexListedScheduler.class);
    private static final String URL = "https://www.okex.com/api/v1/userinfo.do";
    private static final String API_KEY = "4b47a99a-bc50-4bf2-9ae3-3bb53b681148";
    private static final String SIGN = "E03C5D7899A25793A9E173EC80FC1B81";
    private static Map<String, Boolean> OKEX_LAST_LIST;

    @Scheduled(initialDelay = 5000, fixedDelay = 1000 * 10)
    public void checkListedCoin() {

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("api_key", API_KEY));
        params.add(new BasicNameValuePair("sign", SIGN));

        try {
            JSONObject jsonObject = httpUtils.getPostResponseByObject(URL, params);
            JSONObject list = jsonObject.getJSONObject("info").getJSONObject("funds").getJSONObject("free");

            if (OKEX_LAST_LIST == null) {
                LOGGER.info("@#@#@# OKEX_LAST_LIST is null");

                OKEX_LAST_LIST = Maps.newHashMap();

                for (Object item : list.keySet()) {
                    OKEX_LAST_LIST.put(item.toString(), true);
                }
            }

            Date date = new Date();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd, hh:mm:ss a");
            LOGGER.info(simpleDateFormat.format(date).toString() + " : " + String.valueOf(OKEX_LAST_LIST.size()) + "//" + String.valueOf(list.length()));


            if (OKEX_LAST_LIST.size() != list.length()) {

                for (Object item : list.keySet()) {
                    if (!OKEX_LAST_LIST.containsKey(item.toString())) {

                        OKEX_LAST_LIST.put(item.toString(), true);
                        StringBuilder sb = new StringBuilder();
                        sb.append(" !!!! OKEx 상장 정보 !!!! ");
                        sb.append("\n지갑이 생성 되었나 봅니다.");
                        sb.append("\n확인시간 : ");
                        sb.append(simpleDateFormat.format(date).toString());
                        sb.append("\n코인 Symbol : ");
                        sb.append(item.toString());

                        String url = CommonConstant.URL_TELEGRAM_BASE + apiKey + CommonConstant.METHOD_TELEGRAM_SENDMESSAGE;
                        messageUtils.sendMessage(url, -294606763L, sb.toString());
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
