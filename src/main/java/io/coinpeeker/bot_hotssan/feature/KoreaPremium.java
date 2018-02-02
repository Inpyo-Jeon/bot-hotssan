package io.coinpeeker.bot_hotssan.feature;

import com.google.common.collect.Maps;
import io.coinpeeker.bot_hotssan.external.ApiClient;
import io.coinpeeker.bot_hotssan.model.CoinPrice;
import io.coinpeeker.bot_hotssan.model.constant.CoinType;
import io.coinpeeker.bot_hotssan.utils.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


@Service
public class KoreaPremium {

    private static final Logger LOGGER = LoggerFactory.getLogger(KoreaPremium.class);

    public String calculate(String key, Map<CoinType, List<ApiClient>> exchangeMap, double krwRate) {
        Map<String, CoinPrice> coinPriceMap = Maps.newHashMap();

        for (ApiClient item : exchangeMap.get(CoinType.valueOf(key))) {
            CoinPrice coin = item.getCoinPrice(key, krwRate);
            coinPriceMap.put(coin.getExchangeName(), coin);
        }

        List<String> koreaExchange = new ArrayList<>();
        List<String> foreignExchange = new ArrayList<>();

        for (String item : coinPriceMap.keySet()) {
            if ("빗썸, 코인네스트, 코인원, 코인레일, 업비트".contains(item)) {
                Arrays.asList(item);
                koreaExchange.add(item);
            } else {
                foreignExchange.add(item);
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("[ ");
        sb.append(key);
        sb.append(" ]");
        sb.append("\n");

        for (String nameOfKoreaExchange : koreaExchange) {
            for (String nameOfForeignExchange : foreignExchange) {
                CoinPrice coinPriceAtKorea = coinPriceMap.get(nameOfKoreaExchange);
                CoinPrice coinPriceAtForeign = coinPriceMap.get(nameOfForeignExchange);

                sb.append(nameOfKoreaExchange);
                sb.append("-");
                sb.append(nameOfForeignExchange);
                sb.append(" : ");
                sb.append(CommonUtils.convertPremium(((Double.valueOf(coinPriceAtKorea.getKrw()) / Double.valueOf(coinPriceAtForeign.getKrw())))));
                sb.append("\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
