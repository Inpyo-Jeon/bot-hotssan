package io.coinpeeker.bot_hotssan.module;


import com.google.common.collect.Maps;
import io.coinpeeker.bot_hotssan.utils.HttpUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CommonTest {

    @Autowired
    HttpUtils httpUtils;


    @Test
    public void test() throws IOException {
        Map<String, Map<String, String>> market = Maps.newHashMap();
        String url = "";
        url = "https://coinmarketcap.com/currencies/ethereum";


        StringBuilder pair = new StringBuilder();
//        pair.append(coin);
        pair.append("/BTC");
        pair.append(", ");
//        pair.append(coin);
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
                        Map<String, String> map = Maps.newHashMap();
                        map.put(td.get(2).text(), td.get(5).text());
                        market.put(td.get(1).text(), map);
                    } else {
                        Map<String, String> map = market.get(td.get(1).text());
                        map.put(td.get(2).text(), td.get(5).text());
                    }
                }
            }
        } catch (NullPointerException e) {
        } finally {

        }

        System.out.println(market.get("Upbit").containsKey("ETH/KRW"));
        market.get("Upbit").forEach((String pairKey, String volume) -> System.out.println(pairKey + " " + volume));
        System.out.println(market.toString());

    }

}
