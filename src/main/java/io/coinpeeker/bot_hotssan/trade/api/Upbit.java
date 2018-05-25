package io.coinpeeker.bot_hotssan.trade.api;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.coinpeeker.bot_hotssan.utils.HttpUtils;
import org.apache.commons.codec.binary.Hex;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class Upbit {

    private static final Logger LOGGER = LoggerFactory.getLogger(Upbit.class);
    String standardUrl = "https://api.binance.com";
    String apiKey = "X-MBX-APIKEY";
    String secretKey = "";
    HttpUtils httpUtils;
    List<NameValuePair> header = new ArrayList<>();


    public Upbit(String apiValue, String secretKey, HttpUtils httpUtils) {
        this.httpUtils = httpUtils;
        this.apiKey = apiValue;
        this.secretKey = secretKey;
    }

    public void aaa(){


    }

    public String signature() throws JOSEException, ParseException {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        JWSSigner signer = new MACSigner("gL9xrMTAnj9sQrDF9JU2Yv9NpYzibJM1q2YGXT0q");
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .claim("access_key", "NzH0lJvdHynCsH61TKf6bSNMdCjF6aKJTgWNcmyP")
                .claim("nonce", timestamp.getTime())
                .build();
        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
        signedJWT.sign(signer);

        String jwsString = signedJWT.serialize();
        return jwsString;
    }
}
