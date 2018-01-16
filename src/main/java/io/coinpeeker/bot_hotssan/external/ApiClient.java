package io.coinpeeker.bot_hotssan.external;

import java.net.URISyntaxException;

public interface ApiClient {
    String lastPrice(String key) throws URISyntaxException;
}
