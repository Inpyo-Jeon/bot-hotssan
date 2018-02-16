package io.coinpeeker.bot_hotssan.scheduler.listed;

import io.coinpeeker.bot_hotssan.common.CommonConstant;
import io.coinpeeker.bot_hotssan.scheduler.CoinMarketCapScheduler;
import io.coinpeeker.bot_hotssan.scheduler.Listing;
import io.coinpeeker.bot_hotssan.utils.HttpUtils;
import io.coinpeeker.bot_hotssan.utils.MessageUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Component
public class UpbitListedScheduler implements Listing{

    @Autowired
    CoinMarketCapScheduler coinMarketCapScheduler;

    @Autowired
    MessageUtils messageUtils;

    @Autowired
    HttpUtils httpUtils;

    @Value("${property.hotssan_id}")
    private String apiKey;

    @Resource(name = "redisTemplate")
    private HashOperations<String, String, String> hashOperations;
    private static final Logger LOGGER = LoggerFactory.getLogger(UpbitListedScheduler.class);
    private int count = 1;

    @Override
    public void init() throws IOException {
        if (hashOperations.keys("CoinMarketCap").isEmpty()) {
            LOGGER.info("@#@#@# CoinMarketCap Listing is null");
            coinMarketCapScheduler.refreshCoinData();
        }

        if (hashOperations.keys("UpbitListing").isEmpty()) {
            LOGGER.info("@#@#@# Upbit Listing is null");
            hashOperations.put("UpbitListing", "WAX", "0");
            hashOperations.put("UpbitListing", "LTC", "0");
            hashOperations.put("UpbitListing", "XRP", "0");
            hashOperations.put("UpbitListing", "ETH", "0");
            hashOperations.put("UpbitListing", "ETC", "0");
            hashOperations.put("UpbitListing", "NBT", "0");
            hashOperations.put("UpbitListing", "ADA", "0");
            hashOperations.put("UpbitListing", "XLM", "0");
            hashOperations.put("UpbitListing", "NEO", "0");
            hashOperations.put("UpbitListing", "BCC", "0");
            hashOperations.put("UpbitListing", "SRN", "0");
            hashOperations.put("UpbitListing", "BCC", "0");
            hashOperations.put("UpbitListing", "LSK", "0");
            hashOperations.put("UpbitListing", "WAVES", "0");
            hashOperations.put("UpbitListing", "DOGE", "0");
            hashOperations.put("UpbitListing", "SC", "0");
            hashOperations.put("UpbitListing", "XVG", "0");
            hashOperations.put("UpbitListing", "STRAT", "0");
            hashOperations.put("UpbitListing", "QTUM", "0");
            hashOperations.put("UpbitListing", "XEM", "0");
            hashOperations.put("UpbitListing", "KMD", "0");
            hashOperations.put("UpbitListing", "OMG", "0");
            hashOperations.put("UpbitListing", "ZEC", "0");
            hashOperations.put("UpbitListing", "DGB", "0");
            hashOperations.put("UpbitListing", "NXS", "0");
            hashOperations.put("UpbitListing", "XMR", "0");
            hashOperations.put("UpbitListing", "DASH", "0");
            hashOperations.put("UpbitListing", "BTG", "0");
            hashOperations.put("UpbitListing", "STEEM", "0");
            hashOperations.put("UpbitListing", "RDD", "0");
            hashOperations.put("UpbitListing", "IGNIS", "0");
            hashOperations.put("UpbitListing", "SYS", "0");
            hashOperations.put("UpbitListing", "NXT", "0");
            hashOperations.put("UpbitListing", "PIVX", "0");
            hashOperations.put("UpbitListing", "SALT", "0");
            hashOperations.put("UpbitListing", "GMT", "0");
            hashOperations.put("UpbitListing", "POWR", "0");
            hashOperations.put("UpbitListing", "ARK", "0");
            hashOperations.put("UpbitListing", "MONA", "0");
            hashOperations.put("UpbitListing", "VTC", "0");
            hashOperations.put("UpbitListing", "XDN", "0");
            hashOperations.put("UpbitListing", "MANA", "0");
            hashOperations.put("UpbitListing", "GUP", "0");
            hashOperations.put("UpbitListing", "ARDR", "0");
            hashOperations.put("UpbitListing", "GBYTE", "0");
            hashOperations.put("UpbitListing", "VOX", "0");
            hashOperations.put("UpbitListing", "ZEN", "0");
            hashOperations.put("UpbitListing", "BITB", "0");
            hashOperations.put("UpbitListing", "UKG", "0");
            hashOperations.put("UpbitListing", "SBD", "0");
            hashOperations.put("UpbitListing", "BAT", "0");
            hashOperations.put("UpbitListing", "SNT", "0");
            hashOperations.put("UpbitListing", "EDG", "0");
            hashOperations.put("UpbitListing", "REP", "0");
            hashOperations.put("UpbitListing", "EMC2", "0");
            hashOperations.put("UpbitListing", "ION", "0");
            hashOperations.put("UpbitListing", "PAY", "0");
            hashOperations.put("UpbitListing", "ADT", "0");
            hashOperations.put("UpbitListing", "XZC", "0");
            hashOperations.put("UpbitListing", "BAY", "0");
            hashOperations.put("UpbitListing", "ENG", "0");
            hashOperations.put("UpbitListing", "DNT", "0");
            hashOperations.put("UpbitListing", "VIA", "0");
            hashOperations.put("UpbitListing", "ADX", "0");
            hashOperations.put("UpbitListing", "CVC", "0");
            hashOperations.put("UpbitListing", "DCR", "0");
            hashOperations.put("UpbitListing", "QRL", "0");
            hashOperations.put("UpbitListing", "BURST", "0");
            hashOperations.put("UpbitListing", "RCN", "0");
            hashOperations.put("UpbitListing", "RLC", "0");
            hashOperations.put("UpbitListing", "MAID", "0");
            hashOperations.put("UpbitListing", "STORJ", "0");
            hashOperations.put("UpbitListing", "MCO", "0");
            hashOperations.put("UpbitListing", "LBC", "0");
            hashOperations.put("UpbitListing", "VIB", "0");
            hashOperations.put("UpbitListing", "GRS", "0");
            hashOperations.put("UpbitListing", "FCT", "0");
            hashOperations.put("UpbitListing", "BLOCK", "0");
            hashOperations.put("UpbitListing", "ANT", "0");
            hashOperations.put("UpbitListing", "AMP", "0");
            hashOperations.put("UpbitListing", "HMQ", "0");
            hashOperations.put("UpbitListing", "BNT", "0");
            hashOperations.put("UpbitListing", "SYNX", "0");
            hashOperations.put("UpbitListing", "SWT", "0");
            hashOperations.put("UpbitListing", "EXP", "0");
            hashOperations.put("UpbitListing", "GAME", "0");
            hashOperations.put("UpbitListing", "OK", "0");
            hashOperations.put("UpbitListing", "KORE", "0");
            hashOperations.put("UpbitListing", "NMR", "0");
            hashOperations.put("UpbitListing", "1ST", "0");
            hashOperations.put("UpbitListing", "WINGS", "0");
            hashOperations.put("UpbitListing", "UBQ", "0");
            hashOperations.put("UpbitListing", "NAV", "0");
            hashOperations.put("UpbitListing", "MER", "0");
            hashOperations.put("UpbitListing", "IOP", "0");
            hashOperations.put("UpbitListing", "PTOY", "0");
            hashOperations.put("UpbitListing", "VRC", "0");
            hashOperations.put("UpbitListing", "XEL", "0");
            hashOperations.put("UpbitListing", "PART", "0");
            hashOperations.put("UpbitListing", "BLK", "0");
            hashOperations.put("UpbitListing", "DYN", "0");
            hashOperations.put("UpbitListing", "TX", "0");
            hashOperations.put("UpbitListing", "SHIFT", "0");
            hashOperations.put("UpbitListing", "CFI", "0");
            hashOperations.put("UpbitListing", "TIX", "0");
            hashOperations.put("UpbitListing", "CLOAK", "0");
            hashOperations.put("UpbitListing", "AGRS", "0");
            hashOperations.put("UpbitListing", "GNO", "0");
            hashOperations.put("UpbitListing", "UNB", "0");
            hashOperations.put("UpbitListing", "RADS", "0");
            hashOperations.put("UpbitListing", "BSD", "0");
            hashOperations.put("UpbitListing", "DCT", "0");
            hashOperations.put("UpbitListing", "SLS", "0");
            hashOperations.put("UpbitListing", "MEME", "0");
            hashOperations.put("UpbitListing", "FTC", "0");
            hashOperations.put("UpbitListing", "SPHR", "0");
            hashOperations.put("UpbitListing", "SIB", "0");
            hashOperations.put("UpbitListing", "MUE", "0");
            hashOperations.put("UpbitListing", "EXCL", "0");
            hashOperations.put("UpbitListing", "MTL", "0");
            hashOperations.put("UpbitListing", "GNT", "0");
            hashOperations.put("UpbitListing", "MYST", "0");    // 지원종료
            hashOperations.put("UpbitListing", "RISE", "0");    // 지원종료
            hashOperations.put("UpbitListing", "DGD", "0");     // 지원종료
            hashOperations.put("UpbitListing", "FUN", "0");     // 지원종료
            hashOperations.put("UpbitListing", "TRIG", "0");    // 지원종료
            hashOperations.put("UpbitListing", "SAFEX", "0");   // 지원종료
            hashOperations.put("UpbitListing", "SNGLS", "0");   // 지원종료
            hashOperations.put("UpbitListing", "XAUR", "0");    // 지원종료
            hashOperations.put("UpbitListing", "ZRX", "0");

        }
    }

