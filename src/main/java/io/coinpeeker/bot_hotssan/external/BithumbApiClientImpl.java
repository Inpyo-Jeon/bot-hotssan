package io.coinpeeker.bot_hotssan.external;

import io.coinpeeker.bot_hotssan.model.CoinPrice;
import org.springframework.stereotype.Component;

@Component
public class BithumbApiClientImpl implements ApiClient {

    @Override
    public CoinPrice getCoinPrice(String key) {
        CoinPrice coinPrice = new CoinPrice(key, "빗썸");

        return coinPrice;
    }
}
