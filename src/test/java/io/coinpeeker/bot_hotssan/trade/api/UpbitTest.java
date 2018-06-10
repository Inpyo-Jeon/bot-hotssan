package io.coinpeeker.bot_hotssan.trade.api;

import com.google.common.collect.Maps;
import com.neovisionaries.ws.client.*;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sun.org.glassfish.gmbal.NameValue;
import io.coinpeeker.bot_hotssan.feature.MarketInfo;
import io.coinpeeker.bot_hotssan.utils.HttpUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.yaml.snakeyaml.util.UriEncoder;

import javax.swing.text.html.parser.Entity;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
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

    @Autowired
    MarketInfo marketInfo;


    private static final String SERVER = "wss://api-beta.upbit.com/websocket/v1";
    private static final int TIMEOUT = 1000 * 5;


    @Test
    public void AA() throws ParseException, JOSEException, IOException, URISyntaxException, WebSocketException, InterruptedException {
//        Upbit upbit = new Upbit("NzH0lJvdHynCsH61TKf6bSNMdCjF6aKJTgWNcmyP", "gL9xrMTAnj9sQrDF9JU2Yv9NpYzibJMlq2YGXT0q", httpUtils);
//        String streamData = "";
//        Double myAxisAmount = upbit.getAsset("KRW");
//
//        // Connect to the echo server.
//        WebSocket upbitWebSocket = upbit.connect("KRW", "TRX");
//        while (upbit.streamData == "") {
//        }
//        streamData = upbit.streamData;
//
//        Double selectAskPrice = upbit.calcBestSellOrderBook(5, new JSONObject(streamData), myAxisAmount);
//        Double buyAmount = (myAxisAmount / selectAskPrice) * (0.9005);
//        upbit.excuteOrder("KRW", "TRX", "bid", buyAmount, selectAskPrice, "limit");
//        LOGGER.info("호가상 선택된 가격 : " + selectAskPrice);
//        LOGGER.info("주문 량(수수료 포함) : " + buyAmount);
    }


    public static class Upbit {

        private final Logger LOGGER = LoggerFactory.getLogger(io.coinpeeker.bot_hotssan.trade.api.Upbit.class);
        String standardUrl = "https://api-beta.upbit.com";
        String accessKey = "";
        String secretKey = "";
        HttpUtils httpUtils;
        List<NameValuePair> header = new ArrayList<>();
        String streamData = "";


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


//            JSONArray jsonArray = httpUtils.getResponseByArrays(this.standardUrl + endPoint + queryString, header);
            System.out.println(EntityUtils.toString(httpUtils.get(this.standardUrl + endPoint + queryString, header).getEntity(), "UTF-8"));


        }

        public void excuteOrder(String axisSymbol, String buySymbol, String side, Double volume, Double price, String ord_type) throws ParseException, JOSEException, IOException {
            String endPoint = "/v1/orders?";
            String market = axisSymbol + "-" + buySymbol;
            String queryString = "market=" + market + "&side=" + side + "&volume=" + volume + "&price=" + price + "&ord_type=" + ord_type;


            List<NameValuePair> header = new ArrayList<>();
            header.add(new BasicNameValuePair("Authorization", "Bearer " + signature(queryString)));


//            JSONArray jsonArray = httpUtils.getResponseByArrays(this.standardUrl + endPoint + queryString, header);
            LOGGER.info(EntityUtils.toString(httpUtils.post(this.standardUrl + endPoint + queryString, header, "upbitTrade").getEntity(), "UTF-8"));


        }

        public Double calcBestSellOrderBook(int sequence, JSONObject streamData, Double myAxisCoinAmount) {
            JSONArray jsonArray = streamData.getJSONArray("orderbook_units");
            Double totalAmount = 0.0;
            Double selectAskPrice = 0.0;
            System.out.println(jsonArray.toString());

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
            System.out.println("Start get data from upbit");
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
                            System.out.println(message);
                        }
                    })
                    .addExtension(WebSocketExtension.PERMESSAGE_DEFLATE)
                    .connect()
                    .sendText("[{\"ticket\":\"gazuatest\"},{\"type\":\"orderbook\",\"codes\":[\"" + pair + "\"]}]")
                    .disconnect();


        }
    }
}
