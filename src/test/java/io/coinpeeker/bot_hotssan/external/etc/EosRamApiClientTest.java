package io.coinpeeker.bot_hotssan.external.etc;

import io.coinpeeker.bot_hotssan.common.CommonConstant;
import io.coinpeeker.bot_hotssan.module.CommonTest;
import io.coinpeeker.bot_hotssan.utils.HttpUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class EosRamApiClientTest {

    @Autowired
    HttpUtils httpUtils;

    private static final Logger LOGGER = LoggerFactory.getLogger(EosRamApiClientTest.class);

    @Test
    public void getEosInfo() throws IOException {
        CloseableHttpResponse httpResponse = httpUtils.get(CommonConstant.EOS_INFO_URL);
        String convertData = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");

        Element body = Jsoup.parseBodyFragment(convertData).body();
        Elements currentRamPriceDiv = body.getElementsByClass("event-single-info");

        StringBuilder sendMessage = new StringBuilder();

        currentRamPriceDiv.forEach((item) -> {
            sendMessage.append(String.valueOf(item.getElementsByTag("h6").get(0).text()));
            sendMessage.append("\n : ");
            sendMessage.append(String.valueOf(item.getElementsByTag("p").get(0).text()));
            sendMessage.append("\n");
        });

        LOGGER.info(sendMessage.toString());
    }
}

