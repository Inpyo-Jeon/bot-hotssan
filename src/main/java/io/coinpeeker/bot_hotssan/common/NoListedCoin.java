package io.coinpeeker.bot_hotssan.common;

import com.google.common.collect.Maps;
import io.coinpeeker.bot_hotssan.utils.HttpUtils;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class NoListedCoin {
    private List<String> coinList = new ArrayList<>();
    private Map<String, String> binanceCoinMap = Maps.newHashMap();

    @Autowired
    HttpUtils httpUtils;

    private static final Logger LOGGER = LoggerFactory.getLogger(NoListedCoin.class);


    @Autowired
    public NoListedCoin(HttpUtils httpUtils) throws IOException {
        this.httpUtils = httpUtils;

        try {
            JSONArray jsonArrayBinance = httpUtils.getResponseByArrays("https://api.binance.com/api/v3/ticker/price");
            for (int i = 0; i < jsonArrayBinance.length(); i++){
                if(jsonArrayBinance.getJSONObject(i).getString("symbol").contains("BTC")){
                    binanceCoinMap.put(jsonArrayBinance.getJSONObject(i).getString("symbol").replace("BTC", ""), jsonArrayBinance.getJSONObject(i).getString("symbol").replace("BTC", ""));
                }
            }

            JSONArray jsonArray = httpUtils.getResponseByArrays("https://api.coinmarketcap.com/v1/ticker/?limit=1600");
            for (int i = 0; i < jsonArray.length(); i++){
                if(!binanceCoinMap.containsKey(jsonArray.getJSONObject(i).getString("symbol").toUpperCase())){
                    coinList.add(jsonArray.getJSONObject(i).getString("symbol").toUpperCase());
                }
            }

            coinList.remove("BTC");


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> getCoinList(){
        return this.coinList;
    }
}
