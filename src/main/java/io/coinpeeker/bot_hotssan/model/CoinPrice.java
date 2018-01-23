package io.coinpeeker.bot_hotssan.model;

public class CoinPrice {

    private String key;
    private String exchangeName;
    private String krw;
    private String usd;
    private String satoshi;

    public CoinPrice(String key, String exchangeName) {
        this.key = key;
        this.exchangeName = exchangeName;
    }

    public String getKey() {
        return key;
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public String getKrw() {
        return krw;
    }

    public void setKrw(String krw) {
        this.krw = krw;
    }

    public String getUsd() {
        return usd;
    }

    public void setUsd(String usd) {
        this.usd = usd;
    }

    public String getSatoshi() {
        return satoshi;
    }

    public void setSatoshi(String satoshi) {
        this.satoshi = satoshi;
    }
}
