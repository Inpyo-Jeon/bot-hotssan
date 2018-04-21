package io.coinpeeker.bot_hotssan.module;

import io.coinpeeker.bot_hotssan.utils.HttpUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ModuleTest {


    @Value("${property.hotssan_id}")
    private String apiKey;

    @Value("${property.env}")
    private String env;

    @Autowired
    HttpUtils httpUtils;

    String standardUrl = "https://api.binance.com";
    String url = "";
    String headerApiKey = "X-MBX-APIKEY";
    String headerApiValue = "";
    String secretKey = "";
    List<NameValuePair> header = new ArrayList<>();
    List<String> list = Arrays.asList("비트코인", "이더리움", "리플", "비트코인 캐시", "이오스", "라이트코인", "모네로", "트론", "대시", "비체인", "이더리움 클래식", "퀀텀", "아이콘", "비트코인 골드", "제트캐시", "미스릴", "엘프");

    @Test
    public void Test() throws IOException, NoSuchAlgorithmException, InvalidKeyException, InterruptedException, ParserConfigurationException {
        CloseableHttpResponse httpResponse = httpUtils.get("https://bithumb.cafe/feed");
        String convertData = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");

        Elements items = Jsoup.parseBodyFragment(convertData).body().getElementsByTag("item");

        for (Element item : items) {
            if (item.getElementsByTag("title").get(0).text().contains("검토보고서")) {
                System.out.println(item.getElementsByTag("title").get(0).text());
            }
        }

//
    }


}
