package io.coinpeeker.bot_hotssan.trade;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public interface AutoTrade {

    void orderBinance(String axisCoin, String buyCoin) throws NoSuchAlgorithmException, InvalidKeyException, IOException, InterruptedException;

    void orderKucoin(String axisCoin, String buyCoin) throws NoSuchAlgorithmException, InvalidKeyException, IOException;

    void orderBittrex(String axisCoin, String buyCoin) throws NoSuchAlgorithmException, InvalidKeyException, IOException;
}
