package io.coinpeeker.bot_hotssan.scheduler.listed;

import io.coinpeeker.bot_hotssan.common.CommonConstant;
import io.coinpeeker.bot_hotssan.common.SecretKey;
import io.coinpeeker.bot_hotssan.feature.MarketInfo;
import io.coinpeeker.bot_hotssan.scheduler.Listing;
import io.coinpeeker.bot_hotssan.utils.HttpUtils;
import io.coinpeeker.bot_hotssan.utils.MessageUtils;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringEscapeUtils;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static io.coinpeeker.bot_hotssan.common.CommonConstant.capList;

@Component
public class BittrexListedScheduler implements Listing {
    @Autowired
    MarketInfo marketInfo;

    @Autowired
    HttpUtils httpUtils;

    @Autowired
    Jedis jedis;

    @Autowired
    MessageUtils messageUtils;

    @Value("${property.hotssan_id}")
    private String apiKey;

    @Value("${property.env}")
    private String env;

    private static final Logger LOGGER = LoggerFactory.getLogger(BittrexListedScheduler.class);

    @Override
    @Scheduled(initialDelay = 1000 * 10, fixedDelay = 1000 * 10)
    public void inspectListedCoin() throws IOException, NoSuchAlgorithmException, InvalidKeyException {
//        /** env validation check.**/
//        if (!StringUtils.equals("real", env)) {
//            return;
//        }
//
        List<String> noListedCoinList = new ArrayList<>();

        for (String item : capList) {
            synchronized (jedis) {
                if (!jedis.hexists("BittrexListing", item)) {
                    noListedCoinList.add(item);
                }
            }
        }

        for (String item : noListedCoinList) {

            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            long nonce = timestamp.getTime();
            String endpoint = "https://bittrex.com/api/v1.1/account/getdepositaddress?apikey=" + SecretKey.getApiKeyBittrex() + "&currency=" + item + "&nonce=" + nonce;  // API endpoint


            Mac sha512_HMAC = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(SecretKey.getSecretKeyBittrex().getBytes("UTF-8"), "HmacSHA512");
            sha512_HMAC.init(secretKeySpec);

            //KC-API-SIGNATURE in header
            String signatureResult = Hex.encodeHexString(sha512_HMAC.doFinal(endpoint.getBytes("UTF-8")));

            List<NameValuePair> header = new ArrayList<>();
            header.add(new BasicNameValuePair("apisign", signatureResult));

            JSONObject jsonObject = httpUtils.getResponseByObject(endpoint, header);
            LOGGER.info(jsonObject.toString() + " : " + item);

            if (jsonObject.getBoolean("success") || jsonObject.getString("message").equals("ADDRESS_GENERATING") || jsonObject.getString("message").equals("CURRENCY_OFFLINE")) {
                Date date = new Date();
                StringBuilder messageContent = new StringBuilder();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd, hh:mm:ss a");

                messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
                messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
                messageContent.append(" [ Bittrex ] 상장 정보 - Dev Test - ");
                messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
                messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
                messageContent.append("\n상장 리스트 탐지되었습니다(지갑주소)");
                messageContent.append("\n코인 정보 : ");
                synchronized (jedis) {
                    messageContent.append(jedis.hget("CoinMarketCap", item));
                }
                messageContent.append(" (");
                messageContent.append(item);
                messageContent.append(")");
                messageContent.append("\n구매 가능 거래소");
                messageContent.append(marketInfo.availableMarketList(item));

                String url = CommonConstant.URL_TELEGRAM_BASE + apiKey + CommonConstant.METHOD_TELEGRAM_SENDMESSAGE;
                messageUtils.sendMessage(url, -294606763L, messageContent.toString());

                synchronized (jedis) {
                    jedis.hset("BittrexListing", item, "0");
                }

                LOGGER.info("Bittrex 상장 : " + item + " (" + simpleDateFormat.format(date).toString() + ")");
            }

            try {
                Thread.sleep(1500);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
