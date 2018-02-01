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
    public static String convertKRW(String price) {
        Double tempPrice = Double.valueOf(price);
        DecimalFormat decimalFormat = new DecimalFormat("###,###,### 원");
        return decimalFormat.format(tempPrice);
    }

    public static String convertUSD(String price){
        Double tempPrice = Double.valueOf(price);
        DecimalFormat decimalFormat = new DecimalFormat("###,###,###.### 달러");
        return decimalFormat.format(tempPrice);
    }

    public static String convertSatoshi(String price){
        Double tempPrice = Double.valueOf(price);
        DecimalFormat decimalFormat = null;

        if(tempPrice >= 1.0){
             decimalFormat = new DecimalFormat("0.00000000 비트");
        } else {
            decimalFormat = new DecimalFormat("0.00000000 사토시");
        }

        return decimalFormat.format(tempPrice);
    }

    public static String convertPremium(double price){
        DecimalFormat decimalFormat = new DecimalFormat("###.##%");
        return decimalFormat.format(price);
    }

}
