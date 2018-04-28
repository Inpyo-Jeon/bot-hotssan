package io.coinpeeker.bot_hotssan.scheduler.wallet;

import io.coinpeeker.bot_hotssan.common.CommonConstant;
import io.coinpeeker.bot_hotssan.common.SecretKey;
import io.coinpeeker.bot_hotssan.utils.HttpUtils;
import io.coinpeeker.bot_hotssan.utils.MessageUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class UpbitWalletExplorerScheduler {

    @Autowired
    HttpUtils httpUtils;

    @Autowired
    MessageUtils messageUtils;

    @Autowired
    Jedis jedis;

    @Value("${property.hotssan_id}")
    private String apiKey;

    @Value("${property.env}")
    private String env;

    private static final Logger LOGGER = LoggerFactory.getLogger(UpbitWalletExplorerScheduler.class);

    @Scheduled(initialDelay = 1000 * 10, fixedDelay = 1000 * 60 * 5)
    public void searchWallet() throws IOException {
        /** env validation check.**/
        if (!StringUtils.equals("dev", env)) {
            return;
        }

        String bittrexAddress = "0xfbb1b73c4f0bda4f67dca266ce6ef42f520fbb98";
        String upbitContractAddress = "0x93d4aa6a6b7f3b1762cfe7098e7272d2c260e02e";

        String explorerApiAddress = "https://api.ethplorer.io";
        String endPoint = "/getAddressHistory";
        String address = "/" + upbitContractAddress;
        String parameter = "?apiKey=" + SecretKey.getApiKeyEthplorer();

        String requestUrl = explorerApiAddress + endPoint + address + parameter;

        List<NameValuePair> header = new ArrayList<>();
        header.add(new BasicNameValuePair("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8"));
        header.add(new BasicNameValuePair("Accept-Encoding", "gzip, deflate, br"));
        header.add(new BasicNameValuePair("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7"));
        header.add(new BasicNameValuePair("Cache-Control", "max-age=0"));
        header.add(new BasicNameValuePair("Connection", "keep-alive"));
        header.add(new BasicNameValuePair("Host", "api.ethplorer.io"));
        header.add(new BasicNameValuePair("Upgrade-Insecure-Requests", "1"));
        header.add(new BasicNameValuePair("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.186 Safari/537.36"));

        JSONObject jsonObject = httpUtils.getResponseByObject(requestUrl, header);

        JSONArray jsonArray = jsonObject.getJSONArray("operations");

        for (int i = 0; i < jsonArray.length(); i++) {
            if (!jedis.hexists("W-Upbit-Tx", jsonArray.getJSONObject(i).getString("transactionHash"))) {
                String from = jsonArray.getJSONObject(i).getString("from");
                String to = jsonArray.getJSONObject(i).getString("to");
                String tx = jsonArray.getJSONObject(i).getString("transactionHash");
                String coinName = jsonArray.getJSONObject(i).getJSONObject("tokenInfo").getString("name");
                String coinSymbol = jsonArray.getJSONObject(i).getJSONObject("tokenInfo").getString("symbol");
                String link = "https://etherscan.io/tx/" + tx;
                String io = "";

                if (upbitContractAddress.equals(to)) {
                    io = "In";
                } else {
                    io = "Out";
                }

                StringBuilder messageContent = new StringBuilder();
                messageContent.append("W-Upbit-Tx 트랜잭션 감지");
                messageContent.append("\nCoin Name : ");
                messageContent.append(coinName);
                messageContent.append("\nCoin Symbol : ");
                messageContent.append(coinSymbol);
                messageContent.append("\n입, 출금 : ");
                messageContent.append(io);
                messageContent.append("\nEtherScan : \n");
                messageContent.append(link);

                if (bittrexAddress.equals(from)) {
                    messageContent.append("\n-- 비트렉스발 트랜잭션, 이미 상장되었을 가능성 --");
                }

                String botUrl = CommonConstant.URL_TELEGRAM_BASE + apiKey + CommonConstant.METHOD_TELEGRAM_SENDMESSAGE;
                messageUtils.sendMessage(botUrl, -294606763L, messageContent.toString());

                jedis.hset("W-Upbit-Tx", jsonArray.getJSONObject(i).getString("transactionHash"), "0");
            }
        }
    }
}
