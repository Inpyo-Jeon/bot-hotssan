package io.coinpeeker.bot_hotssan.utils;

import io.coinpeeker.bot_hotssan.external.ApiClient;
import io.coinpeeker.bot_hotssan.model.CoinPrice;
import io.coinpeeker.bot_hotssan.model.constant.CoinType;
import org.apache.commons.lang3.EnumUtils;
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
        String coinSymbol = instruction.replace("/", "").toUpperCase();


        if(EnumUtils.isValidEnum(CoinType.class, coinSymbol)){
            CoinPrice coinPrice = coinrailApiClient.getCoinPrice(coinSymbol);
            result.append("### ");
            result.append(coinPrice.getKey());
            result.append(" ###");
            result.append("\n거래소명 : ");
            result.append(coinPrice.getExchangeName());
            result.append("\nkrw : ");
            result.append(coinPrice.getKrw());
            result.append("\nusd : ");
            result.append(coinPrice.getUsd());
            result.append("\n사토시 : ");
            result.append(coinPrice.getSatoshi());
        } else {
            result.append("등록 되어있지 않은 명령어 혹은 심볼입니다.");
        }



        return result.toString();
    }
}