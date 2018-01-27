package io.coinpeeker.bot_hotssan.external.exchage;

import io.coinpeeker.bot_hotssan.external.ApiClient;
import io.coinpeeker.bot_hotssan.model.CoinPrice;
import io.coinpeeker.bot_hotssan.utils.HttpUtils;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static io.coinpeeker.bot_hotssan.common.CommonConstant.API_BINANCE_URL;

@Component
public class BinanceApiClient implements ApiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(BinanceApiClient.class);

    @Autowired
    private HttpUtils httpUtils;

    @Override
    public CoinPrice getCoinPrice(String key, double krwRate) {
        CoinPrice coinPrice = new CoinPrice(key, "바이낸스");

        Double satoshi = getLastSatoshi(key);
        Double usdt = getLastUsdt();
        Double usd = usdt * satoshi;
        Double krw = krwRate * usd;

        coinPrice.setSatoshi(String.valueOf(satoshi));
        coinPrice.setUsd(String.valueOf(usd));
        coinPrice.setKrw(String.valueOf(krw));
        return coinPrice;
    }

    private Double getLastSatoshi(String key) {
        Double price = 0.0;

        if ("BTC".equals(key)) {
            price = 1.0000000;
        } else {

            try {
                String symbol = key + "BTC";
                URIBuilder uriInfo = new URIBuilder(API_BINANCE_URL);
                uriInfo.addParameter("symbol", symbol);

                JSONObject jsonObject = httpUtils.getResponseByObject(uriInfo.toString());
                price = jsonObject.getDouble("price");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return price;
    }

    private Double getLastUsdt() {
        Double price = 0.0;

        try {
            String symbol = "BTCUSDT";
            URIBuilder uriInfo = new URIBuilder(API_BINANCE_URL);
            uriInfo.addParameter("symbol", symbol);
            JSONObject jsonObject = httpUtils.getResponseByObject(uriInfo.toString());
            price = jsonObject.getDouble("price");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return price;
    }

}
