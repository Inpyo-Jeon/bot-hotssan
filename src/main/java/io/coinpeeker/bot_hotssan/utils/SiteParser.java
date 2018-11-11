package io.coinpeeker.bot_hotssan.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author : Jeon
 * @version : 1.0
 * @date : 2018-11-09
 * @description :
 */

@Component
public class SiteParser {

    @Value("${data.targetUrl}")
    private String URL;

    public SiteParser() {

    }

    public Elements execute() throws IOException {
        Document doc = Jsoup.connect(URL).get();
        Element body = doc.body();
        Elements div = body.getElementsByClass("next_game");
        Elements pTag = div.get(0).getElementsByTag("p");

        return pTag;
    }
}