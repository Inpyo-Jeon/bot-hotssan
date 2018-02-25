package io.coinpeeker.bot_hotssan.scheduler.listed;

import io.coinpeeker.bot_hotssan.common.CommonConstant;
import io.coinpeeker.bot_hotssan.common.SecretKey;
import io.coinpeeker.bot_hotssan.feature.MarketInfo;
import io.coinpeeker.bot_hotssan.scheduler.Listing;
import io.coinpeeker.bot_hotssan.utils.HttpUtils;
import io.coinpeeker.bot_hotssan.utils.MessageUtils;
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
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static io.coinpeeker.bot_hotssan.common.CommonConstant.capList;

@Component
public class BinanceListedScheduler implements Listing {
    @Autowired
    MarketInfo marketInfo;

    @Autowired
    HttpUtils httpUtils;

    @Autowired
    MessageUtils messageUtils;

    @Autowired
    private Jedis jedis;

    @Value("${property.hotssan_id}")
    private String apiKey;

    @Value("${property.env}")
    private String env;

    private static final Logger LOGGER = LoggerFactory.getLogger(BinanceListedScheduler.class);

    @Override
    @Scheduled(initialDelay = 1000 * 10, fixedDelay = 1000 * 5)
    public void inspectListedCoin() throws IOException {
//        /** env validation check.**/
//        if (!StringUtils.equals("real", env)) {
//            return;
//        }

        List<NameValuePair> header = new ArrayList<>();
        header.add(new BasicNameValuePair(SecretKey.getHeaderKeyBinance(), SecretKey.getHeaderValueBinance()));

        List<String> noListedCoinList = new ArrayList<>();

        for (String item : capList) {
            synchronized (jedis) {
                if (!jedis.hexists("BinanceListing", item)) {
                    noListedCoinList.add(item);
                }
            }
        }

        for (String item : noListedCoinList) {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            StringBuilder sb = new StringBuilder();
            sb.append("https://api.binance.com/wapi/v3/depositAddress.html?");
            String queryString = "asset=" + item + "&recvWindow=5000&timestamp=" + timestamp.getTime();
            sb.append(queryString);

            try {
                sb.append("&signature=");
                Mac mac = Mac.getInstance("HMACSHA256");
                mac.init(new SecretKeySpec(SecretKey.getSecretKeyBinance().getBytes(), "HMACSHA256"));

                for (byte byteItem : mac.doFinal(queryString.getBytes())) {
                    sb.append(Integer.toString((byteItem & 0xFF) + 0x100, 16).substring(1));
                }

                JSONObject jsonObject = httpUtils.getResponseByObject(sb.toString(), header);

                if (jsonObject.getBoolean("success")) {
                    Date date = new Date();
                    StringBuilder messageContent = new StringBuilder();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd, hh:mm:ss a");

                    messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
                    messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
                    messageContent.append(" [ Binance ] 상장 정보 ");
                    messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
                    messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
                    messageContent.append("\n상장 리스트 탐지되었습니다(지갑주소)");
                    messageContent.append("\n확인시간 : ");
                    messageContent.append(simpleDateFormat.format(date).toString());
                    messageContent.append("\n코인 정보 : ");
                    synchronized (jedis) {
                        messageContent.append(jedis.hget("CoinMarketCap", item));
                    }
                    messageContent.append(" (");
                    messageContent.append(item);
                    messageContent.append(")");
                    messageContent.append("\n지갑주소 : ");
                    messageContent.append(jsonObject.getString("address"));
                    messageContent.append("\n구매 가능 거래소");
                    messageContent.append(marketInfo.availableMarketList(item));


                    String url = CommonConstant.URL_TELEGRAM_BASE + apiKey + CommonConstant.METHOD_TELEGRAM_SENDMESSAGE;
                    messageUtils.sendMessage(url, -294606763L, messageContent.toString());
//                            messageUtils.sendMessage(url, -300048567L, messageContent.toString());
//                            messageUtils.sendMessage(url, -277619118L, messageContent.toString());

                    synchronized (jedis) {
                        jedis.hset("BinanceListing", item, "0");
                    }

                    LOGGER.info("Binance 상장 : " + item + " (" + simpleDateFormat.format(date).toString() + ")");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            sb.setLength(0);
        }
    }
}