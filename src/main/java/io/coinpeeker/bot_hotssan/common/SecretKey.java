package io.coinpeeker.bot_hotssan.common;

public class SecretKey {

    private static final String SECRET_KEY_BINANCE = "7pKYaqrMkI2o0sJRatGjRuaFwolPw4gfxhhZprcu9dqECZYFE0dBSdo2LgQY2cGp";
    private static final String HEADER_KEY_BINANCE = "X-MBX-APIKEY";
    private static final String HEADER_VALUE_BINANCE = "jPgNEo8XGAyDPuqpJJV3gnzNROGbV3F2jzWhVP7lpYAOcuBTex0OBZlfRApoiY2D";

    private static final String URL_OKEX = "https://www.okex.com/api/v1/userinfo.do";
    private static final String API_KEY_OKEX = "4b47a99a-bc50-4bf2-9ae3-3bb53b681148";
    private static final String SIGN_OKEX = "E03C5D7899A25793A9E173EC80FC1B81";

    public static String getUrlOkex() {
        return URL_OKEX;
    }

    public static String getApiKeyOkex() {
        return API_KEY_OKEX;
    }

    public static String getSignOkex() {
        return SIGN_OKEX;
    }

    public static String getSecretKeyBinance() {
        return SECRET_KEY_BINANCE;
    }

    public static String getHeaderKeyBinance() {
        return HEADER_KEY_BINANCE;
    }

    public static String getHeaderValueBinance() {
        return HEADER_VALUE_BINANCE;
    }
}
