package io.coinpeeker.bot_hotssan.trade;

import com.neovisionaries.ws.client.WebSocketException;
import com.nimbusds.jose.JOSEException;
import io.coinpeeker.bot_hotssan.common.CommonConstant;
import io.coinpeeker.bot_hotssan.scheduler.listed.OkexListedScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

@Component
public class TradeAgency {

    @Autowired
    BuyTrade buyTrade;

    private static final Logger LOGGER = LoggerFactory.getLogger(OkexListedScheduler.class);

    public void list(String exchangeName, String symbol, Map<String, Map<String, String>> market) throws NoSuchAlgorithmException, InvalidKeyException, IOException, ParseException, JOSEException, WebSocketException {

        if (CommonConstant.autoTrade) {
            if (market.containsKey(exchangeName)) {
                if (!exchangeName.equals("Upbit")) {
                    return;
                }
            }

            if (market.containsKey("Binance")) {
                LOGGER.info("-- Binance 자동 매수 시작 --");
                buyTrade.orderBinance("BTC", symbol);
                LOGGER.info("-- Binance 자동 매수 종료 --");
            }

            if (market.containsKey("Bittrex")) {
                LOGGER.info("-- Bittrex 자동 매수 시작 --");
                buyTrade.orderBittrex("BTC", symbol);
                LOGGER.info("-- Bittrex 자동 매수 종료 --");
            }

            if (market.containsKey("Upbit")) {
                if (market.get("Upbit").containsKey(symbol + "/KRW")) {
                    LOGGER.info("-- Upbit 자동 매수 시작 --");
                    buyTrade.orderUpbit("KRW", symbol);
                    LOGGER.info("-- Upbit 자동 매수 종료 --");
                }
            }

            if (market.containsKey("Kucoin")) {
                LOGGER.info("-- Kucoin 자동 매수 시작 --");
                buyTrade.orderKucoin("BTC", symbol);
                LOGGER.info("-- Kucoin 자동 매수 종료 --");
            }
        }
    }
}
