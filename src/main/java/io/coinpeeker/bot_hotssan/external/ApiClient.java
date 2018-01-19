package io.coinpeeker.bot_hotssan.external;

import java.io.IOException;
import java.net.URISyntaxException;

public interface ApiClient {
    String lastPrice(String key) throws IOException, URISyntaxException;
}
