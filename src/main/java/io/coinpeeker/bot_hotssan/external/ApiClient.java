package io.coinpeeker.bot_hotssan.external;

import io.coinpeeker.bot_hotssan.model.CoinPrice;

import java.io.IOException;
import java.net.URISyntaxException;

public interface ApiClient {
    CoinPrice getCoinPrice(String key);
}
