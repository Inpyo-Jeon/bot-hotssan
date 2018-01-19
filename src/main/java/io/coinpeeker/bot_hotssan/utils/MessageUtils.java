package io.coinpeeker.bot_hotssan.utils;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class MessageUtils {

    @Autowired
    private HttpUtils httpUtils;

    /**
     * post 방식으로 간단하게 단문의 문장을 전송하는 메소드
     *
     * @param url
     * @param chatId
     * @param text
     */
    public void sendMessage(String url, long chatId, String text) {
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("chat_id", String.valueOf(chatId)));
        params.add(new BasicNameValuePair("text", text));

        try {
            httpUtils.post(url, params);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}
