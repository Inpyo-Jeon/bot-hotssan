package io.coinpeeker.bot_hotssan.utils;

import io.coinpeeker.bot_hotssan.external.ApiClient;
import io.coinpeeker.bot_hotssan.model.CoinPrice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class Commander {

    @Qualifier("upbitApiClientImpl")
    @Autowired
    ApiClient upbitApiClient;

    @Qualifier("coinrailApiClientImpl")
    @Autowired
    ApiClient coinrailApiClient;

    @Qualifier("exchangeApiClientImpl")
    @Autowired
    ApiClient exchangeApiClient;


    public String execute(String instruction) {
        StringBuilder result = new StringBuilder();

        CoinPrice coinPrice = coinrailApiClient.getCoinPrice(instruction);

        result.append(coinPrice.toString());

        return result.toString();
    }
}