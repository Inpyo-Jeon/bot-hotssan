package io.coinpeeker.bot_hotssan.scheduler;

import io.coinpeeker.bot_hotssan.common.CommonConstant;
import io.coinpeeker.bot_hotssan.common.SecretKey;
import io.coinpeeker.bot_hotssan.utils.HttpUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static io.coinpeeker.bot_hotssan.common.CommonConstant.capList;

@Component
public class RefreshRedis {

    @Autowired
    private HttpUtils httpUtils;

    @Autowired
    private Jedis jedis;

    @Value("${property.env}")
    String env;

    private static final Logger LOGGER = LoggerFactory.getLogger(RefreshRedis.class);

    @Scheduled(initialDelay = 1000, fixedDelay = 1000 * 60 * 60 * 24)
    public void start() throws IOException {
        /** env validation check.**/
        if (!StringUtils.equals("real", env)) {
            JSONArray jsonArray = httpUtils.getResponseByArrays("https://api.coinmarketcap.com/v1/ticker/?limit=1600");
            for (int i = 0; i < jsonArray.length(); i++) {
                CommonConstant.getCapList().add(jsonArray.getJSONObject(i).getString("symbol"));
            }
            return;
        }

        coinMarketCap();
        binance();
        okex();
        upbit();
        kucoin();
    }


    public void coinMarketCap() throws IOException {

        JSONArray jsonArray = httpUtils.getResponseByArrays("https://api.coinmarketcap.com/v1/ticker/?limit=1600");

        synchronized (jedis) {
            if (!jedis.exists("CoinMarketCap")) {
                LOGGER.info("@#@#@# CoinMarketCap Listing is null");
            } else {
                LOGGER.info("@#@#@# CoinMarketCap Listing is delete and push");
                jedis.del("CoinMarketCap");
                jedis.del("CoinMarketCap_Address");
                CommonConstant.getCapList().clear();
            }

            for (int i = 0; i < jsonArray.length(); i++) {
                jedis.hset("CoinMarketCap", jsonArray.getJSONObject(i).getString("symbol"), jsonArray.getJSONObject(i).getString("name"));
                jedis.hset("CoinMarketCap_Address", jsonArray.getJSONObject(i).getString("symbol"), jsonArray.getJSONObject(i).getString("id"));
                CommonConstant.getCapList().add(jsonArray.getJSONObject(i).getString("symbol"));
            }

        }

    }

    public void binance() throws IOException {
        synchronized (jedis) {
            // Redis에 binance 상장목록 존재 여부 체크
            if (!jedis.exists("BinanceListing")) {
                LOGGER.info("@#@#@# binance Listing is null");

                jedis.hset("BinanceListing", "BTC", "0");

                JSONArray jsonArrayBinance = httpUtils.getResponseByArrays("https://api.binance.com/api/v3/ticker/price");
                for (int i = 0; i < jsonArrayBinance.length(); i++) {
                    if (jsonArrayBinance.getJSONObject(i).getString("symbol").contains("BTC")) {
                        jedis.hset("BinanceListing", jsonArrayBinance.getJSONObject(i).getString("symbol").replace("BTC", ""), "0");
                    }
                }
            }
        }

    }

