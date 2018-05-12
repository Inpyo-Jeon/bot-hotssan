package io.coinpeeker.bot_hotssan.trade.api;


import io.coinpeeker.bot_hotssan.utils.HttpUtils;
import org.apache.commons.codec.binary.Hex;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BittrexTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(Binance.class);

    @Autowired
    HttpUtils httpUtils;

    @Test
    public void AA() throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        Bittrex bittrex = new Bittrex("d88cf2bb52c842c9962b6c00ee425fed", "c0f422f5587d48f39204bb3f4af2612e", httpUtils);

//        BigDecimal myAxisCoinAmount = new BigDecimal(Double.valueOf(bittrex.getBalanceOfCoin("BTC"))).setScale(8, BigDecimal.ROUND_DOWN);
//        BigDecimal selectSatoshi = bittrex.calcBestSellOrderBook(0, bittrex.getOrderBook("BTC-2GIVE", "sell"), myAxisCoinAmount.doubleValue());
//        BigDecimal buyAmount = new BigDecimal((myAxisCoinAmount.doubleValue() / selectSatoshi.doubleValue()) * 0.9).setScale(2, BigDecimal.ROUND_DOWN);
//
//        bittrex.sendOrder("BTC-2GIVE", buyAmount.toString(), selectSatoshi.toString());
//
//        LOGGER.info("Total BTC Amount : " + myAxisCoinAmount.toString());
//        LOGGER.info("Select Satoshi : " + selectSatoshi.toString());
//        LOGGER.info("Buy Amount : " + buyAmount.toString());
    }

    public class Bittrex {

        private final Logger LOGGER = LoggerFactory.getLogger(io.coinpeeker.bot_hotssan.trade.api.Bittrex.class);
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
}

