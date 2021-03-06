package io.coinpeeker.bot_hotssan.external.bank;

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
public class HanaBankApiClient {

    @Autowired
    private HttpUtils httpUtils;

    public Double lastPrice() throws IOException {

        CloseableHttpResponse httpResponse = httpUtils.get(CommonConstant.HANA_BANK_URL);
        String convertData = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");

        Element body = Jsoup.parseBodyFragment(convertData).body();

        Elements buy = body.getElementsByClass("tbl_cont")
                .get(0).getElementsByClass("first")
                .get(0).getElementsByClass("buy");
        Elements sell = body.getElementsByClass("tbl_cont")
                .get(0).getElementsByClass("first")
                .get(0).getElementsByClass("sell");

        return (Double.valueOf(buy.get(0).text()) + Double.valueOf(sell.get(0).text())) / 2.0;
    }
}
