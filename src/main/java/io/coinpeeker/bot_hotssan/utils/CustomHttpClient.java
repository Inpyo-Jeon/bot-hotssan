package io.coinpeeker.bot_hotssan.utils;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

public class CustomHttpClient {

    public String http(String url){
        try{
            CloseableHttpClient client  = HttpClientBuilder.create().build();
            HttpPost            request = new HttpPost(url);

            request.addHeader("Content-Type", "application/json");
            HttpResponse result = client.execute(request);

            return EntityUtils.toString(result.getEntity(), "UTF-8");


        }catch(Exception e){
            return "";
        }
    }
}
