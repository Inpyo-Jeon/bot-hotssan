package io.coinpeeker.bot_hotssan.external;

import io.coinpeeker.bot_hotssan.utils.HttpUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;

import static io.coinpeeker.bot_hotssan.common.CommonConstant.API_UPBIT_URL;

public class UpbitApiClientImpl implements ApiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpbitApiClientImpl.class);

    @Override
    public String lastPrice(String symbol) throws URISyntaxException {
        LOGGER.info(symbol);
        String code = "CRIX.UPBIT.KRW-" + symbol;
        String count = "1";
        int price = 0;

        URIBuilder urlInfo = new URIBuilder(API_UPBIT_URL);
        urlInfo.addParameter("code", code);
        urlInfo.addParameter("count", count);

        HttpUtils httpUtils = new HttpUtils();
        try {
            HttpResponse result = httpUtils.get(urlInfo.toString());
            JSONArray jsonArray = new JSONArray(EntityUtils.toString(result.getEntity(), "UTF-8"));
            JSONObject jsonObject = new JSONObject(jsonArray.get(0).toString());
            price = jsonObject.getInt("tradePrice");

            LOGGER.info(String.valueOf(price));
            return String.valueOf(price);


        } catch (IOException e) {
            e.printStackTrace();
        }

    return "";
    }
}
