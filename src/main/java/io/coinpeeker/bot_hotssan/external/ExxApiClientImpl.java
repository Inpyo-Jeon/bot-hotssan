package io.coinpeeker.bot_hotssan.external;

import io.coinpeeker.bot_hotssan.model.CoinPrice;
import io.coinpeeker.bot_hotssan.utils.HttpUtils;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ExxApiClientImpl implements ApiClient {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ExxApiClientImpl.class);

    @Autowired
    private HttpUtils httpUtils;

    @Autowired
    private ExchangeApiClientImpl exchangeApiClient;

    @Override
    public CoinPrice getCoinPrice(String key) {
        CoinPrice coinPrice = new CoinPrice(key, "EXX");

        coinPrice.setSatoshi(getLastSatoshi(key));
        coinPrice.setUsd(String.valueOf((Double.valueOf(getLastUsdt()) * Double.valueOf(getLastSatoshi(key)))));

        return coinPrice;
    }

    private String getLastSatoshi(String key) {

        String url = "https://api.exx.com/data/v1/ticker?currency=" + key + "_btc";

        String price = null;

        try {
            JSONObject jsonObject = httpUtils.getResponseByObject(url);
            price = jsonObject.getJSONObject("ticker").getString("last");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return price;
    }


    private String getLastUsdt() {

        String url = "https://api.exx.com/data/v1/ticker?currency=btc_usdt";

        String price = null;

        try {
            JSONObject jsonObject = httpUtils.getResponseByObject(url);
            price = jsonObject.getJSONObject("ticker").getString("last");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return price;
    }

}
