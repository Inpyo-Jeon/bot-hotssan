package io.coinpeeker.bot_hotssan.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.coinpeeker.bot_hotssan.external.ApiClient;
import io.coinpeeker.bot_hotssan.external.bank.HanaBankApiClient;
import io.coinpeeker.bot_hotssan.external.etc.XgoxApiClient;
import io.coinpeeker.bot_hotssan.feature.KoreaPremium;
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
    @Qualifier("bitfinexApiClient")
    ApiClient bitfinexApiClient;

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
    @Qualifier("okexApiClient")
    ApiClient okexApiClient;

    @Autowired
    HanaBankApiClient exchangeApi;

    @Autowired
    XgoxApiClient xgoxApiClient;

    @Autowired
    KoreaPremium koreaPremium;


    private Map<CoinType, List<ApiClient>> tradeInfoMap = Maps.newHashMap();
    private Map<CoinType, List<ApiClient>> premiumExchangeMap = Maps.newHashMap();

    private void init() {
        if (CollectionUtils.isEmpty(tradeInfoMap)) {
            // BTC - 비트파이넥스, 빗썸, 비트렉스, 업비트
            tradeInfoMap.put(CoinType.valueOf("BTC"), Arrays.asList(bitfinexApiClient, bithumbApiClient, bittrexApiClient, upbitApiClient));

            // ETH - 빗썸, 비트렉스, 코인원, 업비트
            tradeInfoMap.put(CoinType.valueOf("ETH"), Arrays.asList(bithumbApiClient, bittrexApiClient, coinoneApiClient, upbitApiClient));

            // ETC - 빗썸, 비트렉스, 코인원, 업비트
            tradeInfoMap.put(CoinType.valueOf("ETC"), Arrays.asList(bithumbApiClient, bittrexApiClient, coinoneApiClient, upbitApiClient));

            // QTUM - 빗썸, 비트렉스, 코인원, 코인네스트, 업비트
            tradeInfoMap.put(CoinType.valueOf("QTUM"), Arrays.asList(bithumbApiClient, bittrexApiClient, coinoneApiClient, coinnestApiClient, upbitApiClient));

            // NEO - 비트렉스, 업비트
            tradeInfoMap.put(CoinType.valueOf("NEO"), Arrays.asList(bittrexApiClient, upbitApiClient));

            // DENT - 코인레일, 쿠코인, OKEx
            tradeInfoMap.put(CoinType.valueOf("DENT"), Arrays.asList(coinrailApiClient, kucoinApiClient, okexApiClient));

            // MED - 코인레일
            tradeInfoMap.put(CoinType.valueOf("MED"), Arrays.asList(coinrailApiClient));

            // TRX - 바이낸스, 코인네스트, 코인레일
            tradeInfoMap.put(CoinType.valueOf("TRX"), Arrays.asList(binanceApiClient, coinnestApiClient, coinrailApiClient));

            // XGOX - 크립토피아
            tradeInfoMap.put(CoinType.valueOf("XGOX"), Arrays.asList(crytopiaApiClient));

            // XRP - 빗썸, 비트렉스, 코인원, 업비트
            tradeInfoMap.put(CoinType.valueOf("XRP"), Arrays.asList(bithumbApiClient, bittrexApiClient, coinoneApiClient, upbitApiClient));

            // SPC - EXX
            tradeInfoMap.put(CoinType.valueOf("SPC"), Arrays.asList(exxApiClient, coinnestApiClient));

            // ADA - 업비트, 코인네스트, 비트렉스
            tradeInfoMap.put(CoinType.valueOf("ADA"), Arrays.asList(upbitApiClient, coinnestApiClient));

            // TSL - 코인네스트, 코인레일
            tradeInfoMap.put(CoinType.valueOf("TSL"), Arrays.asList(coinnestApiClient, coinrailApiClient));

            // EOS - 빗썸, 비트파이넥스, OKEx
            tradeInfoMap.put(CoinType.valueOf("EOS"), Arrays.asList(bithumbApiClient, bitfinexApiClient, okexApiClient));

            // ADX - 바이낸스
            tradeInfoMap.put(CoinType.valueOf("ADX"), Arrays.asList(binanceApiClient));
        }
    }

    private void premiumInit() {
        premiumExchangeMap.put(CoinType.valueOf("BTC"), Arrays.asList(bithumbApiClient, upbitApiClient, coinoneApiClient, bitfinexApiClient, bittrexApiClient));
        premiumExchangeMap.put(CoinType.valueOf("ETH"), Arrays.asList(bithumbApiClient, upbitApiClient, coinoneApiClient, bitfinexApiClient, bittrexApiClient));
        premiumExchangeMap.put(CoinType.valueOf("ETC"), Arrays.asList(bithumbApiClient, upbitApiClient, coinoneApiClient, bitfinexApiClient, bittrexApiClient));
        premiumExchangeMap.put(CoinType.valueOf("XRP"), Arrays.asList(bithumbApiClient, upbitApiClient, coinoneApiClient, bitfinexApiClient, bittrexApiClient));
        premiumExchangeMap.put(CoinType.valueOf("QTUM"), Arrays.asList(bithumbApiClient, upbitApiClient, coinoneApiClient, bitfinexApiClient, bittrexApiClient));
    }


    public String execute(String instruction) {
        init();
        premiumInit();

        StringBuilder result = new StringBuilder();
        System.out.println("---------");
        System.out.println(instruction);
        String coinSymbol = "";

        if(instruction.contains("@")){
            int atIndex = instruction.indexOf("@");
            for(int idx = 0; idx > atIndex - 1; idx++){
                coinSymbol += instruction.charAt(idx);
            }
        } else {
            coinSymbol = instruction.replace("/", "").toUpperCase();
        }

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

            result.append("-- ").append(coinSymbol).append(" --\n");
            for (CoinPrice coinPrice : responseList) {
                result.append(coinPrice.toString());
                result.append("\n\n");
            }

        } else if ("LIST".equals(coinSymbol)) {
            result.append(getCoinList());
        } else if ("PP".equals(coinSymbol)) {
            try {
                double krwRate = 0.0;
//                try {
//                    krwRate = exchangeApi.lastPrice();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                result.append("----- 한국 프리미엄 계산 -----");
                result.append("\n기준환율 : ");
                result.append(krwRate);
                result.append(" 원\n\n");
                for (CoinType coin : premiumExchangeMap.keySet()) {
                    String symbol = coin.getSymbol().toUpperCase();
                    result.append(koreaPremium.calculate(symbol, premiumExchangeMap, krwRate));
                }
            } catch (Exception e) {
                e.printStackTrace();
                result.append("오류");
            }

        } else {
            result.append("등록 되어있지 않은 명령어 혹은 심볼입니다.");
        }

        return result.toString();
    }

    private List<CoinPrice> getCoinPriceList(String symbol, List<ApiClient> apiClientList) {
        List<CoinPrice> resultList = Lists.newArrayList();
        double krwRate = 0.0;
//        try {
//            krwRate = exchangeApi.lastPrice();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


        for (ApiClient apiClient : apiClientList) {
            resultList.add(apiClient.getCoinPrice(symbol, krwRate));
        }

        return resultList;
    }

    private String getCoinList() {
        StringBuilder sb = new StringBuilder();

        for (CoinType item : CoinType.values()) {
            sb.append(item.getSymbol());
            sb.append(" : ");
            sb.append(item.getDesc());
            sb.append("\n");
        }

        return sb.toString();
    }


}