package io.coinpeeker.bot_hotssan.utils;

import org.springframework.stereotype.Component;

import java.text.DecimalFormat;

@Component
public class CurrencyFormat {

    public String replaceKRWUnit(int price){
        DecimalFormat decimalFormat = new DecimalFormat("###,###,###,###.###");
        String result = decimalFormat.format(price) + "원";
        return result;
    }

    // TODO : Satoshi 계산 후 메서드 정의 필요
    public String replaceSatoshiUnit(double satoshi){
        DecimalFormat decimalFormat = new DecimalFormat(".########");
        String result = decimalFormat.format(satoshi) + "사토시";
        return result;
    }
}
