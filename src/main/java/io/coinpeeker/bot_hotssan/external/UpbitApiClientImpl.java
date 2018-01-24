package io.coinpeeker.bot_hotssan.external;

import io.coinpeeker.bot_hotssan.model.CoinPrice;
import io.coinpeeker.bot_hotssan.utils.CommonUtils;
import io.coinpeeker.bot_hotssan.utils.HttpUtils;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URISyntaxException;

import static io.coinpeeker.bot_hotssan.common.CommonConstant.API_UPBIT_URL;

@Component
public class UpbitApiClientImpl implements ApiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpbitApiClientImpl.class);

    @Autowired
    private HttpUtils httpUtils;

    public String lastPrice(String symbol) throws URISyntaxException {
        String code = "CRIX.UPBIT.KRW-" + symbol;
        String count = "1";
        int price = 0;

        URIBuilder urlInfo = new URIBuilder(API_UPBIT_URL);
        urlInfo.addParameter("code", code);
        urlInfo.addParameter("count", count);

        try {
            JSONObject jsonObject = httpUtils.getResponseByArray(urlInfo.toString());
            price = jsonObject.getInt("tradePrice");

            return CommonUtils.convertKRW(price);
        } catch (IOException e) {
            e.printStackTrace();
            return "-";
        }
    }

    @Override
    public CoinPrice getCoinPrice(String key) {
        CoinPrice coinPrice = new CoinPrice(key, "업비트");

        return coinPrice;
    }
}
