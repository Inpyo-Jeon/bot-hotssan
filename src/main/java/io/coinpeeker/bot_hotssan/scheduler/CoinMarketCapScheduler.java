package io.coinpeeker.bot_hotssan.scheduler;

import io.coinpeeker.bot_hotssan.utils.HttpUtils;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

@Component
public class CoinMarketCapScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoinMarketCapScheduler.class);

    @Autowired
    private HttpUtils httpUtils;

    @Resource(name="redisTemplate")
    private HashOperations<String, String, String> hashOperations;

    @Scheduled(fixedRate = 1000 * 60 * 60 * 24)
    public void refreshCoinData() throws IOException {

        JSONArray jsonArray = httpUtils.getResponseByArrays("https://api.coinmarketcap.com/v1/ticker/?limit=1600");

        for (int i = 0; i < jsonArray.length(); i++){
            hashOperations.put("cap", jsonArray.getJSONObject(i).getString("symbol"), jsonArray.getJSONObject(i).getString("name"));
        }

        LOGGER.info(hashOperations.entries("cap").toString());
    }
}
