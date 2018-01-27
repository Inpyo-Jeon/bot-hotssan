package io.coinpeeker.bot_hotssan.external;

import io.coinpeeker.bot_hotssan.model.CoinPrice;
import io.coinpeeker.bot_hotssan.utils.HttpUtils;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static io.coinpeeker.bot_hotssan.common.CommonConstant.API_EXX_URL;

@Component
public class ExxApiClientImpl implements ApiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExxApiClientImpl.class);

    @Autowired
    private HttpUtils httpUtils;

    @Override
    public CoinPrice getCoinPrice(String key, double krwRate) {
        CoinPrice coinPrice = new CoinPrice(key, "EXX");

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
            price = 1.00000000;
        } else {
            try {
                String currency = key + "_btc";
                URIBuilder uriInfo = new URIBuilder(API_EXX_URL);
                uriInfo.addParameter("currency", currency);

                JSONObject jsonObject = httpUtils.getResponseByObject(uriInfo.toString());
                price = jsonObject.getJSONObject("ticker").getDouble("last");
            } catch (Exception e) {
                LOGGER.info("@!@@!$!@$" + key);
                e.printStackTrace();
            }
        }
        return price;
    }


    private Double getLastUsdt() {
        Double price = 0.0;

        try {
            String currency = "btc_usdt";
            URIBuilder urlInfo = new URIBuilder(API_EXX_URL);
            urlInfo.addParameter("currency", currency);

            JSONObject jsonObject = httpUtils.getResponseByObject(urlInfo.toString());
            price = jsonObject.getJSONObject("ticker").getDouble("last");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return price;
    }


}
