package io.coinpeeker.bot_hotssan.utils;

import io.coinpeeker.bot_hotssan.external.ApiClient;
import io.coinpeeker.bot_hotssan.external.UpbitApiClientImpl;
import io.coinpeeker.bot_hotssan.model.CoinPrice;
import io.coinpeeker.bot_hotssan.model.RelationCoinWithExchange;
import io.coinpeeker.bot_hotssan.model.constant.CoinType;
import io.coinpeeker.bot_hotssan.model.constant.ExchangeType;
import org.apache.commons.lang3.EnumUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Component
public class Commander {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Commander.class);

    @Qualifier("upbitApiClientImpl")
    @Autowired
    ApiClient upbitApiClient;

    @Qualifier("coinrailApiClientImpl")
    @Autowired
    ApiClient coinrailApiClient;

    @Qualifier("exchangeApiClientImpl")
    @Autowired
    ApiClient exchangeApiClient;

    @Autowired
    RelationCoinWithExchange exchange;



    public String execute(String instruction) {


        StringBuilder result = new StringBuilder();
        String coinSymbol = instruction.replace("/", "").toUpperCase();




        if(EnumUtils.isValidEnum(CoinType.class, coinSymbol)){
            LOGGER.info(exchange.getExchangeList(coinSymbol));
            CoinPrice coinPrice = coinrailApiClient.getCoinPrice(coinSymbol);
            result.append(coinPrice.toString());
        } else {
            result.append("등록 되어있지 않은 명령어 혹은 심볼입니다.");
        }
        
        return result.toString();
    }
}