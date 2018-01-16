package io.coinpeeker.bot_hotssan.external;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

@Component
public class CoinContainer {

    @Qualifier("upbitApiClientImpl")
    @Autowired
    ApiClient upbitApiClient;

    public String execute(String symbol){
        Map<String, String> info = new HashMap<>();

        try {
            info.put("업비트", upbitApiClient.lastPrice(symbol).toString());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return info.toString();
    }

}