    public void okex() throws IOException {
        synchronized (jedis) {
            if (!jedis.exists("OKExListing")) {
                LOGGER.info("@#@#@# OKEx Listing is null");

                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("api_key", SecretKey.getApiKeyOkex()));
                params.add(new BasicNameValuePair("sign", SecretKey.getSignOkex()));

                JSONObject jsonObject = httpUtils.getPostResponseByObject(SecretKey.getUrlOkex(), params);
                JSONObject list = jsonObject.getJSONObject("info").getJSONObject("funds").getJSONObject("free");

                for (Object item : list.keySet()) {
                    jedis.hset("OKExListing", item.toString().toUpperCase(), "0");
                }
            }
        }

    }

    public void upbit() {
        synchronized (jedis) {
            if (!jedis.exists("UpbitListing")) {
                LOGGER.info("@#@#@# Upbit Listing is null");
                jedis.hset("UpbitListing", "BTC", "0");
                jedis.hset("UpbitListing", "WAX", "0");
                jedis.hset("UpbitListing", "LTC", "0");
                jedis.hset("UpbitListing", "XRP", "0");
                jedis.hset("UpbitListing", "ETH", "0");
                jedis.hset("UpbitListing", "ETC", "0");
                jedis.hset("UpbitListing", "NBT", "0");
                jedis.hset("UpbitListing", "ADA", "0");
                jedis.hset("UpbitListing", "XLM", "0");
                jedis.hset("UpbitListing", "NEO", "0");
                jedis.hset("UpbitListing", "BCC", "0");
                jedis.hset("UpbitListing", "SRN", "0");
                jedis.hset("UpbitListing", "BCC", "0");
                jedis.hset("UpbitListing", "LSK", "0");
                jedis.hset("UpbitListing", "WAVES", "0");
                jedis.hset("UpbitListing", "DOGE", "0");
                jedis.hset("UpbitListing", "SC", "0");
                jedis.hset("UpbitListing", "XVG", "0");
                jedis.hset("UpbitListing", "STRAT", "0");
                jedis.hset("UpbitListing", "QTUM", "0");
                jedis.hset("UpbitListing", "XEM", "0");
                jedis.hset("UpbitListing", "KMD", "0");
                jedis.hset("UpbitListing", "OMG", "0");
                jedis.hset("UpbitListing", "ZEC", "0");
                jedis.hset("UpbitListing", "DGB", "0");
                jedis.hset("UpbitListing", "NXS", "0");
                jedis.hset("UpbitListing", "XMR", "0");
                jedis.hset("UpbitListing", "DASH", "0");
                jedis.hset("UpbitListing", "BTG", "0");
                jedis.hset("UpbitListing", "STEEM", "0");
                jedis.hset("UpbitListing", "RDD", "0");
                jedis.hset("UpbitListing", "IGNIS", "0");
                jedis.hset("UpbitListing", "SYS", "0");
                jedis.hset("UpbitListing", "NXT", "0");
                jedis.hset("UpbitListing", "PIVX", "0");
                jedis.hset("UpbitListing", "SALT", "0");
                jedis.hset("UpbitListing", "GMT", "0");
                jedis.hset("UpbitListing", "POWR", "0");
                jedis.hset("UpbitListing", "ARK", "0");
                jedis.hset("UpbitListing", "MONA", "0");
                jedis.hset("UpbitListing", "VTC", "0");
                jedis.hset("UpbitListing", "XDN", "0");
                jedis.hset("UpbitListing", "MANA", "0");
                jedis.hset("UpbitListing", "GUP", "0");
                jedis.hset("UpbitListing", "ARDR", "0");
                jedis.hset("UpbitListing", "GBYTE", "0");
                jedis.hset("UpbitListing", "VOX", "0");
                jedis.hset("UpbitListing", "ZEN", "0");
                jedis.hset("UpbitListing", "BITB", "0");
                jedis.hset("UpbitListing", "UKG", "0");
                jedis.hset("UpbitListing", "SBD", "0");
                jedis.hset("UpbitListing", "BAT", "0");
                jedis.hset("UpbitListing", "SNT", "0");
                jedis.hset("UpbitListing", "EDG", "0");
                jedis.hset("UpbitListing", "REP", "0");
                jedis.hset("UpbitListing", "EMC2", "0");
                jedis.hset("UpbitListing", "ION", "0");
                jedis.hset("UpbitListing", "PAY", "0");
                jedis.hset("UpbitListing", "ADT", "0");
                jedis.hset("UpbitListing", "XZC", "0");
                jedis.hset("UpbitListing", "BAY", "0");
                jedis.hset("UpbitListing", "ENG", "0");
                jedis.hset("UpbitListing", "DNT", "0");
                jedis.hset("UpbitListing", "VIA", "0");
                jedis.hset("UpbitListing", "ADX", "0");
                jedis.hset("UpbitListing", "CVC", "0");
                jedis.hset("UpbitListing", "DCR", "0");
                jedis.hset("UpbitListing", "QRL", "0");
                jedis.hset("UpbitListing", "BURST", "0");
                jedis.hset("UpbitListing", "RCN", "0");
                jedis.hset("UpbitListing", "RLC", "0");
                jedis.hset("UpbitListing", "MAID", "0");
                jedis.hset("UpbitListing", "STORJ", "0");
                jedis.hset("UpbitListing", "MCO", "0");
                jedis.hset("UpbitListing", "LBC", "0");
                jedis.hset("UpbitListing", "VIB", "0");
                jedis.hset("UpbitListing", "GRS", "0");
                jedis.hset("UpbitListing", "FCT", "0");
                jedis.hset("UpbitListing", "BLOCK", "0");
                jedis.hset("UpbitListing", "ANT", "0");
                jedis.hset("UpbitListing", "AMP", "0");
                jedis.hset("UpbitListing", "HMQ", "0");
                jedis.hset("UpbitListing", "BNT", "0");
                jedis.hset("UpbitListing", "SYNX", "0");
                jedis.hset("UpbitListing", "SWT", "0");
                jedis.hset("UpbitListing", "EXP", "0");
                jedis.hset("UpbitListing", "GAME", "0");
                jedis.hset("UpbitListing", "OK", "0");
                jedis.hset("UpbitListing", "KORE", "0");
                jedis.hset("UpbitListing", "NMR", "0");
                jedis.hset("UpbitListing", "1ST", "0");
                jedis.hset("UpbitListing", "WINGS", "0");
                jedis.hset("UpbitListing", "UBQ", "0");
                jedis.hset("UpbitListing", "NAV", "0");
                jedis.hset("UpbitListing", "MER", "0");
                jedis.hset("UpbitListing", "IOP", "0");
                jedis.hset("UpbitListing", "PTOY", "0");
                jedis.hset("UpbitListing", "VRC", "0");
                jedis.hset("UpbitListing", "XEL", "0");
                jedis.hset("UpbitListing", "PART", "0");
                jedis.hset("UpbitListing", "BLK", "0");
                jedis.hset("UpbitListing", "DYN", "0");
                jedis.hset("UpbitListing", "TX", "0");
                jedis.hset("UpbitListing", "SHIFT", "0");
                jedis.hset("UpbitListing", "CFI", "0");
                jedis.hset("UpbitListing", "TIX", "0");
                jedis.hset("UpbitListing", "CLOAK", "0");
                jedis.hset("UpbitListing", "AGRS", "0");
                jedis.hset("UpbitListing", "GNO", "0");
                jedis.hset("UpbitListing", "UNB", "0");
                jedis.hset("UpbitListing", "RADS", "0");
                jedis.hset("UpbitListing", "BSD", "0");
                jedis.hset("UpbitListing", "DCT", "0");
                jedis.hset("UpbitListing", "SLS", "0");
                jedis.hset("UpbitListing", "MEME", "0");
                jedis.hset("UpbitListing", "FTC", "0");
                jedis.hset("UpbitListing", "SPHR", "0");
                jedis.hset("UpbitListing", "SIB", "0");
                jedis.hset("UpbitListing", "MUE", "0");
                jedis.hset("UpbitListing", "EXCL", "0");
                jedis.hset("UpbitListing", "MTL", "0");
                jedis.hset("UpbitListing", "GNT", "0");
                jedis.hset("UpbitListing", "DOPE", "0");
                jedis.hset("UpbitListing", "MYST", "0");    // 지원종료
                jedis.hset("UpbitListing", "RISE", "0");    // 지원종료
                jedis.hset("UpbitListing", "DGD", "0");     // 지원종료
                jedis.hset("UpbitListing", "FUN", "0");     // 지원종료
                jedis.hset("UpbitListing", "TRIG", "0");    // 지원종료
                jedis.hset("UpbitListing", "SAFEX", "0");   // 지원종료
                jedis.hset("UpbitListing", "SNGLS", "0");   // 지원종료
                jedis.hset("UpbitListing", "XAUR", "0");    // 지원종료
                jedis.hset("UpbitListing", "ZRX", "0");
                jedis.hset("UpbitListing", "VEE", "0");
                jedis.hset("UpbitListing", "BCPT", "0");
            }
        }

    }

    public void kucoin() throws IOException {
        synchronized (jedis) {
            if (!jedis.exists("KucoinListing")) {
                LOGGER.info("@#@#@# Kucoin Listing is null");

                jedis.hset("KucoinListing", "USDT", "0");

                JSONObject jsonObject = httpUtils.getResponseByObject("https://api.kucoin.com/v1/open/tick");
                JSONArray list = jsonObject.getJSONArray("data");

                for (int i = 0; i < list.length(); i++) {
                    jedis.hset("KucoinListing", list.getJSONObject(i).getString("coinType"), "0");
                }
            }
        }
    }
}
