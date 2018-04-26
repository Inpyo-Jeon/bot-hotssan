package io.coinpeeker.bot_hotssan.trade.api;

import io.coinpeeker.bot_hotssan.utils.HttpUtils;
import org.apache.commons.codec.binary.Hex;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
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

    @Test
    public void kucoinTest() throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        KucoinModule kucoinModule = new KucoinModule("5adf48b73f705c289cc5753d", "97aa6445-f7ea-465b-86f6-ccd9c48461c0", httpUtils);

//        BigDecimal myAxisCoinAmount = new BigDecimal(Double.valueOf(kucoinModule.getBalanceOfCoin("BTC"))).setScale(8, BigDecimal.ROUND_DOWN);
//        BigDecimal buyCoinMarketPrice = new BigDecimal(Double.valueOf(kucoinModule.getTick("KCS", "BTC"))).setScale(8, BigDecimal.ROUND_DOWN);
//        System.out.println(myAxisCoinAmount);
//        System.out.println(buyCoinMarketPrice);
//        int buyAmount = (int) (myAxisCoinAmount.doubleValue() / buyCoinMarketPrice.doubleValue());
//        System.out.println(buyAmount);

        System.out.println(kucoinModule.getCoinList());

    }


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

        public String getBalanceOfCoin(String symbol) throws NoSuchAlgorithmException, InvalidKeyException, IOException {
            String endPoint = "/v1/account/" + symbol + "/balance";
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            long nonce = timestamp.getTime();

            String strForSign = endPoint + "/" + nonce + "/";
            String signatureResult = signature(strForSign);

            JSONObject jsonObject = requestGetHeader(endPoint, nonce, signatureResult);

            return jsonObject.getJSONObject("data").getString("balanceStr");
        }

        public String getCoinList() throws NoSuchAlgorithmException, InvalidKeyException, IOException {
            String endPoint = "/v1/account/balances";
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            long nonce = timestamp.getTime();

            String strForSign = endPoint + "/" + nonce + "/";
            String signatureResult = signature(strForSign);

            endPoint += "?limit=20&page=1";

            JSONObject jsonObject = requestGetHeader(endPoint, nonce, signatureResult);
            //            System.out.println(jsonObject.getJSONObject("data").getDouble("buy"));

            return String.valueOf(jsonObject);


        }

        public String getTick(String targetSymbol, String axisSymbol) throws NoSuchAlgorithmException, InvalidKeyException, IOException {
            String endPoint = "/v1/open/tick";
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            long nonce = timestamp.getTime();

            String strForSign = endPoint + "/" + nonce + "/";
            String signatureResult = signature(strForSign);

            endPoint += "?symbol=" + targetSymbol + "-" + axisSymbol;

            JSONObject jsonObject = requestGetParam(endPoint, nonce, signatureResult);
            //            System.out.println(jsonObject.getJSONObject("data").getDouble("buy"));

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
            header.add(new BasicNameValuePair("KC-api-KEY", apiKey));
            header.add(new BasicNameValuePair("KC-api-NONCE", String.valueOf(nonce)));
            header.add(new BasicNameValuePair("KC-api-SIGNATURE", signatureResult));

            return httpUtils.getResponseByObject(standardUrl + endPoint, header);
        }

        public JSONObject requestGetParam(String endPoint, long nonce, String signatureResult) throws IOException {

            return httpUtils.getResponseByObject(standardUrl + endPoint);
        }
    }


}


