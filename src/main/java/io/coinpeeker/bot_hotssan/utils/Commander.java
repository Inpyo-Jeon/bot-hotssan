package io.coinpeeker.bot_hotssan.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.coinpeeker.bot_hotssan.external.ApiClient;
import io.coinpeeker.bot_hotssan.external.bank.HanaBankApiClient;
import io.coinpeeker.bot_hotssan.external.etc.XgoxApiClient;
import io.coinpeeker.bot_hotssan.model.CoinPrice;
import io.coinpeeker.bot_hotssan.model.constant.CoinType;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class Commander {

    private static final Logger LOGGER = LoggerFactory.getLogger(Commander.class);

    @Autowired
    @Qualifier("binanceApiClient")
    ApiClient binanceApiClient;

    @Autowired
    @Qualifier("bithumbApiClient")
    ApiClient bithumbApiClient;

    @Autowired
    @Qualifier("bittrexApiClient")
    ApiClient bittrexApiClient;

    @Autowired
    @Qualifier("coinnestApiClient")
    ApiClient coinnestApiClient;

    @Autowired
    @Qualifier("coinoneApiClient")
    ApiClient coinoneApiClient;

    @Autowired
    @Qualifier("coinrailApiClient")
    ApiClient coinrailApiClient;

    @Autowired
    @Qualifier("cryptopiaApiClient")
    ApiClient crytopiaApiClient;

    @Autowired
    @Qualifier("exxApiClient")
    ApiClient exxApiClient;

    @Autowired
    @Qualifier("kucoinApiClient")
    ApiClient kucoinApiClient;

    @Autowired
    @Qualifier("upbitApiClient")
    ApiClient upbitApiClient;

    @Autowired
    HanaBankApiClient exchangeApi;

    @Autowired
    XgoxApiClient xgoxApiClient;


    private Map<CoinType, List<ApiClient>> tradeInfoMap = Maps.newHashMap();

    private void init() {
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
            tradeInfoMap.put(CoinType.valueOf("XRP"), Arrays.asList(binanceApiClient, bithumbApiClient, bittrexApiClient, coinoneApiClient, coinrailApiClient, upbitApiClient));
            tradeInfoMap.put(CoinType.valueOf("SPC"), Arrays.asList(exxApiClient));
        }
    }


    public String execute(String instruction) {
        init();

        StringBuilder result = new StringBuilder();
        String coinSymbol = instruction.replace("/", "").toUpperCase();

        if (StringUtils.equals("test", instruction)) {
            try {
                Double testResult = xgoxApiClient.getLastBalance();
                LOGGER.info("#$@#$@#$@#$ testResult = {}", testResult);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

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
        double krwRate = 0.0;
        try {
            krwRate = exchangeApi.lastPrice();
        } catch (IOException e) {
            e.printStackTrace();
        }


        for (ApiClient apiClient : apiClientList) {
            resultList.add(apiClient.getCoinPrice(symbol, krwRate));
        }

        return resultList;
    }
}