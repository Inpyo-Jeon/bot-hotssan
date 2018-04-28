package io.coinpeeker.bot_hotssan.trade.api;

import io.coinpeeker.bot_hotssan.trade.BuyTrade;
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
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class KucoinTest {

    @Autowired
    HttpUtils httpUtils;

    @Autowired
    BuyTrade buyTrade;

    @Test
    public void Test() throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        LOGGER.info("-- Kucoin 자동 매수 시작 --");
        buyTrade.orderKucoin("BTC", "SNC");
        LOGGER.info("-- Kucoin 자동 매수 종료 --");

    }

//    public void kucoinTest() throws NoSuchAlgorithmException, InvalidKeyException, IOException {
//        Kucoin kucoin = new Kucoin("5ae43b72a57c577d638b807a", "5416d149-136c-447e-9fda-e0a0b1946ad7", httpUtils);
//        String buyCoinSymbol = buyCoin + "-" + axisCoin;
//
//        BigDecimal myAxisCoinAmount = new BigDecimal(Double.valueOf(kucoin.getBalanceOfCoin(axisCoin))).setScale(8, BigDecimal.ROUND_DOWN);
//        BigDecimal selectSatoshi = kucoin.calcBestSellOrderBook(2, kucoin.getSellOrderBooks(buyCoinSymbol, "50"), myAxisCoinAmount.doubleValue());
//        BigDecimal buyAmount = new BigDecimal((myAxisCoinAmount.doubleValue() / selectSatoshi.doubleValue()) * 0.9).setScale(2, BigDecimal.ROUND_DOWN);
//
//        kucoin.requestOrder(buyCoinSymbol, "BUY", selectSatoshi.toString(), buyAmount.toString());
//
//        LOGGER.info("Total BTC Amount : " + myAxisCoinAmount.toString());
//        LOGGER.info("Select Satoshi : " + selectSatoshi.toString());
//        LOGGER.info("Buy Amount : " + buyAmount.toString());
//    }


    private static final Logger LOGGER = LoggerFactory.getLogger(Binance.class);

    class KucoinModule {
        String standardUrl = "https://api.kucoin.com";
        String apiKey = "";
        String secretKey = "";
        HttpUtils httpUtils;

        public KucoinModule(String apiKey, String secretKey, HttpUtils httpUtils) {
            this.apiKey = apiKey;
            this.secretKey = secretKey;
            this.httpUtils = httpUtils;
        }

        public BigDecimal calcBestSellOrderBook(int sequence, JSONObject sellOrderBook, Double myAxisCoinAmount) {
            JSONArray dataArray = sellOrderBook.getJSONArray("data");
            Double totalBtcAmount = 0.0;
            Double selectSatoshi = 0.0;


            for (int i = sequence; i < dataArray.length(); i++) {

                if (myAxisCoinAmount < totalBtcAmount) {
                    break;
                }

                String replaceData = dataArray.getJSONArray(i).toString().replace("[", "").replace("]", "");
                String[] splitData = replaceData.split(",");
                String satoshi = splitData[0];
                String sellBtcAmount = splitData[2];

                totalBtcAmount += Double.parseDouble(sellBtcAmount);
                selectSatoshi = Double.parseDouble(satoshi);
            }
            return new BigDecimal(Double.valueOf(selectSatoshi)).setScale(8, BigDecimal.ROUND_DOWN);

        }

        public void requestOrder(String buyCoin, String type, String price, String amount) throws NoSuchAlgorithmException, InvalidKeyException, IOException {
            String endPoint = "/v1/order";

            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            long nonce = timestamp.getTime();

            StringBuilder queryString = new StringBuilder();
            queryString.append("amount=");
            queryString.append(amount);
            queryString.append("&price=");
            queryString.append(price);
            queryString.append("&symbol=");
            queryString.append(buyCoin);
            queryString.append("&type=");
            queryString.append(type);


            String strForSign = endPoint + "/" + nonce + "/" + queryString.toString();
            String signatureResult = signature(strForSign);

            endPoint += "?amount=" + amount;
            endPoint += "&price=" + price;
            endPoint += "&symbol=" + buyCoin;
            endPoint += "&type=" + type;


            requestPostHeader(endPoint, nonce, signatureResult);
        }

        public JSONObject getSellOrderBooks(String buySymbol, String limit) throws NoSuchAlgorithmException, InvalidKeyException, IOException {
            String endPoint = "/v1/open/orders-sell";
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            long nonce = timestamp.getTime();

            String strForSign = endPoint + "/" + nonce + "/";
            String signatureResult = signature(strForSign);

            endPoint += "?symbol=" + buySymbol;
            endPoint += "&limit=" + limit;

            JSONObject jsonObject = requestGetHeader(endPoint, nonce, signatureResult);

            return jsonObject;
        }

        public String getBalanceOfCoin(String symbol) throws NoSuchAlgorithmException, InvalidKeyException, IOException {
            String endPoint = "/v1/account/" + symbol + "/balance";
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            long nonce = timestamp.getTime();

            String strForSign = endPoint + "/" + nonce + "/";
            String signatureResult = signature(strForSign);

            JSONObject jsonObject = requestGetHeader(endPoint, nonce, signatureResult);

            return jsonObject.getJSONObject("data").getString("balanceStr");
        }

        public String getCoinList(String limit) throws NoSuchAlgorithmException, InvalidKeyException, IOException {
            String endPoint = "/v1/account/balances";
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            long nonce = timestamp.getTime();

            StringBuilder queryString = new StringBuilder();
            queryString.append("limit=");
            queryString.append(limit);

            String strForSign = endPoint + "/" + nonce + "/" + queryString.toString();
            String signatureResult = signature(strForSign);

            endPoint += "?limit=" + limit;

            JSONObject jsonObject = requestGetHeader(endPoint, nonce, signatureResult);

            return String.valueOf(jsonObject);


        }

        public String getTick(String selectCoin) throws NoSuchAlgorithmException, InvalidKeyException, IOException {
            String endPoint = "/v1/open/tick";
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            long nonce = timestamp.getTime();

            String strForSign = endPoint + "/" + nonce + "/";
            String signatureResult = signature(strForSign);

            endPoint += "?symbol=" + selectCoin;

            JSONObject jsonObject = requestGetHeader(endPoint, nonce, signatureResult);

            return String.valueOf(jsonObject.getJSONObject("data").getDouble("lastDealPrice"));


        }

        public String signature(String strForSign) throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
            String signatureStr = Base64.getEncoder().encodeToString(strForSign.getBytes("UTF-8"));

            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA256");
            sha256_HMAC.init(secretKeySpec);

            return Hex.encodeHexString(sha256_HMAC.doFinal(signatureStr.getBytes("UTF-8")));
        }

        public JSONObject requestGetHeader(String endPoint, long nonce, String signatureResult) throws IOException {
            List<NameValuePair> header = new ArrayList<>();
            header.add(new BasicNameValuePair("KC-API-KEY", apiKey));
            header.add(new BasicNameValuePair("KC-API-NONCE", String.valueOf(nonce)));
            header.add(new BasicNameValuePair("KC-API-SIGNATURE", signatureResult));

            return httpUtils.getResponseByObject(standardUrl + endPoint, header);
        }

        public JSONObject requestPostHeader(String endPoint, long nonce, String signatureResult) throws IOException {
            List<NameValuePair> header = new ArrayList<>();
            header.add(new BasicNameValuePair("Content-Type", "x-www-form-urlencoded"));
            header.add(new BasicNameValuePair("KC-API-KEY", apiKey));
            header.add(new BasicNameValuePair("KC-API-NONCE", String.valueOf(nonce)));
            header.add(new BasicNameValuePair("KC-API-SIGNATURE", signatureResult));

            JSONObject jsonObject = httpUtils.getPostResponseByObject(standardUrl + endPoint, header, "kucoinTrade");
            LOGGER.info(jsonObject.toString());

            return jsonObject;
        }

    }


}


