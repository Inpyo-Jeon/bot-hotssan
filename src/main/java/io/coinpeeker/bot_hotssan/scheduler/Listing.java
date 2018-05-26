package io.coinpeeker.bot_hotssan.scheduler;

import com.neovisionaries.ws.client.WebSocketException;
import com.nimbusds.jose.JOSEException;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;

public interface Listing {

    void inspectListedCoin() throws IOException, NoSuchAlgorithmException, InvalidKeyException, ParseException, JOSEException, WebSocketException;

}
