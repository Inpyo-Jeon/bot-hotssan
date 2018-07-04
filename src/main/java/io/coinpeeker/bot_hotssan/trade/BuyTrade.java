package io.coinpeeker.bot_hotssan.trade;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketException;
import com.nimbusds.jose.JOSEException;
import io.coinpeeker.bot_hotssan.trade.api.Binance;
import io.coinpeeker.bot_hotssan.trade.api.Bittrex;
import io.coinpeeker.bot_hotssan.trade.api.Kucoin;
import io.coinpeeker.bot_hotssan.trade.api.Upbit;
import io.coinpeeker.bot_hotssan.utils.HttpUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;

@Component
public class BuyTrade implements AutoTrade {

    @Autowired
    HttpUtils httpUtils;

    private static final Logger LOGGER = LoggerFactory.getLogger(BuyTrade.class);

    @Override
    public String orderBinance(String axisCoin, String buyCoin) throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        Binance binance = new Binance("YRDfvImkwVuyNN5eJQQLgkQTC2vBGPZwL40qt7XKiC2YCd02ShxxK3VXWRr0q5Br", "1MA1zA2FtJ0BisG6omL9uzVacJoklE46skCypkfNWRAMGBlqjnV8xuykRs3jKggo", httpUtils);
        String buyCoinSymbol = buyCoin + axisCoin;
//        int loopCount = 1;
//        Boolean check = true;

        BigDecimal myAxisCoinAmount = new BigDecimal(Double.valueOf(binance.getHaveCoinAmount(axisCoin))).setScale(8, BigDecimal.ROUND_DOWN);
        BigDecimal buyCoinMarketPrice = new BigDecimal(Double.valueOf(binance.getCurrentCoinMarketPrice(buyCoinSymbol))).setScale(8, BigDecimal.ROUND_DOWN);
        BigDecimal buyAmount = new BigDecimal((myAxisCoinAmount.doubleValue() / buyCoinMarketPrice.doubleValue()) * 0.9).setScale(2, BigDecimal.ROUND_DOWN);

        String orderResult = binance.sendOrder(buyCoinSymbol, "BUY", "MARKET", String.valueOf(buyAmount.intValue()));
        return orderResult;


//        //이전 로직, 혹시 몰라 남겨둠 (갯수를 계속 체크하는 방식이나, 시장가로 주문하면 한 번만 주문하면 되기에 주석처리)
//        while (check) {
//            if (loopCount < 10) {
//                ++loopCount;
//                BigDecimal myAxisCoinAmount = new BigDecimal(Double.valueOf(binance.getHaveCoinAmount(axisCoin)));
//                BigDecimal buyCoinMarketPrice = new BigDecimal(Double.valueOf(binance.getCurrentCoinMarketPrice(buyCoinSymbol)));
//                int buyAmount = (int) (myAxisCoinAmount.doubleValue() / buyCoinMarketPrice.doubleValue());
//                Double minQuantity = Double.valueOf(binance.getQuantityMinOrder(buyCoinSymbol));
//
//                if (buyAmount > minQuantity) {
//                    binance.sendOrder(buyCoinSymbol, "BUY", "MARKET", String.valueOf(buyAmount));
//                } else {
//                    check = false;
//                }
//            } else {
//                check = false;
//            }
//        }

    }

    @Override
    public void orderKucoin(String axisCoin, String buyCoin) throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        Kucoin kucoin = new Kucoin("5ae43b72a57c577d638b807a", "5416d149-136c-447e-9fda-e0a0b1946ad7", httpUtils);
        String buyCoinSymbol = buyCoin + "-" + axisCoin;

        BigDecimal myAxisCoinAmount = new BigDecimal(Double.valueOf(kucoin.getBalanceOfCoin(axisCoin))).setScale(8, BigDecimal.ROUND_DOWN);
        BigDecimal selectSatoshi = kucoin.calcBestSellOrderBook(3, kucoin.getSellOrderBooks(buyCoinSymbol, "30"), myAxisCoinAmount.doubleValue());
        BigDecimal buyAmount = new BigDecimal((myAxisCoinAmount.doubleValue() / selectSatoshi.doubleValue()) * 0.9).setScale(2, BigDecimal.ROUND_DOWN);

        kucoin.requestOrder(buyCoinSymbol, "BUY", selectSatoshi.toString(), String.valueOf(buyAmount.intValue()));

        LOGGER.info("Total BTC Amount : " + myAxisCoinAmount.toString());
        LOGGER.info("Select Satoshi : " + selectSatoshi.toString());
        LOGGER.info("Buy Amount : " + buyAmount.toString());

    }

    @Override
    public String orderBittrex(String axisCoin, String buyCoin) throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        Bittrex bittrex = new Bittrex("d88cf2bb52c842c9962b6c00ee425fed", "c0f422f5587d48f39204bb3f4af2612e", httpUtils);
        String buyCoinSymbol = axisCoin + "-" + buyCoin;

        BigDecimal myAxisCoinAmount = new BigDecimal(Double.valueOf(bittrex.getBalanceOfCoin(axisCoin))).setScale(8, BigDecimal.ROUND_DOWN);
        BigDecimal selectSatoshi = bittrex.calcBestSellOrderBook(5, bittrex.getOrderBook(buyCoinSymbol, "sell"), myAxisCoinAmount.doubleValue());
        BigDecimal buyAmount = new BigDecimal((myAxisCoinAmount.doubleValue() / selectSatoshi.doubleValue()) * 0.9).setScale(2, BigDecimal.ROUND_DOWN);

        String orderResult = bittrex.sendOrder(buyCoinSymbol, String.valueOf(buyAmount.intValue()), selectSatoshi.toString());
        return orderResult;
    }

    @Override
    public String orderUpbit(String axisCoin, String buyCoin) throws IOException, ParseException, JOSEException, WebSocketException {
        Upbit upbit = new Upbit("NzH0lJvdHynCsH61TKf6bSNMdCjF6aKJTgWNcmyP", "gL9xrMTAnj9sQrDF9JU2Yv9NpYzibJMlq2YGXT0q", httpUtils);
        String streamData = "";
        Double myAxisAmount = upbit.getAsset(axisCoin);

        WebSocket upbitWebSocket = upbit.connect(axisCoin, buyCoin);
        while (upbit.streamData == "") {
        }
        streamData = upbit.streamData;

        Double selectAskPrice = upbit.calcBestSellOrderBook(5, new JSONObject(streamData), myAxisAmount);
        Double buyAmount = (myAxisAmount / selectAskPrice) * (0.9005);

        String orderResult = upbit.excuteOrder(axisCoin, buyCoin, "bid", buyAmount, selectAskPrice, "limit");
        return orderResult;
    }
}
