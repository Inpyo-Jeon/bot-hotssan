package io.coinpeeker.bot_hotssan.service.lotto;

/**
 * @author : Jeon
 * @version : 1.0
 * @date : 2018-11-09
 * @description :
 */

import io.coinpeeker.bot_hotssan.model.Prizes;
import io.coinpeeker.bot_hotssan.repository.PrizesRepository;
import io.coinpeeker.bot_hotssan.utils.SiteParser;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.Timestamp;

@Component
public class PriceProcess {

    @Autowired
    SiteParser siteParser;

    @Autowired
    PrizesRepository prizesRepository;

    public void executePrice() throws IOException {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        Elements pTags = siteParser.execute();
        long estimatedPrizes = 0;
        long cumulativeSales = 0;

        for (Element item : pTags) {
            if (item.className().equals("fl")) {
                estimatedPrizes = Long.parseLong(item.getElementsByClass("money").text().replaceAll("\\D", ""));
            } else if (item.className().equals("fr")) {
                cumulativeSales = Long.parseLong(item.getElementsByClass("money").text().replaceAll("\\D", ""));
            } else {

            }
        }

        prizesRepository.save(new Prizes(estimatedPrizes, cumulativeSales, timestamp));
    }
}