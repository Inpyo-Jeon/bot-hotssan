package io.coinpeeker.bot_hotssan.external;

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

    @Override
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
}
