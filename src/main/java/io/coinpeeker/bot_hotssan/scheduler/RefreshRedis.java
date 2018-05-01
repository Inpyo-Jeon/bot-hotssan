package io.coinpeeker.bot_hotssan.scheduler;

import io.coinpeeker.bot_hotssan.common.CommonConstant;
import io.coinpeeker.bot_hotssan.common.CustomJedis;
import io.coinpeeker.bot_hotssan.common.SecretKey;
import io.coinpeeker.bot_hotssan.utils.HttpUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

        binance();
        okex();
        upbit();
        kucoin();
        bittrex();
        huobiPro();
        bithumb();
        binanceVer2();
        upbitS3();

        /** env validation check.**/
        if (!StringUtils.equals("real", env)) {
            JSONArray jsonArray = httpUtils.getResponseByArrays("https://api.coinmarketcap.com/v1/ticker/?limit=1600");
            for (int i = 0; i < jsonArray.length(); i++) {
                CommonConstant.getCapList().add(jsonArray.getJSONObject(i).getString("symbol"));
            }
            return;
        }

        coinMarketCap();
    }


    public void coinMarketCap() throws IOException {

        JSONArray jsonArray = httpUtils.getResponseByArrays("https://api.coinmarketcap.com/v1/ticker/?limit=1600");

        synchronized (jedis) {
            if (!jedis.exists("I-CoinMarketCap")) {
                LOGGER.info("@#@#@# I-CoinMarketCap Listing is null");
            } else {
                LOGGER.info("@#@#@# I-CoinMarketCap Listing is delete and push");
                jedis.del("I-CoinMarketCap");
                jedis.del("I-CoinMarketCap-Address");
                CommonConstant.getCapList().clear();
            }
        }

        for (int i = 0; i < jsonArray.length(); i++) {
            synchronized (jedis) {
                jedis.hset("I-CoinMarketCap", jsonArray.getJSONObject(i).getString("symbol"), jsonArray.getJSONObject(i).getString("name"));
                jedis.hset("I-CoinMarketCap-Address", jsonArray.getJSONObject(i).getString("symbol"), jsonArray.getJSONObject(i).getString("id"));
            }
            CommonConstant.getCapList().add(jsonArray.getJSONObject(i).getString("symbol"));
        }
    }

    public void binance() throws IOException {
        String endPoint = "https://www.binance.com/dictionary/getAssetPic.html";
        JSONObject jsonObject = httpUtils.getPostResponseByObject(endPoint);
        JSONArray jsonArray = jsonObject.getJSONArray("data");

        synchronized (jedis) {
            if (!jedis.exists("L-Binance")) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    String asset = jsonArray.getJSONObject(i).getString("asset");
                    String pic = "-";

                    if (jsonArray.getJSONObject(i).has("pic")) {
                        pic = jsonArray.getJSONObject(i).getString("pic");
                    }

                    jedis.hset("L-Binance", asset, pic);

                }
            }
        }
    }

    public void okex() throws IOException {

        synchronized (jedis) {
            if (!jedis.exists("L-OKEx")) {
                LOGGER.info("@#@#@# L-OKEx is null");

                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("api_key", SecretKey.getApiKeyOkex()));
                params.add(new BasicNameValuePair("sign", SecretKey.getSignOkex()));

                JSONObject jsonObject = httpUtils.getPostResponseByObject(SecretKey.getUrlOkex(), params, "");
                JSONObject list = jsonObject.getJSONObject("info").getJSONObject("funds").getJSONObject("free");

                for (Object item : list.keySet()) {
                    jedis.hset("L-OKEx", item.toString().toUpperCase(), "1");
                }
            }
        }
    }

    public void upbit() {
        synchronized (jedis) {
            if (!jedis.exists("L-Upbit")) {
                LOGGER.info("@#@#@# L-Upbit is null");
                jedis.hset("L-Upbit", "BTC", "0");
                jedis.hset("L-Upbit", "WAX", "0");
                jedis.hset("L-Upbit", "LTC", "0");
                jedis.hset("L-Upbit", "XRP", "0");
                jedis.hset("L-Upbit", "ETH", "0");
                jedis.hset("L-Upbit", "ETC", "0");
                jedis.hset("L-Upbit", "NBT", "0");
                jedis.hset("L-Upbit", "ADA", "0");
                jedis.hset("L-Upbit", "XLM", "0");
                jedis.hset("L-Upbit", "NEO", "0");
                jedis.hset("L-Upbit", "BCC", "0");
                jedis.hset("L-Upbit", "SRN", "0");
                jedis.hset("L-Upbit", "BCC", "0");
                jedis.hset("L-Upbit", "LSK", "0");
                jedis.hset("L-Upbit", "WAVES", "0");
                jedis.hset("L-Upbit", "DOGE", "0");
                jedis.hset("L-Upbit", "SC", "0");
                jedis.hset("L-Upbit", "XVG", "0");
                jedis.hset("L-Upbit", "STRAT", "0");
                jedis.hset("L-Upbit", "QTUM", "0");
                jedis.hset("L-Upbit", "XEM", "0");
                jedis.hset("L-Upbit", "KMD", "0");
                jedis.hset("L-Upbit", "OMG", "0");
                jedis.hset("L-Upbit", "ZEC", "0");
                jedis.hset("L-Upbit", "DGB", "0");
                jedis.hset("L-Upbit", "NXS", "0");
                jedis.hset("L-Upbit", "XMR", "0");
                jedis.hset("L-Upbit", "DASH", "0");
                jedis.hset("L-Upbit", "BTG", "0");
                jedis.hset("L-Upbit", "STEEM", "0");
                jedis.hset("L-Upbit", "RDD", "0");
                jedis.hset("L-Upbit", "IGNIS", "0");
                jedis.hset("L-Upbit", "SYS", "0");
                jedis.hset("L-Upbit", "NXT", "0");
                jedis.hset("L-Upbit", "PIVX", "0");
                jedis.hset("L-Upbit", "SALT", "0");
                jedis.hset("L-Upbit", "GMT", "0");
                jedis.hset("L-Upbit", "POWR", "0");
                jedis.hset("L-Upbit", "ARK", "0");
                jedis.hset("L-Upbit", "MONA", "0");
                jedis.hset("L-Upbit", "VTC", "0");
                jedis.hset("L-Upbit", "XDN", "0");
                jedis.hset("L-Upbit", "MANA", "0");
                jedis.hset("L-Upbit", "GUP", "0");
                jedis.hset("L-Upbit", "ARDR", "0");
                jedis.hset("L-Upbit", "GBYTE", "0");
                jedis.hset("L-Upbit", "VOX", "0");
                jedis.hset("L-Upbit", "ZEN", "0");
                jedis.hset("L-Upbit", "BITB", "0");
                jedis.hset("L-Upbit", "UKG", "0");
                jedis.hset("L-Upbit", "SBD", "0");
                jedis.hset("L-Upbit", "BAT", "0");
                jedis.hset("L-Upbit", "SNT", "0");
                jedis.hset("L-Upbit", "EDG", "0");
                jedis.hset("L-Upbit", "REP", "0");
                jedis.hset("L-Upbit", "EMC2", "0");
                jedis.hset("L-Upbit", "ION", "0");
                jedis.hset("L-Upbit", "PAY", "0");
                jedis.hset("L-Upbit", "ADT", "0");
                jedis.hset("L-Upbit", "XZC", "0");
                jedis.hset("L-Upbit", "BAY", "0");
                jedis.hset("L-Upbit", "ENG", "0");
                jedis.hset("L-Upbit", "DNT", "0");
                jedis.hset("L-Upbit", "VIA", "0");
                jedis.hset("L-Upbit", "ADX", "0");
                jedis.hset("L-Upbit", "CVC", "0");
                jedis.hset("L-Upbit", "DCR", "0");
                jedis.hset("L-Upbit", "QRL", "0");
                jedis.hset("L-Upbit", "BURST", "0");
                jedis.hset("L-Upbit", "RCN", "0");
                jedis.hset("L-Upbit", "RLC", "0");
                jedis.hset("L-Upbit", "MAID", "0");
                jedis.hset("L-Upbit", "STORJ", "0");
                jedis.hset("L-Upbit", "MCO", "0");
                jedis.hset("L-Upbit", "LBC", "0");
                jedis.hset("L-Upbit", "VIB", "0");
                jedis.hset("L-Upbit", "GRS", "0");
                jedis.hset("L-Upbit", "FCT", "0");
                jedis.hset("L-Upbit", "BLOCK", "0");
                jedis.hset("L-Upbit", "ANT", "0");
                jedis.hset("L-Upbit", "AMP", "0");
                jedis.hset("L-Upbit", "HMQ", "0");
                jedis.hset("L-Upbit", "BNT", "0");
                jedis.hset("L-Upbit", "SYNX", "0");
                jedis.hset("L-Upbit", "SWT", "0");
                jedis.hset("L-Upbit", "EXP", "0");
                jedis.hset("L-Upbit", "GAME", "0");
                jedis.hset("L-Upbit", "OK", "0");
                jedis.hset("L-Upbit", "KORE", "0");
                jedis.hset("L-Upbit", "NMR", "0");
                jedis.hset("L-Upbit", "1ST", "0");
                jedis.hset("L-Upbit", "WINGS", "0");
                jedis.hset("L-Upbit", "UBQ", "0");
                jedis.hset("L-Upbit", "NAV", "0");
                jedis.hset("L-Upbit", "MER", "0");
                jedis.hset("L-Upbit", "IOP", "0");
                jedis.hset("L-Upbit", "PTOY", "0");
                jedis.hset("L-Upbit", "VRC", "0");
                jedis.hset("L-Upbit", "XEL", "0");
                jedis.hset("L-Upbit", "PART", "0");
                jedis.hset("L-Upbit", "BLK", "0");
                jedis.hset("L-Upbit", "DYN", "0");
                jedis.hset("L-Upbit", "TX", "0");
                jedis.hset("L-Upbit", "SHIFT", "0");
                jedis.hset("L-Upbit", "CFI", "0");
                jedis.hset("L-Upbit", "TIX", "0");
                jedis.hset("L-Upbit", "CLOAK", "0");
                jedis.hset("L-Upbit", "AGRS", "0");
                jedis.hset("L-Upbit", "GNO", "0");
                jedis.hset("L-Upbit", "UNB", "0");
                jedis.hset("L-Upbit", "RADS", "0");
                jedis.hset("L-Upbit", "BSD", "0");
                jedis.hset("L-Upbit", "DCT", "0");
                jedis.hset("L-Upbit", "SLS", "0");
                jedis.hset("L-Upbit", "MEME", "0");
                jedis.hset("L-Upbit", "FTC", "0");
                jedis.hset("L-Upbit", "SPHR", "0");
                jedis.hset("L-Upbit", "SIB", "0");
                jedis.hset("L-Upbit", "MUE", "0");
                jedis.hset("L-Upbit", "EXCL", "0");
                jedis.hset("L-Upbit", "MTL", "0");
                jedis.hset("L-Upbit", "GNT", "0");
                jedis.hset("L-Upbit", "DOPE", "0");
                jedis.hset("L-Upbit", "MYST", "0");    // 지원종료
                jedis.hset("L-Upbit", "RISE", "0");    // 지원종료
                jedis.hset("L-Upbit", "DGD", "0");     // 지원종료
                jedis.hset("L-Upbit", "FUN", "0");     // 지원종료
                jedis.hset("L-Upbit", "TRIG", "0");    // 지원종료
                jedis.hset("L-Upbit", "SAFEX", "0");   // 지원종료
                jedis.hset("L-Upbit", "SNGLS", "0");   // 지원종료
                jedis.hset("L-Upbit", "XAUR", "0");    // 지원종료
                jedis.hset("L-Upbit", "ZRX", "0");
                jedis.hset("L-Upbit", "VEE", "0");
                jedis.hset("L-Upbit", "BCPT", "0");
                jedis.hset("L-Upbit", "TRX", "0");
                jedis.hset("L-Upbit", "DRGN", "0");
                jedis.hset("L-Upbit", "LRC", "0");
                jedis.hset("L-Upbit", "TUSD", "0");
                jedis.hset("L-Upbit", "RVR", "0");
                jedis.hset("L-Upbit", "UP", "0");
                jedis.hset("L-Upbit", "ICX", "0");
                jedis.hset("L-Upbit", "STORM", "0");
                jedis.hset("L-Upbit", "EOS", "0");
            }
        }
    }

    public void kucoin() throws IOException {
        synchronized (jedis) {
            if (!jedis.exists("L-Kucoin")) {
                LOGGER.info("@#@#@# L-Kucoin is null");

                jedis.hset("L-Kucoin", "USDT", "0");

                JSONObject jsonObject = httpUtils.getResponseByObject("https://api.kucoin.com/v1/open/tick");
                JSONArray list = jsonObject.getJSONArray("data");

                for (int i = 0; i < list.length(); i++) {
                    jedis.hset("L-Kucoin", list.getJSONObject(i).getString("coinType"), "0");
                }
            }
        }
    }

    public void bittrex() throws IOException {
        synchronized (jedis) {
            if (!jedis.exists("L-Bittrex")) {
                LOGGER.info("@#@#@# L-Bittrex is null");


                JSONObject jsonObject = httpUtils.getResponseByObject("https://bittrex.com/api/v2.0/pub/markets/GetMarketSummaries");
                JSONArray list = jsonObject.getJSONArray("result");

                for (int i = 0; i < list.length(); i++) {
                    jedis.hset("L-Bittrex", list.getJSONObject(i).getJSONObject("Market").getString("MarketCurrency"), "1");
                }
            }
        }
    }

    public void huobiPro() throws IOException {
        synchronized (jedis) {
            if (!jedis.exists("L-HuobiPro")) {
                LOGGER.info("@#@#@# L-HuobiPro is null");

                JSONObject jsonObject = httpUtils.getResponseByObject("https://api.huobi.pro/v1/common/currencys");
                JSONArray list = jsonObject.getJSONArray("data");

                for (int i = 0; i < list.length(); i++) {
                    jedis.hset("L-HuobiPro", list.getString(i).toUpperCase(), "0");
                }
            }
        }
    }

    public void bithumb() throws IOException {
        synchronized (jedis) {
            if (!jedis.exists("L-Bithumb")) {
                JSONObject jsonObject = httpUtils.getResponseByObject("https://api.bithumb.com/public/ticker/all");

                jsonObject.getJSONObject("data").keySet().remove("date");
                Set keySet = jsonObject.getJSONObject("data").keySet();

                keySet.stream().forEach((key) -> {
                    synchronized (jedis) {
                        jedis.hset("L-Bithumb", String.valueOf(key), "0");
                    }
                });
            }
        }
    }

    public void binanceVer2() throws IOException {
        synchronized (jedis) {
            if (!jedis.exists("L-Binance-InternalAPI")) {
                CloseableHttpResponse response = httpUtils.get("https://support.binance.com/api/v2/help_center/en-us/sections/115000106672/articles.json?page=2&per_page=1");

                if (response.getStatusLine().getStatusCode() == 200) {
                    LOGGER.info("Binance-Internal-OK");
                    JSONObject jsonObject = new JSONObject(EntityUtils.toString(response.getEntity(), "UTF-8"));

                    synchronized (jedis) {
                        jedis.hset("L-Binance-InternalAPI", "count", String.valueOf(jsonObject.getInt("count")));
                    }
                }
            }
        }
    }

    public void upbitS3() throws IOException {
        synchronized (jedis) {
            if (!jedis.exists("L-Upbit-S3")) {
                List<NameValuePair> header = new ArrayList<>();
                header.add(new BasicNameValuePair("Accept", "*/*"));
                header.add(new BasicNameValuePair("Accept-Encoding", "gzip, deflate, br"));
                header.add(new BasicNameValuePair("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7"));
                header.add(new BasicNameValuePair("Connection", "keep-alive"));
                header.add(new BasicNameValuePair("Host", "s3.ap-northeast-2.amazonaws.com"));
                header.add(new BasicNameValuePair("Origin", "https://upbit.com"));
                header.add(new BasicNameValuePair("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36"));

                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                String callUrl = "https://s3.ap-northeast-2.amazonaws.com/crix-production/crix_master?nonce" + timestamp.getTime();

                JSONArray jsonArray = httpUtils.getResponseByArrays(callUrl, header);

                for(int i = 0; i < jsonArray.length(); i++){
                    synchronized (jedis){
                        jedis.hset("L-Upbit-S3", jsonArray.getJSONObject(i).getString("code"), jsonArray.getJSONObject(i).getString("baseCurrencyCode"));
                    }
                }
            }
        }
    }
}
