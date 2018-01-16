package io.coinpeeker.bot_hotssan.external;

import org.springframework.stereotype.Component;

@Component
public class CoinrailApiClientImpl implements ApiClient {
    @Override
    public String lastPrice(String key) {
        return "코레테스트";
    }
}
