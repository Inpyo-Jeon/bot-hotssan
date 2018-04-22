package io.coinpeeker.bot_hotssan.trade.api;

import io.coinpeeker.bot_hotssan.utils.HttpUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Binance {

    private static final Logger LOGGER = LoggerFactory.getLogger(Binance.class);
    String standardUrl = "https://api.binance.com";
    String headerApiKey = "X-MBX-APIKEY";
    String headerApiValue = "";
    String secretKey = "";
    HttpUtils httpUtils;
    List<NameValuePair> header = new ArrayList<>();


    public Binance(String headerApiValue, String secretKey, HttpUtils httpUtils) {
        this.httpUtils = httpUtils;
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
        LOGGER.info(jsonObject.toString());
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