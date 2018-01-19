package io.coinpeeker.bot_hotssan.external;

import io.coinpeeker.bot_hotssan.utils.CurrencyFormat;
import io.coinpeeker.bot_hotssan.utils.HttpUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
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

    @Autowired
    private CurrencyFormat currencyFormat;

    @Override
    public String lastPrice(String symbol) throws URISyntaxException {
        String  code        = "CRIX.UPBIT.KRW-" + symbol;
        String  count       = "1";
        int     price       = 0;

        URIBuilder urlInfo = new URIBuilder(API_UPBIT_URL);
        urlInfo.addParameter("code", code);
        urlInfo.addParameter("count", count);

        try {
            HttpResponse result = httpUtils.get(urlInfo.toString());
            JSONArray jsonArray = new JSONArray(EntityUtils.toString(result.getEntity(), "UTF-8"));
            JSONObject jsonObject = new JSONObject(jsonArray.get(0).toString());
            price = jsonObject.getInt("tradePrice");

            LOGGER.info(symbol + " : " + String.valueOf(price));

            return currencyFormat.replaceKRWUnit(price);

        } catch (Exception e) {
            e.printStackTrace();
            return "-";
        }
    }
}
