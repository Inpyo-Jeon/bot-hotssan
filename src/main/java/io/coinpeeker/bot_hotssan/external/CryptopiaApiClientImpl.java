package io.coinpeeker.bot_hotssan.external;

import io.coinpeeker.bot_hotssan.model.CoinPrice;
import org.springframework.stereotype.Component;

@Component
public class CryptopiaApiClientImpl implements ApiClient {

    @Override
    public CoinPrice getCoinPrice(String key) {
        CoinPrice coinPrice = new CoinPrice(key, "크립토피아");

        return coinPrice;
    }
}
