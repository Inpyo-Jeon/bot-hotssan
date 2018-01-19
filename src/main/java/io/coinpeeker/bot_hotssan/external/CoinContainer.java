package io.coinpeeker.bot_hotssan.external;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

@Component
public class CoinContainer {

    @Qualifier("upbitApiClientImpl")
    @Autowired
    ApiClient upbitApiClient;

    @Qualifier("coinrailApiClientImpl")
    @Autowired
    ApiClient coinrailApiClient;

    public String execute(String symbol){
        Map<String, String> info = new HashMap<>();

        try {
//            info.put("업비트", upbitApiClient.lastPrice(symbol).toString());
            info.put("코인레일", coinrailApiClient.lastPrice(symbol).toString());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return info.toString();
    }

}
