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
import java.util.concurrent.atomic.AtomicLong;

@Component
public class PriceProcess {

    @Autowired
    SiteParser siteParser;

    @Autowired
    PrizesRepository prizesRepository;

    public void executePrice() throws IOException {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        Elements result = siteParser.execute();
        AtomicLong estimatedPrizes = new AtomicLong();
        AtomicLong cumulativeSales = new AtomicLong();


        result.forEach(item -> {
            String kind = item.getElementsByTag("strong").text();
            switch (kind) {
                case "예상당첨금":
                    estimatedPrizes.set(Long.parseLong(item.text().replaceAll("\\D", ""))); break;
                case "누적판매금":
                    cumulativeSales.set(Long.parseLong(item.text().replaceAll("\\D", ""))); break;
            }
        });

        prizesRepository.save(new Prizes(estimatedPrizes.get(), cumulativeSales.get(), timestamp));
    }
}