package io.coinpeeker.bot_hotssan.scheduler.listed;

import io.coinpeeker.bot_hotssan.common.CommonConstant;
import io.coinpeeker.bot_hotssan.common.SecretKey;
import io.coinpeeker.bot_hotssan.feature.MarketInfo;
import io.coinpeeker.bot_hotssan.scheduler.Listing;
import io.coinpeeker.bot_hotssan.trade.TradeAgency;
import io.coinpeeker.bot_hotssan.utils.HttpUtils;
import io.coinpeeker.bot_hotssan.utils.MessageUtils;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class KucoinListedScheduler implements Listing {
    @Autowired
    MarketInfo marketInfo;

    @Autowired
    HttpUtils httpUtils;

    @Autowired
    MessageUtils messageUtils;

    @Autowired
    Jedis jedis;

    @Value("${property.hotssan_id}")
    private String apiKey;

    @Value("${property.env}")
    private String env;

    @Autowired
    private TradeAgency tradeAgency;

    private static final Logger LOGGER = LoggerFactory.getLogger(KucoinListedScheduler.class);

    @Override
    @Scheduled(initialDelay = 1000 * 35, fixedDelay = 1000 * 10)
    public void inspectListedCoin() throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        /** env validation check.**/
        if (!StringUtils.equals("real", env)) {
            return;
        }
        LOGGER.info("Kucoin 시작");

        List<String> noListedCoinList = new ArrayList<>();
        List<String> capList = new ArrayList<>();
        capList.addAll(CommonConstant.getCapList());


        for (String item : capList) {
            synchronized (jedis) {
                if (!jedis.hexists("L-Kucoin", item)) {
                    noListedCoinList.add(item);
                }
            }
        }

        for (String item : noListedCoinList) {

            String host = "https://api.kucoin.com";
            String endpoint = "/v1/account/" + item + "/wallet/address";  // API endpoint
            String secret = SecretKey.getSecretKeyKucoin(); //The secret assigned when the API created

            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            long nonce = timestamp.getTime();

            //splice string for signing
            String strForSign = endpoint + "/" + nonce + "/";

            //Make a base64 encoding of the completed string
            String signatureStr = Base64.getEncoder().encodeToString(strForSign.getBytes("UTF-8"));

            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA256");
            sha256_HMAC.init(secretKeySpec);

            //KC-API-SIGNATURE in header
            String signatureResult = Hex.encodeHexString(sha256_HMAC.doFinal(signatureStr.getBytes("UTF-8")));

            List<NameValuePair> header = new ArrayList<>();
            header.add(new BasicNameValuePair("KC-API-KEY", SecretKey.getApiKeyKucoin()));
            header.add(new BasicNameValuePair("KC-API-NONCE", String.valueOf(nonce)));
            header.add(new BasicNameValuePair("KC-API-SIGNATURE", signatureResult));

            JSONObject jsonObject = httpUtils.getResponseByObject(host + endpoint, header);

            if (jsonObject.has("ContentLengthZero")) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }

            if (jsonObject.has("data") && !jsonObject.get("data").equals("null")) {
                Map<String, List<String>> marketList = marketInfo.availableMarketList(item);
                tradeAgency.list("Kucoin", item, marketList);

                Date nowDate = new Date();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss (z Z)");
                simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
                StringBuilder messageContent = new StringBuilder();

                messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
                messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
                messageContent.append(" [ Kucoin ] 상장 정보 ");
                messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
                messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
                messageContent.append("\n");
                messageContent.append(simpleDateFormat.format(nowDate));
                messageContent.append("\n확인방법 : Address");
                messageContent.append("\n코인정보 : ");

                synchronized (jedis) {
                    messageContent.append(jedis.hget("I-CoinMarketCap", item));
                }

                messageContent.append(" (");
                messageContent.append(item);
                messageContent.append(")");
                messageContent.append("\n구매가능 거래소 : ");
                messageContent.append(marketInfo.marketInfo(marketList));


                String url = CommonConstant.URL_TELEGRAM_BASE + apiKey + CommonConstant.METHOD_TELEGRAM_SENDMESSAGE;
                messageUtils.sendMessage(url, -300048567L, messageContent.toString());
                messageUtils.sendMessage(url, -319177275L, messageContent.toString());

                LOGGER.info(messageContent.toString());

                synchronized (jedis) {
                    jedis.hset("L-Kucoin", item, "0");
                }

            } else if ((jsonObject.has("error") && jsonObject.getString("error").equals("Not Found"))) {

            } else {
                LOGGER.info(" [ Kucoin ] 상장 정보 이상발생");
                LOGGER.info("코인 정보 : " + item);
                LOGGER.info("에러내용 : " + jsonObject.toString());
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
