package io.coinpeeker.bot_hotssan.module;

import io.coinpeeker.bot_hotssan.utils.HttpUtils;
import org.apache.commons.codec.binary.Hex;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Base64Utils;
import redis.clients.jedis.Jedis;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ModuleTest {

    @Autowired
    HttpUtils httpUtils;

    @Test
    public void inspectListedCoin() throws IOException, NoSuchAlgorithmException, InvalidKeyException {

        String accessKey = "d1b56a93-db2daaff-4d3eb6c6-7ab88";

        String serverTimestamp = httpUtils.getResponseByObject("https://api.huobi.pro/v1/common/timestamp").get("data").toString();
        StringBuilder convertTimestamp = new StringBuilder();

        Timestamp tsp = new Timestamp(Long.valueOf(serverTimestamp.toString()));
        Timestamp aa = new Timestamp(System.currentTimeMillis());


        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));


        Date date = new Date(tsp.getTime());

        String cc = sdf.format(date).toString();
        String encodeTimestamp = URLEncoder.encode(cc, "UTF-8");

        StringBuilder sb = new StringBuilder();

        System.out.println(sb.toString());

        StringBuilder queryString = new StringBuilder();
        queryString.append("GET\\n");
        queryString.append("api.huobi.pro\\n");
        queryString.append("/v1/account/accounts\\n");
        queryString.append("AccessKeyId=");
        queryString.append(accessKey);
        queryString.append("&SignatureMethod=HmacSHA256");
        queryString.append("&SignatureVersion=2");
        queryString.append("&Timestamp=");
        queryString.append(encodeTimestamp);

        System.out.println(queryString.toString());


        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec("69a10cb6-9ecd6fbc-42034afb-7beec".getBytes("UTF-8"), "HmacSAH256");
        sha256_HMAC.init(secretKeySpec);

        String signatureResult = Hex.encodeHexString(sha256_HMAC.doFinal(queryString.toString().getBytes("UTF-8")));

        System.out.println(signatureResult);


        String finalData = new String(Base64Utils.encode(signatureResult.getBytes("UTF-8")));

        String encodeFinalData = URLEncoder.encode(finalData, "UTF-8");

        StringBuilder finalURL = new StringBuilder();
        finalURL.append("https://api.huobi.pro/v1/account/accounts");
        finalURL.append("?AccessKeyId=");
        finalURL.append(accessKey);
        finalURL.append("&SignatureMethod=HmacSHA256");
        finalURL.append("&SignatureVersion=2");
        finalURL.append("&Timestamp=");
        finalURL.append(encodeTimestamp);
        finalURL.append("&Signature=");
        finalURL.append(encodeFinalData);

        System.out.println(finalURL);

        System.out.println(EntityUtils.toString(httpUtils.get(finalURL.toString()).getEntity(), "UTF-8"));
//


    }


}
