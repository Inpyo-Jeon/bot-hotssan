package io.coinpeeker.bot_hotssan.scheduler.listed;

import com.google.common.collect.Maps;
import io.coinpeeker.bot_hotssan.common.CommonConstant;
import io.coinpeeker.bot_hotssan.utils.HttpUtils;
import io.coinpeeker.bot_hotssan.utils.MessageUtils;
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
import java.util.Map;

@Component
public class WalletExplorerScheduler {

    @Autowired
    HttpUtils httpUtils;

    @Autowired
    Jedis jedis;

    @Autowired
    MessageUtils messageUtils;

    @Value("${property.hotssan_id}")
    private String apiKey;

    @Value("${property.env}")
    private String env;

    private static final Logger LOGGER = LoggerFactory.getLogger(WalletExplorerScheduler.class);

    public Map<String, String> getMarketWallet() {
        Map<String, String> marketAddressMap = Maps.newHashMap();
        marketAddressMap.put("Upbit-01", "0x03747F06215B44E498831dA019B27f53E483599F");

        marketAddressMap.put("Binance-01", "0x3f5CE5FBFe3E9af3971dD833D26bA9b5C936f0bE");
        marketAddressMap.put("Binance-02", "0xD551234Ae421e3BCBA99A0Da6d736074f22192FF");
        marketAddressMap.put("Binance-03", "0x564286362092D8e7936f0549571a803B203aAceD");
        marketAddressMap.put("Binance-04", "0x0681d8Db095565FE8A346fA0277bFfdE9C0eDBBF");

        marketAddressMap.put("Bittrex-01", "0xFBb1b73C4f0BDa4f67dcA266ce6Ef42f520fBB98");
        marketAddressMap.put("Bittrex-02", "0xE94b04a0FeD112f3664e45adb2B8915693dD5FF3");

        marketAddressMap.put("OKEx-01", "0x236F9F97e0E62388479bf9E5BA4889e46B0273C3");

//        marketAddressMap.put("Huobi-01", "0xFA4B5Be3f2f84f56703C42eB22142744E95a2c58");

        return marketAddressMap;
    }


    @Scheduled(initialDelay = 1000, fixedDelay = 1000)
    public void searchWallet() throws IOException, InterruptedException {
        /** env validation check.**/
        if (!StringUtils.equals("dev", env)) {
            return;
        }

        Map<String, String> marketWalletMap = getMarketWallet();
        String explorerApiAddress = "https://api.ethplorer.io/";
        String endPoint = "getAddressInfo/";
        String parameter = "?apiKey=freekey";

        for (String item : marketWalletMap.keySet()) {
            int jedisCount = 0;
            String marketWallet = marketWalletMap.get(item);
            String url = explorerApiAddress + endPoint + marketWallet + parameter;

            List<NameValuePair> header = new ArrayList<>();
            header.add(new BasicNameValuePair("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8"));
            header.add(new BasicNameValuePair("Accept-Encoding", "gzip, deflate, br"));
            header.add(new BasicNameValuePair("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7"));
            header.add(new BasicNameValuePair("Cache-Control", "max-age=0"));
            header.add(new BasicNameValuePair("Connection", "keep-alive"));
            header.add(new BasicNameValuePair("Host", "api.ethplorer.io"));
            header.add(new BasicNameValuePair("Upgrade-Insecure-Requests", "1"));
            header.add(new BasicNameValuePair("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.186 Safari/537.36"));


            JSONObject jsonObject = httpUtils.getResponseByObject(url, header);
            JSONArray jsonArray = jsonObject.getJSONArray("tokens");
            Map<String, Integer> checkMap = Maps.newHashMap();
            JSONArray toJsonArray = new JSONArray();
            boolean isExistWallet = false;

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject checkJsonObject = jsonArray.getJSONObject(i).getJSONObject("tokenInfo");
                String checkItem = checkJsonObject.getString("symbol");

                if ("".equals(checkItem) || checkMap.containsKey(checkItem)) {
                    continue;
                } else {
                    checkMap.put(checkItem, 1);
                    toJsonArray.put(checkJsonObject);
                }
            }
            synchronized (jedis) {
                if (jedis.exists("Wallet-" + item)) {
                    isExistWallet = true;
                }
            }

            if (isExistWallet) {
                synchronized (jedis) {
                    jedisCount = Math.toIntExact(jedis.hlen("Wallet-" + item));
                }

                if (jedisCount != toJsonArray.length()) {
                    for (int i = 0; i < toJsonArray.length(); i++) {
                        String symbol = toJsonArray.getJSONObject(i).getString("symbol");
                        String address = toJsonArray.getJSONObject(i).getString("address");
                        boolean isExistSymbol = true;

                        synchronized (jedis) {
                            if (!jedis.hexists("Wallet-" + item, symbol)) {
                                isExistSymbol = false;
                            }
                        }

                        if (!isExistSymbol) {
                            StringBuilder messageContent = new StringBuilder();
                            messageContent.append("Wallet-");
                            messageContent.append(item);
                            messageContent.append(" ");
                            messageContent.append(symbol);
                            messageContent.append("심볼 생성");
                            messageContent.append("\nEtherScan : \n");
                            messageContent.append("https://etherscan.io/token/");
                            messageContent.append(address);
                            messageContent.append("?a=");
                            messageContent.append(marketWallet);

                            String botUrl = CommonConstant.URL_TELEGRAM_BASE + apiKey + CommonConstant.METHOD_TELEGRAM_SENDMESSAGE;
                            messageUtils.sendMessage(botUrl, -259666461L, messageContent.toString());
                            messageUtils.sendMessage(botUrl, -294606763L, messageContent.toString());

                            LOGGER.info("Wallet-" + item + " : " + symbol + " 심볼 생성");

                            synchronized (jedis) {
                                jedis.hset("Wallet-" + item, symbol, "0");
                            }
                        }
                    }
                }
            } else {
                for (int i = 0; i < toJsonArray.length(); i++) {
                    String symbol = toJsonArray.getJSONObject(i).getString("symbol");

                    synchronized (jedis) {
                        jedis.hset("Wallet-" + item, symbol, "0");
                    }
                }
            }

            Thread.sleep(1000 * 60);

        }
    }
}
