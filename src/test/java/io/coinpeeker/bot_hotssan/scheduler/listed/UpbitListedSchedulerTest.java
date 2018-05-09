package io.coinpeeker.bot_hotssan.scheduler.listed;


import io.coinpeeker.bot_hotssan.trade.api.Binance;
import io.coinpeeker.bot_hotssan.utils.HttpUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UpbitListedSchedulerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(Binance.class);

    @Autowired
    HttpUtils httpUtils;

    int preTotalCount = 203;

    @Test
    public void AA() throws NoSuchAlgorithmException, InvalidKeyException, IOException {

        String tempURL = "https://api-manager.upbit.com/api/v1/notices?page=1&per_page=4";

        JSONObject jsonObject = httpUtils.getResponseByObject(tempURL);

        int totalCount = jsonObject.getJSONObject("data").getInt("total_count");

        if (preTotalCount != totalCount) {
            String title = jsonObject.getJSONObject("data").getJSONArray("list").getJSONObject(3).getString("title");
            System.out.println(title);

            if ((title.contains("[이벤트]") && title.contains("상장")) || (title.contains("[거래]") && title.contains("원화") && ((title.contains("추가")) || title.contains("상장")))) {
                int bracketCount = StringUtils.countMatches(title, "(");
                if(bracketCount == 1){
                    System.out.println("심볼 : " + title.replaceAll("(\\W)", "").toUpperCase());
                }
            }
            preTotalCount = totalCount;
        }
    }
}
