package io.coinpeeker.bot_hotssan.external.etc;

import io.coinpeeker.bot_hotssan.common.CommonConstant;
import io.coinpeeker.bot_hotssan.utils.HttpUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class EosRamApiClient {

    @Autowired
    HttpUtils httpUtils;

    public String lastPrice() throws IOException {
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

        return sendMessage.toString();
    }
}
