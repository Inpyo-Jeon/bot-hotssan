package io.coinpeeker.bot_hotssan.trade;

import io.coinpeeker.bot_hotssan.scheduler.listed.OkexListedScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(OkexListedScheduler.class);

    public void list(String exchangeName, String symbol, Map<String, List<String>> market) throws NoSuchAlgorithmException, InvalidKeyException, IOException {

        if (market.containsKey(exchangeName)) {
            return;
        }

        if (market.containsKey("Binance")) {
            LOGGER.info("-- Binance 자동 매수 시작 --");
            buyTrade.orderBinance("BTC", symbol);
            LOGGER.info("-- Binance 자동 매수 종료 --");
        }

        if (market.containsKey("Kucoin")) {
            LOGGER.info("-- Kucoin 자동 매수 시작 --");
            buyTrade.orderKucoin("BTC", symbol);
            LOGGER.info("-- Kucoin 자동 매수 종료 --");
        }

        if (market.containsKey("Bittrex")){
            LOGGER.info("-- Kucoin 자동 매수 시작 --");
            buyTrade.orderBittrex("BTC", symbol);
            LOGGER.info("-- Kucoin 자동 매수 종료 --");
        }
    }
}
