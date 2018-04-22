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
        int loopCount = 1;
        Boolean check = true;
        String buyCoinSymbol = buyCoin + axisCoin;


        while (check) {
            if (loopCount < 10) {
                ++loopCount;
                BigDecimal myAxisCoinAmount = new BigDecimal(Double.valueOf(binance.getHaveCoinAmount(axisCoin)));
                BigDecimal buyCoinMarketPrice = new BigDecimal(Double.valueOf(binance.getCurrentCoinMarketPrice(buyCoinSymbol)));
                int buyAmount = (int) (myAxisCoinAmount.doubleValue() / buyCoinMarketPrice.doubleValue());
                Double minQuantity = Double.valueOf(binance.getQuantityMinOrder(buyCoinSymbol));

                if (buyAmount > minQuantity) {
                    binance.sendOrder(buyCoinSymbol, "BUY", "MARKET", String.valueOf(buyAmount));
                } else {
                    check = false;
                }
            } else {
                check = false;
            }
        }

    }
}
