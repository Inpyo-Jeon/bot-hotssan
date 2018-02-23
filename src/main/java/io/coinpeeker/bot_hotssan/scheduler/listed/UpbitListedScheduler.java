package io.coinpeeker.bot_hotssan.scheduler.listed;

import io.coinpeeker.bot_hotssan.common.CommonConstant;
import io.coinpeeker.bot_hotssan.feature.MarketInfo;
import io.coinpeeker.bot_hotssan.scheduler.CoinMarketCapScheduler;
import io.coinpeeker.bot_hotssan.scheduler.Listing;
import io.coinpeeker.bot_hotssan.utils.HttpUtils;
import io.coinpeeker.bot_hotssan.utils.MessageUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import javax.annotation.Resource;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class UpbitListedScheduler implements Listing {
    @Autowired
    MarketInfo marketInfo;

    @Autowired
    CoinMarketCapScheduler coinMarketCapScheduler;

    @Autowired
    MessageUtils messageUtils;

    @Autowired
    HttpUtils httpUtils;

    @Value("${property.hotssan_id}")
    private String apiKey;

    @Value("${property.env}")
    private String env;

//    @Resource(name = "redisTemplate")
//    private HashOperations<String, String, String> hashOperations;

    @Autowired
    private Jedis jedis;

    private static final Logger LOGGER = LoggerFactory.getLogger(UpbitListedScheduler.class);
    private int count = 1;

    @Override
    public void init() throws IOException {
//        if (hashOperations.keys("CoinMarketCap").isEmpty()) {
        if (jedis.hkeys("CoinMarketCap").isEmpty()) {
            LOGGER.info("@#@#@# CoinMarketCap Listing is null");
            coinMarketCapScheduler.refreshCoinData();
        }

//        if (hashOperations.keys("UpbitListing").isEmpty()) {
        if (jedis.hkeys("UpbitListing").isEmpty()) {
            LOGGER.info("@#@#@# Upbit Listing is null");
            jedis.hset("UpbitListing", "WAX", "0");
            jedis.hset("UpbitListing", "LTC", "0");
            jedis.hset("UpbitListing", "XRP", "0");
            jedis.hset("UpbitListing", "ETH", "0");
            jedis.hset("UpbitListing", "ETC", "0");
            jedis.hset("UpbitListing", "NBT", "0");
            jedis.hset("UpbitListing", "ADA", "0");
            jedis.hset("UpbitListing", "XLM", "0");
            jedis.hset("UpbitListing", "NEO", "0");
            jedis.hset("UpbitListing", "BCC", "0");
            jedis.hset("UpbitListing", "SRN", "0");
            jedis.hset("UpbitListing", "BCC", "0");
            jedis.hset("UpbitListing", "LSK", "0");
            jedis.hset("UpbitListing", "WAVES", "0");
            jedis.hset("UpbitListing", "DOGE", "0");
            jedis.hset("UpbitListing", "SC", "0");
            jedis.hset("UpbitListing", "XVG", "0");
            jedis.hset("UpbitListing", "STRAT", "0");
            jedis.hset("UpbitListing", "QTUM", "0");
            jedis.hset("UpbitListing", "XEM", "0");
            jedis.hset("UpbitListing", "KMD", "0");
            jedis.hset("UpbitListing", "OMG", "0");
            jedis.hset("UpbitListing", "ZEC", "0");
            jedis.hset("UpbitListing", "DGB", "0");
            jedis.hset("UpbitListing", "NXS", "0");
            jedis.hset("UpbitListing", "XMR", "0");
            jedis.hset("UpbitListing", "DASH", "0");
            jedis.hset("UpbitListing", "BTG", "0");
            jedis.hset("UpbitListing", "STEEM", "0");
            jedis.hset("UpbitListing", "RDD", "0");
            jedis.hset("UpbitListing", "IGNIS", "0");
            jedis.hset("UpbitListing", "SYS", "0");
            jedis.hset("UpbitListing", "NXT", "0");
            jedis.hset("UpbitListing", "PIVX", "0");
            jedis.hset("UpbitListing", "SALT", "0");
            jedis.hset("UpbitListing", "GMT", "0");
            jedis.hset("UpbitListing", "POWR", "0");
            jedis.hset("UpbitListing", "ARK", "0");
            jedis.hset("UpbitListing", "MONA", "0");
            jedis.hset("UpbitListing", "VTC", "0");
            jedis.hset("UpbitListing", "XDN", "0");
            jedis.hset("UpbitListing", "MANA", "0");
            jedis.hset("UpbitListing", "GUP", "0");
            jedis.hset("UpbitListing", "ARDR", "0");
            jedis.hset("UpbitListing", "GBYTE", "0");
            jedis.hset("UpbitListing", "VOX", "0");
            jedis.hset("UpbitListing", "ZEN", "0");
            jedis.hset("UpbitListing", "BITB", "0");
            jedis.hset("UpbitListing", "UKG", "0");
            jedis.hset("UpbitListing", "SBD", "0");
            jedis.hset("UpbitListing", "BAT", "0");
            jedis.hset("UpbitListing", "SNT", "0");
            jedis.hset("UpbitListing", "EDG", "0");
            jedis.hset("UpbitListing", "REP", "0");
            jedis.hset("UpbitListing", "EMC2", "0");
            jedis.hset("UpbitListing", "ION", "0");
            jedis.hset("UpbitListing", "PAY", "0");
            jedis.hset("UpbitListing", "ADT", "0");
            jedis.hset("UpbitListing", "XZC", "0");
            jedis.hset("UpbitListing", "BAY", "0");
            jedis.hset("UpbitListing", "ENG", "0");
            jedis.hset("UpbitListing", "DNT", "0");
            jedis.hset("UpbitListing", "VIA", "0");
            jedis.hset("UpbitListing", "ADX", "0");
            jedis.hset("UpbitListing", "CVC", "0");
            jedis.hset("UpbitListing", "DCR", "0");
            jedis.hset("UpbitListing", "QRL", "0");
            jedis.hset("UpbitListing", "BURST", "0");
            jedis.hset("UpbitListing", "RCN", "0");
            jedis.hset("UpbitListing", "RLC", "0");
            jedis.hset("UpbitListing", "MAID", "0");
            jedis.hset("UpbitListing", "STORJ", "0");
            jedis.hset("UpbitListing", "MCO", "0");
            jedis.hset("UpbitListing", "LBC", "0");
            jedis.hset("UpbitListing", "VIB", "0");
            jedis.hset("UpbitListing", "GRS", "0");
            jedis.hset("UpbitListing", "FCT", "0");
            jedis.hset("UpbitListing", "BLOCK", "0");
            jedis.hset("UpbitListing", "ANT", "0");
            jedis.hset("UpbitListing", "AMP", "0");
            jedis.hset("UpbitListing", "HMQ", "0");
            jedis.hset("UpbitListing", "BNT", "0");
            jedis.hset("UpbitListing", "SYNX", "0");
            jedis.hset("UpbitListing", "SWT", "0");
            jedis.hset("UpbitListing", "EXP", "0");
            jedis.hset("UpbitListing", "GAME", "0");
            jedis.hset("UpbitListing", "OK", "0");
            jedis.hset("UpbitListing", "KORE", "0");
            jedis.hset("UpbitListing", "NMR", "0");
            jedis.hset("UpbitListing", "1ST", "0");
            jedis.hset("UpbitListing", "WINGS", "0");
            jedis.hset("UpbitListing", "UBQ", "0");
            jedis.hset("UpbitListing", "NAV", "0");
            jedis.hset("UpbitListing", "MER", "0");
            jedis.hset("UpbitListing", "IOP", "0");
            jedis.hset("UpbitListing", "PTOY", "0");
            jedis.hset("UpbitListing", "VRC", "0");
            jedis.hset("UpbitListing", "XEL", "0");
            jedis.hset("UpbitListing", "PART", "0");
            jedis.hset("UpbitListing", "BLK", "0");
            jedis.hset("UpbitListing", "DYN", "0");
            jedis.hset("UpbitListing", "TX", "0");
            jedis.hset("UpbitListing", "SHIFT", "0");
            jedis.hset("UpbitListing", "CFI", "0");
            jedis.hset("UpbitListing", "TIX", "0");
            jedis.hset("UpbitListing", "CLOAK", "0");
            jedis.hset("UpbitListing", "AGRS", "0");
            jedis.hset("UpbitListing", "GNO", "0");
            jedis.hset("UpbitListing", "UNB", "0");
            jedis.hset("UpbitListing", "RADS", "0");
            jedis.hset("UpbitListing", "BSD", "0");
            jedis.hset("UpbitListing", "DCT", "0");
            jedis.hset("UpbitListing", "SLS", "0");
            jedis.hset("UpbitListing", "MEME", "0");
            jedis.hset("UpbitListing", "FTC", "0");
            jedis.hset("UpbitListing", "SPHR", "0");
            jedis.hset("UpbitListing", "SIB", "0");
            jedis.hset("UpbitListing", "MUE", "0");
            jedis.hset("UpbitListing", "EXCL", "0");
            jedis.hset("UpbitListing", "MTL", "0");
            jedis.hset("UpbitListing", "GNT", "0");
            jedis.hset("UpbitListing", "DOPE", "0");
            jedis.hset("UpbitListing", "MYST", "0");    // 지원종료
            jedis.hset("UpbitListing", "RISE", "0");    // 지원종료
            jedis.hset("UpbitListing", "DGD", "0");     // 지원종료
            jedis.hset("UpbitListing", "FUN", "0");     // 지원종료
            jedis.hset("UpbitListing", "TRIG", "0");    // 지원종료
            jedis.hset("UpbitListing", "SAFEX", "0");   // 지원종료
            jedis.hset("UpbitListing", "SNGLS", "0");   // 지원종료
            jedis.hset("UpbitListing", "XAUR", "0");    // 지원종료
            jedis.hset("UpbitListing", "ZRX", "0");
            jedis.hset("UpbitListing", "VEE", "0");
            jedis.hset("UpbitListing", "BCPT", "0");

        }
    }

    @Override
//    @Scheduled(initialDelay = 1000 * 6, fixedDelay = 1000 * 10)
    public void inspectListedCoin() throws IOException {
        /** env validation check.**/
        if (!StringUtils.equals("real", env)) {
            return;
        }

        init();

        LOGGER.info(count + "회차 Upbit");

        List<String> noListedCoinList = new ArrayList<>();
//        for (String item : hashOperations.keys("CoinMarketCap")) {
//            if (!hashOperations.hasKey("UpbitListing", item)) {
//                noListedCoinList.add(item);
//            }
//        }
        for (String item : jedis.hkeys("CoinMarketCap")) {
            if (!jedis.hexists("UpbitListing", item)) {
                noListedCoinList.add(item);
            }
        }

        for (String item : noListedCoinList) {

            String tempURL = "https://ccx.upbit.com/api/v1/market_status?market=BTC-" + item;

            JSONObject jsonObject = httpUtils.getResponseByObject(tempURL);

            if (!jsonObject.has("error") && jsonObject.has("name")) {
                StringBuilder messageContent = new StringBuilder();

                Date today = new Date();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd, hh:mm:ss a");

                messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
                messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
                messageContent.append(" [ Upbit ] 상장 정보 ");
                messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
                messageContent.append(StringEscapeUtils.unescapeJava("\\ud83d\\ude80"));
                messageContent.append("\n상장 리스트 탐지되었습니다.");
                messageContent.append("\n확인 시간 : ");
                messageContent.append(simpleDateFormat.format(today).toString());
                messageContent.append("\n코인 정보 : ");
//                messageContent.append(hashOperations.get("CoinMarketCap", item));
                messageContent.append(jedis.hget("CoinMarketCap", item));
                messageContent.append(" (");
                messageContent.append(item);
                messageContent.append(")");
                messageContent.append("\n구매 가능 거래소");
                messageContent.append(marketInfo.availableMarketList(item));

                String url = CommonConstant.URL_TELEGRAM_BASE + apiKey + CommonConstant.METHOD_TELEGRAM_SENDMESSAGE;
                messageUtils.sendMessage(url, -300048567L, messageContent.toString());
                messageUtils.sendMessage(url, -277619118L, messageContent.toString());

//                hashOperations.put("UpbitListing", item, "0");
                jedis.hset("UpbitListing", item, "0");

                LOGGER.info("Upbit 상장 : " + item + " (" + today + ")");

            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        count++;
    }
}
