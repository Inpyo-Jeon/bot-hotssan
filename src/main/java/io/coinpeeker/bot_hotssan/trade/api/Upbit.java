package io.coinpeeker.bot_hotssan.trade.api;

import com.neovisionaries.ws.client.*;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.coinpeeker.bot_hotssan.utils.HttpUtils;
import org.apache.commons.codec.binary.Hex;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class Upbit {

    private static final String SERVER = "wss://api.upbit.com/websocket/v1";
    private static final int TIMEOUT = 1000 * 5;
    private final Logger LOGGER = LoggerFactory.getLogger(Upbit.class);

    String standardUrl = "https://api.upbit.com";
    String accessKey = "";
    String secretKey = "";
    HttpUtils httpUtils;
    List<NameValuePair> header = new ArrayList<>();
    public String streamData = "";

    public Upbit(String accessKey, String secretKey, HttpUtils httpUtils) {
        this.httpUtils = httpUtils;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }

    public String signature(String query) throws JOSEException, ParseException, UnsupportedEncodingException {
        Timestamp nonce = new Timestamp(System.currentTimeMillis());
        JWSSigner signer = new MACSigner(this.secretKey);
        JWTClaimsSet claimsSet;

        if (query == null) {
            claimsSet = new JWTClaimsSet.Builder()
                    .claim("access_key", this.accessKey)
                    .claim("nonce", nonce.getNanos())
                    .build();
        } else {
            claimsSet = new JWTClaimsSet.Builder()
                    .claim("access_key", this.accessKey)
                    .claim("nonce", nonce.getNanos())
                    .claim("query", query)
                    .build();
        }

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
        signedJWT.sign(signer);

        return signedJWT.serialize();
    }

    public Double getAsset(String axisAsset) throws IOException, ParseException, JOSEException {
        String endPoint = "/v1/assets";
        Double balance = 0.0;

        List<NameValuePair> header = new ArrayList<>();
        header.add(new BasicNameValuePair("Authorization", "Bearer " + signature(null)));

        JSONArray jsonArray = httpUtils.getResponseByArrays(this.standardUrl + endPoint, header);

        for (int i = 0; i < jsonArray.length(); i++) {
            if (axisAsset.equals(jsonArray.getJSONObject(i).getString("currency"))) {
                balance = Double.valueOf(jsonArray.getJSONObject(i).getString("balance"));
            }
        }
        return balance;
    }

    public void orderList(String market, String state, int page, String order) throws ParseException, JOSEException, IOException {
        String endPoint = "/v1/orders?";
        String queryString = "market=" + market + "&state=" + state + "&page=" + page + "&order_by=" + order;


        List<NameValuePair> header = new ArrayList<>();
        header.add(new BasicNameValuePair("Authorization", "Bearer " + signature(queryString)));


        LOGGER.info(EntityUtils.toString(httpUtils.get(this.standardUrl + endPoint + queryString, header).getEntity(), "UTF-8"));
    }

    public String excuteOrder(String axisSymbol, String buySymbol, String side, Double volume, Double price, String ord_type) throws ParseException, JOSEException, IOException {
        String endPoint = "/v1/orders?";
        String market = axisSymbol + "-" + buySymbol;
        String queryString = "market=" + market + "&side=" + side + "&volume=" + volume + "&price=" + price + "&ord_type=" + ord_type;


        List<NameValuePair> header = new ArrayList<>();
        header.add(new BasicNameValuePair("Authorization", "Bearer " + signature(queryString)));

        String orderResult = EntityUtils.toString(httpUtils.post(this.standardUrl + endPoint + queryString, header, "upbitTrade").getEntity(), "UTF-8");
        return orderResult;
    }

    public Double calcBestSellOrderBook(int sequence, JSONObject streamData, Double myAxisCoinAmount) {
        JSONArray jsonArray = streamData.getJSONArray("orderbook_units");
        Double totalAmount = 0.0;
        Double selectAskPrice = 0.0;

        for (int i = sequence; i < jsonArray.length(); i++) {
            if (myAxisCoinAmount < totalAmount) {
                break;
            }


            totalAmount += (jsonArray.getJSONObject(i).getDouble("ask_size") * jsonArray.getJSONObject(i).getDouble("ask_price"));
            selectAskPrice = jsonArray.getJSONObject(i).getDouble("ask_price");
        }
        return selectAskPrice;
    }


    public WebSocket connect(String axisSymbol, String buySymbol) throws IOException, WebSocketException {
        LOGGER.info("-- Start Get Data From Upbit --");
        String pair = axisSymbol + "-" + buySymbol;

        return new WebSocketFactory()
                .setConnectionTimeout(TIMEOUT)
                .createSocket(SERVER)
                .addListener(new WebSocketAdapter() {

                    // binary message arrived from the server
                    public void onBinaryMessage(WebSocket websocket, byte[] binary) throws ParseException, JOSEException, IOException {
                        String str = new String(binary);
                        streamData = str;
                    }

                    // A text message arrived from the server.
                    public void onTextMessage(WebSocket websocket, String message) {
                        LOGGER.info("on Text Message : " + message);
                    }
                })
                .addExtension(WebSocketExtension.PERMESSAGE_DEFLATE)
                .connect()
                .sendText("[{\"ticket\":\"hotssan\"},{\"type\":\"orderbook\",\"codes\":[\"" + pair + "\"]}]")
                .disconnect();
    }
}
