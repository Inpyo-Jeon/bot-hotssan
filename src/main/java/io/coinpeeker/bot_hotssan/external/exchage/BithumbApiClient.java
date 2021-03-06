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

import static io.coinpeeker.bot_hotssan.common.CommonConstant.API_BITHUMB_URL;

@Component
public class BithumbApiClient implements ApiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(BithumbApiClient.class);

    @Autowired
    private HttpUtils httpUtils;

    @Override
    public CoinPrice getCoinPrice(String key, double krwRate) {
        CoinPrice coinPrice = new CoinPrice(key, "빗썸");

        double krw = getLastKrw(key);

        coinPrice.setKrw(String.valueOf(krw));

        return coinPrice;
    }

    private double getLastKrw(String key) {

        double price = 0.0;

        try {
            String symbol = "/public/ticker/" + key;
            URIBuilder uriInfo = new URIBuilder(API_BITHUMB_URL);
            uriInfo.setPath(symbol);

            JSONObject jsonObject = httpUtils.getResponseByObject(uriInfo.toString());
            price = jsonObject.getJSONObject("data").getDouble("closing_price");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return price;

    }
}
