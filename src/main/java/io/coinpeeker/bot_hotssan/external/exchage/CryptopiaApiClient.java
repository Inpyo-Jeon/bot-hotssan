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

import static io.coinpeeker.bot_hotssan.common.CommonConstant.API_CRYPTOPIA_URL;

@Component
public class CryptopiaApiClient implements ApiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(CryptopiaApiClient.class);

    @Autowired
    private HttpUtils httpUtils;

    @Override
    public CoinPrice getCoinPrice(String key, double krwRate) {
        CoinPrice coinPrice = new CoinPrice(key, "크립토피아");

        double satoshi = getLastSatoshi(key);
        double usdtPerBit = getLastUsdt("BTC") * satoshi;
        double krw = krwRate * usdtPerBit;

        coinPrice.setSatoshi(String.valueOf(satoshi));
        coinPrice.setUsd(String.valueOf(usdtPerBit));
        coinPrice.setKrw(String.valueOf(krw));

        return coinPrice;
    }

    private double getLastSatoshi(String key) {
        double price = 0.0;

        if ("BTC".equals(key)) {
            price = 1.00000000;
        } else {

            try {
                String symbol = "/api/GetMarket/" + key + "_BTC";
                URIBuilder uriInfo = new URIBuilder(API_CRYPTOPIA_URL);
                uriInfo.setPath(symbol);

                JSONObject jsonObject = httpUtils.getResponseByObject(uriInfo.toString());
                price = jsonObject.getJSONObject("Data").getDouble("LastPrice");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return price;
    }

    private double getLastUsdt(String key) {
        double price = 0.0;

        try {
            String symbol = "/api/GetMarket/" + key + "_USDT";
            URIBuilder uriInfo = new URIBuilder(API_CRYPTOPIA_URL);
            uriInfo.setPath(symbol);

            JSONObject jsonObject = httpUtils.getResponseByObject(uriInfo.toString());
            price = jsonObject.getJSONObject("Data").getDouble("LastPrice");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return price;
    }
}
