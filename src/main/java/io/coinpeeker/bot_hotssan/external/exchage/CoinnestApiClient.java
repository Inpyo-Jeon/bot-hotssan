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

import static io.coinpeeker.bot_hotssan.common.CommonConstant.API_COINNEST_URL;
import static io.coinpeeker.bot_hotssan.common.CommonConstant.API_UPBIT_URL;

@Component
public class CoinnestApiClient implements ApiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoinnestApiClient.class);

    @Autowired
    private HttpUtils httpUtils;

    @Override
    public CoinPrice getCoinPrice(String key, double krwRate) {
        CoinPrice coinPrice = new CoinPrice(key, "코인네스트");

        Double krw = getLastKrw(key);

        coinPrice.setKrw(String.valueOf(krw));

        return coinPrice;
    }

    private Double getLastKrw(String symbol) {
        double price = 0;

        try {
            URIBuilder uriInfo = new URIBuilder(API_COINNEST_URL);
            uriInfo.addParameter("coin", symbol.toLowerCase());

            JSONObject jsonObject = httpUtils.getResponseByObject(uriInfo.toString());
            price = jsonObject.getDouble("last");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return price;
    }


}
