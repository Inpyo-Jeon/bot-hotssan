package io.coinpeeker.bot_hotssan.utils;

import org.springframework.stereotype.Component;

import java.text.DecimalFormat;

@Component
public class CommonUtils {

    /**
     * 원화표기 메소드
     *
     * @param price
     * @return
     */
    public static String convertKRW(double price) {
        DecimalFormat decimalFormat = new DecimalFormat("###,###,###.### 원");
        return decimalFormat.format(price);
    }

    public static String convertUSD(double price){
        DecimalFormat decimalFormat = new DecimalFormat("###,###,###.### 달러");
        return decimalFormat.format(price);
    }
}
