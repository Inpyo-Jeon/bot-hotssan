package io.coinpeeker.bot_hotssan.model;

import io.coinpeeker.bot_hotssan.model.constant.CoinType;
import io.coinpeeker.bot_hotssan.model.constant.ExchangeType;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Component
public class RelationCoinWithExchange {

    private static final HashMap<CoinType, List<ExchangeType>> supportExchange = new HashMap<>();

    private RelationCoinWithExchange(){
        supportExchange.put(CoinType.valueOf("BTC"), Arrays.asList(ExchangeType.BINANCE, ExchangeType.BITHUMB, ExchangeType.BITTREX, ExchangeType.COINNEST, ExchangeType.COINONE, ExchangeType.COINRAIL, ExchangeType.CRYPTOPIA, ExchangeType.EXX, ExchangeType.KUCOIN, ExchangeType.UPBIT));
        supportExchange.put(CoinType.valueOf("ETH"), Arrays.asList(ExchangeType.BINANCE, ExchangeType.BITHUMB, ExchangeType.BITTREX, ExchangeType.COINNEST, ExchangeType.COINONE, ExchangeType.COINRAIL, ExchangeType.CRYPTOPIA, ExchangeType.EXX, ExchangeType.KUCOIN, ExchangeType.UPBIT));
        supportExchange.put(CoinType.valueOf("ETC"), Arrays.asList(ExchangeType.BINANCE, ExchangeType.BITHUMB, ExchangeType.BITTREX, ExchangeType.COINNEST, ExchangeType.COINONE, ExchangeType.CRYPTOPIA, ExchangeType.EXX, ExchangeType.KUCOIN, ExchangeType.UPBIT));
        supportExchange.put(CoinType.valueOf("DENT"), Arrays.asList(ExchangeType.COINRAIL, ExchangeType.KUCOIN));
        supportExchange.put(CoinType.valueOf("MED"), Arrays.asList(ExchangeType.COINRAIL));
        supportExchange.put(CoinType.valueOf("TRX"), Arrays.asList(ExchangeType.BINANCE, ExchangeType.COINNEST, ExchangeType.COINRAIL));
        supportExchange.put(CoinType.valueOf("QTUM"), Arrays.asList(ExchangeType.BINANCE, ExchangeType.BITHUMB, ExchangeType.BITTREX, ExchangeType.COINNEST, ExchangeType.COINONE, ExchangeType.COINRAIL, ExchangeType.CRYPTOPIA, ExchangeType.EXX, ExchangeType.KUCOIN, ExchangeType.UPBIT));
        supportExchange.put(CoinType.valueOf("NEO"), Arrays.asList(ExchangeType.BINANCE, ExchangeType.BITTREX, ExchangeType.COINNEST, ExchangeType.COINRAIL, ExchangeType.CRYPTOPIA, ExchangeType.KUCOIN, ExchangeType.UPBIT));
        supportExchange.put(CoinType.valueOf("XGOX"), Arrays.asList(ExchangeType.CRYPTOPIA));
        supportExchange.put(CoinType.valueOf("BCH"), Arrays.asList(ExchangeType.BINANCE, ExchangeType.BITHUMB, ExchangeType.BITTREX, ExchangeType.COINNEST, ExchangeType.COINONE, ExchangeType.COINRAIL, ExchangeType.CRYPTOPIA, ExchangeType.EXX, ExchangeType.KUCOIN, ExchangeType.UPBIT));
        supportExchange.put(CoinType.valueOf("XRP"), Arrays.asList(ExchangeType.BINANCE, ExchangeType.BITHUMB, ExchangeType.BITTREX, ExchangeType.COINONE, ExchangeType.COINRAIL, ExchangeType.UPBIT));
    }

    public String getExchangeList(String symbol){
        return supportExchange.get(CoinType.valueOf(symbol)).toString();
    }
}
