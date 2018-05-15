package io.coinpeeker.bot_hotssan.scheduler.listed;


import io.coinpeeker.bot_hotssan.trade.api.Binance;
import io.coinpeeker.bot_hotssan.utils.HttpUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class HuobiListedSchedulerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(Binance.class);

    @Autowired
    HttpUtils httpUtils;

    @Autowired
    private Jedis jedis;

    @Test
    public void huobiKorAssetInfo() throws NoSuchAlgorithmException, InvalidKeyException, IOException {

        String checkUrl = "https://www.huobi.com/p/api/contents/pro/single_page?lang=ko-kr&pageType=1";

        JSONObject jsonObject = httpUtils.getResponseByObject(checkUrl);
        JSONArray jsonArray = jsonObject.getJSONArray("data");
        int currentCount = 0;

        synchronized (jedis){
            currentCount = Math.toIntExact(jedis.hlen("L-Huobi-Kor-Asset"));
        }

        if(jsonArray.length() != currentCount){
            for(int i = 0; i < jsonArray.length(); i++){
                boolean isExist = true;

                synchronized (jedis) {
                    if (!jedis.hexists("L-Huobi-Kor-Asset", jsonArray.getJSONObject(i).getString("pageIdentifier"))) {
                        isExist = false;
                    }
                }

                if (!isExist) {
                    System.out.println("--상장--");
                    System.out.println("심볼 : " + jsonArray.getJSONObject(i).getString("pageIdentifier").toUpperCase());

                    synchronized (jedis) {
                        jedis.hset("L-Huobi-Kor-Asset", jsonArray.getJSONObject(i).getString("pageIdentifier"), jsonArray.getJSONObject(i).getString("title"));
                    }
                }
            }
        }
    }

    @Test
    public void huobiEngAssetInfo() throws NoSuchAlgorithmException, InvalidKeyException, IOException {

        String checkUrl = "https://www.huobi.com/p/api/contents/pro/single_page?lang=en-us&pageType=1";

        JSONObject jsonObject = httpUtils.getResponseByObject(checkUrl);
        JSONArray jsonArray = jsonObject.getJSONArray("data");
        int currentCount = 0;

        synchronized (jedis){
            currentCount = Math.toIntExact(jedis.hlen("L-Huobi-Eng-Asset"));
        }

        if(jsonArray.length() != currentCount){
            for(int i = 0; i < jsonArray.length(); i++){
                boolean isExist = true;

                synchronized (jedis) {
                    if (!jedis.hexists("L-Huobi-Eng-Asset", jsonArray.getJSONObject(i).getString("pageIdentifier"))) {
                        isExist = false;
                    }
                }

                if (!isExist) {
                    System.out.println("--상장--");
                    System.out.println("심볼 : " + jsonArray.getJSONObject(i).getString("pageIdentifier").toUpperCase());

                    synchronized (jedis) {
                        jedis.hset("L-Huobi-Eng-Asset", jsonArray.getJSONObject(i).getString("pageIdentifier"), jsonArray.getJSONObject(i).getString("title"));
                    }
                }
            }
        }
    }
}