package io.coinpeeker.bot_hotssan.trade;

import io.coinpeeker.bot_hotssan.trade.API.Binance;
import io.coinpeeker.bot_hotssan.utils.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Component
public class BuyTrade implements AutoTrade {

    @Autowired
    HttpUtils httpUtils;

    private static final Logger LOGGER = LoggerFactory.getLogger(BuyTrade.class);

    @Override
    public void orderBinance(String axisCoin, String buyCoin) throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        Binance binance = new Binance("m6yQuY6E1BscKqlxIHMhqzkSEa7l9vMKWEQTusyzN9Ozslq3k023x3ou6wxWlJGk", "ZR80HbvYPlckwsbEiyMHNT6nu5SHmLZU3TF95n2uqxloLUmSAz4Rd1yEIooPIbXF", httpUtils);
        String buyCoinSymbol = buyCoin + axisCoin;
//        int loopCount = 1;
//        Boolean check = true;

        BigDecimal myAxisCoinAmount = new BigDecimal(Double.valueOf(binance.getHaveCoinAmount(axisCoin))).setScale(8, BigDecimal.ROUND_DOWN);
        BigDecimal buyCoinMarketPrice = new BigDecimal(Double.valueOf(binance.getCurrentCoinMarketPrice(buyCoinSymbol))).setScale(8, BigDecimal.ROUND_DOWN);
        BigDecimal buyAmount = new BigDecimal((myAxisCoinAmount.doubleValue() / buyCoinMarketPrice.doubleValue()) * 0.9).setScale(2, BigDecimal.ROUND_DOWN);

        binance.sendOrder(buyCoinSymbol, "BUY", "MARKET", String.valueOf(buyAmount));


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
}
