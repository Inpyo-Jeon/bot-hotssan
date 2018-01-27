package io.coinpeeker.bot_hotssan.external;

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
public class UpbitApiClientImpl implements ApiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpbitApiClientImpl.class);

    @Autowired
    private HttpUtils httpUtils;

    @Override
    public CoinPrice getCoinPrice(String key, double krwRate) {
        CoinPrice coinPrice = new CoinPrice(key, "업비트");

        Double krw = getLastKrw(key);

        coinPrice.setKrw(String.valueOf(krw));

        return coinPrice;
    }

    public Double getLastKrw(String symbol) {
        String code = "CRIX.UPBIT.KRW-" + symbol;
        String count = "1";
        double price = 0;

        try {
            URIBuilder urlInfo = new URIBuilder(API_UPBIT_URL);
            urlInfo.addParameter("code", code);
            urlInfo.addParameter("count", count);

            JSONObject jsonObject = httpUtils.getResponseByArray(urlInfo.toString());
            price = jsonObject.getDouble("tradePrice");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return price;
    }
}
