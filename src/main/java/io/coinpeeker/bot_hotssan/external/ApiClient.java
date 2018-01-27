package io.coinpeeker.bot_hotssan.external;

import io.coinpeeker.bot_hotssan.model.CoinPrice;

public interface ApiClient {
    CoinPrice getCoinPrice(String key, double krwRate);
}
