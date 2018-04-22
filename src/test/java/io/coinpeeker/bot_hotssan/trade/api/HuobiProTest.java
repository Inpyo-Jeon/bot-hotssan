package io.coinpeeker.bot_hotssan.trade.api;


import io.coinpeeker.bot_hotssan.common.SecretKey;
import io.coinpeeker.bot_hotssan.utils.HttpUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jackson.JsonObjectDeserializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
public class HuobiProTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(Binance.class);

    @Autowired
    HttpUtils httpUtils;

    @Test
    public void AA() throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        HuobiPro huobiPro = new HuobiPro("2550195", "2623e226-ed02dee7-d7d4b5f6-acc97", "34b79120-5b0a1540-8bcee5c2-5f82b", httpUtils);
//        BigDecimal myAxisCoinAmount = new BigDecimal(Double.valueOf(huobiPro.getHaveCoinAmount("BTC")));
//        BigDecimal buyCoinMarketPrice = new BigDecimal(Double.valueOf(huobiPro.getCurrentCoinMarketPrice("EDU", "BTC")));
//        int buyAmount = (int) (myAxisCoinAmount.doubleValue() / buyCoinMarketPrice.doubleValue());
//        System.out.println(buyAmount);

        huobiPro.sendOrder("1.0", "buy-market", "TRX", "BTC");




    }


    public class HuobiPro {

        String basicProtocol = "https://";
        String accountUid = "";
        String apiKey = "";
        String secretKey = "";
        HttpUtils httpUtils;


        public HuobiPro(String accountUid, String apiKey, String secretKey, HttpUtils httpUtils) {
            this.accountUid = accountUid;
            this.httpUtils = httpUtils;
            this.apiKey = apiKey;
            this.secretKey = secretKey;
        }

        public void sendOrder(String amount, String type, String targetSymbol, String axisSymbol) throws NoSuchAlgorithmException, InvalidKeyException, IOException {
            String timeStamp = Instant.ofEpochSecond(Instant.now().getEpochSecond()).atZone(ZoneId.of("Z")).format(DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss"));
            String kind = "POST";
            String domain = "api.huobi.pro";
            String endPoint = "/v1/order/orders/place";

            Map<String, String> params = new HashMap<>();
            params.put("AccessKeyId", apiKey);
            params.put("SignatureVersion", "2");
            params.put("SignatureMethod", "HmacSHA256");
            params.put("Timestamp", timeStamp);
            params.put("account-id", accountUid);
            params.put("amount", amount);
            params.put("symbol", targetSymbol.toLowerCase() + axisSymbol.toLowerCase());
            params.put("type", type);


            List<NameValuePair> params2 = new ArrayList<>();
            params2.add(new BasicNameValuePair("account-id", accountUid));
            params2.add(new BasicNameValuePair("amount", amount));
            params2.add(new BasicNameValuePair("symbol", targetSymbol.toLowerCase() + axisSymbol.toLowerCase()));
            params2.add(new BasicNameValuePair("type", type));

            List<NameValuePair> header = new ArrayList<>();
            header.add(new BasicNameValuePair("Content-Type", "application/x-www-form-urlencoded"));



            String signatureUrl = makeUrlWithSignature(kind, domain, endPoint, secretKey, params);
            JSONObject jsonObject = httpUtils.getPostResponseByObject(signatureUrl, header, params2, "huobiTrade");
            System.out.println(jsonObject);


        }

        public String getHaveCoinAmount(String symbol) throws IOException, InvalidKeyException, NoSuchAlgorithmException {
            String balance = "";
            String timeStamp = Instant.ofEpochSecond(Instant.now().getEpochSecond()).atZone(ZoneId.of("Z")).format(DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss"));
            String kind = "GET";
            String domain = "api.huobi.pro";
            String endPoint = "/v1/account/accounts/" + accountUid + "/balance";

            Map<String, String> params = new HashMap<>();
            params.put("AccessKeyId", apiKey);
            params.put("SignatureVersion", "2");
            params.put("SignatureMethod", "HmacSHA256");
            params.put("account-id", accountUid); // 후오비프로
            params.put("Timestamp", timeStamp);

            String signatureUrl = makeUrlWithSignature(kind, domain, endPoint, secretKey, params);

            JSONObject jsonObject = httpUtils.getResponseByObject(signatureUrl);
            JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("list");

            for (int i = 0; i < jsonArray.length(); i++) {
                if (jsonArray.getJSONObject(i).getString("currency").equals(symbol.toLowerCase()) && jsonArray.getJSONObject(i).getString("type").equals("trade")) {
                    balance = jsonArray.getJSONObject(i).getString("balance");
                    break;
                }
            }
            return balance;
        }

        public void getAccountUid() throws NoSuchAlgorithmException, InvalidKeyException, IOException {
            String timeStamp = Instant.ofEpochSecond(Instant.now().getEpochSecond()).atZone(ZoneId.of("Z")).format(DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss"));

            Map<String, String> params = new HashMap<>();
            params.put("AccessKeyId", apiKey);
            params.put("SignatureVersion", "2");
            params.put("SignatureMethod", "HmacSHA256");
            params.put("Timestamp", timeStamp);

            String signatureUrl = makeUrlWithSignature("GET", "api.huobi.pro", "/v1/account/accounts", secretKey, params);
            JSONObject jsonObject = httpUtils.getResponseByObject(signatureUrl);
            System.out.println(jsonObject);

        }

        public String getCurrentCoinMarketPrice(String targetSymbol, String axisSymbol) throws NoSuchAlgorithmException, InvalidKeyException, IOException {
            String balance = "";
            String timeStamp = Instant.ofEpochSecond(Instant.now().getEpochSecond()).atZone(ZoneId.of("Z")).format(DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss"));
            String kind = "GET";
            String domain = "api.huobi.pro";
            String endPoint = "/market/detail/merged";
            Double close = 0.0;


            Map<String, String> params = new HashMap<>();
            params.put("symbol", targetSymbol.toLowerCase() + axisSymbol.toLowerCase());
            params.put("Timestamp", timeStamp);

            String signatureUrl = makeUrlWithSignature(kind, domain, endPoint, secretKey, params);

            JSONObject jsonObject = httpUtils.getResponseByObject(signatureUrl);
            close = jsonObject.getJSONObject("tick").getDouble("close");

            return balance = String.valueOf(close);
        }

        public String makeUrlWithSignature(String kind, String domain, String endPoint, String secretKey, Map<String, String> params) throws NoSuchAlgorithmException, InvalidKeyException, IOException {
            StringBuilder sb = new StringBuilder();
            sb.append(kind);
            sb.append('\n');
            sb.append(domain);
            sb.append('\n');
            sb.append(endPoint);
            sb.append('\n');



            // build signature:
            SortedMap<String, String> map = new TreeMap<>(params);
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                sb.append(key).append('=').append(urlEncode(value)).append('&');
            }
            // remove last '&':
            sb.deleteCharAt(sb.length() - 1);

            Mac hmacSha256 = null;
            hmacSha256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secKey = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmacSha256.init(secKey);

            System.out.println("sb : " + sb.toString());

            String payload = sb.toString();
            byte[] hash = hmacSha256.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String actualSign = Base64.getEncoder().encodeToString(hash);

            params.put("Signature", actualSign);

            StringBuilder finalURL = new StringBuilder();
            finalURL.append(basicProtocol);
            finalURL.append(domain);
            finalURL.append(endPoint);
            finalURL.append("?");
            finalURL.append(String.join("&", params.entrySet().stream().map((entry) -> {
                return entry.getKey() + "=" + urlEncode(entry.getValue());
            }).collect(Collectors.toList())));

            System.out.println("final : " + finalURL.toString());

            return finalURL.toString();
        }

        public String urlEncode(String string) {
            try {
                return URLEncoder.encode(string, "UTF-8").replaceAll("\\+", "%20");
            } catch (UnsupportedEncodingException e) {
                throw new IllegalArgumentException("UTF-8 encoding not supported!");
            }
        }
    }
}

