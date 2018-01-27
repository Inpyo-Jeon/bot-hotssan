package io.coinpeeker.bot_hotssan.external;

import io.coinpeeker.bot_hotssan.model.CoinPrice;
import io.coinpeeker.bot_hotssan.utils.HttpUtils;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static io.coinpeeker.bot_hotssan.common.CommonConstant.API_COINRAIL_URL;

@Component
public class CoinrailApiClientImpl implements ApiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoinrailApiClientImpl.class);

    @Autowired
    private HttpUtils httpUtils;

    @Override
    public CoinPrice getCoinPrice(String key, double krwRate) {
        CoinPrice coinPrice = new CoinPrice(key, "코인레일");

        try {
            coinPrice.setKrw(String.valueOf(getKrw(key)));
        } catch (JSONException e) {
            Double satoshi = getLastSatoshi(key);
            Double krw = getKrw("btc") * satoshi;
            coinPrice.setSatoshi(String.valueOf(satoshi));
            coinPrice.setKrw(String.valueOf(krw));
        }


        return coinPrice;
    }

    private Double getLastSatoshi(String key) {
        Double price = 0.0;

        if ("BTC".equals(key)) {
            price = 1.0000000;
        } else {

            try {
                String currency = key + "-btc";
                URIBuilder uriInfo = new URIBuilder(API_COINRAIL_URL);
                uriInfo.addParameter("currency", currency);

                JSONObject jsonObject = httpUtils.getResponseByObject(uriInfo.toString());
                price = jsonObject.getDouble("last_price");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return price;
    }

    private Double getKrw(String key) {
        Double price = 0.0;
        try {
            String currency = key + "-krw";
            URIBuilder uriInfo = new URIBuilder(API_COINRAIL_URL);
            uriInfo.addParameter("currency", currency);

            JSONObject jsonObject = httpUtils.getResponseByObject(uriInfo.toString());
            price = jsonObject.getDouble("last_price");

        } catch (JSONException jsonException) {
            throw new JSONException("JSONException");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return price;
    }

}
