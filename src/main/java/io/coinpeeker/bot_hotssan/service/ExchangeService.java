package io.coinpeeker.bot_hotssan.service;

import io.coinpeeker.bot_hotssan.common.CommonConstant;
import io.coinpeeker.bot_hotssan.utils.HttpUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ExchangeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeService.class);

    public String getUSDExchangeRate() throws IOException {

        HttpUtils httpUtils = new HttpUtils();
        HttpResponse result = httpUtils.get(CommonConstant.HANA_BANK_URL);
        String convertData = EntityUtils.toString(result.getEntity(), "UTF-8");
        Document document = Jsoup.parseBodyFragment(convertData);
        Element body = document.body();
        Elements moneyTable = body.getElementsByClass("tbl_cont");
        Elements USDTable = moneyTable.get(0).getElementsByClass("first");
        Elements buy = USDTable.get(0).getElementsByClass("buy");
        Elements sell = USDTable.get(0).getElementsByClass("sell");

        LOGGER.info(buy.get(0).text());
        LOGGER.info(sell.get(0).text());

        return buy.get(0).text();
    }
}
