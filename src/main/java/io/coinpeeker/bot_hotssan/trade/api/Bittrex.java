package io.coinpeeker.bot_hotssan.trade.api;

import io.coinpeeker.bot_hotssan.utils.HttpUtils;
import org.apache.commons.codec.binary.Hex;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class Bittrex {
    private static final Logger LOGGER = LoggerFactory.getLogger(io.coinpeeker.bot_hotssan.trade.api.Bittrex.class);

    String standardUrl = "https://bittrex.com/api/v1.1";
    String apiKey = "";
    String secretKey = "";
    HttpUtils httpUtils;
    List<NameValuePair> header = new ArrayList<>();


    public Bittrex(String apiKey, String secretKey, HttpUtils httpUtils) {
        this.httpUtils = httpUtils;
        this.apiKey = apiKey;
        this.secretKey = secretKey;
    }

    public void sendOrder(String symbol, String quantity, String rate) throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("?apikey=");
        sb.append(this.apiKey);
        sb.append("&market=");
        sb.append(symbol);
        sb.append("&quantity=");
        sb.append(quantity);
        sb.append("&rate=");
        sb.append(rate);
        sb.append("&nonce=");
        sb.append(generateNonce());


        LOGGER.info(getPrivateRequest("/market/buylimit", sb.toString()).toString());

    }

    public BigDecimal calcBestSellOrderBook(int sequence, JSONObject sellOrderBook, Double myAxisCoinAmount) {
        JSONArray dataArray = sellOrderBook.getJSONArray("result");
        Double totalBtcAmount = 0.0;
        Double selectSatoshi = 0.0;


        for (int i = sequence; i < dataArray.length(); i++) {
            if (myAxisCoinAmount < totalBtcAmount) {
                break;
            }

            JSONObject innerJsonObject = dataArray.getJSONObject(i);

            Double satoshi = innerJsonObject.getDouble("Rate");
            Double quantity = innerJsonObject.getDouble("Quantity");
            Double sellBtcAmount = satoshi * quantity;

            totalBtcAmount += sellBtcAmount;
            selectSatoshi = satoshi;
        }
        return new BigDecimal(Double.valueOf(selectSatoshi)).setScale(8, BigDecimal.ROUND_DOWN);
    }

    public String getBalanceOfCoin(String coin) throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("?apikey=");
        sb.append(this.apiKey);
        sb.append("&currency=");
        sb.append(coin);
        sb.append("&nonce=");
        sb.append(generateNonce());


        JSONObject jsonObject = getPrivateRequest("/account/getbalance", sb.toString());
        return jsonObject.getJSONObject("result").getString("Available");



    }


    public JSONObject getPrivateRequest(String endPoint, String queryString) throws InvalidKeyException, NoSuchAlgorithmException, IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(this.standardUrl);
        sb.append(endPoint);
        sb.append(queryString);

        List<NameValuePair> header = new ArrayList<>();
        header.add(new BasicNameValuePair("apisign", generateSignature(sb.toString())));


        JSONObject jsonObject = httpUtils.getResponseByObject(sb.toString(), header);
        return jsonObject;
    }

    public JSONObject getPublicRequest(String endPoint, String queryString) throws InvalidKeyException, NoSuchAlgorithmException, IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(this.standardUrl);
        sb.append(endPoint);
        sb.append(queryString);


        JSONObject jsonObject = httpUtils.getResponseByObject(sb.toString());
        return jsonObject;
    }

    public JSONObject getOrderBook(String symbol, String type) throws IOException, InvalidKeyException, NoSuchAlgorithmException {
        StringBuilder sb = new StringBuilder();
        sb.append("?market=");
        sb.append(symbol);
        sb.append("&type=");
        sb.append(type);

        return getPublicRequest("/public/getorderbook", sb.toString());
    }

    public String generateNonce() {

        SecureRandom random = null;

        try {

            random = SecureRandom.getInstance("SHA1PRNG");

        } catch (NoSuchAlgorithmException e) {

            e.printStackTrace();
        }

        random.setSeed(System.currentTimeMillis());

        byte[] nonceBytes = new byte[16];
        random.nextBytes(nonceBytes);

        String nonce = null;

        try {

            nonce = new String(Base64.getEncoder().encode(nonceBytes), "UTF-8");

        } catch (UnsupportedEncodingException e) {

            e.printStackTrace();
        }

        return nonce;
    }

    public String generateSignature(String url) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac sha512_HMAC = Mac.getInstance("HmacSHA512");
        SecretKeySpec secretKeySpec = new SecretKeySpec(this.secretKey.getBytes(), "HmacSHA512");
        sha512_HMAC.init(secretKeySpec);

        byte[] hash = sha512_HMAC.doFinal(url.toString().getBytes());
        String signatureResult = Hex.encodeHexString(hash);

        return signatureResult;
    }

}