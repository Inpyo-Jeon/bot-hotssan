package io.coinpeeker.bot_hotssan.scheduler;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public interface Listing {

    void inspectListedCoin() throws IOException, NoSuchAlgorithmException, InvalidKeyException;

}
