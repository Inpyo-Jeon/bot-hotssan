package io.coinpeeker.bot_hotssan.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.coinpeeker.bot_hotssan.external.ApiClient;
import io.coinpeeker.bot_hotssan.model.CoinPrice;
import io.coinpeeker.bot_hotssan.model.constant.CoinType;
import org.apache.commons.lang3.EnumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class Commander {

    private static final Logger LOGGER = LoggerFactory.getLogger(Commander.class);

    @Autowired
    @Qualifier("binanceApiClientImpl")
    ApiClient binanceApiClient;

    @Autowired
    @Qualifier("bithumbApiClientImpl")
    ApiClient bithumbApiClient;

    @Qualifier("bittrexApiClientImpl")
    @Autowired
    ApiClient bittrexApiClient;

    @Qualifier("coinnestApiClientImpl")
    @Autowired
    ApiClient coinnestApiClient;

    @Qualifier("coinoneApiClientImpl")
    @Autowired
    ApiClient coinoneApiClient;

    @Qualifier("coinrailApiClientImpl")
    @Autowired
    ApiClient coinrailApiClient;

    @Qualifier("cryptopiaApiClientImpl")
    @Autowired
    ApiClient crytopiaApiClient;

    @Qualifier("exxApiClientImpl")
    @Autowired
    ApiClient exxApiClient;

    @Qualifier("kucoinApiClientImpl")
    @Autowired
    ApiClient kucoinApiClient;

    @Qualifier("upbitApiClientImpl")
    @Autowired
    ApiClient upbitApiClient;

    @Qualifier("exchangeApiClientImpl")
    @Autowired
    ApiClient exchangeApiClient;


    private Map<CoinType, List<ApiClient>> tradeInfoMap = Maps.newHashMap();

    private void init2() {
        if (CollectionUtils.isEmpty(tradeInfoMap)) {
            tradeInfoMap.put(CoinType.valueOf("BTC"), Arrays.asList(binanceApiClient, bithumbApiClient, bittrexApiClient, coinnestApiClient, coinoneApiClient, coinrailApiClient, crytopiaApiClient, exxApiClient, kucoinApiClient, upbitApiClient));
            tradeInfoMap.put(CoinType.valueOf("ETH"), Arrays.asList(binanceApiClient, bithumbApiClient, bittrexApiClient, coinnestApiClient, coinoneApiClient, coinrailApiClient, crytopiaApiClient, exxApiClient, kucoinApiClient, upbitApiClient));
            tradeInfoMap.put(CoinType.valueOf("ETC"), Arrays.asList(binanceApiClient, bithumbApiClient, bittrexApiClient, coinnestApiClient, coinoneApiClient, crytopiaApiClient, exxApiClient, kucoinApiClient, upbitApiClient));
            tradeInfoMap.put(CoinType.valueOf("DENT"), Arrays.asList(coinrailApiClient, kucoinApiClient));
            tradeInfoMap.put(CoinType.valueOf("MED"), Arrays.asList(coinrailApiClient));
            tradeInfoMap.put(CoinType.valueOf("TRX"), Arrays.asList(binanceApiClient, coinnestApiClient, coinrailApiClient));
            tradeInfoMap.put(CoinType.valueOf("QTUM"), Arrays.asList(binanceApiClient, bithumbApiClient, bittrexApiClient, coinnestApiClient, coinoneApiClient, coinrailApiClient, crytopiaApiClient, exxApiClient, kucoinApiClient, upbitApiClient));
            tradeInfoMap.put(CoinType.valueOf("NEO"), Arrays.asList(binanceApiClient, bittrexApiClient, coinnestApiClient, coinrailApiClient, crytopiaApiClient, kucoinApiClient, upbitApiClient));
            tradeInfoMap.put(CoinType.valueOf("XGOX"), Arrays.asList(crytopiaApiClient));
            tradeInfoMap.put(CoinType.valueOf("BCH"), Arrays.asList(binanceApiClient, bithumbApiClient, bittrexApiClient, coinnestApiClient, coinoneApiClient, coinrailApiClient, crytopiaApiClient, exxApiClient, kucoinApiClient, upbitApiClient));
            tradeInfoMap.put(CoinType.valueOf("XRP"), Arrays.asList(binanceApiClient, bithumbApiClient, bittrexApiClient, coinoneApiClient, coinrailApiClient, upbitApiClient));
        }
    }


    public String execute(String instruction) {
        init2();

        StringBuilder result = new StringBuilder();
        String coinSymbol = instruction.replace("/", "").toUpperCase();

        if (EnumUtils.isValidEnum(CoinType.class, coinSymbol)) {
            List<CoinPrice> responseList = getCoinPriceList(coinSymbol, tradeInfoMap.get(CoinType.valueOf(coinSymbol)));

            for (CoinPrice coinPrice : responseList) {
                result.append(coinPrice.toString());
                result.append("\n\n");
            }

        } else {
            result.append("등록 되어있지 않은 명령어 혹은 심볼입니다.");
        }

        return result.toString();
    }

    private List<CoinPrice> getCoinPriceList(String symbol, List<ApiClient> apiClientList) {
        List<CoinPrice> resultList = Lists.newArrayList();


        for (ApiClient apiClient : apiClientList) {
            resultList.add(apiClient.getCoinPrice(symbol));
        }

        return resultList;
    }
}