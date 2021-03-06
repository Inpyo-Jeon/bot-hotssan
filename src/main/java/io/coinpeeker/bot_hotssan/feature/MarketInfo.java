package io.coinpeeker.bot_hotssan.feature;

import com.google.common.collect.Maps;
import io.coinpeeker.bot_hotssan.utils.HttpUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.*;

@Component
public class MarketInfo {

    @Autowired
    private HttpUtils httpUtils;

    @Autowired
    private Jedis jedis;

    private static final Logger LOGGER = LoggerFactory.getLogger(MarketInfo.class);

    public Map<String, List<String>> availableMarketList(String coin) throws IOException {
        Map<String, List<String>> market = Maps.newHashMap();
        String url = "";
        synchronized (jedis) {
             url = "https://coinmarketcap.com/currencies/" + jedis.hget("I-CoinMarketCap-Address", coin) + "#markets";
        }

        StringBuilder pair = new StringBuilder();
        pair.append(coin);
        pair.append("/BTC");
        pair.append(", ");
        pair.append(coin);
        pair.append("/KRW");

        String htmlData = httpUtils.getResponseByHtmlString(url);

        try {
            Element body = Jsoup.parseBodyFragment(htmlData).body();
            Elements tbody = body.getElementById("markets-table").getElementsByTag("tbody").get(0).getElementsByTag("tr");

            // 거래소 명과 화폐 체크
            for (Element item : tbody) {
                Elements td = item.getElementsByTag("td");
                if (pair.toString().contains(td.get(2).text())) {

                    if (!market.containsKey(td.get(1).text())) {
                        List<String> list = new ArrayList<>();
                        list.add(td.get(2).text());

                        market.put(td.get(1).text(), list);
                    } else {
                        List<String> list = market.get(td.get(1).text());
                        list.add(td.get(2).text());
                    }
                }
            }
        } catch (NullPointerException e) {
        } finally {
            return market;
        }
    }

    public String marketInfo(Map<String, List<String>> market) {
        StringBuilder sb = new StringBuilder();

        for (String item : market.keySet()) {
            sb.append("\n");
            sb.append("  ");
            sb.append(item);
            sb.append(" -> ");
            sb.append(market.get(item));
        }

        return sb.toString();
    }
}