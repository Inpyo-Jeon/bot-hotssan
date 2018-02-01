package io.coinpeeker.bot_hotssan.feature;

import com.google.common.collect.Maps;
import io.coinpeeker.bot_hotssan.external.ApiClient;
import io.coinpeeker.bot_hotssan.external.bank.HanaBankApiClient;
import io.coinpeeker.bot_hotssan.model.CoinPrice;
import io.coinpeeker.bot_hotssan.model.constant.CoinType;
import io.coinpeeker.bot_hotssan.utils.Commander;
import io.coinpeeker.bot_hotssan.utils.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Service
public class PremiumCalc {

    private static final Logger LOGGER = LoggerFactory.getLogger(PremiumCalc.class);


    public String str(String key, Map<CoinType, List<ApiClient>> premiumMap, double krwRate) {
        Map<String, CoinPrice> restMap = Maps.newHashMap();


        for (ApiClient item : premiumMap.get(CoinType.valueOf(key))) {

            CoinPrice coin = item.getCoinPrice(key, krwRate);
            restMap.put(coin.getExchangeName(), coin);
        }

        List<String> koreaExchange = new ArrayList<>();
        List<String> foreignExchange = new ArrayList<>();

        for (String item : restMap.keySet()) {
            if ("빗썸, 코인네스트, 코인원, 코인레일, 업비트".contains(item)) {
                koreaExchange.add(item);
            } else {
                foreignExchange.add(item);
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("-- ");
        sb.append(key);
        sb.append(" --");
        sb.append("\n");

        for (String koreaEx : koreaExchange) {
            for (String foreignEx : foreignExchange) {
                CoinPrice korea = restMap.get(koreaEx);
                CoinPrice foreign = restMap.get(foreignEx);

                sb.append(koreaEx);
                sb.append("/");
                sb.append(foreignEx);
                sb.append("\n");
                sb.append(CommonUtils.convertPremium((Double.valueOf(korea.getKrw()) / Double.valueOf(foreign.getKrw()))));
                sb.append("\n");
                sb.append("\n");
            }
        }
        return sb.toString();
    }

}
