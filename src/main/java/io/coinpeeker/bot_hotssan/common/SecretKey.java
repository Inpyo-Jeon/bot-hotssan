package io.coinpeeker.bot_hotssan.common;

public class SecretKey {

    private static final String SECRET_KEY_BINANCE = "PGBDTxySfFP3xA3ajxq7FvztRJOt9R0DNsQHcPpDw45acmP10KXD3hSjD5leQ7uV";
    private static final String HEADER_KEY_BINANCE = "X-MBX-APIKEY";
    private static final String HEADER_VALUE_BINANCE = "n1im5sz8hhEx5JfOILACxdB6SF6EoP5Rpz6JnGulhayiVX2So8f06ArmYsmjv81k";

    private static final String URL_OKEX = "https://www.okex.com/api/v1/userinfo.do";
    private static final String API_KEY_OKEX = "4b47a99a-bc50-4bf2-9ae3-3bb53b681148";
    private static final String SIGN_OKEX = "E03C5D7899A25793A9E173EC80FC1B81";

    private static final String SECRET_KEY_KUCOIN = "fba73622-f0b1-45fc-a42d-c6c5f8860639";
    private static final String API_KEY_KUCOIN = "5a952d64df542b46a01e55c0";

    public static String getApiKeyKucoin() {
        return API_KEY_KUCOIN;
    }

    public static String getSecretKeyKucoin() {
        return SECRET_KEY_KUCOIN;
    }

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
