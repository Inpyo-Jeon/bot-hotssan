package io.coinpeeker.bot_hotssan.utils;

import java.text.DecimalFormat;

public class CommonUtils {

    /**
     * 원화표기 메소드
     *
     * @param price
     * @return
     */
    public static String convertKRW(int price) {
        DecimalFormat decimalFormat = new DecimalFormat("###,###,###원");
        return decimalFormat.format(price);
    }
}
