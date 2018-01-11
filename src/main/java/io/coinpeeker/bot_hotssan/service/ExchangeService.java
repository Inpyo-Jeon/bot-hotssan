package io.coinpeeker.bot_hotssan.service;

import io.coinpeeker.bot_hotssan.common.CommonConstant;
import io.coinpeeker.bot_hotssan.util.CustomHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ExchangeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExchangeService.class);

    public String getUSDExchangeRate(){

        CustomHttpClient httpClient = new CustomHttpClient();

        String result       = httpClient.http(CommonConstant.HANA_BANK_URL);
        Document document   = Jsoup.parseBodyFragment(result);
        Element body        = document.body();
        Elements moneyTable = body.getElementsByClass("tbl_cont");
        Elements USDTable   = moneyTable.get(0).getElementsByClass("first");
        Elements buy        = USDTable.get(0).getElementsByClass("buy");
        Elements sell       = USDTable.get(0).getElementsByClass("sell");

        LOGGER.info(buy.get(0).text());
        LOGGER.info(sell.get(0).text());

        return buy.get(0).text();
    }
}
