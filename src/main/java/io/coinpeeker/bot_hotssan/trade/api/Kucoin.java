package io.coinpeeker.bot_hotssan.trade.api;

import io.coinpeeker.bot_hotssan.common.SecretKey;
import io.coinpeeker.bot_hotssan.utils.HttpUtils;
import org.apache.commons.codec.binary.Hex;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class Kucoin {

    private static final Logger LOGGER = LoggerFactory.getLogger(Binance.class);
    String standardUrl = "https://api.kucoin.com";
    String apiKey = "";
    String secretKey = "";
    HttpUtils httpUtils;
    List<NameValuePair> header = new ArrayList<>();

    public Kucoin(String secretKey, HttpUtils httpUtils) {
        this.httpUtils = httpUtils;
        this.secretKey = secretKey;
    }

    public void getAAA() throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        String endPoint = "";



        String signatureResult = sinature(endPoint);

        request(endPoint, signatureResult);


    }

    public String sinature(String endPoint) throws NoSuchAlgorithmException, InvalidKeyException, IOException {

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        long nonce = timestamp.getTime();

        //splice string for signing
        String strForSign = endPoint + "/" + nonce + "/";

        //Make a base64 encoding of the completed string
        String signatureStr = Base64.getEncoder().encodeToString(strForSign.getBytes("UTF-8"));

        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA256");
        sha256_HMAC.init(secretKeySpec);

        String signatureResult = Hex.encodeHexString(sha256_HMAC.doFinal(signatureStr.getBytes("UTF-8")));

        List<NameValuePair> header = new ArrayList<>();
        header.add(new BasicNameValuePair("KC-api-KEY", apiKey));
        header.add(new BasicNameValuePair("KC-api-NONCE", String.valueOf(nonce)));
        header.add(new BasicNameValuePair("KC-api-SIGNATURE", signatureResult));

        JSONObject jsonObject = httpUtils.getResponseByObject(standardUrl + endPoint, header);
        System.out.println(jsonObject);

        //KC-api-SIGNATURE in header
        return Hex.encodeHexString(sha256_HMAC.doFinal(signatureStr.getBytes("UTF-8")));
    }

    public void request(String endPoint, String signatureResult) throws IOException {

    }


}
