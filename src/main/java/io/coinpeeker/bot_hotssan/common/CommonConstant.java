package io.coinpeeker.bot_hotssan.common;

import java.util.ArrayList;
import java.util.List;

public class CommonConstant {

    public static final String URL_TELEGRAM_BASE = "https://api.telegram.org/bot";

    public static final String METHOD_TELEGRAM_SET_WEBHOOK = "/setWebhook";
    public static final String METHOD_TELEGRAM_DELETE_WEBHOOK = "/deleteWebhook";
    public static final String METHOD_TELEGRAM_SENDMESSAGE = "/sendmessage";

    public static final String HANA_BANK_URL = "http://fx.kebhana.com/fxportal/jsp/RS/DEPLOY_EXRATE/fxrate_B_v2.html";

    public static final String API_UPBIT_URL = "https://crix-api-endpoint.upbit.com/v1/crix/candles/minutes/1";
    public static final String API_BINANCE_URL = "https://api.binance.com/api/v3/ticker/price";
    public static final String API_BITTREX_URL = "https://bittrex.com/api/v1.1/public/getmarketsummary";
    public static final String API_EXX_URL = "https://api.exx.com/data/v1/ticker";
    public static final String API_COINRAIL_URL = "https://api.coinrail.co.kr/public/last/order";
    public static final String API_KUCOIN_URL = "https://api.kucoin.com/v1/open/tick";
    public static final String API_CRYPTOPIA_URL = "https://www.cryptopia.co.nz";
    public static final String API_COINNEST_URL = "https://api.coinnest.co.kr/api/pub/ticker";
    public static final String API_BITHUMB_URL = "https://api.bithumb.com";
    public static final String API_COINONE_URL = "https://api.coinone.co.kr/ticker";
    public static final String API_BITFINEX_URL = "https://api.bitfinex.com";
    public static final String API_OKEX_URL = "https://www.okex.com/api/v1/ticker.do";

    private static List<String> capList = new ArrayList<>();

    public static synchronized List<String> getCapList(){ return capList; }

    public static boolean autoTrade = true;

}
