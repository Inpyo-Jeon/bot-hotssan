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

import static io.coinpeeker.bot_hotssan.common.CommonConstant.API_UPBIT_URL;

@Component
public class UpbitApiClient implements ApiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpbitApiClient.class);

    @Autowired
    private HttpUtils httpUtils;

    @Override
    public CoinPrice getCoinPrice(String key, double krwRate) {
        CoinPrice coinPrice = new CoinPrice(key, "업비트");

        double krw = getLastKrw(key);

        coinPrice.setKrw(String.valueOf(krw));

        return coinPrice;
    }

    private double getLastKrw(String key) {
        String code = "CRIX.UPBIT.KRW-" + key;
        String count = "1";
        double price = 0.0;

        try {
            URIBuilder uriInfo = new URIBuilder(API_UPBIT_URL);
            uriInfo.addParameter("code", code);
            uriInfo.addParameter("count", count);

            JSONObject jsonObject = httpUtils.getResponseByArray(uriInfo.toString());
            price = jsonObject.getDouble("tradePrice");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return price;
    }
}