    @Override
    @Scheduled(initialDelay = 1000 * 6, fixedDelay = 1000 * 10)
    public void inspectListedCoin() throws IOException {
        init();

        ExecutorService executorService = Executors.newFixedThreadPool(1);

        List<String> noListedCoinList = new ArrayList<>();
        for (String item : hashOperations.keys("CoinMarketCap")) {
            if (!hashOperations.hasKey("UpbitListing", item)) {
                noListedCoinList.add(item);
            }
        }

        LOGGER.info("Upbit  " + count + "회차 뺑뺑이");

        Future<?> future = null;
        for (String item : noListedCoinList) {
            Runnable task = new Runnable() {
                @Override
                public void run() {
                    try {
                        String tempURL = "https://ccx.upbit.com/api/v1/market_status?market=BTC-" + item;

                        JSONObject jsonObject = httpUtils.getResponseByObject(tempURL);

                        if(!jsonObject.has("error") && jsonObject.has("name")){
                            StringBuilder messageContent = new StringBuilder();

                            Date today = new Date();
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd, hh:mm:ss a");

                            messageContent.append("!! Upbit 상장 정보 !!");
                            messageContent.append("\n상장 리스트 탐지되었습니다.");
                            messageContent.append("\n확인 시간 : ");
                            messageContent.append(simpleDateFormat.format(today).toString());
                            messageContent.append("\n코인 정보 : ");
                            messageContent.append(hashOperations.get("CoinMarketCap", item.toString()));
                            messageContent.append(" (");
                            messageContent.append(jsonObject.getString("name"));
                            messageContent.append(")");

                            String url = CommonConstant.URL_TELEGRAM_BASE + apiKey + CommonConstant.METHOD_TELEGRAM_SENDMESSAGE;
                            messageUtils.sendMessage(url, -294606763L, messageContent.toString());

                            hashOperations.put("UpbitListing", item.toString(), "0");

                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            future = executorService.submit(task);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            future.get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        count++;

    }
}
