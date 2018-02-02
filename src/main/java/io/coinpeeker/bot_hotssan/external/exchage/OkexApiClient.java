package io.coinpeeker.bot_hotssan.external.exchage;

import io.coinpeeker.bot_hotssan.external.ApiClient;
import io.coinpeeker.bot_hotssan.model.CoinPrice;
import io.coinpeeker.bot_hotssan.utils.HttpUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.apache.http.client.utils.URIBuilder;


import static io.coinpeeker.bot_hotssan.common.CommonConstant.API_OKEX_URL;

@Component
public class OkexApiClient implements ApiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(OkexApiClient.class);

    @Autowired
    private HttpUtils httpUtils;

    @Override
    public CoinPrice getCoinPrice(String key, double krwRate) {
        CoinPrice coinPrice = new CoinPrice(key, "OKEx");

        double satoshi = getLastSatoshi(key);

        if ("DENT".equals(key)) {
            double usdtPerDent = getLastUsdt(key);
            double krw = krwRate * usdtPerDent;

            coinPrice.setSatoshi(String.valueOf(satoshi));
            coinPrice.setUsd(String.valueOf(usdtPerDent));
            coinPrice.setKrw(String.valueOf(krw));
        } else {
            double usdtPerBit = getLastUsdt("BTC") * satoshi;
            double krw = krwRate * usdtPerBit;

            coinPrice.setSatoshi(String.valueOf(satoshi));
            coinPrice.setUsd(String.valueOf(usdtPerBit));
            coinPrice.setKrw(String.valueOf(krw));
        }


        return coinPrice;
    }

    private double getLastSatoshi(String key) {
        double price = 0.0;

        if ("BTC".equals(key)) {
            price = 1.00000000;
        } else {
            try {
                String symbol = key + "_btc";
                URIBuilder uriInfo = new URIBuilder(API_OKEX_URL);
                uriInfo.addParameter("symbol", symbol);

                JSONObject jsonObject = httpUtils.getResponseByObject(uriInfo.toString());
                price = jsonObject.getJSONObject("ticker").getDouble("last");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return price;
    }


    private double getLastUsdt(String key) {
        double price = 0.0;

        try {
            String symbol = key + "_usdt";
            URIBuilder uriInfo = new URIBuilder(API_OKEX_URL);
            uriInfo.addParameter("symbol", symbol);

            JSONObject jsonObject = httpUtils.getResponseByObject(uriInfo.toString());
            price = jsonObject.getJSONObject("ticker").getDouble("last");

        } catch (Exception e) {
            e.printStackTrace();

        }
        return price;
    }
}
