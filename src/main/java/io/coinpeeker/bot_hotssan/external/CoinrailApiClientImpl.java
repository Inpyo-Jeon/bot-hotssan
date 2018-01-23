package io.coinpeeker.bot_hotssan.external;

import io.coinpeeker.bot_hotssan.model.CoinPrice;
import io.coinpeeker.bot_hotssan.utils.HttpUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CoinrailApiClientImpl implements ApiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoinrailApiClientImpl.class);

    @Autowired
    private HttpUtils httpUtils;

    public String lastPrice(String key) {

        String url = "https://api.coinrail.co.kr/public/last/order?currency=dent-btc";

        String price = "";
        try {
            JSONObject jsonObject = httpUtils.getResponseByObject(url);

            price = jsonObject.getString("last_price");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return price;
    }

    @Override
    public CoinPrice getCoinPrice(String key) {
        CoinPrice coinPrice = new CoinPrice(key, "코인레일");

        coinPrice.setKrw("1000 원");
        coinPrice.setSatoshi("0.00000004");
        coinPrice.setUsd("0.23 USD");

        return coinPrice;
    }


}
