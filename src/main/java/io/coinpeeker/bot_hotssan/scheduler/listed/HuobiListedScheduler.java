package io.coinpeeker.bot_hotssan.scheduler.listed;

import com.google.common.collect.Maps;
import com.neovisionaries.ws.client.WebSocketException;
import com.nimbusds.jose.JOSEException;
import io.coinpeeker.bot_hotssan.common.CommonConstant;
import io.coinpeeker.bot_hotssan.common.SecretKey;
import io.coinpeeker.bot_hotssan.feature.MarketInfo;
import io.coinpeeker.bot_hotssan.scheduler.Listing;
import io.coinpeeker.bot_hotssan.trade.TradeAgency;
import io.coinpeeker.bot_hotssan.utils.HttpUtils;
import io.coinpeeker.bot_hotssan.utils.MessageUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class HuobiListedScheduler implements Listing {

    @Autowired
    HttpUtils httpUtils;

    @Autowired
    MessageUtils messageUtils;

    @Autowired
    MarketInfo marketInfo;

    @Autowired
    Jedis jedis;

    @Value("${hotssan_id}")
    private String apiKey;

    @Value("${spring.profiles.active}")
    private String env;

    @Autowired
    private TradeAgency tradeAgency;

    int lastCount = 0;

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(HuobiListedScheduler.class);

    @Override
//    @Scheduled(initialDelay = 1000 * 20, fixedDelay = 1000 * 3)
    public void inspectListedCoin() throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        /** env validation check.**/
        if (!StringUtils.equals("real", env)) {
            return;
        }

        int listingCount = 0;
        String timeStamp = Instant.ofEpochSecond(Instant.now().getEpochSecond()).atZone(ZoneId.of("Z")).format(DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss"));

        StringBuilder sb = new StringBuilder();
        sb.append("GET");
        sb.append('\n');
        sb.append("api.huobi.pro");
        sb.append('\n');
        sb.append("/v1/account/accounts/2610066/balance");
        sb.append('\n');

        Map<String, String> params = new HashMap<>();
        params.put("AccessKeyId", SecretKey.getApiKeyHuobi());
        params.put("SignatureVersion", "2");
        params.put("SignatureMethod", "HmacSHA256");
        params.put("account-id", "2610066"); // 후오비프로
//        params.put("account-id", "105360"); // 후오비코리아
        params.put("Timestamp", timeStamp);

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
        SecretKeySpec secKey = new SecretKeySpec(SecretKey.getSecretKeyHuobi().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        hmacSha256.init(secKey);

        String payload = sb.toString();
        byte[] hash = hmacSha256.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        String actualSign = Base64.getEncoder().encodeToString(hash);

        params.put("Signature", actualSign);

        StringBuilder finalURL = new StringBuilder();
        finalURL.append("https://api.huobi.pro/v1/account/accounts/2610066/balance?");
        finalURL.append(String.join("&", params.entrySet().stream().map((entry) -> {
            return entry.getKey() + "=" + urlEncode(entry.getValue());
        }).collect(Collectors.toList())));


        JSONObject jsonObject = httpUtils.getResponseByObject(finalURL.toString());

        int currentCount = jsonObject.getJSONObject("data").getJSONArray("list").length();

        synchronized (jedis) {
            listingCount = Math.toIntExact(jedis.hlen("L-HuobiPro"));
        }

        if ((listingCount * 2) != currentCount) {
            JSONArray array = jsonObject.getJSONObject("data").getJSONArray("list");

            HashMap<String, Integer> deDuplicationListMap = Maps.newHashMap();

            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);
                deDuplicationListMap.put(item.getString("currency"), 0);
            }

            for (String item : deDuplicationListMap.keySet()) {
                boolean isExist = true;

                synchronized (jedis) {
                    if (!jedis.hexists("L-HuobiPro", item.toUpperCase())) {
                        isExist = false;
                    }
                }

                if (!isExist) {
                    Map<String, Map<String, String>> marketList = marketInfo.availableMarketList(item.toUpperCase());
//                    tradeAgency.list("Huobi", item.toUpperCase(), marketList);

                    Date nowDate = new Date();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss (z Z)");
                    simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

                    StringBuilder messageContent = new StringBuilder();
                    messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
                    messageContent.append(" [ Huobi-Pro ] 상장 정보 ");
                    messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
                    messageContent.append("\n");
                    messageContent.append(simpleDateFormat.format(nowDate));
                    messageContent.append("\n확인방법 : List");
                    messageContent.append("\n코인정보 : ");

                    synchronized (jedis) {
                        messageContent.append(jedis.hget("I-CoinMarketCap", item.toUpperCase()));
                    }

                    messageContent.append(" (");
                    messageContent.append(item.toUpperCase());
                    messageContent.append(")");
                    messageContent.append("\n구매가능 거래소 : ");
                    messageContent.append(marketInfo.marketInfo(marketList));

                    String url = CommonConstant.URL_TELEGRAM_BASE + apiKey + CommonConstant.METHOD_TELEGRAM_SENDMESSAGE;
                    messageUtils.sendMessage(url, -300048567L, messageContent.toString());
                    messageUtils.sendMessage(url, -319177275L, messageContent.toString());

                    LOGGER.info(messageContent.toString());

                    synchronized (jedis) {
                        jedis.hset("L-HuobiPro", item.toUpperCase(), "1");
                    }
                }
            }
        }
    }

    @Scheduled(initialDelay = 1000 * 20, fixedDelay = 1000 * 5)
    public void huobiKorAssetInfo() throws IOException, InvalidKeyException, NoSuchAlgorithmException {
        if (!StringUtils.equals("real", env)) {
            return;
        }

        String checkUrl = "https://www.huobi.com/p/api/contents/pro/single_page?lang=ko-kr&pageType=1";

        JSONObject jsonObject = httpUtils.getResponseByObject(checkUrl);
        JSONArray jsonArray = jsonObject.getJSONArray("data");
        int currentCount = 0;

        synchronized (jedis) {
            currentCount = Math.toIntExact(jedis.hlen("L-Huobi-Kor-Asset"));
        }

        if (jsonArray.length() != currentCount) {
            for (int i = 0; i < jsonArray.length(); i++) {
                boolean isExist = true;

                synchronized (jedis) {
                    if (!jedis.hexists("L-Huobi-Kor-Asset", jsonArray.getJSONObject(i).getString("pageIdentifier"))) {
                        isExist = false;
                    }
                }

                if (!isExist) {
                    String symbol = jsonArray.getJSONObject(i).getString("pageIdentifier").toUpperCase();
                    Map<String, Map<String, String>> marketList = marketInfo.availableMarketList(symbol);
//                    tradeAgency.list("Huobi", symbol, marketList);

                    sendMessage("Huobi(Kor)", symbol, marketList, "");

                    synchronized (jedis) {
                        jedis.hset("L-Huobi-Kor-Asset", jsonArray.getJSONObject(i).getString("pageIdentifier"), jsonArray.getJSONObject(i).getString("title"));
                    }
                }
            }
        }
    }

    @Scheduled(initialDelay = 1000 * 20, fixedDelay = 1000 * 2)
    public void huobiEngAssetInfo() throws IOException, InvalidKeyException, NoSuchAlgorithmException, ParseException, JOSEException, WebSocketException {
        if (!StringUtils.equals("real", env)) {
            return;
        }

        String checkUrl = "https://www.huobi.com/p/api/contents/pro/single_page?lang=en-us&pageType=1";

        JSONObject jsonObject = httpUtils.getResponseByObject(checkUrl);
        JSONArray jsonArray = jsonObject.getJSONArray("data");
        int currentCount = 0;

        synchronized (jedis) {
            currentCount = Math.toIntExact(jedis.hlen("L-Huobi-Eng-Asset"));
        }

        if (jsonArray.length() != currentCount) {
            for (int i = 0; i < jsonArray.length(); i++) {
                boolean isExist = true;

                synchronized (jedis) {
                    if (!jedis.hexists("L-Huobi-Eng-Asset", jsonArray.getJSONObject(i).getString("pageIdentifier"))) {
                        isExist = false;
                    }
                }

                if (!isExist) {
                    String symbol = jsonArray.getJSONObject(i).getString("pageIdentifier").toUpperCase();
                    Map<String, Map<String, String>> marketList = marketInfo.availableMarketList(symbol);
                    String orderResult = tradeAgency.list("Huobi", symbol, marketList);

                    sendMessage("Huobi(Pro/Hadax)", symbol, marketList, orderResult);

                    synchronized (jedis) {
                        jedis.hset("L-Huobi-Eng-Asset", jsonArray.getJSONObject(i).getString("pageIdentifier"), jsonArray.getJSONObject(i).getString("title"));
                    }
                }
            }
        }
    }


    public void sendMessage(String exchangeType, String symbol, Map<String, Map<String,String>> marketList, String orderResult) {
        Date nowDate = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss (z Z)");
        StringBuilder messageContent = new StringBuilder();
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

        messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
        messageContent.append("[ ");
        messageContent.append(exchangeType);
        messageContent.append(" ] ");
        messageContent.append("상장정보");
        messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
        messageContent.append("\n");

        if ("Huobi(Kor)".equals(exchangeType)) {
            messageContent.append("- Test 단계 -");
            messageContent.append("\n");
            messageContent.append("- 당분간 Huobi(Kor)는 매수 금지 -");
            messageContent.append("\n");
        }

        messageContent.append(simpleDateFormat.format(nowDate));
        messageContent.append("\n코인정보 : ");

        synchronized (jedis) {
            messageContent.append(jedis.hget("I-CoinMarketCap", symbol));
        }

        messageContent.append(" (");
        messageContent.append(symbol);
        messageContent.append(")");
        messageContent.append("\n구매가능 거래소 : ");
        messageContent.append(marketInfo.marketInfo(marketList));
        messageContent.append(orderResult);

        LOGGER.info(messageContent.toString());

        String url = CommonConstant.URL_TELEGRAM_BASE + apiKey + CommonConstant.METHOD_TELEGRAM_SENDMESSAGE;
        messageUtils.sendMessage(url, -300048567L, messageContent.toString());
        messageUtils.sendMessage(url, -319177275L, messageContent.toString());

    }

    public String urlEncode(String string) {
        try {
            return URLEncoder.encode(string, "UTF-8").replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("UTF-8 encoding not supported!");
        }
    }
}

