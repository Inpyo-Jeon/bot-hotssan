package io.coinpeeker.bot_hotssan.module;

import io.coinpeeker.bot_hotssan.trade.API.Binance;
import io.coinpeeker.bot_hotssan.utils.HttpUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ModuleTest {


    @Value("${property.hotssan_id}")
    private String apiKey;

    @Value("${property.env}")
    private String env;

    String standardUrl = "https://api.binance.com";
    String url = "";
    String headerApiKey = "X-MBX-APIKEY";
    String headerApiValue = "";
    String secretKey = "";
    List<NameValuePair> header = new ArrayList<>();

    @Test
    public void Test() throws IOException, NoSuchAlgorithmException, InvalidKeyException, InterruptedException {
        Binance binance = new Binance("m6yQuY6E1BscKqlxIHMhqzkSEa7l9vMKWEQTusyzN9Ozslq3k023x3ou6wxWlJGk", "ZR80HbvYPlckwsbEiyMHNT6nu5SHmLZU3TF95n2uqxloLUmSAz4Rd1yEIooPIbXF");
        Boolean check = true;

        String buyCoin = "IOST";
        String axisCoin = "BTC";
        String buyCoinSymbol = buyCoin + axisCoin;

        BigDecimal myAxisCoinAmount = new BigDecimal(Double.valueOf(binance.getHaveCoinAmount(axisCoin)));
        BigDecimal buyCoinMarketPrice = new BigDecimal(Double.valueOf(binance.getCurrentCoinMarketPrice(buyCoinSymbol)));
        BigDecimal buyAmount = new BigDecimal((myAxisCoinAmount.doubleValue() / buyCoinMarketPrice.doubleValue()) * 0.9);


        System.out.println(myAxisCoinAmount);
        System.out.println(buyCoinMarketPrice);
        System.out.println(buyAmount);

        System.out.println(myAxisCoinAmount.setScale(8, BigDecimal.ROUND_DOWN));
        System.out.println(buyCoinMarketPrice.setScale(8, BigDecimal.ROUND_DOWN));
        System.out.println(buyAmount.setScale(2, BigDecimal.ROUND_DOWN));



    }


    public class Binance {

        @Value("${property.hotssan_id}")
        private String apiKey;

        @Value("${property.env}")
        private String env;

        String standardUrl = "https://api.binance.com";
        String url = "";
        String headerApiKey = "X-MBX-APIKEY";
        String headerApiValue = "";
        String secretKey = "";
        List<NameValuePair> header = new ArrayList<>();
        HttpUtils httpUtils = new HttpUtils();


        public Binance(String headerApiValue, String secretKey) {
            this.headerApiValue = headerApiValue;
            this.secretKey = secretKey;
            header.add(new BasicNameValuePair(this.headerApiKey, this.headerApiValue));
        }

        public String getHaveCoinAmount(String symbol) throws IOException, InvalidKeyException, NoSuchAlgorithmException {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            String free = "";
            StringBuilder sb = new StringBuilder();
            sb.append("recvWindow=");
            sb.append("5000");
            sb.append("&timestamp=");
            sb.append(timestamp.getTime());

            JSONObject jsonObject = getRequest("/api/v3/account", sb.toString(), true);
            JSONArray jsonArray = jsonObject.getJSONArray("balances");

            for (int i = 0; i < jsonArray.length(); i++) {
                if (jsonArray.getJSONObject(i).getString("asset").equals(symbol)) {
                    free = jsonArray.getJSONObject(i).getString("free");
                    break;
                }
            }
            return free;
        }

        public String getCurrentCoinMarketPrice(String symbol) throws NoSuchAlgorithmException, InvalidKeyException, IOException {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            String free = "";
            StringBuilder sb = new StringBuilder();
            sb.append("symbol=");
            sb.append(symbol);

            JSONObject jsonObject = getRequest("/api/v3/ticker/price", sb.toString(), false);

            return jsonObject.getString("price");
        }

        public Double getQuantityMinOrder(String symbol) throws NoSuchAlgorithmException, InvalidKeyException, IOException {
            Double minQuantity = 0.0;
            JSONObject jsonObject = getRequest("/api/v1/exchangeInfo", null, false);
            JSONArray jsonArray = jsonObject.getJSONArray("symbols");

            for (int i = 0; i < jsonArray.length(); i++) {
                if (symbol.equals(jsonArray.getJSONObject(i).getString("symbol"))) {
                    JSONObject item = jsonArray.getJSONObject(i);
                    JSONArray filters = item.getJSONArray("filters");

                    for (int j = 0; j < filters.length(); j++) {
                        if (filters.getJSONObject(j).has("minQty")) {
                            minQuantity = filters.getJSONObject(j).getDouble("minQty");
                            break;
                        }
                    }
                }
            }
            return minQuantity;
        }

        public void sendOrder(String symbol, String side, String type, String quantity) throws NoSuchAlgorithmException, InvalidKeyException, IOException {
            StringBuilder sb = new StringBuilder();
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            sb.append("symbol=");
            sb.append(symbol);
            sb.append("&side=");
            sb.append(side);
            sb.append("&type=");
            sb.append(type);
            sb.append("&quantity=");
            sb.append(quantity);
            sb.append("&recvWindow=");
            sb.append("5000");
            sb.append("&timestamp=");
            sb.append(timestamp.getTime());

            postRequest("/api/v3/order", sb.toString());
        }

        public JSONObject postRequest(String endPoint, String queryString) throws InvalidKeyException, NoSuchAlgorithmException, IOException {
            StringBuilder sb = new StringBuilder();

            sb.append(this.standardUrl);
            sb.append(endPoint);
            sb.append("?");
            sb.append(queryString);
            sb.append("&signature=");
            Mac mac = Mac.getInstance("HMACSHA256");
            mac.init(new SecretKeySpec(this.secretKey.getBytes(), "HMACSHA256"));

            for (byte byteItem : mac.doFinal(queryString.getBytes())) {
                sb.append(Integer.toString((byteItem & 0xFF) + 0x100, 16).substring(1));
            }

            JSONObject jsonObject = httpUtils.getPostResponseByObject(sb.toString(), header, "binanceTrade");
            System.out.println(jsonObject);
            return jsonObject;

        }

        public JSONObject getRequest(String endPoint, String queryString, Boolean isSignature) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
            StringBuilder sb = new StringBuilder();
            JSONObject jsonObject = null;

            sb.append(this.standardUrl);
            sb.append(endPoint);

            if (queryString != null) {
                sb.append("?");
                sb.append(queryString);
            }

            if (isSignature) {
                sb.append("&signature=");
                Mac mac = Mac.getInstance("HMACSHA256");
                mac.init(new SecretKeySpec(this.secretKey.getBytes(), "HMACSHA256"));

                for (byte byteItem : mac.doFinal(queryString.getBytes())) {
                    sb.append(Integer.toString((byteItem & 0xFF) + 0x100, 16).substring(1));
                }
            }

            jsonObject = httpUtils.getResponseByObject(sb.toString(), header);

            return jsonObject;

        }
    }
}
