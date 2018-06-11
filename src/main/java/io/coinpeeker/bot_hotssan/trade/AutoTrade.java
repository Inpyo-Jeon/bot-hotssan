package io.coinpeeker.bot_hotssan.trade;

import com.neovisionaries.ws.client.WebSocketException;
import com.nimbusds.jose.JOSEException;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;

public interface AutoTrade {

    String orderBinance(String axisCoin, String buyCoin) throws NoSuchAlgorithmException, InvalidKeyException, IOException, InterruptedException;

    void orderKucoin(String axisCoin, String buyCoin) throws NoSuchAlgorithmException, InvalidKeyException, IOException;

    String orderBittrex(String axisCoin, String buyCoin) throws NoSuchAlgorithmException, InvalidKeyException, IOException;

    String orderUpbit(String axisCoin, String buyCoin) throws IOException, ParseException, JOSEException, WebSocketException;
}
