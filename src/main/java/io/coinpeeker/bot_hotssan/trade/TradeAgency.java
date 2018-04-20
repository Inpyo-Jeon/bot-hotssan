package io.coinpeeker.bot_hotssan.trade;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

@Component
public class TradeAgency {

    @Autowired
    BuyTrade buyTrade;

    public void list(String exchangeName, String symbol, Map<String, List<String>> market) throws NoSuchAlgorithmException, InvalidKeyException, IOException {

        if (market.containsKey(exchangeName)) {
            return;
        }

        if (market.containsKey("Binance")) {
            buyTrade.orderBinance("BTC", symbol);
        }
    }
}
