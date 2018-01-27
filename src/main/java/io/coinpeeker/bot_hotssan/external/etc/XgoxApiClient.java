package io.coinpeeker.bot_hotssan.external.etc;

import io.coinpeeker.bot_hotssan.utils.HttpUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class XgoxApiClient {

    @Autowired
    private HttpUtils httpUtils;

    private static final String BASE_URL = "http://173.254.204.74:12313/ext/getaddress";
    private static final String XGOX_URL = BASE_URL + "/GfqKHoGr6khczC4MWjpN8xuxUUm15UnqtC";

    public Double getLastBalance() throws IOException {

        JSONObject response = httpUtils.getResponseByObject(XGOX_URL);

        String lastBalance = response.getString("balance");

        return Double.valueOf(lastBalance);
    }
}
