package io.coinpeeker.bot_hotssan.trade.api;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.coinpeeker.bot_hotssan.utils.HttpUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_10;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.swing.text.html.parser.Entity;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UpbitTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(Upbit.class);

    @Autowired
    HttpUtils httpUtils;


    @Test
    public void AA() throws ParseException, JOSEException, IOException, URISyntaxException, WebSocketException {
        Upbit upbit = new Upbit("NzH0lJvdHynCsH61TKf6bSNMdCjF6aKJTgWNcmyP", "gL9xrMTAnj9sQrDF9JU2Yv9NpYzibJMlq2YGXT0q", httpUtils);
//        System.out.println(upbit.getAsset("KRW"));
//        upbit.orderList("KRW-BTC", "done", 1, "asc");


        org.json.simple.JSONObject first = new org.json.simple.JSONObject();
        first.put("ticket", "test");
        org.json.simple.JSONObject second = new org.json.simple.JSONObject();
        org.json.simple.JSONArray arrayFirst = new org.json.simple.JSONArray();
        arrayFirst.add(0,"KRW-BTC");
        second.put("type", "ticker");
        second.put("codes", arrayFirst);

        org.json.simple.JSONArray finalArray = new org.json.simple.JSONArray();
        finalArray.add(0, first);
        finalArray.add(1, second);
        System.out.println(finalArray);

        new WebSocketFactory()
                .createSocket("wss://api-beta.upbit.com/websocket/v1", 5000)
                .
                .addListener(new WebSocketAdapter() {
                    @Override
                    public void onTextMessage(WebSocket ws, String message) {
                        // Received a response. Print the received message.
                        System.out.println(message);

                        // Close the WebSocket connection.
                        ws.disconnect();
                    }
                })
                .connect()
                .sendText(finalArray.toString());




    }


    public class Upbit {

        private final Logger LOGGER = LoggerFactory.getLogger(io.coinpeeker.bot_hotssan.trade.api.Upbit.class);
        String standardUrl = "https://api-beta.upbit.com";
        String accessKey = "";
        String secretKey = "";
        HttpUtils httpUtils;
        List<NameValuePair> header = new ArrayList<>();


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
                System.out.println("AAAA");
                System.out.println(query);
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
            String endPoint = "/v1/orders";
            String queryString = "?market=" + market + "&state=" + state + "&page=" + page;

            JSONObject queryJSON = new JSONObject();
            queryJSON.put("market", market);
            queryJSON.put("market", market);
            queryJSON.put("state", state);
            queryJSON.put("page", page);
            queryJSON.put("order", order);

            System.out.println(queryJSON.toString());


            List<NameValuePair> header = new ArrayList<>();
            header.add(new BasicNameValuePair("Authorization", "Bearer " + signature(queryJSON.toString())));

            httpUtils.get(this.standardUrl + endPoint + queryString, header);


//            JSONArray jsonArray = httpUtils.getResponseByArrays(this.standardUrl + endPoint + queryString, header);
            System.out.println(EntityUtils.toString(httpUtils.get(this.standardUrl + endPoint + queryString, header).getEntity(), "UTF-8"));


        }
    }
}
