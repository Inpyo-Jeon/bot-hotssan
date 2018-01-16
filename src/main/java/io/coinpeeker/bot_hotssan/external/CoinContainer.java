package io.coinpeeker.bot_hotssan.external;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class CoinContainer {

    public String execute(String symbol){
        Map<String, String> info = new HashMap<>();


        ApiClient upbit = new UpbitApiClientImpl();
        try {
            info.put("업비트", upbit.lastPrice(symbol).toString());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }


        return info.toString();


    }

}
